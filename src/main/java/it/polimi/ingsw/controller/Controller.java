package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.enumeration.Resource;
import it.polimi.ingsw.model.singleplayer.ActionToken;
import it.polimi.ingsw.server.VirtualView;

import java.util.ArrayList;

/**
 * Controller class handles single and multi player matches
 * @author Lorenzo Iovine
 */
public class Controller {
    private final Game gameModel;
    private final TurnController turncontroller;
    private final EndGameController endgame;
    private final ArrayList<String> players=new ArrayList<>();
    private final VirtualView view;
    private boolean isEnd=false;
    ActionToken actionToken;


    /**
     * Controller constructor: creates a new instance of Controller
     * @param players are the players of the match
     * @param view is an instance of class VirtualView
     */
    public Controller(ArrayList<String> players, VirtualView view) {
        this.players.addAll(players);
        gameModel=new Game(players.size(), players);
        turncontroller=new TurnController(gameModel, players, view);
        endgame=new EndGameController();
        this.view=view;
    }


    /**
     * This method handles the multi and single player connected match
     */
    public void play() {
        try {
            for(int i=0; i<players.size(); i++) {
                setPlayers(i);
            }
            for (int i = 0; i < players.size(); i++) {
                view.sendTurnStatus("START", players.get(i));
                setInitialBenefits(i);
                setInitialLeaderCards(i);
                discardFirstLeaders(i);
                view.sendTurnStatus("END", players.get(i));
            }
            for(int i=0; i<players.size(); i++){
                view.initialInfo(players.get(i), players.size(), players);
                startGame(i);
                view.initializeGameBoard(players.get(i), gameModel.getGameBoard().getMarket(),gameModel.getGameBoard().getDevelopmentCardGrid().getGrid().IdDeck(), gameModel.getPlayer(players.get(i)).getLeaders().IdDeck());
                //view.initialInfo(players.get(i), players.size(), players);

                for(String player: players){
                    if(!player.equals(players.get(i)))
                        view.seeStorage(players.get(i), gameModel.getPlayer(player).getPlayerDashboard().getStorage(), null, player);
                }
            }

            for(String s: players){
                for(String n: players){
                    view.updateFaithPath(s, n, 0);
                }
            }

            while(!isEnd){
                for(int i=0; i<players.size();i++){
                    //send start turn
                    view.sendTurnStatus("START", players.get(i));
                    turncontroller.seePlayerDashboards(i);
                    turncontroller.seeGameBoard(true, i);
                    turncontroller.chooseTurn(i);
                    view.sendTurnStatus("END", players.get(i));
                    //send end turn
                }

                if(players.size()==1){
                    try{
                        actionToken = gameModel.drawActionToken();
                        view.seeActionToken(players.get(0), actionToken);
                    } catch (EmptyDecksException e){
                        break;
                    } catch (InvalidChoiceException e){
                        e.printStackTrace();
                    }
                    isEnd = endgame.SinglePlayerIsEndGame(gameModel);
                } else {
                    isEnd = isEndGame();
                }
            }

            endGame();

        } catch (NotExistingPlayerException | InterruptedException | InvalidChoiceException e){
            e.printStackTrace();
        }
    }


    /**
     * This method is used to set the player in Game class in the model
     * @param player is the player index in array list players
     */
    public void setPlayers(int player) {
        gameModel.createPlayer(players.get(player));
    }


    /**
     * This method set the initial benefits based on corresponding player position:
     * 1st player-nothing
     * 2nd player-1 resource of his choice
     * 3rd player-1 resource of his choice and 1 faith point
     * 4th player-2 resource of his choice and 1 faith point
     * @param i is the player index in array list players
     * @throws NotExistingPlayerException if the selected player doesn't exists
     * @throws InterruptedException is due to multithreading message send
     */
    public void setInitialBenefits(int i) throws NotExistingPlayerException, InterruptedException {
        switch(i){
            case 0: view.firstPlayer(players.get(i));
                    break;
            case 1:
                int resource1 = view.chooseResource(players.get(i), "second", 1);
                    addInitialResource(i, resource1,1);
                    break;
            case 2: resource1 =view.chooseResource(players.get(i), "third",1);
                    addInitialResource(i, resource1,1);
                    gameModel.getPlayer(players.get(i)).getPlayerDashboard().getFaithPath().moveForward(1);
                    for(String s: players){
                        view.updateFaithPath(s, players.get(i), gameModel.getPlayer(players.get(i)).getPlayerDashboard().getFaithPath().getPositionFaithPath());
                    }
                    break;
            case 3: resource1 =view.chooseResource(players.get(i), "fourth",1);
                    addInitialResource(i, resource1,2);
                int resource2 = view.chooseResource(players.get(i), "fourth", 2);
                    if(resource1 == resource2) {
                        addInitialResource(i, resource2, 2);
                    } else {
                        addInitialResource(i, resource2,1);
                    }
                    gameModel.getPlayer(players.get(i)).getPlayerDashboard().getFaithPath().moveForward(1);
                    for(String s: players){
                        view.updateFaithPath(s, players.get(i), gameModel.getPlayer(players.get(i)).getPlayerDashboard().getFaithPath().getPositionFaithPath());
                    }
        }
    }


