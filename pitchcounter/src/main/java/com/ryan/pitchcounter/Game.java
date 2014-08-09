package com.ryan.pitchcounter;

import java.util.Calendar;
/**
 * Created by Ryan on 7/8/14.
 */
public class Game {

    private String date;
    private Pitcher thePitcher;
    private int numStrike, numBall;
    private Calendar theDate;
    private long ID;

    public Game(final Calendar date, final Pitcher thePitcher, final long ID) {
        this.theDate = date;
        this.thePitcher = thePitcher;
        this.numBall = 0;
        this.numStrike = 0;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public Game(final Calendar date, final Pitcher thePitcher, final int numStrike,
                final int numBall, final long ID) {
        this.theDate = date;
        this.thePitcher = thePitcher;
        this.numStrike = numStrike;
        this.numBall = numBall;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public long getID() {
        return this.ID;
    }

    public void setID(final short ID) {
        this.ID = ID;
    }

    public String calendarToDate(Calendar theCal) {
        return days[theCal.get(Calendar.DAY_OF_WEEK)-1] +  ", " +
                months[theCal.get(Calendar.MONTH)] + " " + theCal.get(Calendar.DAY_OF_MONTH) +
                ", " + theCal.get(Calendar.YEAR);
    }

    public Calendar getDateCalendar() {
        return this.theDate;
    }

    public String getPitcherName() {
        return this.thePitcher.getName();
    }

    public int getTotalPitches() {
        return this.numBall + this.numStrike;
    }

    public void gotStrike() {
        this.numStrike++;
        this.thePitcher.addPitch();
    }

    public void gotBall() {
        this.numBall++;
        this.thePitcher.addPitch();
    }

    public void undoStrike() {
        this.numStrike--;
        this.thePitcher.subtractPitch();
    }

    public void undoBall() {
        this.numBall--;
        this.thePitcher.subtractPitch();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Pitcher getThePitcher() {
        return thePitcher;
    }

    public void setThePitcher(Pitcher thePitcher) {
        this.thePitcher = thePitcher;
    }

    public int getNumStrike() {
        return numStrike;
    }

    public void setNumStrike(int numStrike) {
        this.numStrike = numStrike;
    }

    public int getNumBall() {
        return numBall;
    }

    public void setNumBall(int numBall) {
        this.numBall = numBall;
    }

    @Override
    public String toString() {
        return thePitcher.toString() + " ON: " + this.date + " #Str: " + this.numStrike + " #Ball: " +
            this.numBall + " ID: " + this.ID;
    }

    @Override
    public boolean equals(Object otherGame) {
        if(otherGame instanceof Game)
            return this.getDateCalendar().equals(((Game) otherGame).getDateCalendar()) &&
                    this.getThePitcher().equals((Game)otherGame);
        return false;
    }

    private static final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] months = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};
}