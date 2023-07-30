package io.github.hlysine;

import com.opencsv.exceptions.CsvException;
import io.github.hlysine.table.Table;

import java.io.IOException;

import static io.github.hlysine.Helper.dataPath;

public class Player {
    public int id;
    public float eloRating = 1000; //the rating point after playing with all other players
    public float fitness; //rating point squared, used in genetic algo

    //--------AI stuff
    private NeuralNet brain;


    //------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Player() {
        brain = new NeuralNet(66, 66, 64, 4);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //for genetic algorithm
    public void calculateFitness() {
        fitness = eloRating * eloRating;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public void mutate() {
        brain.mutate(Main.globalMutationRate);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns a clone of this player with the same brian
    public Player clone() {
        Player clone = new Player();
        clone.brain = brain.clone();
        clone.eloRating = eloRating;
        clone.id = id;
        return clone;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public Player crossover(Player parent2) {
        Player child = new Player();
        child.id = id;
        child.brain = brain.crossover(parent2.brain);
        return child;
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    //looks in 8 directions to find asteroids
    public void play(int gameId, int myColor) {
        Game myGame = Main.population.games[gameId];
        if (myGame.activePlayer != myColor) return;
        if (myGame.endResult != 0) return;

        float[] vision = new float[66];//the input array fed into the neuralNet
        float[] decision = new float[64]; //the output of the NN
        for (int i = 0; i < Main.boardSize; i++) {
            for (int j = 0; j < Main.boardSize; j++) {
                if (myGame.board[j][i] == 3 - myColor) {
                    vision[i * 8 + j] = -1;
                }
                if (myGame.board[j][i] == myColor) {
                    vision[i * 8 + j] = 1;
                } else {
                    vision[i * 8 + j] = 0;
                }
            }
        }
        vision[64] = myGame.step;
        vision[65] = myColor;

        float max = -9999;
        int maxIndex = 0;
        //get the output of the neural network
        decision = brain.output(vision);

        for (int i = 0; i < decision.length; i++) {
            if (decision[i] >= max && myGame.isValid(i % 8, i / 8, myColor)) {
                max = decision[i];
                maxIndex = i;
            }
        }

        myGame.Move(maxIndex % 8, maxIndex / 8, myColor);
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    //saves the player to a file by converting it to a table
    public void savePlayer() {
        //save players brain
        try {
            brain.NetToTable().saveToFile(dataPath("player" + id + ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePlayer(String playerName) {
        //save players brain
        try {
            brain.NetToTable().saveToFile(dataPath("player" + playerName + ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    //return the player saved in the parameter position
    public Player loadPlayer() {
        try {
            Player load = new Player();
            Table t = Table.loadFromFile(dataPath("player" + id + ".csv"));
            load.brain.TableToNet(t);
            return load;
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public Player loadPlayer(String playerName) {
        try {
            Player load = new Player();
            Table t = Table.loadFromFile(dataPath("player" + playerName + ".csv"));
            load.brain.TableToNet(t);
            return load;
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }
}

