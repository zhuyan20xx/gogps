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
import org.ejml.data.SimpleMatrix;

/**
 * <p>
 * Satellite position class
 * </p>
 *
 * @author ege, Cryms.com
 */
public class SatellitePosition extends Coordinates{
	private int satID; /* Satellite ID number */
	//private Coordinates coord; /* Satellite coordinates */
	private double timeCorrection; /* Correction due to satellite clock error */
	//private double range;
	private long time;

	public SatellitePosition(EphGps eph, long time, int satID, double obsPseudorange) {
		super();

		this.time = time;
		this.satID = satID;
		//this.range = range;

		this.computePositionGps(eph, obsPseudorange);
	}

	public SatellitePosition(long time, int satID, double x, double y, double z) {
		super();

		this.time = time;
		this.satID = satID;
		//this.range = range;

		this.setXYZ(x, y, z);
	}

//	/**
//	 * @param time
//	 *            (GPS time in seconds)
//	 * @param satID
//	 * @param range
//	 * @param approxPos
//	 */
//	private void computePositionGps(NavigationProducer navigation) {
//
//		//this.coord = new Coordinates();
//
//		// Find reference ephemerides (given satID and time)
//		EphGps eph = navigation.findEph(this.time, this.satID);
//
//		if (eph != null) {
//			computePositionGps(eph);
//		}
//	}
	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param satID
	 * @param range
	 * @param approxPos
	 */
	private void computePositionGps(EphGps eph, double obsPseudorange) {



		// Compute clock correction
		double tGPS = clockCorrection(eph, obsPseudorange);

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
		this.setXYZ(x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
				y1 * Math.sin(ik));

	}

	/**
	 * @param traveltime
	 */
	public void earthRotationCorrection(Coordinates approxPos) {

		// Computation of signal travel time
		//SimpleMatrix diff = this.coord.ecef.minus(approxPos.ecef);
		SimpleMatrix diff = this.minusXYZ(approxPos);//this.coord.minusXYZ(approxPos);
		double rho2 = Math.pow(diff.get(0), 2) + Math.pow(diff.get(1), 2)
				+ Math.pow(diff.get(2), 2);
		double traveltime = Math.sqrt(rho2) / Constants.SPEED_OF_LIGHT;

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
		this.setSMMultXYZ(R);// = R.mult(this.coord.ecef);

	}

	/**
	 * @param eph
	 * @return Clock-corrected GPS time
	 */
	private double clockCorrection(EphGps eph, double obsPseudorange) {

		// Remove signal travel time from observation time
		double tRaw = (this.time - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		// Compute eccentric anomaly
		double Ek = eccAnomaly(tRaw, eph);

		// Relativistic correction term computation
		double dtr = Constants.RELATIVISTIC_ERROR_CONSTANT * eph.getE() * eph.getRootA() * Math.sin(Ek);

		// Clock error correction
		double dt = checkGpsTime(tRaw - eph.getToc());
		timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
		double tGPS = tRaw - timeCorrection;
		dt = checkGpsTime(tGPS - eph.getToc());
		timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
		tGPS = tRaw - timeCorrection;

		return tGPS;

	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param eph
	 * @return Eccentric anomaly
	 */
	private static double eccAnomaly(double time, EphGps eph) {

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

	/**
	 * @param time
	 *            (Uncorrected GPS time)
	 * @return GPS time accounting for beginning or end of week crossover
	 */
	private static double checkGpsTime(double time) {

		// Account for beginning or end of week crossover
		if (time > Constants.SEC_IN_HALF_WEEK) {
			time = time - 2 * Constants.SEC_IN_HALF_WEEK;
		} else if (time < -Constants.SEC_IN_HALF_WEEK) {
			time = time + 2 * Constants.SEC_IN_HALF_WEEK;
		}
		return time;
	}

	/**
	 * @return the satID
	 */
	public int getSatID() {
		return satID;
	}

	/**
	 * @param satID the satID to set
	 */
	public void setSatID(int satID) {
		this.satID = satID;
	}

	/**
	 * @return the coord
	 */
//	public Coordinates getCoord() {
//		return coord;
//	}

	/**
	 * @param coord the coord to set
	 */
//	public void setCoord(Coordinates coord) {
//		this.coord = coord;
//	}

	/**
	 * @return the timeCorrection
	 */
	public double getTimeCorrection() {
		return timeCorrection;
	}

	/**
	 * @param timeCorrection the timeCorrection to set
	 */
	public void setTimeCorrection(double timeCorrection) {
		this.timeCorrection = timeCorrection;
	}

//	/**
//	 * @return the range
//	 */
//	public double getRange() {
//		return range;
//	}

//	/**
//	 * @param range the range to set
//	 */
//	public void setRange(double range) {
//		this.range = range;
//	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
}
