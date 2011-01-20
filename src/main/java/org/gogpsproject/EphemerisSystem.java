/*
 * Copyright (c) 2011, Lorenzo Patocchi. All Rights Reserved.
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
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

public abstract class EphemerisSystem {

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param satID
	 * @param range
	 * @param approxPos
	 */
	protected SatellitePosition computePositionGps(long utcTime,int satID, EphGps eph, double obsPseudorange, double receiverClockError) {

		// Compute satellite clock error
		double satelliteClockError = computeSatelliteClockError(utcTime, eph, obsPseudorange);

		// Compute clock corrected transmission time
		double tGPS = computeClockCorrectedTransmissionTime(utcTime, satelliteClockError, obsPseudorange);

		// Compute eccentric anomaly
		double Ek = computeEccentricAnomaly(tGPS, eph);

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
		sp.setSatelliteClockError(satelliteClockError);

		// Apply the correction due to the Earth rotation during signal travel time
		SimpleMatrix R = computeEarthRotationCorrection(utcTime, receiverClockError, tGPS);
		sp.setSMMultXYZ(R);

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
	protected double checkGpsTime(double time) {

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
	protected SimpleMatrix computeEarthRotationCorrection(long utcTime, double receiverClockError, double transmissionTime) {

		// Computation of signal travel time
		// SimpleMatrix diff = satellitePosition.minusXYZ(approxPos);//this.coord.minusXYZ(approxPos);
		// double rho2 = Math.pow(diff.get(0), 2) + Math.pow(diff.get(1), 2)
		// 		+ Math.pow(diff.get(2), 2);
		// double traveltime = Math.sqrt(rho2) / Constants.SPEED_OF_LIGHT;
		long receptionTime = (new Time(utcTime)).getGpsTime();
		double traveltime = receptionTime + receiverClockError - transmissionTime;

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

		return R;
		// Apply rotation
		//this.coord.ecef = R.mult(this.coord.ecef);
		//this.coord.setSMMultXYZ(R);// = R.mult(this.coord.ecef);
		//satellitePosition.setSMMultXYZ(R);// = R.mult(this.coord.ecef);

	}

	/**
	 * @param eph
	 * @return Clock-corrected GPS transmission time
	 */
	protected double computeClockCorrectedTransmissionTime(long utcTime, double satelliteClockError, double obsPseudorange) {

		long gpsTime = (new Time(utcTime)).getGpsTime();

		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		return tRaw - satelliteClockError;
	}

	/**
	 * @param eph
	 * @return Satellite clock error
	 */
	protected double computeSatelliteClockError(long utcTime, EphGps eph, double obsPseudorange){
		long gpsTime = (new Time(utcTime)).getGpsTime();
		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		// Compute eccentric anomaly
		double Ek = computeEccentricAnomaly(tRaw, eph);

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
	protected double computeEccentricAnomaly(double time, EphGps eph) {

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
