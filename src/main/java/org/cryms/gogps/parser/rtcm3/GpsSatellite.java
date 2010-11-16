/*
 * Copyright (c) 2010, Cryms.com . All Rights Reserved.
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

package org.cryms.gogps.parser.rtcm3;
public class GpsSatellite {

	// GPS Satellite ID DF009 uint6 6
	// GPS L1 Code Indicator DF010 bit(1) 1
	// GPS L1 Pseudorange DF011 uint24 24
	// GPS L1 PhaseRange – L1 Pseudorange DF012 int20 20
	// GPS L1 Lock time Indicator DF013 uint7 7
	// GPS Integer L1 Pseudorange Modulus
	// Ambiguity
	// DF014 uint8 8
	// GPS L1 CNR DF015 uint8 8
	// GPS L2 Code Indicator DF016 bit(2) 2
	// GPS L2-L1 Pseudorange Difference DF017 int14 14
	// GPS L2 PhaseRange – L1 Pseudorange DF018 int20 20
	// GPS L2 Lock time Indicator DF019 uint7 7
	// GPS L2 CNR DF020 uint8 8
	// TOTAL 125
	private int satID;
	private int l1code;
	private int l1pseudorange;
	private double l1phaserange;
	private int l1locktime;
	private int l1psedorangemod;
	private int l1CNR;
	private int l2code;
	private double l2l1psedorangeDif;
	private double l2l1phaserangeDif;
	private int l2locktime;
	private int l2CNR;

	public int getL1CNR() {
		return l1CNR;
	}

	public int getL1code() {
		return l1code;
	}

	public int getL1locktime() {
		return l1locktime;
	}

	public double getL1phaserange() {
		return l1phaserange;
	}

	public int getL1psedorangemod() {
		return l1psedorangemod;
	}

	public int getL1pseudorange() {
		return l1pseudorange;
	}

	public int getL2CNR() {
		return l2CNR;
	}

	public int getL2code() {
		return l2code;
	}

	public double getL2l1phaserangeDif() {
		return l2l1phaserangeDif;
	}

	public double getL2l1psedorangeDif() {
		return l2l1psedorangeDif;
	}

	public int getL2locktime() {
		return l2locktime;
	}

	public int getSatID() {
		return satID;
	}

	public void setL1CNR(int l1CNR) {
		this.l1CNR = l1CNR;
	}

	public void setL1code(int l1code) {
		this.l1code = l1code;
	}

	public void setL1locktime(int l1locktime) {
		this.l1locktime = l1locktime;
	}

	public void setL1phaserange(double l1phaserange) {
		this.l1phaserange = l1phaserange;
	}

	public void setL1psedorangemod(int l1psedorangemod) {
		this.l1psedorangemod = l1psedorangemod;
	}

	public void setL1pseudorange(int l1pseudorange) {
		this.l1pseudorange = l1pseudorange;
	}

	public void setL2CNR(int l2CNR) {
		this.l2CNR = l2CNR;
	}

	public void setL2code(int l2code) {
		this.l2code = l2code;
	}

	public void setL2l1phaserangeDif(double l2l1phaserangeDif) {
		this.l2l1phaserangeDif = l2l1phaserangeDif;
	}

	public void setL2l1psedorangeDif(double l2l1psedorangeDif) {
		this.l2l1psedorangeDif = l2l1psedorangeDif;
	}

	public void setL2locktime(int l2locktime) {
		this.l2locktime = l2locktime;
	}

	public void setSatID(int satID) {
		this.satID = satID;
	}

	@Override
	public String toString() {
		return "Satellite1004 [l1CNR=" + l1CNR + ", l1code=" + l1code
				+ ", l1locktime=" + l1locktime + ", l1phaserange="
				+ l1phaserange + ", l1psedorangemod=" + l1psedorangemod
				+ ", l1pseudorange=" + l1pseudorange + ", l2CNR=" + l2CNR
				+ ", l2code=" + l2code + ", l2l1phaserangeDif="
				+ l2l1phaserangeDif + ", l2l1psedorangeDif="
				+ l2l1psedorangeDif + ", l2locktime=" + l2locktime + ", satID="
				+ satID + "]";
	}

}
