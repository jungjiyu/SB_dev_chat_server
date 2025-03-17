package com.chat.kit.myutil_token.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TokenResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Success{
        private String token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NicknameDuplication{
        private String nickname;

        @JsonProperty("isDuplication")
        private Boolean isDuplication;
    }

}