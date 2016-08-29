package de.thmgames.s3.Utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Benedikt on 05.11.2014.
 */
public final class DateUtils {

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

}
