package com.chat.kit.controller;

import com.chat.kit.api.request.FindChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.ApiResponse;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.api.response.common.success.ResponseCode;
import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations template;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final SimpUserRegistry simpUserRegistry;

    /**
     * 현재 로그인한 회원이 참여중인  (1:1 혹은 그룹 ) 채팅방 목록 반환
     */
    @GetMapping("/chatRooms")
    public ApiResponse<List<ChatRoomListResponse>> retrieveChatRoomList(@AuthenticationPrincipal Member loginMember) {
        return ApiResponse.response(ResponseCode.OK,chatService.getChatRoomList(loginMember.getId()));
    }



    /**
     * n 개의 멤버를 받아 (1:1 혹은 그룹 )채팅방 생성 및 PK를 반환.
     * if : n 개의 멤버에 대해 이미 존재하는 채팅방이 있을 경우 추가 생성 없이 해당 채팅방의 PK 를 반환
     */
    @PostMapping("/chat/room")
    public ApiResponse<ChatRoomListResponse> getChatRoomId(@RequestBody FindChatRoomDto request){
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRoomId(request);
        return ApiResponse.response(ResponseCode.OK, chatRoomListResponse);
    }


    /**
     * 채팅방의 id 를 받아 (1:1 혹은 그룹 )채팅방의 메세지들을 반환.
     * */
    @GetMapping("/chat/{roomId}/messages")
    public ApiResponse<List<ChatRoomMessagesResponse>> getChatMessages(@PathVariable Long roomId){
        List<ChatMessage> chatMessages = chatService.findChatMessages(roomId);
        List<ChatRoomMessagesResponse> data = chatMessages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);

    }

    /**
     * 현재 로그인한 멤버가 읽지 않은 (모든 채팅방의) 메시지들을 반환
     * */
    @GetMapping("/chat/unread-chats")
    public ApiResponse<List<ChatRoomMessagesResponse>> findUnreadChats(@AuthenticationPrincipal Member loginMember){
        List<ChatRoomMessagesResponse> unreadChats = chatService.findUnreadChats(loginMember.getId());
        return ApiResponse.response(ResponseCode.OK, unreadChats);
    }


    //메시지 송신 및 수신, /pub가 생략된 모습. 클라이언트 단에선 /pub/message로 요청
    @MessageMapping("/message")
    @Transactional
    public void receiveMessage(@RequestBody RequestChatMessage chat) {
        chatService.receiveMessage(chat);
    }
}
