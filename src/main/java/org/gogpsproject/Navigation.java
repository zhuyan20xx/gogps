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
import java.util.ArrayList;

/**
 * <p>
 * Navigation data and related methods
 * </p>
 * 
 * @author ege, Cryms.com
 */
public class Navigation {
	//static int n; /* Number of ephemeris sets */
	private ArrayList<EphGps> eph; /* GPS broadcast ephemerides */
	private double[] iono; /* Ionosphere model parameters */
	private double A0; /* Delta-UTC parameters: A0 */
	private double A1; /* Delta-UTC parameters: A1 */
	private double T; /* Delta-UTC parameters: T */
	private double W; /* Delta-UTC parameters: W */
	private int leaps; /* Leap seconds */

	
	public Navigation(){
		eph = new ArrayList<EphGps>();
		iono = new double[8];
	}
	/**
	 * @param time
	 * @param satID
	 * @return Reference ephemeris set for given time and satellite
	 */
	public EphGps findEph(double time, int satID) {

		double dt = 0;
		double dtMin = 0;
		EphGps refEph = null;

		for (int i = 0; i < eph.size(); i++) {
			// Find ephemeris sets for given satellite
			if (eph.get(i).getSatID() == satID) {
				// Compare current time and ephemeris reference time
				dt = Math.abs(eph.get(i).getRefTime().getGpsTime() - time);
				// If it's the first round, set the minimum time difference and
				// select the first ephemeris set candidate
				if (refEph == null) {
					dtMin = dt;
					refEph = eph.get(i);
					// Check if the current ephemeris set is closer in time than
					// the previous candidate; if yes, select new candidate
				} else if (dt < dtMin) {
					dtMin = dt;
					refEph = eph.get(i);
				}
			}
		}
		return refEph;
	}
	
	public int getEphSize(){
		return eph.size();
	}
	
	public void addEph(EphGps eph){
		this.eph.add(eph);
	}
	
	public void setIono(int i, double val){
		this.iono[i] = val;
	}
	public double getIono(int i){
		return iono[i];
	}
	/**
	 * @return the a0
	 */
	public double getA0() {
		return A0;
	}
	/**
	 * @param a0 the a0 to set
	 */
	public void setA0(double a0) {
		A0 = a0;
	}
	/**
	 * @return the a1
	 */
	public double getA1() {
		return A1;
	}
	/**
	 * @param a1 the a1 to set
	 */
	public void setA1(double a1) {
		A1 = a1;
	}
	/**
	 * @return the t
	 */
	public double getT() {
		return T;
	}
	/**
	 * @param t the t to set
	 */
	public void setT(double t) {
		T = t;
	}
	/**
	 * @return the w
	 */
	public double getW() {
		return W;
	}
	/**
	 * @param w the w to set
	 */
	public void setW(double w) {
		W = w;
	}
	/**
	 * @return the leaps
	 */
	public int getLeaps() {
		return leaps;
	}
	/**
	 * @param leaps the leaps to set
	 */
	public void setLeaps(int leaps) {
		this.leaps = leaps;
	}
}
