package com.ryan.pitchcounter;

import java.util.ArrayList;

public class Pitcher {
    private String name;
    private int numPitches;
    private ArrayList<Game> games;

    public Pitcher(String name, int numPitches) {
        this.name = name;
        this.numPitches = numPitches;
        this.games = new ArrayList<Game>();
    }

    public Pitcher(String name) {
        this.name = name;
        this.numPitches = 0;
        this.games = new ArrayList<Game>();
    }

    public void addGame(Game theGame) {
        games.add(theGame);
    }

    public int totalPitches() {
        int total = 0;
        for(int i = 0; i < games.size(); i++)
            total += games.get(i).getTotalPitches();
        return total;
    }

    public void addPitch() {
        this.numPitches++;
    }

    public void subtractPitch() {
        this.numPitches--;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumPitches() {
        return numPitches;
    }

    public void setNumPitches(int numPitches) {
        this.numPitches = numPitches;
    }

    public ArrayList<Game> getGames() {
        return games;
    }

    public void setGames(ArrayList<Game> games) {
        this.games = games;
    }

    @Override
    public boolean equals(final Object otherPitcher) {
        if(otherPitcher instanceof Pitcher)
            return this.name.equals(((Pitcher) otherPitcher).getName());
        return false;
    }

    @Override
    public String toString() {
        return "Pitcher Name: " + this.name + "\tNumPitches: " + this.numPitches;
    }
}
