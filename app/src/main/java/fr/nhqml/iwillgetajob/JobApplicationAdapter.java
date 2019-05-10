package fr.nhqml.iwillgetajob;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.util.List;

import fr.nhqml.iwillgetajob.db.JobApplication;

/**
 * Implementation of the {@link android.support.v7.widget.RecyclerView.Adapter}.
 * @see MainActivity#recyclerView
 */
public class JobApplicationAdapter extends RecyclerView.Adapter<JobApplicationAdapter.LineHolder> {

    /** List of applications to display in the {@link android.support.v7.widget.RecyclerView} */
    private List<JobApplication> jobApplications;
    /** The {@link android.view.View.OnLongClickListener} attached to the adapter*/
    private View.OnLongClickListener longClickListener;
    /** The {@link android.view.View.OnClickListener} attached to the adapter*/
    private View.OnClickListener clickListener;

    /**
     * The {@link android.support.v7.widget.RecyclerView.ViewHolder} of the {@link android.support.v7.widget.RecyclerView}.
     * This correspond to a "line" of the view, which is the graphical representation of a single element of {@link #jobApplications}
     */
    public class LineHolder extends RecyclerView.ViewHolder {
        /** {@link TextView} that contains the {@link JobApplication#jobTitle} value of the element*/
        final TextView jobTitle;
        /** {@link TextView} that contains the {@link JobApplication#companyName} value of the element*/
        final TextView companyName;
        /** {@link TextView} that contains the element of index {@link JobApplication#step} of the {@link JobApplication#dates} value of the element*/
        final TextView date;

        /**
         * Basic constructor of the class. Initialize the attributes.
         */
        LineHolder(View view) {
            super(view);
            jobTitle = view.findViewById(R.id.list_line_job);
            companyName = view.findViewById(R.id.list_line_company);
            date = view.findViewById(R.id.list_line_date);
        }
    }

    /**
     * Basic constructor of the class. Initialize {@link #jobApplications}
     * @param jobApplications the list of {@link JobApplication} you want to display
     */
    JobApplicationAdapter(List<JobApplication> jobApplications)
    {
        this.jobApplications = jobApplications;
    }

    /**
     * Method to change the list displayed by this {@link android.support.v7.widget.RecyclerView.Adapter} in the {@link RecyclerView}
     * @param jobApplications the new list of {@link JobApplication} you want to display
     */
    void setJobApplications(List<JobApplication> jobApplications) {
        this.jobApplications = jobApplications;
    }

    /**
     * Set the {@link android.view.View.OnLongClickListener}
     * @param callback the {@link android.view.View.OnLongClickListener} that will be set
     */
    void setLongClickListener(View.OnLongClickListener callback) {
        longClickListener = callback;
    }

    /**
     * Set the {@link android.view.View.OnClickListener}
     * @param callback the {@link android.view.View.OnClickListener} that will be set
     */
    void setClickListener(View.OnClickListener callback) {
        clickListener = callback;
    }

    /**
     * Inflate the XML ressource and create a {@link LineHolder} with it.
     * This methods also sets the different ClickListener.
     * @param parent the parent {@link ViewGroup} of the {@link LineHolder} that is created
     * @return the created {@link LineHolder}
     * @see android.support.v7.widget.RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     * @see LayoutInflater#inflate(int, ViewGroup, boolean)
     */
    @NonNull
    @Override
    public LineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.application_list_row, parent, false);

        LineHolder holder = new LineHolder(itemView);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickListener.onLongClick(view); // Call onLongClick function of longClickListener (implemented in MainActivity)
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(view); // Call onLongClick function of longClickListener (implemented in MainActivity)
            }
        });

        return holder;
    }

    /**
     * Set the data of the specified {@link LineHolder}
     * @param holder the {@link LineHolder} of which the displayed data is set
     * @see android.support.v7.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull LineHolder holder, int position)
    {
        JobApplication jobApplication = jobApplications.get(position);
        holder.jobTitle.setText(jobApplication.getJobTitle());
        holder.companyName.setText(jobApplication.getCompanyName());
        holder.date.setText(jobApplication.getDates()[jobApplication.getStep()].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    /**
     * Get the number of items displayed in the {@link RecyclerView}
     * @return the size of {@link JobApplicationAdapter#jobApplications}
     */
    @Override
    public int getItemCount()
    {
        return jobApplications.size();
    }
}
