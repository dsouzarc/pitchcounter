package com.ryan.pitchcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import android.graphics.Color;
public class GamesPlayedActivity extends Activity {

    private LinearLayout theGamesLayout;
    private final Context theC = this;
    private Game[] theGames;
    private SQLiteGamesDatabase theGamesDB;

    private Pitcher thePitcher;
    private final int DATE_DIALOG_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_played);

        thePitcher = new Pitcher(getIntent().getExtras().getString("PitcherName"),
                                 getIntent().getExtras().getInt("Pitches"));
        
        theGamesDB = new SQLiteGamesDatabase(theC);
        theGames = removeDuplicates(theGamesDB.getGamesForPitcher(thePitcher));
        theGamesDB.close();
        
        theGamesLayout = (LinearLayout) findViewById(R.id.gamesLinearLayout);
        
        updateActionBarTitle();

        for(int i = 0; i < theGames.length; i++)
            theGamesLayout.addView(getGameLL(theGames[i], i));
    }

    private class EditGameListener implements View.OnClickListener {
        private final Game theGame;

        public EditGameListener(final Game theGame) {
            this.theGame = theGame;
        }

        @Override
        public void onClick(View v) {
            Intent toEditGame = new Intent(GamesPlayedActivity.this, EditGamesActivity.class);
            toEditGame.putExtra("GameDate", fromCalendar(theGame.getDateCalendar()));
            toEditGame.putExtra("PitcherName", theGame.getThePitcher().getName());
            toEditGame.putExtra("PitcherPitches", theGame.getThePitcher().getNumPitches());
            toEditGame.putExtra("NumStrikes", theGame.getNumStrike());
            toEditGame.putExtra("NumBalls", theGame.getNumBall());
            startActivity(toEditGame);
        }
    }

    private class DeleteGameListener implements View.OnLongClickListener {
        private final Game theGame;

        public DeleteGameListener(final Game theGame) {
            this.theGame = theGame;
        }

        @Override
        public boolean onLongClick(View theView) {
            final AlertDialog.Builder deleteGame = new AlertDialog.Builder(theC);
            deleteGame.setTitle("Delete Game");
            deleteGame.setMessage("Are you sure you want to delete the game on " + theGame.getDate());

            deleteGame.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<Game> theGamesAL = new ArrayList<Game>(Arrays.asList(theGames));
                    for(int i = 0; i < theGamesAL.size(); i++) {
                        if(theGamesAL.get(i).getDate().equals(theGame.getDate())) {
                            theGamesAL.remove(i);
                            i = theGamesAL.size() * 2;
                        }
                    }

                    theGames = theGamesAL.toArray(theGames);

                    theGamesLayout.removeAllViews();

                    for(int i = 0; i < theGames.length; i++)
                        theGamesLayout.addView(getGameLL(theGames[i], i));

                    updateActionBarTitle();

                    theGamesDB = new SQLiteGamesDatabase(theC);
                    theGamesDB.deleteGame(theGame);
                    theGamesDB.close();
                }
            });

            deleteGame.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            deleteGame.show();
            return true;
        }
    }

    private LinearLayout getGameLL(final Game theGame, final int num)
    {
        final LinearLayout theLayout = new LinearLayout(theC);
        theLayout.setOrientation(LinearLayout.VERTICAL);

        if(theGame == null || theGame.getDate() == null)
            return new LinearLayout(theC);

        final TextView theView = new TextView(theC);
        theView.setPadding(0, 30, 0, 0);
        theView.setTextSize(20);
        theView.setText(getCalendarString(theGame.getDateCalendar()));
        theView.setOnClickListener(new EditGameListener(theGame));
        theView.setLongClickable(true);
        theView.setClickable(true);
        theView.setOnLongClickListener(new DeleteGameListener(theGame));
        theView.setGravity(Gravity.LEFT);

        if(num % 2 == 0)
            theView.setTextColor(Color.parseColor("#ff33b5e5"));
        else
            theView.setTextColor(Color.BLACK);

        TextView pitches = new TextView(theC);
        pitches.setText(getPitchesText(theGame.getTotalPitches()));
        pitches.setTextSize(20);
        pitches.setGravity(Gravity.RIGHT);
        pitches.setTextColor(android.graphics.Color.LTGRAY);

        theLayout.addView(theView);
        theLayout.addView(pitches);

        return theLayout;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, PitchersHomePage.class));
    }

    public Game[] removeDuplicates(List<Game> theGames)
    {
        SortedSet<Game> theSorted = new TreeSet<Game>(new Comparator<Game>()
        {
            @Override
            public int compare(Game g1, Game g2)
            {
                return g1.getDateCalendar().compareTo(g2.getDateCalendar());
            }
        });
        theSorted.addAll(theGames);
        return theSorted.toArray(new Game[theSorted.size()]);
    }

    /** Create a new dialog for date picker */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id)
        {
            case DATE_DIALOG_ID:
                final Calendar today = new GregorianCalendar();
                return new DatePickerDialog(this,
                        pDateSetListener,
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }

    /** Callback received when the user "picks" a date in the dialog */
    private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            Calendar chosenDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            Calendar today = new GregorianCalendar();

            long numDays = daysBetween(chosenDate, today);

            if(numDays == 0)
                setTitle("Today, " + getCalendarString(today));
            else if(numDays > 0)
                setTitle(numDays + " days ago, " + getCalendarString(chosenDate));
            else if(numDays < 0)
                setTitle(numDays + " from now, " + getCalendarString(chosenDate));

            final Game newGame = new Game(chosenDate, thePitcher);

            theGamesLayout.addView(getGameLL(newGame, theGames.length), 0);
            theGamesDB = new SQLiteGamesDatabase(theC);
            theGamesDB.addGame(newGame);
            theGamesDB.close();

            updateActionBarTitle();
        }
    };

    private String getPitchesText(final int thePitches) {
        if(thePitches == 1)
            return thePitches + " pitch";
        return thePitches + " pitches";
    }


    private void updateActionBarTitle()
    {
        String theStr = "";
        if(theGames.length == 1)
            theStr = "1 Game";
        else
            theStr = theGames.length + " Games";

        getActionBar().setTitle(theStr + " for " + thePitcher.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater theInflater = getMenuInflater();
        theInflater.inflate(R.menu.list_of_games_activitybar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        else if(id == R.id.add_game)
        {
            showDialog(DATE_DIALOG_ID);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(final String message)
    {
        Toast theToast = Toast.makeText(theC, message, Toast.LENGTH_LONG);
        theToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        theToast.show();
    }

    /**Returns something like Thursday, July 10th, 2014 */
    public static String getCalendarString(Calendar theCal)
    {
        return days[theCal.get(Calendar.DAY_OF_WEEK)].substring(0, 3) +  ", " +
                months[theCal.get(Calendar.MONTH)] + " " + theCal.get(Calendar.DAY_OF_MONTH) +
                ", " + theCal.get(Calendar.YEAR);
    }

    /** Using Calendar - THE CORRECT (& Faster) WAY**/
    public static long daysBetween(final Calendar startDate, final Calendar endDate)
    {
        final long diff = startDate.getTimeInMillis() - endDate.getTimeInMillis();
        return diff / (24 * 60 * 60 * 1000);
    }

    private static final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] months = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November", "December"};

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

    private void log(String message)
    {
        Log.e("com.ryan.pitchcounter", message);
    }
}
