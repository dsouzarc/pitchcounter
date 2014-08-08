package com.ryan.pitchcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import android.content.DialogInterface.OnDismissListener;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class PitchersHomePage extends Activity {

    private static final DecimalFormat theFormat = new DecimalFormat("0.000");

    private final Context theC = this;
    private final List<Pitcher> thePitchers = new ArrayList<Pitcher>();

    private LinearLayout thePitcherLayout;
    private SQLitePitcherDatabase thePitcherDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitchers_home_page);

        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        thePitcherLayout = (LinearLayout) findViewById(R.id.pitchersLinearLayout);

        thePitcherDatabase = new SQLitePitcherDatabase(theC);
        thePitchers.addAll(removeDuplicates(thePitcherDatabase.getAllPitchersStrike(theC)));
        thePitcherDatabase.close();

        updateActionBar();
        addPitchersToLL();
    }

    private void updateActionBar() {
        if(thePitchers.size() == 1)
            getActionBar().setTitle("1 Pitcher");
        else
            getActionBar().setTitle(thePitchers.size() + " Pitchers");
    }

    private void addPitchersToLL()  {
        thePitcherLayout.removeAllViews();
        for(int i = 0; i < thePitchers.size(); i++)
            thePitcherLayout.addView(getLL(thePitchers.get(i), i));
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
            final String theRatio = theFormat.format(thePitcher.getRatio());
            if(theRatio.contains("NaN")) {
                theView.setText("Ratio: 0.00%");
            }
            else {
                theView.setText("Ratio: " + theRatio + "%");
            }
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
                        int counter = 0;
                        for(int i = 0; i < thePitchers.size(); i++) {
                            if(thePitcher.getName().equals(thePitchers.get(i).getName())) {
                                thePitchers.remove(i);
                                counter--;
                            }
                        }

                        addPitchersToLL();
                        updateActionBar();

                        thePitcherDatabase = new SQLitePitcherDatabase(theC);
                        thePitcherDatabase.deletePitcher(thePitcher);
                        thePitcherDatabase.close();

                        final SQLiteGamesDatabase theGDB = new SQLiteGamesDatabase(theC);
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


    public List<Pitcher> removeDuplicates(final List<Pitcher> thePitchers) {
        final SortedSet<Pitcher> theSorted = new TreeSet<Pitcher>(new Comparator<Pitcher>() {
            @Override
            public int compare(Pitcher arg0, Pitcher arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        theSorted.addAll(thePitchers);
        return new ArrayList<Pitcher>(theSorted);
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

            final boolean[] isDuplicate = {false};

            getPitcherName.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!isDuplicate[0]) {
                        // you need this flag in order to close the dialog
                        // when there is no issue
                        dialog.dismiss();
                    }
                }
            });
            getPitcherName.setPositiveButton("Add Pitcher", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Pitcher thePitcher = new Pitcher(forPN.getText().toString());

                    for (int i = 0; i < thePitchers.size(); i++) {
                        if (thePitcher.getName().equals(thePitchers.get(i).getName())) {
                            isDuplicate[0] = true;
                            getPitcherName.setMessage("Please choose a different name");
                            makeToast("Please choose a different name");
                        }
                    }

                    if (!isDuplicate[0]) {
                        thePitchers.add(0, thePitcher);
                        addPitchersToLL();
                        updateActionBar();

                        thePitcherDatabase = new SQLitePitcherDatabase(theC);
                        thePitcherDatabase.addPitcher(thePitcher);
                        thePitcherDatabase.close();
                    }
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

    private void makeToast(final String message) {
        Toast theT = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        theT.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.TOP, 0, 100);
        theT.show();
    }

    private void log(String message) {
        Log.e("com.ryan.pitchcounter", message);
    }
}
