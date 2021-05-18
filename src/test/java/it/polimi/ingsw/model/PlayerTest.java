package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.card.deck.DevelopmentCardDeck;
import it.polimi.ingsw.model.card.developmentcard.DevelopmentCard;
import it.polimi.ingsw.model.card.leadercard.*;
import it.polimi.ingsw.model.enumeration.CardColor;
import it.polimi.ingsw.model.enumeration.Resource;
import it.polimi.ingsw.model.gameboard.playerdashboard.Shelf;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class PlayerTest {
    private Player player;

    @Before public void initialize(){
        player = new Player("TestPlayer");
    }

    @Test
    public void buyCard() throws InvalidSpaceCardException, InvalidChoiceException, ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException {
        ArrayList<Goods> input=new ArrayList<>();
        ArrayList<Goods> cost=new ArrayList<>();
        cost.add(new Goods(Resource.SERVANT, 3));
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output, 3);
        DevelopmentCard devCard=new DevelopmentCard(3, 0, CardColor.PURPLE, 1, cost, production);
        player.getPlayerDashboard().getStorage().AddResource(3, Resource.SERVANT, 3);

        player.buyCard(devCard,2);

        assertEquals(player.getPlayerDashboard().getDevCardsSpace().getCard(2).getLevel(),1);
        assertEquals(player.getPlayerDashboard().getDevCardsSpace().getCard(2).getFaithSteps(),3);
    }

    @Test(expected = InvalidSpaceCardException.class)
    public void buyCard_InvalidSpace() throws InvalidSpaceCardException {
        ArrayList<Goods> input=new ArrayList<>();
        ArrayList<Goods> cost=new ArrayList<>();
        cost.add(new Goods(Resource.SERVANT, 3));
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output, 3);
        DevelopmentCard devCard=new DevelopmentCard(3, 1, CardColor.PURPLE, 1, cost, production);

        player.buyCard(devCard,3);
    }

    @Test(expected=NotEnoughResourceException.class)
    public void ProductionLeader_NotEnoughResource() throws NotEnoughResourceException, ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException, InvalidChoiceException {
        player.getPlayerDashboard().getStorage().AddResource(1,Resource.SHIELD,1);
        player.getPlayerDashboard().getStorage().AddResource(2,Resource.COIN,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.COIN,2);

        Goods cost = new Goods(Resource.SERVANT, 0);
        ArrayList<Goods> input = new ArrayList<>();
        Goods g1 = new Goods(Resource.STONE,1);
        input.add(g1);
        ArrayList<Goods> output = new ArrayList<>();
        Goods g2 = new Goods(Resource.UNKNOWN,1);
        output.add(g2);
        Production production = new Production(input, output,2);
        Requirements req1=new Requirements(CardColor.PURPLE, 2, 1, cost);
        LeaderCard leader=new ProductionLeader(4, 12, production, req1);

        player.getPlayerDashboard().getLeaders().add(leader);

        ((ProductionLeader) player.getPlayerDashboard().getLeaders().get(0)).setOutputProduction(Resource.COIN);

        player.ActiveProductionLeader(1);
    }

    @Test
    public void ProductionLeader() throws ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException, NotEnoughResourceException, InvalidChoiceException {
        player.getPlayerDashboard().getStorage().AddResource(1,Resource.STONE,1);
        player.getPlayerDashboard().getStorage().AddResource(2,Resource.COIN,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.STONE,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.COIN,2);

        Goods cost = new Goods(Resource.SERVANT, 0);
        ArrayList<Goods> input = new ArrayList<>();
        Goods g1 = new Goods(Resource.STONE,1);
        input.add(g1);
        ArrayList<Goods> output = new ArrayList<>();
        Goods g2 = new Goods(Resource.UNKNOWN,1);
        output.add(g2);
        Production production = new Production(input, output, 2);
        Requirements req1=new Requirements(CardColor.PURPLE, 2, 1, cost);
        LeaderCard leader=new ProductionLeader(4, 12, production, req1);

        player.getPlayerDashboard().getLeaders().add(leader);

        if(player.getPlayerDashboard().getLeaders().get(0) instanceof ProductionLeader)
            ((ProductionLeader) player.getPlayerDashboard().getLeaders().get(0)).setOutputProduction(Resource.COIN);
        else
            fail();

        player.ActiveProductionLeader(1);
        player.doProduction();

        assertEquals(player.getPlayerDashboard().getStorage().getAmountShelf(1),0);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.COIN), 3);

    }

    @Test
    public void DiscardLeader() throws InvalidChoiceException {
        Shelf shelf=new Shelf(2,0,Resource.COIN);
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        LeaderCard leader=new StorageLeader(3,12, req1,shelf);
        player.getPlayerDashboard().getLeaders().add(leader);

        req1=new Requirements(CardColor.GREEN,0,2, cost);
        Requirements req2=new Requirements(CardColor.PURPLE,0,1, cost);
        leader=new WhiteBallLeader(5,2,Resource.SHIELD,req1,req2);
        player.getPlayerDashboard().getLeaders().add(leader);

        player.DiscardLeader(1);
        player.DiscardLeader(2);

        assert(player.getPlayerDashboard().getLeaders().get(0).getIsDiscarded());
        assert(player.getPlayerDashboard().getLeaders().get(1).getIsDiscarded());
    }

    @Test(expected = InvalidChoiceException.class)
    public void testDiscardLeader_InvalidChoice() throws InvalidChoiceException {
        Shelf shelf=new Shelf(2,0,Resource.COIN);
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        LeaderCard leader=new StorageLeader(3,3,req1,shelf);

        player.DiscardLeader(2);
    }

    @Test
    public void testActiveLeader() throws ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException,
            NotEnoughSpaceException, InvalidChoiceException {

        player.getPlayerDashboard().getStorage().AddResource(3,Resource.SHIELD,3);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,4);
        player.getPlayerDashboard().getVault().AddResource(Resource.COIN,1);

        DevelopmentCardDeck PurpleOne = new DevelopmentCardDeck();
        ArrayList<Goods> cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SERVANT, 3));
        ArrayList<Goods> input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output,0);
        DevelopmentCard devCard=new DevelopmentCard(3,16, CardColor.PURPLE, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);

        DevelopmentCardDeck GreenOne = new DevelopmentCardDeck();
        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,18, CardColor.GREEN, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);

        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        cost1.add(new Goods(Resource.COIN, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.STONE, 1));
        input.add(new Goods(Resource.SERVANT, 1));
        output=new ArrayList<>();
        output.add(new Goods(Resource.COIN, 2));
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(4,26, CardColor.GREEN, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,3);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,3);

        cost1 = new ArrayList<>();
        cost1.add(new Goods(Resource.COIN, 3));
        cost1.add(new Goods(Resource.STONE, 2));
        input= new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        input.add(new Goods(Resource.STONE, 1));
        output= new ArrayList<>();
        output.add(new Goods(Resource.SERVANT, 3));
        production=new Production(input, output, 0);
        devCard=new DevelopmentCard(6,38, CardColor.BLUE, 2, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);

        player.getPlayerDashboard().getStorage().AddResource(2,Resource.COIN,2);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,4);
        player.getPlayerDashboard().getVault().AddResource(Resource.COIN,1);

        Shelf shelf=new Shelf(2,0,Resource.COIN);
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        LeaderCard leader=new StorageLeader(3,3,req1,shelf);
        player.getPlayerDashboard().getLeaders().add(leader);

        cost=new Goods(Resource.COIN, 0);
        req1=new Requirements(CardColor.GREEN,0,2, cost);
        Requirements req2=new Requirements(CardColor.PURPLE,0,1, cost);
        leader=new WhiteBallLeader(5,5,Resource.SHIELD,req1,req2);
        player.getPlayerDashboard().getLeaders().add(leader);

        req1=new Requirements(CardColor.GREEN,0,1, cost);
        req2=new Requirements(CardColor.BLUE,0,1, cost);
        leader=new EconomyLeader(2,2,Resource.STONE,req1,req2);
        player.getPlayerDashboard().getLeaders().add(leader);

        input = new ArrayList<>();
        Goods g1 = new Goods(Resource.SERVANT,1);
        input.add(g1);
        output = new ArrayList<>();
        Goods g2 = new Goods(Resource.UNKNOWN,1);
        output.add(g2);
        production = new Production(input,output,2);
        req1=new Requirements(CardColor.BLUE,2,1, cost);
        leader=new ProductionLeader(4,12,production,req1);
        player.getPlayerDashboard().getLeaders().add(leader);

        player.ActiveLeader(1);
        player.ActiveLeader(2);
        player.ActiveLeader(3);
        player.ActiveLeader(4);

        assertEquals((player.getPlayerDashboard().getStorage().getAmountShelf(3)),0);
        assertEquals((player.getPlayerDashboard().getStorage().getAmountShelf(2)),2);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.SHIELD),6);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.COIN),2);
        assert(player.getPlayerDashboard().getLeaders().get(0).getIsActive());
        assert(player.getPlayerDashboard().getLeaders().get(1).getIsActive());
        assert(player.getPlayerDashboard().getLeaders().get(2).getIsActive());
        assert(player.getPlayerDashboard().getLeaders().get(3).getIsActive());
    }

    @Test(expected = InvalidChoiceException.class)
    public void testActiveLeader_InvalidChoice() throws InvalidChoiceException {

        player.ActiveLeader(-1);
        player.ActiveLeader(3);

    }

    @Test
    public void testMove(){
        player.move(3);
        assertEquals(3, player.getPlayerDashboard().getFaithPath().getPositionFaithPath());
    }

    @Test
    public void testGetDevCards() {
        ArrayList<Integer> cards = new ArrayList<>();


        ArrayList<Goods> cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SERVANT, 3));
        ArrayList<Goods> input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output,0);
        DevelopmentCard devCard=new DevelopmentCard(3,16, CardColor.PURPLE, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,19, CardColor.GREEN, 2, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);
        cards.add(devCard.getCardID());


        cost1 = new ArrayList<>();
        cost1.add(new Goods(Resource.COIN, 3));
        cost1.add(new Goods(Resource.STONE, 2));
        input= new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        input.add(new Goods(Resource.STONE, 1));
        output= new ArrayList<>();
        output.add(new Goods(Resource.SERVANT, 3));
        production=new Production(input, output, 0);
        devCard=new DevelopmentCard(6,38, CardColor.BLUE, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,18, CardColor.GREEN, 2, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,18, CardColor.GREEN, 3, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);
        cards.add(devCard.getCardID());


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        cost1.add(new Goods(Resource.COIN, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.STONE, 1));
        input.add(new Goods(Resource.SERVANT, 1));
        output=new ArrayList<>();
        output.add(new Goods(Resource.COIN, 2));
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(4,26, CardColor.GREEN, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,3);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,3);
        cards.add(devCard.getCardID());


        for(int i=0; i<player.getDevCards().size(); i++){
            assertEquals(cards.get(i), player.getDevCards().get(i));
        }

    }

    @Test
    public void testGetProductions() {
        ArrayList<Integer> cards = new ArrayList<>();


        ArrayList<Goods> cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SERVANT, 3));
        ArrayList<Goods> input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output,0);
        DevelopmentCard devCard=new DevelopmentCard(3,16, CardColor.PURPLE, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,19, CardColor.GREEN, 2, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);
        cards.add(devCard.getCardID());


        cost1 = new ArrayList<>();
        cost1.add(new Goods(Resource.COIN, 3));
        cost1.add(new Goods(Resource.STONE, 2));
        input= new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        input.add(new Goods(Resource.STONE, 1));
        output= new ArrayList<>();
        output.add(new Goods(Resource.SERVANT, 3));
        production=new Production(input, output, 0);
        devCard=new DevelopmentCard(6,38, CardColor.BLUE, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,18, CardColor.GREEN, 2, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.COIN, 1));
        output=new ArrayList<>();
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(1,18, CardColor.GREEN, 3, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,2);
        cards.add(devCard.getCardID());


        cost1=new ArrayList<>();
        cost1.add(new Goods(Resource.SHIELD, 2));
        cost1.add(new Goods(Resource.COIN, 2));
        input=new ArrayList<>();
        input.add(new Goods(Resource.STONE, 1));
        input.add(new Goods(Resource.SERVANT, 1));
        output=new ArrayList<>();
        output.add(new Goods(Resource.COIN, 2));
        production=new Production(input, output, 1);
        devCard=new DevelopmentCard(4,26, CardColor.GREEN, 1, cost1, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,3);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,3);
        cards.add(devCard.getCardID());


        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.GREEN,0,1, cost);
        Requirements req2=new Requirements(CardColor.BLUE,0,1, cost);
        LeaderCard leader=new EconomyLeader(2,2,Resource.STONE,req1,req2);
        player.getPlayerDashboard().getLeaders().add(leader);

        input = new ArrayList<>();
        Goods g1 = new Goods(Resource.SERVANT,1);
        input.add(g1);
        output = new ArrayList<>();
        Goods g2 = new Goods(Resource.UNKNOWN,1);
        output.add(g2);
        production = new Production(input,output,2);
        req1=new Requirements(CardColor.BLUE,2,1, cost);
        leader=new ProductionLeader(4,12,production,req1);
        player.getPlayerDashboard().getLeaders().add(leader);
        cards.add(devCard.getCardID());


        for(int i=0; i<player.getProductions().size(); i++){
            assertEquals(cards.get(i), player.getProductions().get(i));
        }

    }

    @Test
    public void ProductionBase() throws ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException, NotEnoughResourceException {
        player.getPlayerDashboard().getStorage().AddResource(1,Resource.STONE,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.STONE,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,1);
        player.getPlayerDashboard().getDevCardsSpace().setInputBasicProduction(Resource.STONE,Resource.SHIELD);
        player.getPlayerDashboard().getDevCardsSpace().setOutputBasicProduction(Resource.COIN);

        player.ActiveProductionBase();
        player.doProduction();

        assertEquals(player.getPlayerDashboard().getStorage().getAmountShelf(1),0);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.COIN),1);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.STONE),1);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.SHIELD),0);
    }

    @Test
    public void ProductionDevCard() throws ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException, NotEnoughResourceException, InvalidChoiceException {
        ArrayList<Goods> input=new ArrayList<>();
        ArrayList<Goods> cost=new ArrayList<>();
        cost.add(new Goods(Resource.SERVANT, 3));
        input.add(new Goods(Resource.COIN, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,1));
        output.add(new Goods(Resource.SHIELD,1));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output, 0);
        DevelopmentCard devCard=new DevelopmentCard(3, 4, CardColor.PURPLE, 1, cost, production);

        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);

        player.getPlayerDashboard().getStorage().AddResource(1,Resource.STONE,1);
        player.getPlayerDashboard().getStorage().AddResource(2,Resource.COIN,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.STONE,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.SHIELD,1);
        player.getPlayerDashboard().getVault().AddResource(Resource.COIN,2);
        player.getPlayerDashboard().getDevCardsSpace().setInputBasicProduction(Resource.STONE,Resource.SHIELD);
        player.getPlayerDashboard().getDevCardsSpace().setOutputBasicProduction(Resource.COIN);

        player.ActiveProductionDevCard(1);
        player.doProduction();

        assertEquals(player.getPlayerDashboard().getStorage().getAmountShelf(1),1);
        assertEquals(player.getPlayerDashboard().getStorage().getAmountShelf(2),0);
        assertEquals(player.getPlayerDashboard().getStorage().getAmountShelf(3),0);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.COIN),1);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.STONE),2);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.SHIELD),2);
        assertEquals(player.getPlayerDashboard().getVault().getResource(Resource.SERVANT),1);
    }

    @Test
    public void testCalculateVictoryPoints() {
        ArrayList<Goods> input=new ArrayList<>();
        ArrayList<Goods> output=new ArrayList<>();

        ArrayList<Goods> cost=new ArrayList<>();

        Production production=new Production(input, output, 2);
        DevelopmentCard card = new DevelopmentCard(4, 5, CardColor.GREEN, 1, cost, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(card,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(card,1);

        production=new Production(input, output, 5);
        card = new DevelopmentCard(8, 6, CardColor.BLUE, 2, cost, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(card,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(card,1);

        production=new Production(input, output, 9);
        card = new DevelopmentCard(12, 7, CardColor.GREEN, 3, cost, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(card,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(card,1);

        production=new Production(input, output, 2);
        card = new DevelopmentCard(3, 8, CardColor.PURPLE, 1, cost, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(card,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(card,2);

        production=new Production(input, output, 2);
        card = new DevelopmentCard(6, 9, CardColor.YELLOW, 2, cost, production);
        player.getPlayerDashboard().getDevCardsSpace().checkSpace(card,2);
        player.getPlayerDashboard().getDevCardsSpace().addCard(card,2);


        Goods good=new Goods(Resource.COIN, 3);
        Requirements requirements = new Requirements(CardColor.GREEN, 2, 3, good);

        LeaderCard leader=new ProductionLeader(4, 10, production, requirements);
        leader.setIsDiscarded();
        player.getLeaders().add(leader);

        leader=new ProductionLeader(10, 11, production, requirements);
        leader.setIsActive();
        player.getLeaders().add(leader);

        assertEquals(player.calculateVictoryPoints(), 43);

    }

    @Test
    public void testDoProduction() throws ShelfHasDifferentTypeException, AnotherShelfHasTheSameTypeException, NotEnoughSpaceException, NotEnoughResourceException, InvalidChoiceException {
        player.getPlayerDashboard().getStorage().AddResource(1, Resource.SHIELD, 1);
        player.getPlayerDashboard().getStorage().AddResource(2, Resource.STONE, 2);
        player.getPlayerDashboard().getStorage().AddResource(3, Resource.SERVANT, 3);

        player.getPlayerDashboard().getDevCardsSpace().setInputBasicProduction(Resource.SHIELD, Resource.STONE);
        player.getPlayerDashboard().getDevCardsSpace().setOutputBasicProduction(Resource.COIN);
        player.ActiveProductionBase();


        ArrayList<Goods> input=new ArrayList<>();
        ArrayList<Goods> cost=new ArrayList<>();
        cost.add(new Goods(Resource.SERVANT, 2));
        input.add(new Goods(Resource.STONE, 1));
        input.add(new Goods(Resource.SERVANT, 2));
        ArrayList<Goods> output=new ArrayList<>();
        output.add(new Goods(Resource.SERVANT,2));
        output.add(new Goods(Resource.SHIELD,3));
        output.add(new Goods(Resource.STONE,1));
        Production production=new Production(input, output, 0);
        DevelopmentCard devCard=new DevelopmentCard(3, 4, CardColor.PURPLE, 1, cost, production);

        player.getPlayerDashboard().getDevCardsSpace().checkSpace(devCard,1);
        player.getPlayerDashboard().getDevCardsSpace().addCard(devCard,1);

        player.ActiveProductionDevCard(1);



        player.doProduction();
        assertEquals(0, player.getPlayerDashboard().getStorage().getAmountShelf(1));
        assertEquals(0, player.getPlayerDashboard().getStorage().getAmountShelf(2));
        assertEquals(1, player.getPlayerDashboard().getStorage().getAmountShelf(3));
        assertEquals(1, player.getPlayerDashboard().getVault().getResource(Resource.COIN));
        assertEquals(1, player.getPlayerDashboard().getVault().getResource(Resource.STONE));
        assertEquals(2, player.getPlayerDashboard().getVault().getResource(Resource.SERVANT));
        assertEquals(3, player.getPlayerDashboard().getVault().getResource(Resource.SHIELD));

    }

    @Test
    public void testWhiteBallLeader() {
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        Requirements req2=new Requirements(CardColor.BLUE,0,0, cost);
        LeaderCard leader=new WhiteBallLeader(5, 12, Resource.COIN, req1, req2);
        leader.setIsActive();
        player.getPlayerDashboard().getLeaders().add(leader);

        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        req2=new Requirements(CardColor.BLUE,0,0, cost);
        leader=new WhiteBallLeader(11, 28, Resource.SERVANT, req1, req2);
        leader.setIsActive();
        player.getPlayerDashboard().getLeaders().add(leader);

        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        req2=new Requirements(CardColor.BLUE,0,0, cost);
        leader=new WhiteBallLeader(9, 32, Resource.SHIELD, req1, req2);
        leader.setIsDiscarded();
        player.getPlayerDashboard().getLeaders().add(leader);

        assertEquals(2, player.WhiteBallLeader());
    }

    @Test
    public void testStorageLeader() {
        Shelf shelf=new Shelf(2,0,Resource.COIN);
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        LeaderCard leader=new StorageLeader(3,12, req1,shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsActive();

        shelf=new Shelf(2,2,Resource.SERVANT);
        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        leader=new StorageLeader(3,45, req1, shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsDiscarded();

        shelf=new Shelf(2,1,Resource.SHIELD);
        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        leader=new StorageLeader(3,22, req1, shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsActive();

        assert(player.StorageLeader(Resource.COIN));
        assert(!player.StorageLeader(Resource.SERVANT));
        assert(player.StorageLeader(Resource.SHIELD));
    }

    @Test
    public void testIndexOfStorageLeader() throws InvalidChoiceException {
        Goods cost=new Goods(Resource.SHIELD,5);
        Requirements req1=new Requirements(CardColor.PURPLE,0,0, cost);
        Requirements req2=new Requirements(CardColor.BLUE,0,0, cost);
        LeaderCard leader=new WhiteBallLeader(5, 12, Resource.COIN, req1, req2);
        leader.setIsActive();
        player.getPlayerDashboard().getLeaders().add(leader);

        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        req2=new Requirements(CardColor.BLUE,0,0, cost);
        leader=new WhiteBallLeader(11, 28, Resource.SERVANT, req1, req2);
        leader.setIsActive();
        player.getPlayerDashboard().getLeaders().add(leader);

        Shelf shelf=new Shelf(2,2,Resource.SERVANT);
        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        leader=new StorageLeader(3,45, req1, shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsDiscarded();

        shelf=new Shelf(2,0,Resource.COIN);
        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        leader=new StorageLeader(3,12, req1,shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsActive();

        shelf=new Shelf(2,0,Resource.SHIELD);
        cost=new Goods(Resource.SHIELD,5);
        req1=new Requirements(CardColor.PURPLE,0,0, cost);
        leader=new StorageLeader(3,12, req1,shelf);
        player.getPlayerDashboard().getLeaders().add(leader);
        leader.setIsActive();


        assertEquals(4, player.indexOfStorageLeader(Resource.COIN));
    }

}