package org.telegramUserBot.clientInteraction;

import it.tdlight.client.Authenticable;
import it.tdlight.client.ClientInteraction;
import it.tdlight.client.InputParameter;
import it.tdlight.client.ParameterInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CustomClientInteraction implements ClientInteraction {

    private final Executor blockingExecutor;
    private String authCode;

    public CustomClientInteraction(Executor blockingExecutor, Authenticable authenticable) {
        this.blockingExecutor = blockingExecutor;
    }

    @Override
    public CompletableFuture<String> onParameterRequest(InputParameter parameter, ParameterInfo parameterInfo) {

        // displays the question in console for test purpose
        String question = "Waiting for input from external API for: " + parameter.toString();
        System.out.println(question);

        return waitForInput()
                // handle timeout in TelegramService class instead of here !!!
                .orTimeout(10, TimeUnit.MINUTES) // fails if not completed within 10 minutes
                .exceptionally(ex -> {
                    throw new RuntimeException("Timeout waiting for user input.", ex);
                });
    }

    private CompletableFuture<String> waitForInput() {
        return CompletableFuture.supplyAsync(() -> {
            // waiting for authCode to be set with setUserInput
            while (authCode == null) {
                try {
                    // giving small intervals to check condition to avoid busy waiting
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while waiting for input", e);
                }
            }
            return authCode.trim();
        }, blockingExecutor);
    }

    public void setUserInput(String authCode) {
        this.authCode = authCode;
    }
}
