package org.telegramUserBot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.telegramUserBot.dto.AuthCodeRequest;
import org.telegramUserBot.dto.PhoneNumberRequest;
import org.telegramUserBot.service.TelegramService;

@RestController
public class TelegramController {

    @Autowired
    private TelegramService telegramService;

    // correct the response status for both controllers
    // handle errors

    @PostMapping("/sendCode")
    @ResponseStatus(HttpStatus.OK)
    public String startAuth(@RequestBody PhoneNumberRequest phoneNumberRequest) {
        try {
            telegramService.sendCode(phoneNumberRequest);
            return "Authentication code sent. Please enter the code.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/verifyCode")
    @ResponseStatus(HttpStatus.OK)
    public String getUserAuthCode(@RequestBody AuthCodeRequest authCodeRequest) {
        try {
            telegramService.authenticateUser(authCodeRequest);
            return "Authenticated successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

// implement closeSession in service
/*
    @GetMapping("/close")
    public String closeSession() {
        try {
            telegramService.closeClient();
            return "Session closed.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    */
}

