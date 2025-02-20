package com.angellim.tgbot.bot;


public class GameRoom {
    private long player1;
    private long player2;
    private Game game;
    private char currentPlayer;

    public GameRoom(long player1) {
        this.player1 = player1;
        this.game = new Game();
        this.game.initMap();
        this.currentPlayer = 'X';
    }

    public void setPlayer2(long player2) {
        this.player2 = player2;
    }

    public long getPlayer1() {
        return player1;
    }

    public long getPlayer2() {
        return player2;
    }

    public Game getGame() {
        return game;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    public boolean isFull() {
        return player2 != 0;
    }
}