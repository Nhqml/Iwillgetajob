package fr.nhqml.iwillgetajob.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Implementation of the database DAO (Data Access Object)
 */
@Dao
public interface JobApplicationDAO {
    @Insert
    void insert(JobApplication... jobApplications);
    @Update
    void update(JobApplication... jobApplications);
    @Delete
    void delete(JobApplication... jobApplications);

    /**
     * Returns all the {@link JobApplication} of given step
     */
    @Query("SELECT * FROM jobapplication WHERE step = :step")
    List<JobApplication> getAllApplicationsByStep(int step);
}
