package com.chat.kit.api.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmRegisterDto {
    private Long memberId;
    private String fcmToken;
}
