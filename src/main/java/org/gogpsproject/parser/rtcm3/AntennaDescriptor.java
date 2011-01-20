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

package org.gogpsproject.parser.rtcm3;
public class AntennaDescriptor {

	private int stationID;
	private int decriptorCounterN;
	private String antennaDescriptor;
	private int setupID;

	public String getAntennaDescriptor() {
		return antennaDescriptor;
	}

	public int getDecriptorCounterN() {
		return decriptorCounterN;
	}

	public int getSetupID() {
		return setupID;
	}

	public int getStationID() {
		return stationID;
	}

	public void setAntennaDescriptor(String antennaDescriptor) {
		this.antennaDescriptor = antennaDescriptor;
	}

	public void setDecriptorCounterN(int decriptorCounterN) {
		this.decriptorCounterN = decriptorCounterN;
	}

	public void setSetupID(int setupID) {
		this.setupID = setupID;
	}

	public void setStationID(int stationID) {
		this.stationID = stationID;
	}

	@Override
	public String toString() {
		return "AntennaDescriptor [antennaDescriptor=" + antennaDescriptor
				+ ", decriptorCounterN=" + decriptorCounterN + ", setupID="
				+ setupID + ", stationID=" + stationID + "]";
	}

}
