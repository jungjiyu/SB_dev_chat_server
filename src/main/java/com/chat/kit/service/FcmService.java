package com.chat.kit.service;

import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.domain.MemberFcmToken;
import com.chat.kit.persistence.repository.MemberFcmTokenRepository;
import com.chat.kit.persistence.repository.MemberRepository;
import com.chat.kit.api.request.FcmRegisterDto;
import com.chat.kit.customException.FcmException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final MemberFcmTokenRepository memberFcmTokenRepository;
    private final MemberRepository memberRepository;

    // FCM 토큰을 등록하거나 업데이트하는 메서드
    public String registerFcmToken(FcmRegisterDto fcmRegisterDto) {
        // DTO에서 값 추출
        Long memberId = fcmRegisterDto.getMemberId();
        String fcmToken = fcmRegisterDto.getFcmToken();

        // 기존 FCM 토큰을 찾기
        MemberFcmToken existingToken = memberFcmTokenRepository.findByMemberId(memberId)
                .orElse(null);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (existingToken != null) {
            // 기존 토큰이 있으면 업데이트
            existingToken.setToken(fcmToken);
        } else {
            // 새로운 토큰이면 새로운 FCM 토큰 저장
            existingToken = MemberFcmToken.builder()
                    .member(member)
                    .token(fcmToken)
                    .build();

            // Member 객체에 FCM 토큰 추가
            member.setFcmToken(existingToken);
        }

        // FCM 토큰 저장
        memberFcmTokenRepository.save(existingToken);
        return "FCM token registered/updated successfully!";
    }

    // FCM 메시지 전송 메서드
    public void sendMessage(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("Push Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            throw new FcmException("Failed to send FCM message");
        }
    }
}
