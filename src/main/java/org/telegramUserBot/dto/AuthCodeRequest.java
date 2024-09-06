package org.telegramUserBot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthCodeRequest {
    private String phoneNumber;
    private String authCode;
}