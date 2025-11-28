package com.asentinel.common.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.Date;

/**
 * General utility static methods.
 */
public class Utils {
	
	private Utils() {}

	public static double nanosToMillis(long nanos) {
        return ((double) nanos) / 1000000;
	}

	/**
	 * @param date the {@link Date} to convert.
	 * @return {@link LocalDateTime} instance for the specified {@link Date}. The conversion is
	 * 		done using the system default time zone. If the {@link Date} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static ZonedDateTime toZonedDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant()
				.atZone(ZoneId.systemDefault());		
	}
	
	/**
	 * @param date the {@link Date} to convert.
	 * @return {@link LocalDate} instance for the specified {@link Date}. The conversion is
	 * 		done using the system default time zone. If the {@link Date} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return toZonedDateTime(date).toLocalDate();
	}

	/**
	 * @param date the {@link Date} to convert.
	 * @return {@link LocalTime} instance for the specified {@link Date}. The conversion is
	 * 		done using the system default time zone. If the {@link Date} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static LocalTime toLocalTime(Date date) {
		if (date == null) {
			return null;
		}
		return toZonedDateTime(date).toLocalTime();
	}

	/**
	 * @param date the {@link Date} to convert.
	 * @return {@link LocalDateTime} instance for the specified {@link Date}. The conversion is
	 * 		done using the system default time zone. If the {@link Date} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return toZonedDateTime(date).toLocalDateTime();
	}

	/**
	 * @param date the {@link Date} to convert.
	 * @return {@link Instant} representation of the {@code date} parameter. If the
	 *         {@link Date} argument is <code>null</code> this method returns
	 *         <code>null</code>.
	 */
	public static Instant toInstant(Date date) {
		if (date == null) {
			return null;
		}
		return date.toInstant();
	}
	
	/**
	 * @param localDate 
	 * 			the {@link LocalDate} to convert.
	 * @return {@link Date} instance for the specified {@link LocalDate}. The conversion is
	 * 		done using the system default time zone. If the {@link LocalDate} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static Date toDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
        var instant = localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();
		return Date.from(instant);
	}
	
	/**
	 * @param localDateTime 
	 * 			the {@link LocalDateTime} to convert.
	 * @return {@link Date} instance for the specified {@link LocalDateTime}. The conversion is
	 * 		done using the system default time zone. If the {@link LocalDateTime} argument is <code>null</code>
	 * 		this method returns <code>null</code>.
	 */
	public static Date toDate(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	/**
	 * @param localTime 
	 * 			the {@link LocalTime} to convert.
	 * @return {@link Date} instance for the specified {@link LocalTime}. The conversion is
	 * 		done using the system default time zone and the date part is set to the current date. 
	 * 		If the {@link LocalDateTime} argument is <code>null</code> this method returns <code>null</code>.
	 */
	public static Date toDate(LocalTime localTime) {
		if (localTime == null) {
			return null;
		}
		return toDate(localTime.atDate(LocalDate.now()));
	}

    /**
     * @param localTime
     *          the {@link LocalTime} to convert.
     * @return {@link Timestamp} instance for the specified {@link LocalTime}.
     *          The conversion is done using the system default time zone.
     *          The date part is set to the current date.
     *  		If the {@code localTime} argument is {@code null} the result is {@code null}.
     */
    public static Timestamp toTimestamp(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        var instant = localTime.atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault())
                .toInstant();
        return toTimestamp(instant);
    }

    /**
     * @param localDateTime
     *          the {@link LocalDateTime} to convert.
     * @return {@link Timestamp} instance for the specified {@link LocalDateTime}.
     *          The conversion is done using the system default time zone.
     *  		If the {@code localDateTime} argument is {@code null} the result is {@code null}.
     */
    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        var instant = localDateTime.atZone(ZoneId.systemDefault())
                .toInstant();
        return toTimestamp(instant);
    }

    /**
     * @param zonedDateTime
     *          the {@link ZonedDateTime} to convert.
     * @return {@link Timestamp} instance for the specified {@link ZonedDateTime}.
     *  		If the {@code zonedDateTime} argument is {@code null} the result is {@code null}.
     */
    public static Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return toTimestamp(zonedDateTime.toInstant());
    }

    /**
     * @param instant
     *          the {@link Instant} to convert.
     * @return {@link Timestamp} instance for the specified {@link Instant}.
     *  		If the {@code instant} argument is {@code null} the result is {@code null}.
     */
    public static Timestamp toTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.from(instant);
    }

	/**
	 * This method will convert the {@link Temporal} argument to a {@link Date}.
	 * @param someDateOrTime some date/time representation.
	 * @return the converted {@link Date}
	 * @throws IllegalArgumentException if the argument is not one of the supported types.
	 */
	public static Date toDate(Temporal someDateOrTime) {
        if (someDateOrTime == null) {
            return null;
        } else if (someDateOrTime instanceof LocalDate) {
			return toDate((LocalDate) someDateOrTime);
		} else if (someDateOrTime instanceof LocalTime) {
			return toTimestamp((LocalTime) someDateOrTime);
		} else if (someDateOrTime instanceof LocalDateTime) {
			return toTimestamp((LocalDateTime) someDateOrTime);
		} else if (someDateOrTime instanceof ZonedDateTime) {
			return toTimestamp((ZonedDateTime) someDateOrTime);
		} else if (someDateOrTime instanceof Instant) {
            return toTimestamp((Instant) someDateOrTime);
		} else {
			throw new IllegalArgumentException("Unsupported date type: " + someDateOrTime.getClass().getName());
		}
	}

	/**
	 * Helper method for implementing {@link Comparable#compareTo(Object)} for
	 * classes that must implement {@code Comparable} in a consistent with equals
	 * way - usually classes that need to work properly in sorted sets or maps. See
	 * {@code Comparable} for the definition of consistent with equals and the
	 * {@code SortedSet} problems that can occur. If a comparison situation occurs
	 * where the {@code comparator} is not consistent with equals an exception is
	 * thrown.
	 * 
	 * @param <T>        the type of objects to be compared
	 * @param thiz       the object to compare to {@code other}, never {@code null}
	 * @param other      the object compared to {@code thiz}, can be {@code null}
	 * @param comparator the {@code Comparator} to use, never {@code null}. It
	 *                   should include whatever fields the equals method uses to
	 *                   determine that 2 instances are equal.
	 * @return obeys the general {@code Comparable#compareTo(Object)} contract.
	 * 
	 * @throws IllegalStateException if the provided {@link Comparator}
	 *                               implementation is not consistent with equals.
	 * 
	 * 
	 * @see Comparator
	 * @see Comparable
	 */
	public static <T> int consistentWithEqualsCompare(T thiz, T other, Comparator<? super T> comparator) {
		if (thiz.equals(other)) {
			return 0;
		}

		int ret = comparator.compare(thiz, other);
		if (ret == 0) {
			throw new IllegalStateException("The objects " + thiz + " and " + other + " appear to be equal according to the provided comparator, "
					+ "but they are NOT equal according to the equals method. This means the comparator is not consistent with equals.");
		}
		
		return ret;
	}
	
}
