package fr.nhqml.iwillgetajob.db;

import android.arch.persistence.room.TypeConverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converters methods (used to save data in database
 */
class Converters {

    /**
     * Convert dates into a string. Dates are converted to string using the ISO_ZONED_DATE_TIME format.
     * The dates are separated by a '\n' character.
     * @see DateTimeFormatter
     * @param dates the dates to convert into string
     * @return string created
     */
    @TypeConverter
    public static String datesToString(ZonedDateTime dates[])
    {
        if (dates == null)
            return null;

        StringBuilder string = new StringBuilder();
        for (ZonedDateTime date : dates)
        {
            if (date == null)
            {
                string.append("");
                string.append("\n");
            }
            else
            {
                string.append(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                string.append("\n");
            }
        }
        return string.toString();
    }

    /**
     * Convert a string into an array of ZonedDateTime. Dates are extracted from string using the ISO_ZONED_DATE_TIME format.
     * The dates are separated by a '\n' character.
     * @see DateTimeFormatter
     * @param string the string to convert into dates
     * @return dates retrieved from the string
     */
    @TypeConverter
    public static ZonedDateTime[] stringToDates(String string)
    {
        if (string.equals(""))
            return null;

        ZonedDateTime dates[] = new ZonedDateTime[4];
        String strings[] = string.split("\n", -1);
        for (int i = 0; i < 4; i++)
        {
            if (strings[i].equals(""))
            {
                dates[i] = null;
            }
            else
            {
                dates[i] = ZonedDateTime.parse(strings[i], DateTimeFormatter.ISO_ZONED_DATE_TIME);
            }
        }
        return dates;
    }

    /**
     * Convert a {@link JobApplication.Contact} in its string representation.
     * This method only calls {@link JobApplication.Contact#toString()}.
     * @param contact contact to convert
     * @return the string representation of  the {@link JobApplication.Contact} object
     */
    @TypeConverter
    public static String contactToString(JobApplication.Contact contact)
    {
        return contact == null ? null : contact.toString();
    }

    /**
     * Retrieve a {@link JobApplication.Contact} from its string representation.
     * @param string the string representation of  the {@link JobApplication.Contact} object
     * @return the retrieved {@link JobApplication.Contact} object
     */
    @TypeConverter
    public static JobApplication.Contact stringToContact(String string)
    {
        if (string == null)
            return null;

        String[] strings = string.split("\n", -1);
        return new JobApplication.Contact(strings[0], strings[1], strings[2]);
    }
}
