package it.polimi.ingsw.server.answer;

public class GameError implements Answer {
    private final String message;

    public GameError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
