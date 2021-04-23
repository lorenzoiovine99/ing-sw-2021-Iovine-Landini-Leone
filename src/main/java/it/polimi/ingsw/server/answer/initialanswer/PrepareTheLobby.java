package it.polimi.ingsw.server.answer.initialanswer;

import it.polimi.ingsw.server.answer.Answer;

public class PrepareTheLobby implements Answer {
    private final String message;

    public PrepareTheLobby(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
