package com.eshakorps.tateti_firebase.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {
    private String player1;
    private String player2;
    private List<Integer> selectedCells;
    private boolean isPlayer1Turn;
    private Date date;
    private String gooutID;
    private String winnerID;

    public Game(){

    }

    public Game(Date date, String gooutID, String player1, boolean isPlayer1Turn, String player2, List<Integer> selectedCells, String winnerID) {
        this.player1 = player1;
        this.player2 = player2;
        this.selectedCells = selectedCells;
        this.isPlayer1Turn = isPlayer1Turn;
        this.date = date;
        this.gooutID = gooutID;
        this.winnerID = winnerID;
    }

    public Game(String player1) {
        this.player1 = player1;
        this.player2 = "";
        this.selectedCells = new ArrayList<>();
        for (int i=0;i<9;i++){
            this.selectedCells.add(new Integer(0));
        }
        this.isPlayer1Turn = true;
        this.date = new Date();
        this.gooutID = "";
        this.winnerID = "";
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public List<Integer> getSelectedCells() {
        return selectedCells;
    }

    public void setSelectedCells(List<Integer> selectedCells) {
        this.selectedCells = selectedCells;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public void setPlayer1Turn(boolean player1Turn) {
        isPlayer1Turn = player1Turn;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGooutID() {
        return gooutID;
    }

    public void setGooutID(String gooutID) {
        this.gooutID = gooutID;
    }

    public String getWinnerID() {
        return winnerID;
    }

    public void setWinnerID(String winnerID) {
        this.winnerID = winnerID;
    }
}
