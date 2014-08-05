package com.ryan.pitchcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLitePitcherDatabase extends SQLiteOpenHelper
{
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "PitcherDB";
    private static final String TABLE_NAME = "pitcher";
    private static final String KEY_PITCHER_NAME = "PitcherName";
    private static final String KEY_NUM_PITCHES = "NumPitches";
    private final String KEY_ID = "id";

    private final String[] COLUMNS = {KEY_ID, KEY_PITCHER_NAME, KEY_NUM_PITCHES};

    public SQLitePitcherDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // SQL statement to create book table
        String CREATE_PITCHER_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PITCHER_NAME + " TEXT, "+
                KEY_NUM_PITCHES + " SHORT )";

        // create books table
        db.execSQL(CREATE_PITCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // create fresh books table
        this.onCreate(db);
    }

    public void addPitcher(final Pitcher thePitcher)
    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PITCHER_NAME, thePitcher.getName());
        values.put(KEY_NUM_PITCHES, thePitcher.getNumPitches());

        // 3. insert
        db.insert(TABLE_NAME, null, values);

        // 4. close
        db.close();
    }

    public List<Pitcher> getAllPitchers(Context theC)
    {
        Set<Pitcher> thePitchers = new HashSet<Pitcher>();
        List<String> theNames = new ArrayList<String>();

        SQLiteGamesDatabase theGDB = new SQLiteGamesDatabase(theC);
        Game[] theG = getArray(theGDB.getAllGames());

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_NAME;


        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst())
        {
            do
                theNames.add(cursor.getString(1));
            while (cursor.moveToNext());
        }

        short[] numPer = new short[theNames.size()];
        for(int i = 0; i < theNames.size(); i++)
            for(int y = 0; y < theG.length; y++)
                if(theNames.get(i).equals(theG[y].getPitcherName()))
                    numPer[i] += theG[y].getTotalPitches();

        for(int i = 0; i < theNames.size(); i++)
            thePitchers.add(new Pitcher(theNames.get(i), numPer[i]));

        db.close();
        return new ArrayList<Pitcher>(thePitchers);
    }

    public HashMap<String, Short> getHashMapPitchers()
    {
        //The Hash Map
        HashMap<String, Short> theMap = new HashMap<String, Short>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_NAME;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst())
        {
            do
            {
                theMap.put(cursor.getString(2), Short.parseShort(cursor.getString(3)));
            }
            while (cursor.moveToNext());
        }

        db.close();
        return theMap;
    }

    public void deletePitcher(Pitcher thePitcher)
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

    //Delete everything
    public void deleteAllPitchers()
    {
        SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.delete(TABLE_NAME, null, null);
        theDB.close();
    }

    public void addPitchers(Pitcher[] thePitchers)
    {
        SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.beginTransaction();

        for(Pitcher thePitcher : thePitchers)
        {
            ContentValues theVals = new ContentValues();
            theVals.put(KEY_PITCHER_NAME, thePitcher.getName());
            theVals.put(KEY_NUM_PITCHES, thePitcher.getNumPitches());
            theDB.insert(TABLE_NAME, null, theVals);
        }
        theDB.setTransactionSuccessful();
        theDB.endTransaction();
        theDB.close();
    }

    public Game[] getArray(List<Game> theGames)
    {
        return theGames.toArray(new Game[theGames.size()]);
    }


    public void log(final String message)
    {
        Log.e("com.ryan.pitchcounter", message);
    }
}
