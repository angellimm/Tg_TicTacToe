package com.angellim.tgbot.bot;

public class Game {

    static char[][] map;

    static final int MAP_SIZE = 3;

    static final char EMPTY_FIELD = '*';
    static final char X_FIELD = 'X';
    static final char O_FIELD = 'O';

    public Game() {
        initMap();
    }

    public static void initMap() {

        map = new char[MAP_SIZE][MAP_SIZE];
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                map[i][j] = EMPTY_FIELD;
            }
        }
    }

    public String getMapAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                char cell = map[i][j];
                if (cell == EMPTY_FIELD) {
                    sb.append(i * MAP_SIZE + j + 1).append(" ");
                } else {
                    sb.append(cell).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean makeMove(int x, int y, char player) {
        if (isCellValid(x, y)) {
            map[y][x] = player;
            return true;
        }
        return false;
    }

    public static boolean checkWin(char player) {

        for (int i = 0; i < MAP_SIZE; i++) {
            if (map[i][0] == player && map[i][1] == player && map[i][2] == player) return true;
            if (map[0][i] == player && map[1][i] == player && map[2][i] == player) return true;
        }

        if (map[0][0] == player && map[1][1] == player && map[2][2] == player) return true;
        if (map[0][2] == player && map[1][1] == player && map[2][0] == player) return true;
        return false;
    }

    public static boolean checkDraw() {

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (map[i][j] == EMPTY_FIELD) {
                    return false;
                }
            }
        }
        return true;

    }

    public static boolean isCellValid(int x, int y) {
        if (x < 0 || y < 0 || x >= MAP_SIZE || y >= MAP_SIZE) {
            return false;
        }
        if (map[y][x] != EMPTY_FIELD) {
            return false;
        }
        return true;
    }


    public void botTurn() {
        int x, y;
        do {
            x = (int) (Math.random() * MAP_SIZE);
            y = (int) (Math.random() * MAP_SIZE);
        } while (!isCellValid(x, y));
        map[y][x] = O_FIELD;
    }
}




