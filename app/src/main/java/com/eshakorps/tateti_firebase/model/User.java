package com.eshakorps.tateti_firebase.model;

public class User {
    private String name;
    private int points;
    private int playable_games;

    public User(){

    }

    public User(String name, int points, int playable_games) {
        this.name = name;
        this.points = points;
        this.playable_games = playable_games;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPlayable_games() {
        return playable_games;
    }

    public void setPlayable_games(int playable_games) {
        this.playable_games = playable_games;
    }
}
