package com.chat.kit.service;

import com.chat.kit.api.request.FindChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.customException.NoChatRoomException;
import com.chat.kit.customException.NoMemberException;
import com.chat.kit.persistence.domain.*;
import com.chat.kit.persistence.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessageSendingOperations template;
    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final FcmService fcmService;
    private final SimpUserRegistry simpUserRegistry;

    public ChatRoomListResponse getChatRoomId(FindChatRoomDto request){
        return (request.getMemberIds().size()==2) ? getOrCreateChatRoom( request , true ) : getOrCreateChatRoom(request , false ) ;
    }

    private ChatRoomListResponse getOrCreateChatRoom(FindChatRoomDto request, boolean isOneToOne) {
        log.info("{}ChatRoomId 메서드 호출", isOneToOne ? "getOne2One" : "getMultiple");

        Optional<Long> roomNumber = isOneToOne
                ? memberChatRoomRepository.findOne2OneRoomNumber(request.getMemberIds().get(0), request.getMemberIds().get(1))
                : memberChatRoomRepository.findMultipleRoomNumber(request.getMemberIds(), request.getMemberIds().size());

        if (roomNumber.isPresent()) {
            log.info("해당 멤버들에 대한 채팅방 정보 이미 존재: {}", roomNumber);
            String lastMsg = findLastMessageByRoomId(roomNumber.get());
            return ChatRoomListResponse.of(roomNumber.get(), request.getMemberIds(), MessageReadStatus.UN_REDD, lastMsg);
        } else {
            log.info("해당 멤버들에 대해 채팅방 새로 생성");
            Long newRoomId = isOneToOne
                    ? createNewOne2OneChatRoom(request.getMemberIds().get(0), request.getMemberIds().get(1))
                    : createNewMultipleChatRoom(request.getMemberIds());
            return ChatRoomListResponse.of(newRoomId, request.getMemberIds(), MessageReadStatus.UN_REDD, "");
        }
    }

    public Long createNewChatRoom(List<Long> memberIds){
        return (memberIds.size()==2) ?
                createNewOne2OneChatRoom(memberIds.get(0), memberIds.get(1)) :
                createNewMultipleChatRoom(memberIds);
    }

    private Long createNewOne2OneChatRoom(Long member1Id, Long member2Id){
        Member member1 = memberRepository.findById(member1Id)
                .orElseThrow(() -> new NoMemberException("Member not found"));
        Member member2 = memberRepository.findById(member2Id)
                .orElseThrow(() -> new NoMemberException("Member not found"));

        ChatRoom createdChatRoom = ChatRoom.builder()
                .roomType(RoomType.ONE2ONE)
                .build();
        chatRoomRepository.save(createdChatRoom);

        MemberChatRoom memberChatRoom1 = MemberChatRoom.builder()
                .chatRoom(createdChatRoom)
                .member(member1)
                .lastLeavedTime(LocalDateTime.now())
                .build();
        MemberChatRoom memberChatRoom2 = MemberChatRoom.builder()
                .chatRoom(createdChatRoom)
                .member(member2)
                .lastLeavedTime(LocalDateTime.now())
                .build();

        memberChatRoomRepository.save(memberChatRoom1);
        memberChatRoomRepository.save(memberChatRoom2);

        return createdChatRoom.getId();
    }

    private Long createNewMultipleChatRoom(List<Long> memberIds){
        ChatRoom createdChatRoom = ChatRoom.builder()
                .roomType(RoomType.MULTIPLE)
                .build();
        chatRoomRepository.save(createdChatRoom);

        memberIds.forEach(memberId -> {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new NoMemberException("Member not found"));
            MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                    .chatRoom(createdChatRoom)
                    .member(member)
                    .lastLeavedTime(LocalDateTime.now())
                    .build();
            memberChatRoomRepository.save(memberChatRoom);
        });

        return createdChatRoom.getId();
    }

    public List<ChatMessage> findChatMessages(Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoChatRoomException("The chat room id you requested does not exist"));
        return chatMessageRepository.findBychatRoom(chatRoom);
    }

    public List<ChatRoomListResponse> getChatRoomList(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoMemberException("The member you requested does not exist"));

        List<ChatRoomListResponse> res = new ArrayList<>();
        List<MemberChatRoom> memberChatRoom = member.getMemberChatRoom();
        for(MemberChatRoom mc : memberChatRoom){
            ChatRoom chatRoom = mc.getChatRoom();
            List<Long> memberIds = chatRoom.getMemberChatRoomOne().stream()
                    .map(r -> r.getMember().getId())
                    .collect(Collectors.toList());

            String lastMsg = findLastMessageByRoomId(chatRoom.getId());
            ChatRoomListResponse chatRoomListResponse = ChatRoomListResponse.of(chatRoom.getId(), memberIds, MessageReadStatus.ALL_READ, lastMsg);
            res.add(chatRoomListResponse);
        }
        return res;
    }

    public ChatMessage saveMessage(RequestChatMessage requestChatMessage){
        ChatRoom chatRoom = chatRoomRepository.findById(requestChatMessage.getRoomId())
                .orElseThrow(() -> new NoChatRoomException("Chat room not found"));
        Member member = memberRepository.findById(requestChatMessage.getSenderId())
                .orElseThrow(() -> new NoMemberException("Sender not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .memberId(member.getId())
                .message(requestChatMessage.getMessage())
                .chatType(requestChatMessage.getChatType())
                .sentAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(chatMessage);

        return chatMessage;
    }

    // 메시지 저장 및 알림 처리
    public void receiveMessage(RequestChatMessage requestChatMessage) {
        // 메시지 저장
        ChatRoom chatRoom = chatRoomRepository.findById(requestChatMessage.getRoomId())
                .orElseThrow(() -> new NoChatRoomException("Chat room not found"));

        Member member = memberRepository.findById(requestChatMessage.getSenderId())
                .orElseThrow(() -> new NoMemberException("Sender not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .memberId(member.getId())
                .message(requestChatMessage.getMessage())
                .chatType(requestChatMessage.getChatType())
                .sentAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(chatMessage);

        // 해당 채팅방에 참여한 모든 멤버에게 메시지 전송
        template.convertAndSend("/sub/chatroom/" + chatRoom.getId(), ChatRoomMessagesResponse.of(chatMessage));

        // 해당 채팅방에 구독하지 않은 사용자들에게 FCM 알림 전송
        Set<Long> allChatMemberIds = memberChatRoomRepository.findByChatRoomId(chatRoom.getId()).stream()
                .filter(mcr -> !mcr.getMember().getId().equals(member.getId())) // 현재 메시지를 보낸 회원 제외
                .map(mcr -> mcr.getMember().getId())
                .collect(Collectors.toSet());

        Set<Long> inChatRoomMemberIds = simpUserRegistry.getUsers().stream()
                .filter(user -> user.getSessions().stream()
                        .anyMatch(session -> session.getSubscriptions().stream()
                                .anyMatch(sub -> sub.getDestination().equals("/sub/chatroom/" + chatRoom.getId()))
                        ))
                .map(user -> Long.parseLong(user.getName()))
                .collect(Collectors.toSet());

        allChatMemberIds.removeAll(inChatRoomMemberIds);

        // FCM 푸시 알림 전송
        allChatMemberIds.forEach(memberId -> {
            memberFcmTokenRepository.findByMemberId(memberId)
                    .ifPresent(fcmToken -> {
                        fcmService.sendMessage(fcmToken.getToken(), "New message in your chat room", chatMessage.getMessage());
                    });
        });
    }


    public List<ChatRoomMessagesResponse> findUnreadChats(Long memberId) {
        List<ChatMessage> messages = chatMessageRepository.findUnReadMsgByMemberId(memberId);
        return messages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
    }

    private String findLastMessageByRoomId(Long roomId) {
        return chatMessageRepository.findLastMsgByChatRoomId(roomId)
                .map(ChatMessage::getMessage)
                .orElse("");
    }
}

