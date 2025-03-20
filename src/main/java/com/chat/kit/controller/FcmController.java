package com.chat.kit.controller;

import com.chat.kit.api.request.FcmRegisterDto;
import com.chat.kit.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmController {

    private final FcmService fcmService;


    @PostMapping("/registerToken")
    public String registerFcmToken(@RequestBody FcmRegisterDto fcmRegisterDto) {
        return fcmService.registerFcmToken(fcmRegisterDto);
    }
}
