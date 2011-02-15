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

import org.gogpsproject.util.Bits;

public class Decode1005Msg implements Decode {



	public Decode1005Msg() {

	}


	public void decode(boolean[] bits, long referenceTS) {
		int start = 12;
		//System.out.println("Debug : Decode 1005");
		StationaryAntenna stationaryantenne = new StationaryAntenna();

		stationaryantenne.setStationID(Bits.bitsToUInt(Bits.subset(bits, start,
				12)));
		start += 12;
		stationaryantenne.setItrl(Bits.bitsToUInt(Bits.subset(bits, start, 6)));
		start += 6;
		stationaryantenne.setGpsIndicator(Bits.bitsToUInt(Bits.subset(bits,
				start, 1)));
		start += 1;
		stationaryantenne.setGlonassIndicator(Bits.bitsToUInt(Bits.subset(bits,
				start, 1)));
		start += 1;
		stationaryantenne.setRgalileoIndicator(Bits.bitsToUInt(Bits.subset(bits,
				start, 1)));
		start += 1;
		stationaryantenne.setRstationIndicator(Bits.bitsToUInt(Bits.subset(bits,
				start, 1)));
		start += 1;
		stationaryantenne.setAntennaRefPointX(Bits.bitsTwoComplement(Bits
				.subset(bits, start, 38)));
		start += 38;
		stationaryantenne.setSreceiverOscillator(Bits.bitsToUInt(Bits.subset(
				bits, start, 1)));
		start += 1;
		stationaryantenne.setReserved1(Bits.bitsToUInt(Bits.subset(bits, start,
				1)));
		start += 1;
		stationaryantenne.setAntennaRefPointY(Bits.bitsTwoComplement(Bits
				.subset(bits, start, 38)));
		start += 38;
		stationaryantenne.setReserved2(Bits.bitsToUInt(Bits.subset(bits, start,
				2)));
		start += 2;
		stationaryantenne.setAntennaRefPointZ(Bits.bitsTwoComplement(Bits
				.subset(bits, start, 38)));
		start += 38;
		//System.out.println(stationaryantenne);
		//System.out.println("Debug length: " + start);
	}

}
