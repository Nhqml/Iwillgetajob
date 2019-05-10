package fr.nhqml.iwillgetajob;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;

import fr.nhqml.iwillgetajob.db.JobApplication;

/** Activity to display a {@link JobApplication} */
public class ShowEntryActivity extends AppCompatActivity {

    /** The date display format (day/month/year) */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** The displayed {@link JobApplication} */
    private JobApplication jobApplication;


    /** Set the values of the TextViews and hide the empty fields */
    private void displayApplication()
    {
        // Set required fields
        ((TextView) findViewById(R.id.job_title)).setText(jobApplication.getJobTitle());
        ((TextView) findViewById(R.id.company_name)).setText(jobApplication.getCompanyName());
        ((TextView) findViewById(R.id.date)).setText(DATE_FORMAT.format(jobApplication.getDates()[jobApplication.getStep()]));

        // Check if Contact fields are empty, if a field is empty, set its visibility to GONE
        String contact_name = jobApplication.getContact().getName();
        TextView contact_name_view = findViewById(R.id.contact_name);
        if (contact_name.isEmpty()) { contact_name_view.setVisibility(View.GONE); } else { contact_name_view.setText(contact_name); }
        String contact_mail = jobApplication.getContact().getEmail();
        TextView contact_mail_view = findViewById(R.id.contact_mail);
        if (contact_mail.isEmpty()) { contact_mail_view.setVisibility(View.GONE); } else { contact_mail_view.setText(contact_mail); }
        String contact_phone = jobApplication.getContact().getPhone();
        TextView contact_phone_view = findViewById(R.id.contact_phone);
        if (contact_phone.isEmpty()) { contact_phone_view.setVisibility(View.GONE); } else { contact_phone_view.setText(contact_phone); }
        if (contact_name.isEmpty() && contact_mail.isEmpty() && contact_phone.isEmpty())
        {
            findViewById(R.id.title_contact).setVisibility(View.GONE);
        }

        // Same for job ref
        if (jobApplication.getJobRef().isEmpty())
        {
            findViewById(R.id.title_job_ref).setVisibility(View.GONE);
            findViewById(R.id.job_ref).setVisibility(View.GONE);
        }
        else
        {
            ((TextView) findViewById(R.id.job_ref)).setText(jobApplication.getJobRef());
        }

        // Same for notes
        if (jobApplication.getNotes().isEmpty())
        {
            findViewById(R.id.notes).setVisibility(View.GONE);
            findViewById(R.id.title_notes).setVisibility(View.GONE);
        }
        else
        {
            ((TextView) findViewById(R.id.notes)).setText(jobApplication.getNotes());
        }
    }

    /** OnClickListener that handles clicks on the back button (R.id: back_button) */
    private final View.OnClickListener backButtonOnClickListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_entry);

        Intent myIntent = getIntent();

        // Retrieve the JobApplication to display
        int step = myIntent.getIntExtra("step", 0);
        int pos = myIntent.getIntExtra("pos", 0);
        jobApplication = MainActivity.jobApplications.get(step).get(pos);
        ((TextView) findViewById(R.id.text_job_title)).setText(getResources().getTextArray(R.array.description_step_array)[step]);
        displayApplication();

        findViewById(R.id.back_button).setOnClickListener(backButtonOnClickListener);
    }

    @Override
    public void onBackPressed()
    {
        // "Back" must perform the same actions as a click on the cancel button
        backButtonOnClickListener.onClick(findViewById(R.id.back_button));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // "Home" must perform the same actions as a click on the cancel button
            case android.R.id.home:
                backButtonOnClickListener.onClick(findViewById(R.id.back_button));
                break;
        }
        return true;
    }
}
