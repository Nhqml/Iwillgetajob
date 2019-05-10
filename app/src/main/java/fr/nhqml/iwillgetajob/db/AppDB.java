package fr.nhqml.iwillgetajob.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Implementation of the abstract database class
 */
@Database(entities = {JobApplication.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDB extends RoomDatabase {
    public abstract JobApplicationDAO getJobApplicationDAO();
}
