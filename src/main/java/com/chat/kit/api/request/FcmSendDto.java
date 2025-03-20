package com.chat.kit.api.request;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FcmSendDto {
    private String token;

    private String title;

    private String body;
}