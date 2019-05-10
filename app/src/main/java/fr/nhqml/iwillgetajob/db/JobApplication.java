package fr.nhqml.iwillgetajob.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Comparator;

import fr.nhqml.iwillgetajob.MainActivity;

/**
 * Represents a Job Application.
 * This class also provides multiple functions to get / set values.
 */
@Entity
public class JobApplication
{
    //region Subclasses

    /**
     * Represents a Contact.
     * A Contact has 3 attributes: name, email and phone (these attributes can be empty).
     */
    public static final class Contact
    {
        String name;
        String email;
        String phone;

        Contact(String name, String email, String phone)
        {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        /**
         * @return the String representation of the object
         */
        @NonNull
        @Override
        public String toString()
        {
            return name + "\n" + email + "\n" + phone + "\n";
        }
    }
    //endregion

    //region Attributes
    /**
     * Attribute used by the database as Primary Key
     */
    @PrimaryKey
    private long id;

    /**
     * The step of the the JobApplication.
     * (0) = I will apply
     * (1) = I have applied
     * (2) = I have called again
     * (3) = I have an interview
     */
    private int step;
    /**
     * The title of the job you apply.
     */
    private String jobTitle;
    /**
     * The company name.
     */
    private String companyName;
    /**
     * An array of dates corresponding to the dates you (will apply / applied / called again / have a interview).
     * Date of each step is stored in dates[step].
     */
    private final ZonedDateTime[] dates;
    /**
     * The {@link Contact} object.
     * Contains all the information about your contact in the company.
     */
    private final Contact contact;
    /**
     * The reference of the job (e.g. a link to the website on which you found the job).
     */
    private String jobRef;
    /**
     * Any notes about this JobApplication.
     */
    private String notes;
    //endregion

    //region Constructors

    /**
     * Default constructor
     * @param step the step of the application when you create it
     * @param date the date corresponding to the day you (will apply / applied / called again / have a interview)
     */
    public JobApplication(int step, ZonedDateTime date)
    {
        // Id is the epoch time of the object creation
        id = Calendar.getInstance().getTimeInMillis();
        this.step = step;
        jobTitle = "";
        companyName = "";
        dates = new ZonedDateTime[4];
        dates[step] = date;
        contact = new Contact("", "", "");
        jobRef = "";
        notes = "";
    }

    /**
     * Constructor used when retrieving object from database (only used in {@link JobApplicationDAO_Impl}.
     * User is encouraged to use {@link JobApplication#JobApplication(int, ZonedDateTime)} instead.
     */
    public JobApplication(int step, String jobTitle, String companyName, ZonedDateTime[] dates, Contact contact, String jobRef, String notes)
    {
        this.step = step;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.dates = dates;
        this.contact = contact;
        this.jobRef = jobRef;
        this.notes = notes;
    }
    //endregion

    //region Methods

    /**
     * @return True if the jobTitle has not been set yet, False otherwise
     */
    public boolean isNew()
    {
        return this.jobTitle.equals("");
    }

    /**
     * Set parameters of the JobApplication
     * @param editText_values an array containing the values of {@link JobApplication#jobTitle},
     * {@link JobApplication#companyName}, {@link JobApplication.Contact#name},
     * {@link JobApplication.Contact#email}, {@link JobApplication.Contact#phone},
     * {@link JobApplication#jobRef} and {@link JobApplication#notes} in this specific order
     * @param date the date
     */
    public void editApplication(String[] editText_values, ZonedDateTime date)
    {
        jobTitle = editText_values[0];
        companyName = editText_values[1];
        contact.name = editText_values[2];
        contact.email = editText_values[3];
        contact.phone = editText_values[4];
        jobRef = editText_values[5];
        notes = editText_values[6];
        dates[step] = date;
    }

    /**
     * Change the step of the JobApplication, move it to the right ArrayList of {@link MainActivity#jobApplications}, update the
     * database entry and refresh the RecyclerView
     * @param step the target step
     */
    public void changeStep(final int step)
    {
        if (this.step != step)
        {
            // Copy the date
            this.dates[step] = this.dates[this.step];

            // Move to other list
            MainActivity.jobApplications.get(this.step).remove(this);
            this.step = step;
            MainActivity.jobApplications.get(this.step).add(this);

            // Update in DB
            MainActivity.db.getJobApplicationDAO().update(this);

            // Refresh RecyclerView
            MainActivity.adapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "ID: " + id + "\nStep: " + step + "\nTitle: " + jobTitle + "\nCompany: " + companyName + "\nDate: " +
                dates[step] + "\nContact: " + contact + "\nRef: " + jobRef + "\nNotes: " + notes;
    }
    //endregion

    //region Comparators
    /**
     * Compare {@link JobApplication#jobTitle} attribute.
     * This is case insensitive.
     * Alphabetical order.
    */
    public static final Comparator<JobApplication> JAComparatorTitleAZ = new Comparator<JobApplication>() {
        @Override
        public int compare(JobApplication j1, JobApplication j2) {
            return j1.getJobTitle().toLowerCase().compareTo(j2.getJobTitle().toLowerCase());
        }
    };

    /**
     * Compare {@link JobApplication#jobTitle} attribute.
     * This is case insensitive.
     * Reverse alphabetical order.
     */
    public static final Comparator<JobApplication> JAComparatorTitleZA = new Comparator<JobApplication>() {
        @Override
        public int compare(JobApplication j1, JobApplication j2) {
            return j2.getJobTitle().toLowerCase().compareTo(j1.getJobTitle().toLowerCase());
        }
    };

    /**
     * Compare {@link JobApplication#dates}[{@link JobApplication#step}] attribute.
     * Chronological order (12/01/1990 < 18/06/2001).
     * If dates are equals, comparison is done with {@link JobApplication#JAComparatorTitleAZ}
     */
    public static final Comparator<JobApplication> JAComparatorTitleDate = new Comparator<JobApplication>() {
        @Override
        public int compare(JobApplication j1, JobApplication j2) {
            int comp = j1.getDates()[j1.getStep()].toLocalDate().compareTo(j2.getDates()[j2.getStep()].toLocalDate());
            return comp == 0 ? JAComparatorTitleAZ.compare(j1, j2) : comp;
        }
    };
    //endregion

    //region Getters / Setters
    public long getId() {
        return id;
    }

    public int getStep() {
        return step;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ZonedDateTime[] getDates() {
        return dates;
    }

    public Contact getContact() {
        return contact;
    }

    public String getJobRef() {
        return jobRef;
    }

    public String getNotes() {
        return notes;
    }

    public void setId(long id) {
        this.id = id;
    }
    //endregion
}
