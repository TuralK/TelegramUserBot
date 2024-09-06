package org.telegramUserBot;

import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.AuthorizationState;
import it.tdlight.jni.TdApi.MessageContent;
import it.tdlight.jni.TdApi.MessageSenderUser;
import lombok.Getter;
import org.telegramUserBot.clientInteraction.CustomClientInteraction;

import java.util.concurrent.Executors;

public class UserBot implements AutoCloseable {

    @Getter
    private final SimpleTelegramClient client;
    private final CustomClientInteraction customClientInteraction;
    private final long adminId;

    public UserBot(SimpleTelegramClientBuilder clientBuilder,
                      SimpleAuthenticationSupplier<?> authenticationData,
                      long adminId) {
        this.adminId = adminId;

        // prints authorization state updates in console
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);

        // command handlers. commands work when they have '/' before the command name
        // commands without '/' at the beginning can be checked in onUpdateNewMessage method and redirected to necessary methods to handle them
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addCommandHandler("shut-down", this::onShutdownCommand);
        clientBuilder.addCommandHandler("restart", this::onRestartCommand);

        // prints new message updates in console
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);

        // building the client
        this.client = clientBuilder.build(authenticationData);

        // initializing custom client interaction(trying to use minimal resources)
        // check Executors to see what to use (Executors.newSingleThreadExecutor() ?)
        customClientInteraction = new CustomClientInteraction(Executors.newVirtualThreadPerTaskExecutor(), this.client);
        client.setClientInteraction(customClientInteraction);
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setAuthCode(String authCode) {
        customClientInteraction.setUserInput(authCode);
        // Optionally, you can send a CheckAuthenticationCode request directly here
        // this.client.send(new TdApi.CheckAuthenticationCode(authCode), genericResultHandler);
    }

    // Handle updates to authorization state
    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Logged in");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    // Handle new messages received
    private void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
        MessageContent messageContent = update.message.content;

        String text;
        if (messageContent instanceof TdApi.MessageText messageText) {
            text = messageText.text.text;
        } else {
            text = String.format("(%s)", messageContent.getClass().getSimpleName());
        }

        long chatId = update.message.chatId;

        client.send(new TdApi.GetChat(chatId))
                .whenCompleteAsync((chatIdResult, error) -> {
                    if (error != null) {
                        System.err.printf("Can't get chat title of chat %s%n", chatId);
                        error.printStackTrace(System.err);
                    } else {
                        String title = chatIdResult.title;
                        System.out.printf("Received new message from chat %s (%s): %s%n", title, chatId, text);
                    }
                });
    }

    // Stop the client if /stop command is sent by the admin
    private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received stop command. closing...");
            client.sendClose();
        }
    }

    // Shutdown the server if /shut-down command is sent by the admin
    private void onShutdownCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received shut down command from admin.");
            // Add logic to shut down the server here
        }
    }

    // Restart the server if /restart command is sent by the admin
    private void onRestartCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received restart command from admin.");
            // Add logic to restart the server here
        }
    }

    // Check if the command sender is the admin
    private boolean isAdmin(TdApi.MessageSender sender) {
        if (sender instanceof MessageSenderUser messageSenderUser) {
            return messageSenderUser.userId == adminId;
        }
        return false;
    }
}