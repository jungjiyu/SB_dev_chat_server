package com.chat.kit.myutil_token.system.controller;


import com.chat.kit.myutil_token.system.dto.TokenResponse;
import com.chat.kit.myutil_token.system.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {


    private final TokenService tokenService;

    @PostMapping("/system")
    public ResponseEntity<TokenResponse.Success> createSystemToken() {
        return ResponseEntity
                .status(200)
                .body(TokenResponse.Success
                .builder()
                .token(tokenService.createSystemToken())
                .build() );
    }

}
