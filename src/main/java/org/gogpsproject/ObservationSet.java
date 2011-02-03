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
 * Set of observations for one epoch and one satellite
 * </p>
 *
 * @author ege, Cryms.com
 */
public class ObservationSet {

	public final static int L1 = 0;
	public final static int L2 = 1;


	private int satID;	/* Satellite number */
	/* Array of [L1,L2] */
	private double[] codeC;			/* C Coarse/Acquisition (C/A) code [m] */
	private double[] codeP;			/* P Code Pseudorange [m] */
	private double[] phase;			/* L Carrier Phase [cycle] */
	private float[] signalStrength;	/* C/N0 (signal strength) [dBHz] */
	private float[] doppler;		/* Doppler value [Hz] */

	private int[] qualityInd = {-1,-1};	/* Nav Measurements Quality Ind. ublox proprietary? */
	private int[] lossLockInd = {-1,-1};   /* Loss of lock indicator (RINEX definition) */

	public ObservationSet(){
		codeC = new double[2];
		codeC[0] = Double.NaN;
		codeC[1] = Double.NaN;

		codeP = new double[2];
		codeP[0] = Double.NaN;
		codeP[1] = Double.NaN;

		phase = new double[2];
		signalStrength = new float[2];
		doppler = new float[2];
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
	 * @return the pseudorange
	 */
	public double getPseudorange(int i) {
		return Double.isNaN(codeP[i])?codeC[i]:codeP[i];
	}

	public boolean isPseudorangeP(int i){
		return !Double.isNaN(codeP[i]);
	}

	/**
	 * @return the c
	 */
	public double getCodeC(int i) {
		return codeC[i];
	}

	/**
	 * @param c the c to set
	 */
	public void setCodeC(int i,double c) {
		codeC[i] = c;
	}

	/**
	 * @return the p
	 */
	public double getCodeP(int i) {
		return codeP[i];
	}

	/**
	 * @param p the p to set
	 */
	public void setCodeP(int i, double p) {
		codeP[i] = p;
	}

	/**
	 * @return the l
	 */
	public double getPhase(int i) {
		return phase[i];
	}

	/**
	 * @param l the l to set
	 */
	public void setPhase(int i, double l) {
		phase[i] = l;
	}

	/**
	 * @return the s
	 */
	public float getSignalStrength(int i) {
		return signalStrength[i];
	}

	/**
	 * @param s the s to set
	 */
	public void setSignalStrength(int i, float s) {
		signalStrength[i] = s;
	}

	/**
	 * @return the d
	 */
	public float getDoppler(int i) {
		return doppler[i];
	}

	/**
	 * @param d the d to set
	 */
	public void setDoppler(int i, float d) {
		doppler[i] = d;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ObservationSet){
			return ((ObservationSet)obj).getSatID() == satID;
		}else{
			return super.equals(obj);
		}
	}

	/**
	 * @return the qualityInd
	 */
	public int getQualityInd(int i) {
		return qualityInd[i];
	}

	/**
	 * @param qualityInd the qualityInd to set
	 */
	public void setQualityInd(int i,int qualityInd) {
		this.qualityInd[i] = qualityInd;
	}

	/**
	 * @return the lossLockInd
	 */
	public int getLossLockInd(int i) {
		return lossLockInd[i];
	}

	/**
	 * @param lossLockInd the lossLockInd to set
	 */
	public void setLossLockInd(int i,int lossLockInd) {
		this.lossLockInd[i] = lossLockInd;
	}

	public boolean isLocked(int i){
		return lossLockInd[i] == 0;
	}
	public boolean isPossibleCycleSlip(int i){
		return lossLockInd[i]>0 && ((lossLockInd[i]&0x1) == 0x1);
	}
	public boolean isHalfWavelength(int i){
		return lossLockInd[i]>0 && ((lossLockInd[i]&0x2) == 0x2);
	}
	public boolean isUnderAntispoof(int i){
		return lossLockInd[i]>0 && ((lossLockInd[i]&0x4) == 0x4);
	}


}