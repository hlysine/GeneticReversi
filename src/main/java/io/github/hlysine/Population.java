package io.github.hlysine;

import com.opencsv.exceptions.CsvException;
import io.github.hlysine.table.Table;
import io.github.hlysine.table.TableRow;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.hlysine.Helper.*;

public class Population {
    public Player[] players;//all players
    public final Game[] games;//all concurrent games
    //int bestPlayerNo;//the position in the array that the best player of this generation is in
    public int gen = 0;
    public float bestRating = -9999;//the elo rating of the best ever player


    //------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Population(int size) {
        players = new Player[size];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player();
            players[i].id = i;
        }
        games = new Game[Main.concurrentMatches];
        for (int i = 0; i < games.length; i++) {
            games[i] = new Game();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //run all matches in a generation
    public void runMatches() {
        //start a new round
        Main.gameSystem.newRound();

        //get all the matches running
        ExecutorService exec = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < Main.concurrentMatches; i++) {
                final int gid = i;
                exec.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                int gameId = gid;
                                Game g = games[gameId];

                                while (!Main.gameSystem.roundFinished()) {
                                    //prepare for a new game
                                    int matchId = Main.gameSystem.newGame();
                                    Match m = Main.gameSystem.ongoingMatches.get(matchId);

                                    Player p1;
                                    Player p2;
                                    if (m.playerColor) {
                                        p1 = players[m.player2];
                                        p2 = players[m.player1];
                                    } else {
                                        p1 = players[m.player1];
                                        p2 = players[m.player2];
                                    }
                                    g.Init();

                                    //play
                                    while (g.endResult == 0) {
                                        if (g.activePlayer == 1) {
                                            p1.play(gameId, 1);
                                        } else {
                                            p2.play(gameId, 2);
                                        }
                                    }
                                    float p1s;
                                    float p2s;
                                    if (g.endResult == 1) {
                                        p1s = 1;
                                        p2s = 0;
                                    } else if (g.endResult == 2) {
                                        p2s = 1;
                                        p1s = 0;
                                    } else {
                                        p1s = 0.5f;
                                        p2s = 0.5f;
                                    }

                                    synchronized (this) {
                                        float e1 = (float) Math.pow(10, p1.eloRating / 400);
                                        float e2 = (float) Math.pow(10, p2.eloRating / 400);
                                        p1.eloRating += Main.eloK * (p1s - e1 / (e1 + e2));
                                        p2.eloRating += Main.eloK * (p2s - e2 / (e1 + e2));
//                                        System.out.println(padZero(matchId, 5) + " game done - p" + padZero(p1.id, 3) + " " + padZero(p1.eloRating, 4, 2) + " - p" + padZero(p2.id, 3) + " " + padZero(p2.eloRating, 4, 2));
                                    }
                                    m.done = true;
                                }
                            }
                        }
                );
            }
        } finally {
            exec.shutdown();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //sets the best player globally and for this gen
    public int setBestPlayer() {
        //get max fitness
        float max = -9999;
        int maxIndex = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i].fitness > max) {
                max = players[i].fitness;
                maxIndex = i;
            }
        }


        //if best this gen is better than the global best score then set the global best as the best this gen

        if (players[maxIndex].eloRating > bestRating) {
            bestRating = players[maxIndex].eloRating;
        }
        return maxIndex;
    }

    public void savePopulationStat() {
        Table popStats = new Table();
        popStats.addColumn("Best Elo Rating");
        popStats.addColumn("Population Generation");
        TableRow tr = popStats.addRow();
        tr.setFloat(0, bestRating);
        tr.setInt(1, gen);

        try {
            popStats.saveToFile(dataPath(Main.populationStatsFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPopulationStat() {
        try {
            Table t = Table.loadFromFile(dataPath(Main.populationStatsFile));
            TableRow tr = t.getRow(1);
            bestRating = tr.getFloat(0);
            gen = tr.getInt(1);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //returns true if all matches are done
    public boolean done() {
        return Main.gameSystem.roundFinished();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //creates the next generation of players by natural selection
    public void naturalSelection() {

        Player[] newPlayers = new Player[players.length];//Create new players array for the next generation

        int bestNo = setBestPlayer();//set which player is the best

        newPlayers[0] = players[bestNo].clone();//add the best player of this generation to the next generation without mutation
        newPlayers[0].id = 0;
        newPlayers[0].eloRating = 1000;
        for (int i = 1; i < players.length; i++) {
            //for each remaining spot in the next generation
            if (i < players.length / 2) {
                newPlayers[i] = selectPlayer().clone();//select a random player(based on fitness) and clone it
            } else {
                newPlayers[i] = selectPlayer().crossover(selectPlayer());
            }
            newPlayers[i].id = i;
            newPlayers[i].mutate(); //mutate it
            newPlayers[i].eloRating = 1000;
        }

        players = newPlayers.clone();
        System.out.println("Gen: " + gen + " Best Elo: " + bestRating);
        System.out.println("=========================================================");
        System.out.println();
        gen += 1;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //chooses player from the population to return randomly(considering fitness)


    public Player selectPlayer() {
        //this function works by randomly choosing a value between 0 and the sum of all the fitnesses
        //then go through all the players and add their fitness to a running sum and if that sum is greater than the random value generated that player is chosen
        //since players with a higher fitness function add more to the running sum then they have a higher chance of being chosen


        //calculate the sum of all the fitnesses
        float fitnessSum = 0;
        for (Player player : players) {
            fitnessSum += player.fitness;
        }
        float rand = random(fitnessSum);
        //summy is the current fitness sum
        float runningSum = 0;

        for (Player player : players) {
            runningSum += player.fitness;
            if (runningSum > rand) {
                return player;
            }
        }
        //unreachable code to make the parser happy
        return players[players.length - 1];
    }

    //------------------------------------------------------------------------------------------------------------------------------------------

    //mutates all the players
    public void mutate() {
        for (int i = 1; i < players.length; i++) {
            players[i].mutate();
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------

    //calculates the fitness of all the players
    public void calculateFitness() {
        for (int i = 1; i < players.length; i++) {
            players[i].calculateFitness();
        }
    }
}

