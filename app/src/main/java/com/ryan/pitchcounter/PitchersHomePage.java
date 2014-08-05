package com.ryan.pitchcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class PitchersHomePage extends Activity {

    private static final DecimalFormat theFormat = new DecimalFormat("0.000");

    private final Context theC = this;

    private LinearLayout thePitcherLayout;
    private Pitcher[] thePitchers;
    private SQLitePitcherDatabase thePitcherDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitchers_home_page);

        thePitcherLayout = (LinearLayout) findViewById(R.id.pitchersLinearLayout);

        thePitcherDatabase = new SQLitePitcherDatabase(theC);
        thePitchers = removeDuplicates(thePitcherDatabase.getAllPitchersStrike(theC));
        thePitcherDatabase.close();

        updateActionBar();
        addPitchersToLL();
    }

    private void updateActionBar() {
        if(thePitchers.length == 1)
            getActionBar().setTitle("1 Pitcher");
        else
            getActionBar().setTitle(thePitchers.length + " Pitchers");
    }

    private void addPitchersToLL()  {
        for(int i = 0; i < thePitchers.length; i++)
            thePitcherLayout.addView(getLL(thePitchers[i], i));
    }

    private LinearLayout getLL(final Pitcher thePitcher, final int num) {
        final LinearLayout aLayout = new LinearLayout(theC);

        aLayout.setOrientation(LinearLayout.HORIZONTAL);
        aLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        aLayout.setWeightSum(1);
        aLayout.addView(getPitcherLL(thePitcher, true, num));
        aLayout.addView(getPitcherLL(thePitcher, false, num));

        return aLayout;
    }

    private TextView getPitcherLL(final Pitcher thePitcher, final boolean showName, final int num) {
        if(thePitcher == null || thePitcher.getName() == null)
            return new TextView(theC);

        final TextView theView = new TextView(theC);
        theView.setPadding(0, 0, 0, 20);
        theView.setTextSize(20);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);

        theView.setLayoutParams(params);

        if(showName) {
            theView.setText(thePitcher.getName());
            theView.setGravity(Gravity.LEFT);
        }
        else {
            theView.setText("Ratio: " + theFormat.format(thePitcher.getRatio()) + "%");
            theView.setGravity(Gravity.RIGHT);
        }

        if(num % 2 == 0)
            theView.setTextColor(Color.parseColor("#ff33b5e5"));
        else
            theView.setTextColor(Color.BLACK);

        theView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toGamesList = new Intent(PitchersHomePage.this, GamesPlayedActivity.class);
                toGamesList.putExtra("PitcherName", thePitcher.getName());
                toGamesList.putExtra("Pitches", thePitcher.getNumPitches());
                startActivity(toGamesList);
            }
        });
        theView.setLongClickable(true);
        theView.setClickable(true);
        theView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder deletePitcher = new AlertDialog.Builder(theC);
                deletePitcher.setTitle("Delete Pitcher");
                deletePitcher.setMessage("Are you sure you want to delete " + thePitcher.getName());

                deletePitcher.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Pitcher tempPitchers[] = new Pitcher[thePitchers.length-1];
                        int counter = 0;
                        for(int i = 0; i < thePitchers.length; i++) {
                            if(!thePitcher.getName().equals(thePitchers[i].getName())) {
                                tempPitchers[counter] = thePitchers[i];
                                counter++;
                            }
                        }
                        thePitchers = tempPitchers;

                        updateActionBar();

                        thePitcherLayout.removeAllViews();
                        addPitchersToLL();
                        updateActionBar();

                        thePitcherDatabase = new SQLitePitcherDatabase(theC);
                        thePitcherDatabase.deletePitcher(thePitcher);
                        thePitcherDatabase.close();

                        SQLiteGamesDatabase theGDB = new SQLiteGamesDatabase(theC);
                        theGDB.deleteGames(thePitcher);
                        theGDB.close();
                    }
                });

                deletePitcher.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                deletePitcher.show();
                return true;
            }
        });
        return theView;
    }


    public Pitcher[] removeDuplicates(final List<Pitcher> thePitchers) {
        final SortedSet<Pitcher> theSorted = new TreeSet<Pitcher>(new Comparator<Pitcher>()
        {
            @Override
            public int compare(Pitcher arg0, Pitcher arg1)
            {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        theSorted.addAll(thePitchers);
        return theSorted.toArray(new Pitcher[theSorted.size()]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater theInflater = getMenuInflater();
        theInflater.inflate(R.menu.pitcher_home_page_activitybar, menu);
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

        else if(id == R.id.add_pitcher) {
            final AlertDialog.Builder getPitcherName = new AlertDialog.Builder(theC);
            final EditText forPN = new EditText(theC);

            getPitcherName.setTitle("Add Pitcher");
            getPitcherName.setMessage("Enter Pitcher's Name");
            getPitcherName.setView(forPN);

            getPitcherName.setPositiveButton("Add Pitcher", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Pitcher thePitcher = new Pitcher(forPN.getText().toString());

                    thePitcherLayout.addView(getLL(thePitcher, thePitchers.length + 1), thePitchers.length);

                    thePitcherDatabase = new SQLitePitcherDatabase(theC);
                    thePitcherDatabase.addPitcher(thePitcher);
                    thePitcherDatabase.close();
                }
            });

            getPitcherName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            getPitcherName.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void log(String message) {
        Log.e("com.ryan.pitchcounter", message);
    }
}
