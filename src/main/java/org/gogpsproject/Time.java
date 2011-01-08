/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.gogpsproject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * <p>
 * Class for unifying time representations
 * </p>
 *
 * @author ege, Cryms.com
 */
public class Time {
	private long msec; /* time in milliseconds since January 1, 1970 (UNIX standard) */
	private double fraction; /* fraction of millisecond */

	public Time(long msec){
		this.msec = msec;
		this.fraction = Double.NaN;
	}
	public Time(long msec, double fraction){
		this.msec = msec;
		this.fraction = fraction;
	}
	public Time(String dateStr) throws ParseException{
		this.msec = dateStringToTime(dateStr);
		this.fraction = Double.NaN;
	}
	public Time(int gpsWeek, int weekSec){
		this.msec = (Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY + gpsWeek*7L*24L*3600L + weekSec) * 1000L;
	}
	/**
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	private static long dateStringToTime(String dateStr) throws ParseException {

		long dateTime = 0;

		DateFormat df = new SimpleDateFormat("yyyy MM dd HH mm ss.SSS");

		// Set GMT time zone
		TimeZone zone = TimeZone.getTimeZone("GMT Time");
		df.setTimeZone(zone);

		try {
			Date dateObj = df.parse(dateStr);
			dateTime = dateObj.getTime();
		} catch (ParseException e) {
			throw e;
		}

		return dateTime;
	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @return UNIX standard time in milliseconds
	 */
	private static long gpsToUnixTime(long time, int week) {
		// Shift from GPS time (January 6, 1980 - sec)
		// to UNIX time (January 1, 1970 - msec)
		time = (time + (week * Constants.DAYS_IN_WEEK + Constants.UNIX_GPS_DAYS_DIFF) * Constants.SEC_IN_DAY) * Constants.MILLISEC_IN_SEC;

		return time;
	}

	/**
	 * @param time
	 *            (UNIX standard time in milliseconds)
	 * @return GPS time in seconds
	 */
	private static long unixToGpsTime(long time) {
		// Shift from UNIX time (January 1, 1970 - msec)
		// to GPS time (January 6, 1980 - sec)
		time = time / Constants.MILLISEC_IN_SEC - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;

		// Remove integer weeks, to get Time Of Week
		//time = Math.IEEEremainder(time, Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY);

		time = time%(Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY);

		return time;
	}

	public int getGpsWeek(){


		// Shift from UNIX time (January 1, 1970 - msec)
		// to GPS time (January 6, 1980 - sec)
		long time = msec / Constants.MILLISEC_IN_SEC - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;


		return (int)(time/(Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY));


	}
	public int getGpsWeekSec(){


		// Shift from UNIX time (January 1, 1970 - msec)
		// to GPS time (January 6, 1980 - sec)
		long time = msec / Constants.MILLISEC_IN_SEC - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;

		return (int)(time%(Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY));

	}
	public int getGpsWeekDay(){

		return (int)(getGpsWeekSec()/Constants.SEC_IN_DAY);

	}

	public long getGpsTime(){
		return unixToGpsTime(msec);
	}
//
//	private static double unixToGpsTime(double time) {
//		// Shift from UNIX time (January 1, 1970 - msec)
//		// to GPS time (January 6, 1980 - sec)
//		time = (long)(time / Constants.MILLISEC_IN_SEC) - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;
//
//		// Remove integer weeks, to get Time Of Week
//		double dividend  = time;
//		double divisor = Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY;
//		time = dividend  - (divisor * round(dividend / divisor));
//
//		//time = Math.IEEEremainder(time, Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY);
//
//		return time;
//	}



	/**
	 * @return the msec
	 */
	public long getMsec() {
		return msec;
	}

	/**
	 * @param msec the msec to set
	 */
	public void setMsec(long msec) {
		this.msec = msec;
	}

	/**
	 * @return the fraction
	 */
	public double getFraction() {
		return fraction;
	}

	/**
	 * @param fraction the fraction to set
	 */
	public void setFraction(double fraction) {
		this.fraction = fraction;
	}
}
