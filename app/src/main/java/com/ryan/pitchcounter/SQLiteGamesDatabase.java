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

public class SQLiteGamesDatabase extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "GamesDB";
    private static final String TABLE_NAME = "games";
    private static final String KEY_PITCHER_NAME = "PitcherName";
    private static final String KEY_GAME_ID = "GameID";
    private static final String KEY_DATE = "GameDate";
    private static final String KEY_STRIKES = "Strikes";
    private static final String KEY_BALLS = "Balls";

    private final String KEY_ID = "id";

    private final String[] COLUMNS = {KEY_ID, KEY_PITCHER_NAME, KEY_GAME_ID, KEY_DATE, KEY_STRIKES, KEY_BALLS};

    public SQLiteGamesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_PITCHER_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
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
        String getMonth = theStr.substring(0, theStr.indexOf("/")).replace("/", "");
        int month = Integer.parseInt(getMonth);

        theStr = theStr.substring(theStr.indexOf("/") + 1);

        String getDay = theStr.substring(0, theStr.indexOf("/")).replace("/", "");
        int day = Integer.parseInt(getDay);

        String getYear = theStr.substring(theStr.indexOf("/")).replace("/", "");
        int year = Integer.parseInt(getYear);

        return new GregorianCalendar(year, month, day);
    }

    //Returns 07/11/2014
    private String fromCalendar(Calendar theCal) {
        return theCal.get(Calendar.MONTH) + "/" + theCal.get(Calendar.DAY_OF_MONTH) + "/" +
                theCal.get(Calendar.YEAR);
    }

    public void addGame(final Game theGame)
    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PITCHER_NAME, theGame.getPitcherName());
        values.put(KEY_GAME_ID, theGame.getID());
        values.put(KEY_DATE, fromCalendar(theGame.getDateCalendar()));
        values.put(KEY_STRIKES, theGame.getNumStrike());
        values.put(KEY_BALLS, theGame.getNumBall());

        // 3. insert
        db.insert(TABLE_NAME, null, values);

        // 4. close
        db.close();
    }

    public List<Game> getAllGames()
    {
        final List<Game> theGames = new ArrayList<Game>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_NAME;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        String allG = "";

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst())
        {
            do
            {
                theGames.add(new Game(new Pitcher(cursor.getString(1)), Short.parseShort(cursor.getString(2)),
                        fromString(cursor.getString(3)), Integer.parseInt(cursor.getString(4)),
                                Integer.parseInt(cursor.getString(5))));
            }
            while (cursor.moveToNext());
        }

        db.close();

        return theGames;
    }

    public void deleteGames(Pitcher thePitcher)
    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        try
        {
            db.delete(TABLE_NAME,
                    KEY_PITCHER_NAME +" = ?",
                    new String[] {thePitcher.getName()});
        } catch (Exception e) {}

        // 3. close
        db.close();
    }


    public void deleteGame(Game theGame) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        try {
            db.delete(TABLE_NAME,
                    KEY_PITCHER_NAME + " = ? AND " + KEY_GAME_ID + " = ?",
                    new String[] {theGame.getPitcherName(), String.valueOf(theGame.getID())});
        } catch (Exception e) {}

        // 3. close
        db.close();
    }

    //Delete everything
    public void deleteAllGames() {
        SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.delete(TABLE_NAME, null, null);
        theDB.close();
    }

    public List<Game> getGamesForPitcher(Pitcher thePitcher) {
        List<Game> theGames = new ArrayList<Game>(getAllGames());
        List<Game> actual = new ArrayList<Game>();

        for(Game theGame : theGames)
            if(theGame.getThePitcher().equals(thePitcher))
                actual.add(theGame);
        return actual;
    }

    public void addGames(Game theGames[]) {
        SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.beginTransaction();

        for(Game theGame : theGames) {
            ContentValues theVals = new ContentValues();
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
