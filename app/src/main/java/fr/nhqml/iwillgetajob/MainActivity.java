package fr.nhqml.iwillgetajob;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Comparator;

import fr.nhqml.iwillgetajob.db.AppDB;
import fr.nhqml.iwillgetajob.db.JobApplication;

/** Main Activity of the app */
public class MainActivity extends AppCompatActivity {

    /** Shared Preferences name */
    private static final String SHARED_PREFERENCES = "shared_preferences";

    /** View that display the title of the activity */
    private TextView titleMessage;
    /** Step of the {@link JobApplication}s that are currently displayed */
    private int step = 0;

    /** {@link RecyclerView} that display the {@link JobApplication}s */
    private RecyclerView recyclerView;

    /** The {@link AppDB} ({@link android.arch.persistence.room.RoomDatabase}) of the app */
    static public AppDB db;
    /** An ArrayList that contains four ArrayLists containing the {@link JobApplication}s of each step */
    public static final ArrayList<ArrayList<JobApplication>> jobApplications = new ArrayList<>(4);
    /** The sort type choosen by user
     * @see SortType */
    private int sortType = SortType.ATOZ;
    /** {@link android.widget.Adapter} of {@link MainActivity#recyclerView} */
    public static JobApplicationAdapter adapter;

    /** OnNavigationItemSelectedListener that handle clicks on the bottom navigation view (R.menu: navigation) */
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_step1:
                    // Change the step
                    step = 0;
                    // Change the title with respect to the chosen step
                    titleMessage.setText(R.string.description_step0);
                    // Change the list attached to the adapter (to change the displayed {@link JobApplication}s)
                    adapter.setJobApplications(jobApplications.get(0));
                    // Sort the displayed {@link JobApplication}s
                    sortApplications();
                    return true;
                case R.id.navigation_step2:
                    step = 1;
                    titleMessage.setText(R.string.description_step1);
                    adapter.setJobApplications(jobApplications.get(1));
                    sortApplications();
                    return true;
                case R.id.navigation_step3:
                    step = 2;
                    titleMessage.setText(R.string.description_step2);
                    adapter.setJobApplications(jobApplications.get(2));
                    sortApplications();
                    return true;
                case R.id.navigation_step4:
                    step = 3;
                    titleMessage.setText(R.string.description_step3);
                    adapter.setJobApplications(jobApplications.get(3));
                    sortApplications();
                    return true;
            }
            return false;
        }
    };

    /** OnClickListener that handle clicks on the floating add button (R.id: floatingAddButton) */
    private final View.OnClickListener addButtonOnClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Create an intent to start activity_edit_entryy.xml
            Intent myIntent = new Intent(MainActivity.this, EditEntryActivity.class);
            // Pass the step to the activity
            myIntent.putExtra("step", step);
            startActivity(myIntent);
        }
    };

    /**
     * Create and show a {@link PopupMenu} when an item of {@link MainActivity#recyclerView} is long clicked.
     * The menu allows the user to move, edit or delete a {@link JobApplication}.
     * @param view the view that has been long clicked (should be an item of {@link MainActivity#recyclerView}
     * @param pos the position of the clicked item
     */
    private void showPopupMenu(final View view, final int pos)
    {
        // Create the menu and set OnMenuItemClickListener
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialogTheme);
                switch (menuItem.getItemId())
                {
                    case R.id.popup_move:
                        // Create the array of String values to display to the user (to chose "where" to move the JobApplication
                        String[] titleStepArray = getResources().getStringArray(R.array.title_step_array);
                        String[] titleStepArrayTruncated = new String[titleStepArray.length - 1];
                        int j = 0;
                        for (int i = 0; i < titleStepArray.length; i++)
                        {
                            // Does not add the title of current step (it makes no sens to move to the current step)
                            if (i != step)
                            {
                                titleStepArrayTruncated[j] = titleStepArray[i];
                                j++;
                            }
                        }
                        dialogBuilder.setTitle(R.string.popup_menu_move)
                                .setItems(titleStepArrayTruncated, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        /* Add 1 if step clicked is "higher" than the current one
                                          (to prevent shift due to current step not displayed in list) */
                                        i = i < step ? i : i + 1;
                                        // Retrieve the JobApplication and call changeStep with the selected step
                                        jobApplications.get(step).get(pos).changeStep(i);
                                    }
                                });
                        dialogBuilder.create().show();
                        break;
                    case R.id.popup_edit:
                        // Create an intent to start activity_edit_entryy.xml
                        Intent myIntent = new Intent(MainActivity.this, EditEntryActivity.class);
                        // Pass the step and the position of the JobApplication to edit
                        myIntent.putExtra("step", step);
                        myIntent.putExtra("pos", pos);
                        startActivity(myIntent);
                        break;
                    case R.id.popup_delete:
                        dialogBuilder.setTitle(R.string.delete_dialog_title)
                                .setMessage(R.string.delete_dialog_message)
                                .setPositiveButton(R.string.delete_dialog_positiveB, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Remove item from list and update RecyclerView
                                        JobApplication removedJobApplication = jobApplications.get(step).remove(pos);
                                        adapter.notifyDataSetChanged();
                                        // Delete it from the database
                                        db.getJobApplicationDAO().delete(removedJobApplication);
                                    }
                                })
                                .setNegativeButton(R.string.delete_dialog_negativeB, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Nothing, just close the Dialog
                                    }
                                });
                        dialogBuilder.create().show();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        // Inflate and display the menu
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_actions, popupMenu.getMenu());
        popupMenu.show();
    }

    /**
     * Create and show a {@link PopupMenu} when the sort button of the action bar (R.id: action_bar_sort) is clicked.
     * The menu allows the user to choose how the {@link JobApplication}s displayed must be sorted.
     * @param view the action bar
     */
    private void showSortMenu(final View view)
    {
        PopupMenu sortMenu = new PopupMenu(view.getContext(), view);
        sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // Set the sort type according to what user choose
                switch (menuItem.getItemId())
                {
                    case R.id.popup_sortAZ:
                        sortType = SortType.ATOZ;
                        break;
                    case R.id.popup_sortZA:
                        sortType = SortType.ZTOA;
                        break;
                    case R.id.popup_sortDate:
                        sortType = SortType.DATE;
                        break;
                    default:
                        return false;
                }
                // Sort JobApplications
                sortApplications();
                // Edit the SharedPreference to remember the user choice
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit();
                editor.putInt("sortType", sortType);
                editor.apply();
                return true;
            }
        });
        // Inflate and display the menu
        MenuInflater inflater = sortMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_sort, sortMenu.getMenu());
        sortMenu.show();
    }

    /** Pseudo enum, each value corresponds to a sort order */
    public static class SortType
    {
        // Alphabetical order
        final static int ATOZ = 0;
        // Reverse alphabetical order
        final static int ZTOA = 1;
        // Chronological order
        final static int DATE = 2;
    }

    /** Sort <b>displayed</b> {@link JobApplication}s according to the value of {@link #sortType} */
    private void sortApplications()
    {
        Comparator<JobApplication> comparator;
        switch (sortType)
        {
            case SortType.ATOZ:
                comparator = JobApplication.JAComparatorTitleAZ;
                break;
            case SortType.ZTOA:
                comparator = JobApplication.JAComparatorTitleZA;
                break;
            case SortType.DATE:
                comparator = JobApplication.JAComparatorTitleDate;
                break;
            default:
                // Default is ATOZ sort
                comparator = JobApplication.JAComparatorTitleAZ;
                break;
        }
        // Sort JobApplications and update displayed values
        jobApplications.get(step).sort(comparator);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create the database
        db = Room.databaseBuilder(this, AppDB.class, "db").allowMainThreadQueries().build();

        // Retrieve data from the database
        for (int i = 0; i < 4; i++)
        {
            ArrayList<JobApplication> applications = (ArrayList<JobApplication>) db.getJobApplicationDAO().getAllApplicationsByStep(i);
            jobApplications.add(applications);
        }

        // Retrieve SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        sortType = sharedPreferences.getInt("sortType", SortType.ATOZ);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewList);

        // Create the Adapter
        adapter = new JobApplicationAdapter(jobApplications.get(0));
        // Set its onLongClick Listener (showPopupMenu)
        adapter.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int pos = recyclerView.indexOfChild(view);
                showPopupMenu(view, pos);
                return true;
            }
        });
        // Set its onClick Listener (start activity_show_entry)
        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the index of the clicked JobApplication
                int pos = recyclerView.indexOfChild(view);
                // Create an intent to start activity_edit_entry.xmll
                Intent myIntent = new Intent(MainActivity.this, ShowEntryActivity.class);
                // Pass the step and the position of the JobApplication to show
                myIntent.putExtra("step", step);
                myIntent.putExtra("pos", pos);
                startActivity(myIntent);
            }
        });

        // Create and set the LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        // Set the ItemAnimator and the Adapter
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        titleMessage = findViewById(R.id.title);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Find the Action Bar and set it
        Toolbar actionbar = findViewById(R.id.main_actionbar);
        setSupportActionBar(actionbar);

        FloatingActionButton floatingAddButton = findViewById(R.id.floatingAddButton);
        floatingAddButton.setOnClickListener(addButtonOnClickListener);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Sort the applications when resuming the activity (e.g. after insertion of a new element)
        sortApplications();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the Action Bar
        getMenuInflater().inflate(R.menu.main_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // User clicked on sort button
            case R.id.action_bar_sort:
                showSortMenu(findViewById(R.id.action_bar_sort));
                break;
            default:
                return false;
        }
        return true;
    }
}