    /**
     * This method is used to add the initial chosen resources to the storage
     * @param player is the player index in array list players
     * @param resource is the number corresponding to the chosen resource
     * @param shelf is the number corresponding to the shelf where to place the resource on
     * @throws NotExistingPlayerException if the selected player doesn't exists
     */
    public void addInitialResource(int player, int resource, int shelf) throws NotExistingPlayerException {
        try {
            switch (resource) {
                case 1:
                    gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage().AddResource(shelf, Resource.COIN, 1);
                    view.seeStorage(players.get(player),gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage(),null, players.get(player));
                    break;
                case 2:
                    gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage().AddResource(shelf, Resource.STONE, 1);
                    view.seeStorage(players.get(player),gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage(),null, players.get(player));
                    break;
                case 3:
                    gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage().AddResource(shelf, Resource.SHIELD, 1);
                    view.seeStorage(players.get(player),gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage(),null, players.get(player));
                    break;
                case 4:
                    gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage().AddResource(shelf, Resource.SERVANT, 1);
                    view.seeStorage(players.get(player),gameModel.getPlayer(players.get(player)).getPlayerDashboard().getStorage(),null, players.get(player));
                    break;
            }
        } catch (NotEnoughSpaceException | AnotherShelfHasTheSameTypeException | ShelfHasDifferentTypeException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method is used to give the initial 4 leader cards to each player of the game
     * @param player is the player index in array list players
     * @throws NotExistingPlayerException if the selected player doesn't exists
     */
    public void setInitialLeaderCards(int player) throws NotExistingPlayerException {
        for (int i = 0; i < 4; i++) {
            gameModel.getGameBoard().getLeaderDeck().shuffle();
            gameModel.getPlayer(players.get(player)).getLeaders().add(gameModel.getGameBoard().getLeaderDeck().drawFromTail());
        }
    }


    /**
     * This method is used to ask to the player, two leader cards of the initial four, that he
     * wants to discard
     * @param player is the player index in array list players
     * @throws InterruptedException is due to multithreading message send
     * @throws NotExistingPlayerException if the selected player doesn't exists
     */
    public void discardFirstLeaders(int player) throws  InterruptedException, NotExistingPlayerException {
        int card;
        card=view.discardFirstLeaders(players.get(player), 1, gameModel.getPlayer(players.get(player)).getLeaders().IdDeck());
        gameModel.getPlayer(players.get(player)).getPlayerDashboard().getLeaders().remove(card-1);
        card=view.discardFirstLeaders(players.get(player), 2, gameModel.getPlayer(players.get(player)).getLeaders().IdDeck());
        gameModel.getPlayer(players.get(player)).getPlayerDashboard().getLeaders().remove(card-1);
    }


    /**
     * This method is used to notify the player that the game started
     * @param player is the player index in array list players
     */
    public void startGame(int player) {
        view.startGame(players.get(player));
    }


    /**
     * This method handles end game notifications
     * @throws NotExistingPlayerException if the selected player doesn't exists
     */
    public void endGame() throws NotExistingPlayerException {
        if(players.size()==1){
            try {
                if(endgame.SinglePlayerLose(gameModel)){
                    view.lose(players.get(0),endgame.totalVictoryPoints(gameModel.getPlayer(players.get(0))));
                } else {
                    view.win(players.get(0),endgame.totalVictoryPoints(gameModel.getPlayer(players.get(0))));
                }
            } catch (InvalidChoiceException e) {
                e.printStackTrace();
            }
        }
        else {
            Player winner = endgame.getWinner(gameModel);
            view.win(winner.getNickname(),endgame.totalVictoryPoints(winner));

            for(int i=0; i<players.size(); i++) {
                if(i!=players.indexOf(winner.getNickname())) {
                    view.lose(players.get(i),endgame.totalVictoryPoints(gameModel.getPlayer(players.get(i))));
                }
            }
        }
        view.closeConnection();
    }


    /**
     * This method checks if the game is over
     * @return true if the match is over, false otherwise
     */
    public boolean isEndGame(){
        for(Player p : gameModel.getPlayers()) {
            if (p.getPlayerDashboard().getDevCardsSpace().getAmountCards() == 7 || p.getPlayerDashboard().getFaithPath().getPositionFaithPath() == 24) {
                return true;
            }
        }

        return false;
    }


    /**
     * This method gets the instance of EndGameController
     * @return the instance of EndGameController
     */
    public EndGameController getEndgame() {
        return endgame;
    }
}
