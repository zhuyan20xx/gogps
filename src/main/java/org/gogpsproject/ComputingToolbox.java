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
 */
package org.gogpsproject;

import org.ejml.data.SimpleMatrix;

/**
 * <p>
 * Atmospheric correction class
 * </p>
 *
 * @author ege, Cryms.com
 */
public class ComputingToolbox {

	/**
	 * @param elevation
	 * @param height
	 * @return troposphere correction value by Saastamoinen model
	 */
	public static double computeTroposphereCorrection(double elevation, double height) {

		double tropoCorr = 0;

		if (height < 5000) {

			elevation = Math.toRadians(Math.abs(elevation));
			if (elevation == 0){
				elevation = elevation + 0.01;
			}

			// Numerical constants and tables for Saastamoinen algorithm
			// (troposphere correction)
			double hr = 50.0;
			int[] ha = new int[9];
			double[] ba = new double[9];

			ha[0] = 0;
			ha[1] = 500;
			ha[2] = 1000;
			ha[3] = 1500;
			ha[4] = 2000;
			ha[5] = 2500;
			ha[6] = 3000;
			ha[7] = 4000;
			ha[8] = 5000;

			ba[0] = 1.156;
			ba[1] = 1.079;
			ba[2] = 1.006;
			ba[3] = 0.938;
			ba[4] = 0.874;
			ba[5] = 0.813;
			ba[6] = 0.757;
			ba[7] = 0.654;
			ba[8] = 0.563;

			// Saastamoinen algorithm
			double P = Constants.STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
			double T = Constants.STANDARD_TEMPERATURE - 0.0065 * height;
			double H = hr * Math.exp(-0.0006396 * height);

			// If height is below zero, keep the maximum correction value
			double B = ba[0];
			// Otherwise, interpolate the tables
			if (height >= 0) {
				int i = 1;
				while (height > ha[i]) {
					i++;
				}
				double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
				B = ba[i - 1] + m * (height - ha[i - 1]);
			}

			double e = 0.01
					* H
					* Math.exp(-37.2465 + 0.213166 * T - 0.000256908
							* Math.pow(T, 2));

			tropoCorr = ((0.002277 / Math.sin(elevation))
					* (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
					* (1255 / T + 0.05) * e);
		}

		return tropoCorr;
	}

	/**
	 * @param ionoParams
	 * @param coord
	 * @param time
	 * @return ionosphere correction value by Klobuchar model
	 */
	public static double computeIonosphereCorrection(NavigationProducer navigation,
			Coordinates coord, double azimuth, double elevation, Time time) {

		double ionoCorr = 0;

		IonoGps iono = navigation.getIono(time.getMsec());
		if(iono==null) return 0.0;
//		double a0 = navigation.getIono(time.getMsec(),0);
//		double a1 = navigation.getIono(time.getMsec(),1);
//		double a2 = navigation.getIono(time.getMsec(),2);
//		double a3 = navigation.getIono(time.getMsec(),3);
//		double b0 = navigation.getIono(time.getMsec(),4);
//		double b1 = navigation.getIono(time.getMsec(),5);
//		double b2 = navigation.getIono(time.getMsec(),6);
//		double b3 = navigation.getIono(time.getMsec(),7);

		elevation = Math.abs(elevation);

		// Parameter conversion to semicircles
		double lon = coord.getGeodeticLongitude() / 180; // geod.get(0)
		double lat = coord.getGeodeticLatitude() / 180; //geod.get(1)
		azimuth = azimuth / 180;
		elevation = elevation / 180;

		// Klobuchar algorithm
		double f = 1 + 16 * Math.pow((0.53 - elevation), 3);
		double psi = 0.0137 / (elevation + 0.11) - 0.022;
		double phi = lat + psi * Math.cos(azimuth * Math.PI);
		if (phi > 0.416){
			phi = 0.416;
		}
		if (phi < -0.416){
			phi = -0.416;
		}
		double lambda = lon + (psi * Math.sin(azimuth * Math.PI))
				/ Math.cos(phi * Math.PI);
		double ro = phi + 0.064 * Math.cos((lambda - 1.617) * Math.PI);
		double t = lambda * 43200 + time.getGpsTime();
		while (t >= 86400)
			t = t - 86400;
		while (t < 0)
			t = t + 86400;
		double p = iono.getBeta(0) + iono.getBeta(1) * ro + iono.getBeta(2) * Math.pow(ro, 2) + iono.getBeta(3) * Math.pow(ro, 3);

		if (p < 72000)
			p = 72000;
		double a = iono.getAlpha(0) + iono.getAlpha(1) * ro + iono.getAlpha(2) * Math.pow(ro, 2) + iono.getAlpha(3) * Math.pow(ro, 3);
		if (a < 0)
			a = 0;
		double x = (2 * Math.PI * (t - 50400)) / p;
		if (Math.abs(x) < 1.57){
			ionoCorr = Constants.SPEED_OF_LIGHT
					* f
					* (5e-9 + a
							* (1 - (Math.pow(x, 2)) / 2 + (Math.pow(x, 4)) / 24));
		}else{
			ionoCorr = Constants.SPEED_OF_LIGHT * f * 5e-9;
		}
		return ionoCorr;
	}


	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param satID
	 * @param range
	 * @param approxPos
	 */
	public static SatellitePosition computePositionGps(long utcTime,int satID, EphGps eph, double obsPseudorange) {

		double timeCorrection = getTimeCorrection(utcTime, eph, obsPseudorange);

		// Compute clock corrected transmission time
		double tGPS = getClockCorrectedTransmissionTime(utcTime, timeCorrection, obsPseudorange);

		// Compute eccentric anomaly
		double Ek = eccAnomaly(tGPS, eph);

		// Semi-major axis
		double A = eph.getRootA() * eph.getRootA();

		// Time from the ephemerides reference epoch
		double tk = checkGpsTime(tGPS - eph.getToe());

		// Position computation
		double fk = Math.atan2(Math.sqrt(1 - Math.pow(eph.getE(), 2))
				* Math.sin(Ek), Math.cos(Ek) - eph.getE());
		double phi = fk + eph.getOmega();
		phi = Math.IEEEremainder(phi, 2 * Math.PI);
		double u = phi + eph.getCuc() * Math.cos(2 * phi) + eph.getCus()
				* Math.sin(2 * phi);
		double r = A * (1 - eph.getE() * Math.cos(Ek)) + eph.getCrc()
				* Math.cos(2 * phi) + eph.getCrs() * Math.sin(2 * phi);
		double ik = eph.getI0() + eph.getiDot() * tk + eph.getCic() * Math.cos(2 * phi)
				+ eph.getCis() * Math.sin(2 * phi);
		double Omega = eph.getOmega0()
				+ (eph.getOmegaDot() - Constants.EARTH_ANGULAR_VELOCITY) * tk
				- Constants.EARTH_ANGULAR_VELOCITY * eph.getToe();
		Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
		double x1 = Math.cos(u) * r;
		double y1 = Math.sin(u) * r;

		// Coordinates
//			double[][] data = new double[3][1];
//			data[0][0] = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
//			data[1][0] = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
//			data[2][0] = y1 * Math.sin(ik);

		// Fill in the satellite position matrix
		//this.coord.ecef = new SimpleMatrix(data);
		//this.coord = Coordinates.globalXYZInstance(new SimpleMatrix(data));
		SatellitePosition sp = new SatellitePosition(utcTime,satID, x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
				y1 * Math.sin(ik));
		sp.setTimeCorrection(timeCorrection);

		// Apply the correction due to the Earth rotation during signal travel time
		earthRotationCorrection(utcTime, tGPS, sp);

		return sp;
//		this.setXYZ(x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
//				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
//				y1 * Math.sin(ik));

	}

	/**
	 * @param time
	 *            (Uncorrected GPS time)
	 * @return GPS time accounting for beginning or end of week crossover
	 */
	public static double checkGpsTime(double time) {

		// Account for beginning or end of week crossover
		if (time > Constants.SEC_IN_HALF_WEEK) {
			time = time - 2 * Constants.SEC_IN_HALF_WEEK;
		} else if (time < -Constants.SEC_IN_HALF_WEEK) {
			time = time + 2 * Constants.SEC_IN_HALF_WEEK;
		}
		return time;
	}

	/**
	 * @param traveltime
	 */
	public static void earthRotationCorrection(long utcTime, double transmissionTime, Coordinates satellitePosition) {

		// Computation of signal travel time
		// SimpleMatrix diff = satellitePosition.minusXYZ(approxPos);//this.coord.minusXYZ(approxPos);
		// double rho2 = Math.pow(diff.get(0), 2) + Math.pow(diff.get(1), 2)
		// 		+ Math.pow(diff.get(2), 2);
		// double traveltime = Math.sqrt(rho2) / Constants.SPEED_OF_LIGHT;
		long receptionTime = (new Time(utcTime)).getGpsTime();
		double traveltime = receptionTime - transmissionTime;

		// Compute rotation angle
		double omegatau = Constants.EARTH_ANGULAR_VELOCITY * traveltime;

		// Rotation matrix
		double[][] data = new double[3][3];
		data[0][0] = Math.cos(omegatau);
		data[0][1] = Math.sin(omegatau);
		data[0][2] = 0;
		data[1][0] = -Math.sin(omegatau);
		data[1][1] = Math.cos(omegatau);
		data[1][2] = 0;
		data[2][0] = 0;
		data[2][1] = 0;
		data[2][2] = 1;
		SimpleMatrix R = new SimpleMatrix(data);

		// Apply rotation
		//this.coord.ecef = R.mult(this.coord.ecef);
		//this.coord.setSMMultXYZ(R);// = R.mult(this.coord.ecef);
		satellitePosition.setSMMultXYZ(R);// = R.mult(this.coord.ecef);

	}

	/**
	 * @param eph
	 * @return Clock-corrected GPS transmission time
	 */
	public static double getClockCorrectedTransmissionTime(long utcTime, double timeCorrection, double obsPseudorange) {

		long gpsTime = (new Time(utcTime)).getGpsTime();
		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		return tRaw - timeCorrection;
	}

	/**
	 * @param eph
	 * @return Satellite clock error
	 */
	public static double getTimeCorrection(long utcTime, EphGps eph, double obsPseudorange){
		long gpsTime = (new Time(utcTime)).getGpsTime();
		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		// Compute eccentric anomaly
		double Ek = eccAnomaly(tRaw, eph);

		// Relativistic correction term computation
		double dtr = Constants.RELATIVISTIC_ERROR_CONSTANT * eph.getE() * eph.getRootA() * Math.sin(Ek);

		// Clock error computation
		double dt = checkGpsTime(tRaw - eph.getToc());
		double timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
		double tGPS = tRaw - timeCorrection;
		dt = checkGpsTime(tGPS - eph.getToc());
		timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();

		return timeCorrection;
	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param eph
	 * @return Eccentric anomaly
	 */
	public static double eccAnomaly(double time, EphGps eph) {

		// Semi-major axis
		double A = eph.getRootA() * eph.getRootA();

		// Time from the ephemerides reference epoch
		double tk = checkGpsTime(time - eph.getToe());

		// Computed mean motion [rad/sec]
		double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT / Math.pow(A, 3));

		// Corrected mean motion [rad/sec]
		double n = n0 + eph.getDeltaN();

		// Mean anomaly
		double Mk = eph.getM0() + n * tk;

		// Eccentric anomaly starting value
		Mk = Math.IEEEremainder(Mk + 2 * Math.PI, 2 * Math.PI);
		double Ek = Mk;

		int i;
		double EkOld, dEk;

		// Eccentric anomaly iterative computation
		for (i = 0; i < 10; i++) {
			EkOld = Ek;
			Ek = Mk + eph.getE() * Math.sin(Ek);
			dEk = Math.IEEEremainder(Ek - EkOld, 2 * Math.PI);
			if (Math.abs(dEk) < 1e-12)
				break;
		}

		// TODO Display/log warning message
		if (i == 10)
			System.out.println("Eccentric anomaly does not converge");

		return Ek;

	}

}
