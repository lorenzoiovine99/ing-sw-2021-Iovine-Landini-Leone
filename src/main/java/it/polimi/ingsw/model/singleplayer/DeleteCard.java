package it.polimi.ingsw.model.singleplayer;

import it.polimi.ingsw.exceptions.EmptyDecksException;
import it.polimi.ingsw.exceptions.InvalidChoiceException;
import it.polimi.ingsw.model.enumeration.CardColor;
import it.polimi.ingsw.model.gameboard.DevelopmentCardGrid;

import java.io.Serializable;

public class DeleteCard extends ActionToken implements Serializable {

    private final CardColor colorType;

    public DeleteCard(CardColor colorType) {
        this.colorType = colorType;
    }

    public CardColor getColorType() {
        return colorType;
    }

    public void draw(CardColor color, DevelopmentCardGrid developmentCardGrid) throws InvalidChoiceException, EmptyDecksException {
        if(!developmentCardGrid.getDeck(color, 1).isEmpty()){
            developmentCardGrid.removeCard(color, 1);
        } else if(!developmentCardGrid.getDeck(color, 2).isEmpty()){
            developmentCardGrid.removeCard(color, 2);
        } else if(!developmentCardGrid.getDeck(color, 3).isEmpty()){
            developmentCardGrid.removeCard(color, 3);
        } else {
            throw new EmptyDecksException();
        }
    }
}
