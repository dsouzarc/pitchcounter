package com.ryan.pitchcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.LinkedList;

public class SQLiteGamesDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "GamesDB";
    private static final String TABLE_NAME = "games";
    private static final String KEY_PITCHER_NAME = "PitcherName";
    private static final String KEY_GAME_ID = "GameID";
    private static final String KEY_DATE = "GameDate";
    private static final String KEY_STRIKES = "Strikes";
    private static final String KEY_BALLS = "Balls";

    private final String KEY_ID = "id";

    private final String[] COLUMNS = {KEY_ID, KEY_PITCHER_NAME, KEY_GAME_ID, KEY_DATE, KEY_STRIKES, KEY_BALLS};

    public SQLiteGamesDatabase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        // SQL statement to create book table
        final String CREATE_PITCHER_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PITCHER_NAME + " TEXT, "+
                KEY_GAME_ID + " SHORT, " +
                KEY_DATE + " TEXT, " +
                KEY_STRIKES + " SHORT, " +
                KEY_BALLS + " SHORT )";

        // create books table
        db.execSQL(CREATE_PITCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // create fresh books table
        this.onCreate(db);
    }
    //Returns calender from theStr = "07/11/2014"
    private Calendar fromString(String theStr) {
        final String getMonth = theStr.substring(0, theStr.indexOf("/")).replace("/", "");
        final int month = Integer.parseInt(getMonth);

        theStr = theStr.substring(theStr.indexOf("/") + 1);

        final String getDay = theStr.substring(0, theStr.indexOf("/")).replace("/", "");
        final int day = Integer.parseInt(getDay);

        final String getYear = theStr.substring(theStr.indexOf("/")).replace("/", "");
        final int year = Integer.parseInt(getYear);

        return new GregorianCalendar(year, month, day);
    }

    //Returns 07/11/2014
    private String fromCalendar(final Calendar theCal) {
        return theCal.get(Calendar.MONTH) + "/" + theCal.get(Calendar.DAY_OF_MONTH) + "/" +
                theCal.get(Calendar.YEAR);
    }

    public void addGame(final Game theGame) {
        final SQLiteDatabase db = this.getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(KEY_PITCHER_NAME, theGame.getPitcherName());
        values.put(KEY_GAME_ID, theGame.getID());
        values.put(KEY_DATE, fromCalendar(theGame.getDateCalendar()));
        values.put(KEY_STRIKES, theGame.getNumStrike());
        values.put(KEY_BALLS, theGame.getNumBall());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Game> getAllGames() {
        final LinkedList<Game> theGames = new LinkedList<Game>();
        final String query = "SELECT  * FROM " + TABLE_NAME;

        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                theGames.add(new Game(new Pitcher(cursor.getString(1)), Short.parseShort(cursor.getString(2)),
                        fromString(cursor.getString(3)), Integer.parseInt(cursor.getString(4)),
                                Integer.parseInt(cursor.getString(5))));
            }
            while (cursor.moveToNext());
        }

        db.close();

        return new ArrayList<Game>(theGames);
    }

    public void deleteGames(final Pitcher thePitcher) {
        final SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(TABLE_NAME,
                    KEY_PITCHER_NAME +" = ?",
                    new String[] {thePitcher.getName()});
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
    }


    public void deleteGame(final Game theGame) {
        final SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(TABLE_NAME,
                    KEY_PITCHER_NAME + " = ? AND " + KEY_GAME_ID + " = ?",
                    new String[] {theGame.getPitcherName(), String.valueOf(theGame.getID())});
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
    }

    //Delete everything
    public void deleteAllGames() {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.delete(TABLE_NAME, null, null);
        theDB.close();
    }

    public List<Game> getGamesForPitcher(final Pitcher thePitcher) {
        final List<Game> theGames = new ArrayList<Game>(getAllGames());
        final LinkedList<Game> actual = new LinkedList<Game>();

        for(Game theGame : theGames) {
            if (theGame.getThePitcher().equals(thePitcher)) {
                actual.add(theGame);
            }
        }
        return new ArrayList<Game>(actual);
    }

    public void addGames(final Game theGames[]) {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.beginTransaction();

        for(Game theGame : theGames) {
            final ContentValues theVals = new ContentValues();
            theVals.put(KEY_PITCHER_NAME, theGame.getPitcherName());
            theVals.put(KEY_GAME_ID, theGame.getID());
            theVals.put(KEY_DATE, fromCalendar(theGame.getDateCalendar()));
            theVals.put(KEY_STRIKES, theGame.getNumStrike());
            theVals.put(KEY_BALLS, theGame.getNumBall());
            theDB.insert(TABLE_NAME, null, theVals);
        }

        theDB.setTransactionSuccessful();
        theDB.endTransaction();
        theDB.close();
    }

    public void log(final String message) {
        Log.e("com.ryan.pitchcounter", message);
    }
}
