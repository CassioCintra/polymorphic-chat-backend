package io.cassio.polymorphic.chat.domain;

import java.util.List;

public record AiChatRequest(List<AiMessage> messages){

    public static AiChatRequest userRequest(String message){
        return new AiChatRequest(
                List.of(new AiMessage("user", message))
        );
    }
}
