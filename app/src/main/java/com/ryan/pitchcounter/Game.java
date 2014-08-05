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
    private short ID;

    public Game(Calendar date, Pitcher thePitcher, final short ID)
    {
        this.theDate = date;
        this.thePitcher = thePitcher;
        this.numBall = 0;
        this.numStrike = 0;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public Game(Calendar date, String pitcherName, int numStrike, int numBall, final short ID)
    {
        this.theDate = date;
        this.thePitcher = new Pitcher(pitcherName, numBall + numStrike);
        this.numStrike = numStrike;
        this.numBall = numBall;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public Game(Calendar date, Pitcher thePitcher, int numStrike, int numBall, final short ID)
    {
        this.theDate = date;
        this.thePitcher = thePitcher;
        this.numStrike = numStrike;
        this.numBall = numBall;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public Game(Pitcher thePitcher, short ID, Calendar date, int numStrike, int numBall) {
        this.theDate = date;
        this.thePitcher = thePitcher;
        this.numStrike = numStrike;
        this.numBall = numBall;
        this.date = calendarToDate(theDate);
        this.ID = ID;
    }

    public short getID() {
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

    public Calendar getDateCalendar() { return this.theDate; }
    public String getPitcherName() { return this.thePitcher.getName(); }

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
    public String toString()
    {
        return thePitcher.toString() + " ON: " + this.date + " #Str: " + this.numStrike + " #Ball: " +
            this.numBall;
    }



    @Override
    public boolean equals(Object otherGame) {
        if(otherGame instanceof Game)
            return this.getDate().equals(((Game) otherGame).getDate()) &&
                    this.getThePitcher().equals((Game)otherGame);
        return false;
    }

    private static final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] months = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};


}
