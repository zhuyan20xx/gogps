/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
import java.text.DecimalFormat;
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
 * @author Eugenio Realini, Cryms.com
 */
public class Time {
	private long msec; /* time in milliseconds since January 1, 1970 (UNIX standard) */
	private double fraction; /* fraction of millisecond */
	private static DateFormat df = new SimpleDateFormat("yyyy MM dd HH mm ss.SSS");

	private Calendar gc = null;

	{
		gc = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	public Time(long msec){
		this.gc.setTimeInMillis(msec);
		this.msec = msec;
		this.fraction = Double.NaN;
	}
	public Time(long msec, double fraction){
		this.msec = msec;
		this.gc.setTimeInMillis(msec);
		this.fraction = fraction;
	}
	public Time(String dateStr) throws ParseException{
		this.msec = dateStringToTime(dateStr);
		this.gc.setTimeInMillis(this.msec);
		this.fraction = Double.NaN;
	}
	public Time(int gpsWeek, int weekSec){
		this.msec = (Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY + gpsWeek*7L*24L*3600L + weekSec) * 1000L;
		this.gc.setTimeInMillis(this.msec);
	}
	/**
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	private static long dateStringToTime(String dateStr) throws ParseException {

		long dateTime = 0;


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
	public int getGpsHourInDay(){
		long time = msec / Constants.MILLISEC_IN_SEC - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;
		return (int)((time%(Constants.SEC_IN_DAY))/Constants.SEC_IN_HOUR);
	}
	public int getYear(){
		return gc.get(Calendar.YEAR);
	}
	public int getYear2c(){
		return gc.get(Calendar.YEAR)-2000;
	}
	public int getDayOfYear(){
		return gc.get(Calendar.DAY_OF_YEAR);
	}
	public String getHourOfDayLetter(){
		char c = (char)('a'+getGpsHourInDay());
		return ""+c;
	}

	/*
	 * Locating IGS data, products, and format definitions	Key to directory and file name variables
	 * d	day of week (0-6)
	 * ssss	4-character IGS site ID or 4-character LEO ID
	 * yyyy	4-digit year
	 * yy	2-digit year
	 * wwww	4-digit GPS week
	 * ww	2-digit week of year(01-53)
	 * ddd	day of year (1-366)
	 * hh	2-digit hour of day (00-23)
	 * h	single letter for hour of day (a-x = 0-23)
	 * mm	minutes within hour
	 *
	 */
	public String formatTemplate(String template){
		String tmpl = template.replaceAll("\\$\\{wwww\\}", (new DecimalFormat("0000")).format(this.getGpsWeek()));
		tmpl = tmpl.replaceAll("\\$\\{d\\}", (new DecimalFormat("0")).format(this.getGpsWeekDay()));
		tmpl = tmpl.replaceAll("\\$\\{ddd\\}", (new DecimalFormat("000")).format(this.getDayOfYear()));
		tmpl = tmpl.replaceAll("\\$\\{yy\\}", (new DecimalFormat("00")).format(this.getYear2c()));
		tmpl = tmpl.replaceAll("\\$\\{yyyy\\}", (new DecimalFormat("0000")).format(this.getYear()));
		int hh4 = this.getGpsHourInDay();
		tmpl = tmpl.replaceAll("\\$\\{hh\\}", (new DecimalFormat("00")).format(hh4));
		if(0<=hh4&&hh4<6) hh4=0;
		if(6<=hh4&&hh4<12) hh4=6;
		if(12<=hh4&&hh4<18) hh4=12;
		if(18<=hh4&&hh4<24) hh4=18;
		tmpl = tmpl.replaceAll("\\$\\{hh4\\}", (new DecimalFormat("00")).format(hh4));
		tmpl = tmpl.replaceAll("\\$\\{h\\}", this.getHourOfDayLetter());
		return tmpl;
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

	public Object clone(){
		return new Time(this.msec,this.fraction);
	}

	public String toString(){
		return df.format(gc.getTime())+" "+gc.getTime();
	}
}
