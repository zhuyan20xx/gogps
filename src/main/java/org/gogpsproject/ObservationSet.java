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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * <p>
 * Set of observations for one epoch and one satellite
 * </p>
 *
 * @author ege, Cryms.com
 */
public class ObservationSet implements Streamable {

	private final static int STREAM_V = 1;


	public final static int L1 = 0;
	public final static int L2 = 1;


	private int satID;	/* Satellite number */
	/* Array of [L1,L2] */
	private double[] codeC = {Double.NaN,Double.NaN};			/* C Coarse/Acquisition (C/A) code [m] */
	private double[] codeP = {Double.NaN,Double.NaN};			/* P Code Pseudorange [m] */
	private double[] phase = {Double.NaN,Double.NaN};			/* L Carrier Phase [cycle] */
	private float[] signalStrength = {Float.NaN,Float.NaN};	/* C/N0 (signal strength) [dBHz] */
	private float[] doppler = {Float.NaN,Float.NaN};		/* Doppler value [Hz] */

	private int[] qualityInd = {-1,-1};	/* Nav Measurements Quality Ind. ublox proprietary? */
	private int[] lossLockInd = {-1,-1};   /* Loss of lock indicator (RINEX definition) */

	public ObservationSet(){
	}

	public ObservationSet(DataInputStream dai, boolean oldVersion) throws IOException{
		read(dai,oldVersion);
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

	public int write(DataOutputStream dos) throws IOException{
		int size = 0;
		dos.writeUTF(MESSAGE_EPHEMERIS_SET); // 5

		dos.writeInt(STREAM_V); size +=4;
		dos.write(satID);size +=1;		// 1
		// L1 data
		dos.write(qualityInd[L1]);	size+=1;
		dos.write(lossLockInd[L1]);	size+=1;
		dos.writeDouble(codeC[L1]); size+=8;
		dos.writeDouble(codeP[L1]); size+=8;
		dos.writeDouble(phase[L1]); size+=8;
		dos.writeFloat(signalStrength[L1]); size+=4;
		dos.writeFloat(doppler[L1]); size+=4;
		// write L2 data ?
		boolean hasL2 = false;
		if(!Double.isNaN(codeC[L2])) hasL2 = true;
		if(!Double.isNaN(codeP[L2])) hasL2 = true;
		if(!Double.isNaN(phase[L2])) hasL2 = true;
		if(!Float.isNaN(signalStrength[L2])) hasL2 = true;
		if(!Float.isNaN(doppler[L2])) hasL2 = true;
		dos.writeBoolean(hasL2); size+=1;
		if(hasL2){
			dos.write(qualityInd[L2]);	size+=1;
			dos.write(lossLockInd[L2]);	size+=1;
			dos.writeDouble(codeC[L2]); size+=8;
			dos.writeDouble(codeP[L2]); size+=8;
			dos.writeDouble(phase[L2]); size+=8;
			dos.writeFloat(signalStrength[L2]); size+=4;
			dos.writeFloat(doppler[L2]); size+=4;
		}
		return size;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.Streamable#read(java.io.DataInputStream)
	 */
	@Override
	public void read(DataInputStream dai, boolean oldVersion) throws IOException {
		int v = 1;
		if(!oldVersion) v = dai.readInt();

		if(v==1){
			satID = dai.read();

			// L1 data
			qualityInd[L1] = dai.read();
			lossLockInd[L1] = dai.read();
			codeC[L1] = dai.readDouble();
			codeP[L1] = dai.readDouble();
			phase[L1] = dai.readDouble();
			signalStrength[L1] = dai.readFloat();
			doppler[L1] = dai.readFloat();
			if(dai.readBoolean()){
				// L2 data
				qualityInd[L2] = dai.read();
				lossLockInd[L2] = dai.read();
				codeC[L2] = dai.readDouble();
				codeP[L2] = dai.readDouble();
				phase[L2] = dai.readDouble();
				signalStrength[L2] = dai.readFloat();
				doppler[L2] = dai.readFloat();
			}
		}else{
			throw new IOException("Unknown format version:"+v);
		}
	}

}