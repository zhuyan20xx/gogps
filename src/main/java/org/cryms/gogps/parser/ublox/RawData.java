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

package org.cryms.gogps.parser.ublox;

public class RawData {

	// u-blox protocole ** RXM RAW frame

	private int itow; // 4 bytes;
	private int week; // 2 bytes
	private int numsv; // unsigned char 1 byte
	private int res1; // unsigned char 1 byte
	private double cpMes; // IEEE 754 Double precision 8 bytes
	private double prMes; // IEEE 754 Double precision 8 bytes
	private double doMess; // IEEE 754 Single precision 4 bytes
	private int sv; // 1 byte space vehicule number
	private int mesqi; // signed 1 byte nav measurements quality
	private int cno; // signed 1 byte signal strength
	private int lli; // unsigned 1 byte loss of lock indicator

	public int getCno() {
		return cno;
	}

	public double getCpMes() {
		return cpMes;
	}

	public double getDoMess() {
		return doMess;
	}

	public int getItow() {
		return itow;
	}

	public int getLli() {
		return lli;
	}

	public int getMesqi() {
		return mesqi;
	}

	public int getNumsv() {
		return numsv;
	}

	public double getPrMes() {
		return prMes;
	}

	public int getRes1() {
		return res1;
	}

	public int getSv() {
		return sv;
	}

	public int getWeek() {
		return week;
	}

	public void setCno(int cno) {
		this.cno = cno;
	}

	public void setCpMes(double cpMes) {
		this.cpMes = cpMes;
	}

	public void setDoMess(double doMess) {
		this.doMess = doMess;
	}

	public void setItow(int itow) {
		this.itow = itow;
	}

	public void setLli(int lli) {
		this.lli = lli;
	}

	public void setMesqi(int mesqi) {
		this.mesqi = mesqi;
	}

	public void setNumsv(int numsv) {
		this.numsv = numsv;
	}

	public void setPrMes(double prMes) {
		this.prMes = prMes;
	}

	public void setRes1(int res1) {
		this.res1 = res1;
	}

	public void setSv(int sv) {
		this.sv = sv;
	}

	public void setWeek(int week) {
		this.week = week;
	}

}
