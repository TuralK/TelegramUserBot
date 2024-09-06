package org.telegramUserBot.service;

import it.tdlight.Init;
import it.tdlight.client.*;
import it.tdlight.util.UnsupportedNativeLibraryException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.telegramUserBot.UserBot;
import org.telegramUserBot.dto.AuthCodeRequest;
import org.telegramUserBot.dto.PhoneNumberRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class TelegramService {

    private static final ConcurrentHashMap<String, UserBot> appMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCode(PhoneNumberRequest phoneNumberRequest) throws ExecutionException, InterruptedException, TimeoutException {
        init();

        // set to user id who will be admin (messages from this person will be accepted as command)
        long adminId = 1201000174L;

        SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();

        int apiId = Integer.parseInt(System.getenv("TELEGRAM_API_ID"));
        String apiHash = System.getenv("TELEGRAM_API_HASH");
        APIToken apiToken = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(apiToken);

        // saves session in provided folder. when dirs not set, every login attempt (for any user) will require auth code
        Path sessionPath = Paths.get("tdlight-session/" + phoneNumberRequest.getPhoneNumber()); //change this
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
        SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumberRequest.getPhoneNumber());

        //handle timeout in this class instead of CCI
        //authenticationData.get().get(10, TimeUnit.MINUTES);

        UserBot userBotApp = new UserBot(clientBuilder, authenticationData, adminId);
        appMap.put(phoneNumberRequest.getPhoneNumber(), userBotApp);
    }

    public void authenticateUser(AuthCodeRequest authCodeRequest) throws ExecutionException, InterruptedException, TimeoutException {
        UserBot userBotApp = appMap.get(authCodeRequest.getPhoneNumber());
        appMap.remove(authCodeRequest.getPhoneNumber(), userBotApp);
        userBotApp.setAuthCode(authCodeRequest.getAuthCode());
    }

}