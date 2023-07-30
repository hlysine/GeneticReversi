package io.github.hlysine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.hlysine.Helper.random;

public class GameSystem {
    public final List<Match> pendingMatches;
    public final List<Match> ongoingMatches;
    public boolean done = false;

    //initialize matches system
    public GameSystem(int population) {
        pendingMatches = new ArrayList<>();
        ongoingMatches = new ArrayList<>();
        for (int i = 0; i < population; i++) {
            for (int j = i + 1; j < population; j++) {
                pendingMatches.add(new Match(i, j));
            }
        }
        System.out.println("Matches initialized with " + pendingMatches.size() + " pending matches");
    }

    //prepare system for a new round
    public void newRound() {
        for (int i = ongoingMatches.size() - 1; i >= 0; i--) {
            Match m = ongoingMatches.get(i);
            m.playerColor = random(1) >= 0.5;
            m.done = false;
            pendingMatches.add(m);
            ongoingMatches.remove(i);
        }
        Collections.shuffle(pendingMatches);
        done = false;
    }

    //get a new game in the round
    public synchronized int newGame() {
        if (done) return -1;
        if (pendingMatches.size() == 0) return -1;
        Match m = pendingMatches.remove(0);
//        System.out.println(padZero(ongoingMatches.size(), 5) + " game");
        ongoingMatches.add(m);
        return ongoingMatches.indexOf(m);
    }

    //check if all matches have finished
    public boolean roundFinished() {
        if (done) return true;
        if (pendingMatches.size() > 0) return false;
        for (int i = ongoingMatches.size() - 1; i >= 0; i--) {
            if (!ongoingMatches.get(i).done) return false;
        }
        done = true;
        return true;
    }
}

