package io.github.hlysine;

import com.opencsv.exceptions.CsvException;
import io.github.hlysine.table.Table;
import io.github.hlysine.table.TableRow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static io.github.hlysine.Helper.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting in directory: " + new File(".").getAbsolutePath());
        try {
            setup();
            while (!exiting) {
                tick();
                TimeUnit.SECONDS.sleep(Main.printFrequency);
            }
        } catch (IOException | CsvException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final String settingsDir = "";
    public static final String settingsFile = "settings.csv";
    public static String dataDir = "data/";
    public static final String populationStatsFile = "populationStats.csv";
    public static int boardSize = 8;
    public static int saveFrequency = 15;
    public static int printFrequency = 1;
    public static int concurrentMatches = Runtime.getRuntime().availableProcessors() * 2;
    public static int populationSize = 300;
    public static int graphGroup = 100;

    public static float globalMutationRate = 0.1f;
    public static float eloK = 32;

    public static boolean stopForFileLoad = false;
    public static boolean stopForFileSave = false;
    public static boolean showNothing = false;
    public static boolean quitAfterSave = false;
    public static int fileProgress;
    public static long lastGenStart;
    public static float lastGenTime;

    public static Population population;

    public static GameSystem gameSystem;
    public static boolean exiting = false;

    public static void exit() {
        exiting = true;
    }

    public static void setup() throws IOException, CsvException {//on startup
        population = new Population(populationSize);
        gameSystem = new GameSystem(populationSize);
        lastGenStart = System.currentTimeMillis();
        lastGenTime = 0;
        File settingsFile = new File(settingsPath(Main.settingsFile));
        if (settingsFile.exists() && !settingsFile.isDirectory()) {
            Table t = Table.loadFromFile(settingsPath(Main.settingsFile));
            TableRow tr = t.getRow(1);
            saveFrequency = tr.getInt(0);
            TableRow tr3 = t.getRow(3);
            concurrentMatches = tr3.getInt(0);
            TableRow tr5 = t.getRow(5);
            dataDir = tr5.getString(0);
            TableRow tr7 = t.getRow(7);
            printFrequency = tr7.getInt(0);
        } else {
            Table t = new Table();
            t.addColumn("Auto-save frequency (generations)");
            TableRow tr = t.addRow();
            tr.setInt(0, saveFrequency);
            TableRow tr2 = t.addRow();
            tr2.setString(0, "Concurrent thread count");
            TableRow tr3 = t.addRow();
            tr3.setInt(0, concurrentMatches);
            TableRow tr4 = t.addRow();
            tr4.setString(0, "Data directory");
            TableRow tr5 = t.addRow();
            tr5.setString(0, dataDir);
            TableRow tr6 = t.addRow();
            tr6.setString(0, "Console print frequency (seconds)");
            TableRow tr7 = t.addRow();
            tr7.setInt(0, printFrequency);

            t.saveToFile(settingsPath(Main.settingsFile));
        }
        File statsFile = new File(dataPath(populationStatsFile));
        if (statsFile.exists() && !statsFile.isDirectory()) {
            gameSystem.done = true;
            stopForFileLoad = true;
        } else {
            population.runMatches();
        }
        Files.createDirectories(Paths.get(dataDir));
    }

    public static void tick() {
        System.out.println("Gen: " + population.gen + " (auto-saves at every " + saveFrequency + "th gen)\n" +
                "Highest Elo rating: " + population.bestRating + "\n" +
                "Best player Elo rating(this gen): " + population.players[0].eloRating + "\n" +
                "Gen progress: " + gameSystem.ongoingMatches.size() + "/" + (populationSize * (populationSize - 1) / 2) + "\n" +
                "Global mutation rate: " + globalMutationRate + "\n" +
                "Last gen time taken: " + padZero(lastGenTime, 0, 2) + "s"
        );

        String s = "";
        if (stopForFileLoad) {
            if (population.done())
                s = "Loading files";
            else
                s = "Will load files when this gen ends";
        }
        if (stopForFileSave) {
            if (population.done())
                s = "Saving files";
            else {
                if (quitAfterSave)
                    s = "Will save files when this gen ends and quit";
                else
                    s = "Will save files when this gen ends";
            }
        }
        System.out.println(s);


        if (population.done()) {
            population.calculateFitness();
            population.naturalSelection();
            if (stopForFileSave || (population.gen % saveFrequency == 0 && population.gen > 1)) {
                fileProgress = 0;
                System.out.println("Start file saving");
                for (int i = 0; i < population.players.length; i++) {
                    population.players[i].savePlayer();
                    int p = i * 100 / population.players.length;
                    if (floor(p / 10f) * 10 - floor(fileProgress / 10f) * 10 > 10) {
                        System.out.println("Progress: " + p + "%");
                        fileProgress = p;
                    }
                }
                population.savePopulationStat();
                System.out.println("Saving complete");
                population.players[0].savePlayer("Best");
                System.out.println("Saved best player");
                System.out.println("=========================================================");
                System.out.println();
                stopForFileSave = false;
                if (quitAfterSave) {
                    exit();
                    return;
                }
            }
            if (stopForFileLoad) {
                fileProgress = 0;
                System.out.println("Start file loading");
                for (int i = 0; i < population.players.length; i++) {
                    population.players[i] = population.players[i].loadPlayer();
                    population.players[i].id = i;
                    int p = i * 100 / population.players.length;
                    if (floor(p / 10f) * 10 - floor(fileProgress / 10f) * 10 > 10) {
                        System.out.println("Progress: " + p + "%");
                        fileProgress = p;
                    }
                }
                population.loadPopulationStat();
                System.out.println("Loading complete");
                System.out.println("=========================================================");
                System.out.println();
                stopForFileLoad = false;
            }
            //genetic algorithm
            lastGenTime = (System.currentTimeMillis() - lastGenStart) / 1000f;
            lastGenStart = System.currentTimeMillis();
            population.runMatches();
        }
    }

}