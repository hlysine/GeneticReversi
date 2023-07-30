package io.github.hlysine;

public class Match {
    public int player1;
    public int player2;
    public boolean playerColor;
    public boolean done;

    public Match(int p1, int p2) {
        player1 = p1;
        player2 = p2;
        done = false;
    }
}
