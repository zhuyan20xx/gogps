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
/**
 * <p>
 * Constants
 * </p>
 *
 * @author ege, Cryms.com
 */
public class Constants {

	// Speed of Light [m/s]
	public static final double SPEED_OF_LIGHT = 299792458.0;

	// Physical quantities as in IS-GPS
	public static final double EARTH_GRAVITATIONAL_CONSTANT = 3.986004418e14;
	public static final double EARTH_ANGULAR_VELOCITY = 7.2921151467e-5;
	public static final double RELATIVISTIC_ERROR_CONSTANT = -4.442807633e-10;

	// GPS signal approximate travel time
	public static final double GPS_APPROX_TRAVEL_TIME = 0.072;

	// WGS84 ellipsoid features
	public static final double WGS84_SEMI_MAJOR_AXIS = 6378137;
	public static final double WGS84_FLATTENING = 1 / 298.257223563;
	public static final double WGS84_ECCENTRICITY = Math.sqrt(1 - Math.pow(
			(1 - WGS84_FLATTENING), 2));

	// Time-related values
	public static final long DAYS_IN_WEEK = 7L;
	public static final long SEC_IN_DAY = 86400L;
	public static final long MILLISEC_IN_SEC = 1000L;
	public static final long SEC_IN_HALF_WEEK = 302400L;
	// Days difference between UNIX time and GPS time
	public static final long UNIX_GPS_DAYS_DIFF = 3657L;

	// Standard atmosphere - Berg, 1948 (Bernese)
	public static final double STANDARD_PRESSURE = 1013.25;
	public static final double STANDARD_TEMPERATURE = 291.15;

	// GPS L1 and L2 wavelengths
	public static final double LAMBDA_1 = SPEED_OF_LIGHT / 1575420000;
	public static final double LAMBDA_2 = SPEED_OF_LIGHT / 1227600000;

	// Parameters to weigh observations by signal-to-noise ratio
	public static final float SNR_a = 30;
	public static final float SNR_A = 30;
	public static final float SNR_0 = 10;
	public static final float SNR_1 = 50;

}
