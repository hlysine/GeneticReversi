package io.github.hlysine;

public class Game {
    public int activePlayer;
    public int[][] board;
    private int size = Main.boardSize;
    public int endResult;
    public int step;

    public Game() {
    }

    //copy constructor
    public Game(Game copy) {
        activePlayer = copy.activePlayer;
        size = copy.size;
        endResult = copy.endResult;
        board = new int[size][size];
        step = copy.step;
        for (int i = 0; i<size; i++) {
            System.arraycopy(copy.board[i], 0, board[i], 0, size);
        }
    }

    public void Init() {
        board = new int[size][size];
        for (int i = 0; i<size; i++) {
            for (int j = 0; j<size; j++) {
                board[i][j] = 0;
            }
        }
        board[size/2-1][size/2-1] = 1;
        board[size/2-1][size/2] = 2;
        board[size/2][size/2-1] = 2;
        board[size/2][size/2] = 1;
        activePlayer = 1;
        endResult = 0;
        step = 0;
    }

    public int score(int player) {
        return Count(player) - Count(3-player) + 64; //add 64 to prevent negative which mess up fitness
    }

    public void Move(int x, int y, int player) {
        if (activePlayer == player) {
            if (isValid(x, y, player)) {
                board[x][y] = player;
                step++;
                for (int i = x-1; i <=x+1; i++) {
                    for (int j = y-1; j <=y+1; j++) {
                        if (x!=i || y!=j) {
                            if (inBoundary(i, j)) {
                                if (board[i][j] == 3-player) {
                                    int dx = i-x;
                                    int dy = j-y;
                                    int ox = x + dx;
                                    int oy = y + dy;
                                    boolean flag = true;
                                    boolean flip = false;
                                    while (inBoundary(ox, oy) && flag) {
                                        if (flip) {
                                            if (board[ox][oy] == 3-player) {
                                                board[ox][oy] = player;
                                            } else if (board[ox][oy] == player) {
                                                flag = false;
                                            }
                                        } else {
                                            if (board[ox][oy] == player) {
                                                dx = -dx;
                                                dy = -dy;
                                                flip = true;
                                            } else if (board[ox][oy] == 0) {
                                                flag = false;
                                            }
                                        }
                                        ox += dx;
                                        oy += dy;
                                    }
                                }
                            }
                        }
                    }
                }
                if (hasValidMove(3 - activePlayer)) {
                    activePlayer = 3 - activePlayer;
                } else {
                    //println(PlayerName(3 - activePlayer) + " has no valid move! Fallback to " + PlayerName(activePlayer));
                }
                endResult = Win();
            } else {
                System.out.println("Invalid move - Invalid position");
            }
        } else {
            System.out.println("Invalid move - Not player " + player + "'s turn!");
        }
    }

    public String PlayerName(int player) {
        if (player == 1) return "Black";
        else if (player == 2) return "White";
        else return "Unknown";
    }

    public int Win() {
        for (int i = 0; i<size; i++) {
            for (int j = 0; j<size; j++) {
                if (board[i][j] == 0) {
                    if (isValid(i, j, 1) || isValid(i, j, 2)) return 0;
                }
            }
        }
        int c1 = Count(1);
        int c2 = Count(2);
        if (c1>c2) {
            return 1;
        } else if (c2>c1) {
            return 2;
        } else {
            return 3;
        }
    }

    public boolean hasValidMove(int player) {
        for (int i = 0; i<size; i++) {
            for (int j = 0; j<size; j++) {
                if (board[i][j] == 0) {
                    if (isValid(i, j, player)) return true;
                }
            }
        }
        return false;
    }

    public boolean isValid(int x, int y, int player) {
        //has to be empty
        if (board[x][y] != 0) return false;
        boolean valid = false;
        //A valid position must have an opponent's disk next to it
        for (int i = x-1; i <=x+1; i++) {
            for (int j = y-1; j <=y+1; j++) {
                if (x!=i || y!=j) {
                    if (inBoundary(i, j)) {
                        valid = valid || (board[i][j] == 3-player);
                    }
                }
            }
        }
        if (!valid) return false;
        valid = false;
        //A valid position means at least 1 opponent's disk can be flipped
        for (int i = x-1; i <=x+1; i++) {
            for (int j = y-1; j <=y+1; j++) {
                if (x!=i || y!=j) {
                    if (inBoundary(i, j)) {
                        if (board[i][j] == 3-player) {
                            int dx = i-x;
                            int dy = j-y;
                            int ox = x + dx;
                            int oy = y + dy;
                            boolean flag = true;
                            while (inBoundary(ox, oy) && flag) {
                                if (board[ox][oy] == player) {
                                    return true;
                                } else if (board[ox][oy] == 0) {
                                    flag = false;
                                }
                                ox += dx;
                                oy += dy;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean inBoundary(int x, int y) {
        return x>=0 && y>=0 && x<size && y<size;
    }

    public int Count(int player) {
        int count = 0;
        for (int i = 0; i<size; i++) {
            for (int j = 0; j<size; j++) {
                if (board[i][j] == player) count ++;
            }
        }
        return count;
    }
}

