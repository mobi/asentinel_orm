package com.asentinel.common.jdbc.arrays;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.asentinel.common.jdbc.InOutCall;

/**
 * Adapter class for passing a Date/Calendar array to a {@link PreparedStatement}.
 *
 * @see InOutCall
 * @see Array
 * 
 * @author Razvan Popian
 */
public class DateArray extends Array {
	
	// Date constructors

	/**
	 * Constructor for Date[]. 
	 * @param sqlTypeName
	 * @param dates
	 */
	public DateArray(String sqlTypeName, Date ... dates) {
		super(sqlTypeName, convert(dates));
	}

	/**
	 * Constructor for Collection<Date>
	 * @param sqlTypeName
	 * @param dates
	 */
	public DateArray(String sqlTypeName, Collection<? extends Date> dates) {
		super(sqlTypeName, convertToArray(dates));
	}
	
	// Calendar constructor/factory method

	/**
	 * Constructor for Calendar[].
	 * @param sqlTypeName
	 * @param dates
	 */
	public DateArray(String sqlTypeName, Calendar ... dates) {
		super(sqlTypeName, convert(dates));
	}
	
	/**
	 * Constructor for {@link LocalDateTime} array.
	 * @param sqlTypeName
	 * @param dateTimes
	 */
	public DateArray(String sqlTypeName, LocalDateTime... dateTimes) {
		super(sqlTypeName, convert(dateTimes));
	}
	
	/**
	 * Factory method for DateArray instances for Collection<Calendar>.
	 * @param sqlTypeName
	 * @param dates
	 * @return new instance of DateArray.
	 */
	public static DateArray getInstance(String sqlTypeName, Collection<? extends Calendar> dates) {
		return new DateArray(sqlTypeName, dates.toArray(new Calendar[dates.size()]));
	}
	
	
	private static Date[] convertToArray(Collection<? extends Date> dates) {
		if (dates != null) {
			return convert(dates.toArray(new Date[dates.size()]));
		} else {
			return null;
		}
	}
	
	private static Timestamp[] convert(Date[] dates) {
		if (dates == null) {
			return null;
		}
		Timestamp[] timestamps = new Timestamp[dates.length];
		for (int i=0; i<timestamps.length; i++) {
			if (dates[i] != null) {
				timestamps[i] = new Timestamp(dates[i].getTime());
			} else {
				timestamps[i] = null;
			}
		}
		return timestamps;
	}

	private static Timestamp[] convert(Calendar[] dates) {
		if (dates == null) {
			return null;
		}
		Timestamp[] timestamps = new Timestamp[dates.length];
		for (int i=0; i<timestamps.length; i++) {
			if (dates[i] != null) {
				timestamps[i] = new Timestamp(dates[i].getTimeInMillis());
			} else {
				timestamps[i] = null;
			}
		}
		return timestamps;
	}

	
	private static Timestamp[] convert(LocalDateTime... dateTimes) {
		if (dateTimes == null) {
			return null;
		}
		Timestamp[] timestamps = new Timestamp[dateTimes.length];
		for (int i = 0; i < timestamps.length; i++) {
			if (dateTimes[i] != null) {
				timestamps[i] = Timestamp.valueOf(dateTimes[i]);
			} else {
				timestamps[i] = null;
			}
		}
		return timestamps;
	}

}
