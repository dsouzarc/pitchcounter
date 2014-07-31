package com.ryan.pitchcounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private SQLiteGamesDatabase theGamesDB;
    private final Context theC = this;
    private Pitcher thePitcher = null;

    private final Stack<Boolean> isStrikeStack = new Stack<Boolean>();
    private TextView numPitches;
    private TextView numStrikes;
    private TextView numBalls;
    private TextView ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_games);

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

    private void updateData(final Game theG) {
        new UpdateDatabase().execute(theG);
        numPitches.setText(String.valueOf(theG.getTotalPitches()));
        numStrikes.setText(String.valueOf(theG.getNumStrike()));
        numBalls.setText(String.valueOf(theG.getNumBall()));
        ratio.setText(getPercRatio(theG.getNumStrike(), theG.getNumBall()));
    }

    private class UpdateDatabase extends AsyncTask<Game, Void, Void> {
        @Override
        public Void doInBackground(Game... theGames)
        {
            theGamesDB = new SQLiteGamesDatabase(theC);
            theGamesDB.deleteGame(theGames[0]);
            theGamesDB.addGame(theGames[0]);
            theGamesDB.close();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent toGamesList = new Intent(this, GamesPlayedActivity.class);
        toGamesList.putExtra("PitcherName", thePitcher.getName());
        toGamesList.putExtra("Pitches", thePitcher.getNumPitches());
        startActivity(toGamesList);
    }

    private String getPercRatio(int strikes, int balls)
    {
        if ((strikes + balls) == 0)
            return "0%";
        double ratio = (((double)strikes) / ((double)strikes + (double)balls)) * 100;
        return new DecimalFormat("0.000").format(ratio) + "%";
    }

    private Game getGame()
    {
        Bundle theB = getIntent().getExtras();
        String gameD = theB.getString("GameDate");
        String pName = theB.getString("PitcherName");
        int pNum = theB.getInt("PitcherPitches");
        int nStrikes = theB.getInt("NumStrikes");
        int nBalls = theB.getInt("NumBalls");

        Calendar theGameCal = fromString(gameD);

        return new Game(theGameCal, new Pitcher(pName, pNum), nStrikes, nBalls);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.edit_games, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Returns calender from theStr = "07/11/2014"
    private Calendar fromString(String theStr)
    {
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
    private String fromCalendar(Calendar theCal)
    {
        return theCal.get(Calendar.MONTH) + "/" + theCal.get(Calendar.DAY_OF_MONTH) + "/" +
                theCal.get(Calendar.YEAR);
    }

    private void log(String message)
    {
        Log.e("com.ryan.pitchcounter", message);
    }
}
