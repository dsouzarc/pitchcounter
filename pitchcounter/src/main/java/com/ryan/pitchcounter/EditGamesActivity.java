package com.ryan.pitchcounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Stack;

public class EditGamesActivity extends Activity {

    private final Stack<Boolean> isStrikeStack = new Stack<Boolean>();
    private final Context theC = this;

    private static final String prefName = "com.ryan.pitchcounter";
    private static final String ID_KEY = "ID";
    private static final DecimalFormat theFormat = new DecimalFormat("0.000");

    private SQLiteGamesDatabase theGamesDB;
    private Pitcher thePitcher = null;

    private TextView numPitches;
    private TextView numStrikes;
    private TextView numBalls;
    private TextView ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_games);
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        incrementCounter();

        final Game theG = getGame();

        thePitcher = getGame().getThePitcher();

        getActionBar().setTitle(theG.getPitcherName());

        final TextView pNameDate = (TextView) findViewById(R.id.pitcherNameDateTV);
        pNameDate.setText(theG.getPitcherName() + " on " + theG.getDate());

        numPitches = (TextView) findViewById(R.id.numPitchesTV);
        numPitches.setText(String.valueOf(theG.getTotalPitches()));

        numStrikes = (TextView) findViewById(R.id.numStrikesTV);
        numStrikes.setText(String.valueOf(theG.getNumStrike()));

        numBalls = (TextView) findViewById(R.id.numBallsTV);
        numBalls.setText(String.valueOf(theG.getNumBall()));

        ratio = (TextView) findViewById(R.id.percRatioTV);
        ratio.setText(getPercRatio(theG.getNumStrike(), theG.getNumBall()));

        final Button strikeButton = (Button) findViewById(R.id.strikeButton);
        strikeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                theG.gotStrike();
                isStrikeStack.add(true);
                updateData(theG);
            }
        });

        final Button ballButton = (Button) findViewById(R.id.ballButton);
        ballButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                theG.gotBall();
                isStrikeStack.add(false);
                updateData(theG);
            }
        });

        final Button undoPitch = (Button) findViewById(R.id.undoPitchButton);
        undoPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isStrikeStack.size() == 0)
                    return;

                final boolean lastIsStrike = isStrikeStack.pop();

                if(lastIsStrike)
                    theG.undoStrike();
                else
                    theG.undoBall();

                updateData(theG);
            }
        });
    }

    private void incrementCounter() {
        final SharedPreferences settings = getSharedPreferences(prefName, 0);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putInt(ID_KEY, getCounter() + 1);
        editor.commit();
    }

    private short getCounter() {
        final SharedPreferences settings = getSharedPreferences(prefName, 0);
        return (short) settings.getInt(ID_KEY, 0);
    }

    private void updateData(final Game theG) {
        new UpdateDatabase().execute(theG);
        numPitches.setText(String.valueOf(theG.getTotalPitches()));
        numStrikes.setText(String.valueOf(theG.getNumStrike()));
        numBalls.setText(String.valueOf(theG.getNumBall()));
        ratio.setText(getPercRatio(theG.getNumStrike(), theG.getNumBall()));
    }

    private class UpdateDatabase extends AsyncTask<Game, Void, Void> {
        @Override
        public Void doInBackground(final Game... theGames) {
            theGamesDB = new SQLiteGamesDatabase(theC);
            theGamesDB.deleteGame(theGames[0]);
            theGamesDB.addGame(theGames[0]);
            theGamesDB.close();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        final Intent toGamesList = new Intent(this, GamesPlayedActivity.class);
        toGamesList.putExtra("PitcherName", thePitcher.getName());
        toGamesList.putExtra("Pitches", thePitcher.getNumPitches());
        startActivity(toGamesList);
    }

    private String getPercRatio(final int strikes, final int balls) {
        if ((strikes + balls) == 0)
            return "0%";
        final double ratio = (((double)strikes) / ((double)strikes + (double)balls)) * 100;
        return theFormat.format(ratio) + "%";
    }

    private Game getGame() {
        final Bundle theB = getIntent().getExtras();

        final String gameD = theB.getString("GameDate");
        final String pName = theB.getString("PitcherName");
        final int pNum = theB.getInt("PitcherPitches");
        final int nStrikes = theB.getInt("NumStrikes");
        final int nBalls = theB.getInt("NumBalls");
        final long gameID = theB.getLong("GameID");

        final Calendar theGameCal = fromString(gameD);
        return new Game(theGameCal, new Pitcher(pName, pNum), nStrikes, nBalls, gameID);
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
    private String fromCalendar(Calendar theCal) {
        return theCal.get(Calendar.MONTH) + "/" +
                theCal.get(Calendar.DAY_OF_MONTH) + "/" +
                theCal.get(Calendar.YEAR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_games, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(String message) {
        Log.e("com.ryan.pitchcounter", message);
    }
}
