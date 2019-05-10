package fr.nhqml.iwillgetajob;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import fr.nhqml.iwillgetajob.db.JobApplication;

/** Activity to add a new database entry (create a new {@link JobApplication}) */
public class EditEntryActivity extends AppCompatActivity {

    /** The created/edited {@link JobApplication} */
    private JobApplication jobApplication;

    /** The date selected by the user (current date by default) */
    private ZonedDateTime selectedDate;
    /** The EditText that displays the date */
    private EditText editTextDate;
    /** The date display format (day/month/year) */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** List of IDs of EditText views of the activity (except ID of {@link #editTextDate}) */
    private static final int[] EDIT_TEXT_ID = {R.id.entry_job_title, R.id.entry_company_name,
        R.id.entry_contact_name, R.id.entry_contact_mail, R.id.entry_contact_phone,
        R.id.entry_job_ref, R.id.entry_notes};
    /** List of String values of the EditText views */
    private final String[] editText_values = new String[EDIT_TEXT_ID.length];

    /** OnClickListener that handles clicks on the confirm button (R.id: confirm_button) */
    private final View.OnClickListener confirmButtonOnClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean noEmptyField = true;
            // Get values of all EditText views
            EditText curEditText;
            for (int i = 0; i < EDIT_TEXT_ID.length; i++)
            {
                curEditText = findViewById(EDIT_TEXT_ID[i]);
                editText_values[i] = curEditText.getText().toString();
            }

            // Check if one of the required fields is empty (job title and company name are required fields)
            for (int i = 0; i < 2; i++)
            {
                // If one of the fields is empty, display an error to the user
                if (editText_values[i].isEmpty())
                {
                    curEditText = findViewById(EDIT_TEXT_ID[i]);
                    curEditText.setError(getString(R.string.message_warning_fill));
                    curEditText.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    noEmptyField &= false;
                }
            }

            // Save the new JobApplication
            if (noEmptyField)
            {
                // Set the JobApplication values
                jobApplication.editApplication(editText_values, selectedDate);
                // If the JobApplication existed before (and is modified) we want to remove it from database
                try
                {
                    MainActivity.db.getJobApplicationDAO().delete(jobApplication);
                }
                // And then add the new/modified one
                finally
                {
                    MainActivity.db.getJobApplicationDAO().insert(jobApplication);
                }
                // Refresh the RecyclerView
                MainActivity.adapter.notifyDataSetChanged();
                finish();
            }
        }
    };

    /** OnClickListener that handles clicks on cancel button (R.id: cancel_button) */
    private final View.OnClickListener cancelButtonOnClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (jobApplication.isNew())
            {
                // Remove the empty JobApplication created from the list to avoid an empty field in RecyclerView
                MainActivity.jobApplications.get(jobApplication.getStep()).remove(jobApplication);
            }
            finish();
        }
    };

    /** OnDateSetListener that handles the selection of a date through a DatePickerDialog */
    private final DatePickerDialog.OnDateSetListener datePickerOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            // Need to add 1 to month because DatePickerDialog start counting months at 0
            setDate(year, month + 1, day);
        }
    };

    /** OnClickListener that handles clicks on {@link #editTextDate} */
    private final View.OnClickListener editTextDateOnClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Need to subtract 1 to month because DatePickerDialog start counting months at 0
            new DatePickerDialog(EditEntryActivity.this, datePickerOnDateSetListener, selectedDate.getYear(),
                    selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth()).show();
        }
    };

    /** Set {@link #selectedDate} and update the displayed date */
    private void setDate(int year, int month, int day)
    {
        // Set the new value of date
        selectedDate = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        // Set 'entry_date' text
        editTextDate.setText(DATE_FORMAT.format(selectedDate));
    }

    /** Set the text of the EditText views */
    private void setEditTextViews()
    {
        ((EditText) findViewById(R.id.entry_job_title)).setText(jobApplication.getJobTitle());
        ((EditText) findViewById(R.id.entry_company_name)).setText(jobApplication.getCompanyName());
        editTextDate.setText(DATE_FORMAT.format(jobApplication.getDates()[jobApplication.getStep()]));
        ((EditText) findViewById(R.id.entry_contact_name)).setText(jobApplication.getContact().getName());
        ((EditText) findViewById(R.id.entry_contact_mail)).setText(jobApplication.getContact().getEmail());
        ((EditText) findViewById(R.id.entry_contact_phone)).setText(jobApplication.getContact().getPhone());
        ((EditText) findViewById(R.id.entry_job_ref)).setText(jobApplication.getJobRef());
        ((EditText) findViewById(R.id.entry_notes)).setText(jobApplication.getNotes());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        Intent myIntent = getIntent();

        editTextDate = findViewById(R.id.entry_date);

        // Get Date of day
        selectedDate = ZonedDateTime.now();
        // Retrieve data passed through Intent
        int step = myIntent.getIntExtra("step", 0);
        int pos = myIntent.getIntExtra("pos", -1);
        // If pos is positive it means that user want to edit an existing JobApplication
        if (pos > -1)
        {
            // No JobApplication is created, retrieve the existing one
            jobApplication = MainActivity.jobApplications.get(step).get(pos);
        }
        else
        {
            // A new JobApplication is created and added to the list
            jobApplication = new JobApplication(step, selectedDate);
            MainActivity.jobApplications.get(step).add(jobApplication);
        }
        ((TextView) findViewById(R.id.text_job_title)).setText(getResources().getTextArray(R.array.description_step_array)[step]);
        setEditTextViews();

        // Add buttons onClick listeners
        findViewById(R.id.cancel_button).setOnClickListener(cancelButtonOnClickListener);
        findViewById(R.id.confirm_button).setOnClickListener(confirmButtonOnClickListener);
        // Add EditText date onClick listener
        editTextDate.setOnClickListener(editTextDateOnClickListener);
    }

    @Override
    public void onBackPressed() {
        // "Back" must perform the same actions as a click on the cancel button
        cancelButtonOnClickListener.onClick(findViewById(R.id.cancel_button));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // "Home" must perform the same actions as a click on the cancel button
            case android.R.id.home:
                cancelButtonOnClickListener.onClick(findViewById(R.id.cancel_button));
                break;
        }
        return true;
    }
}
