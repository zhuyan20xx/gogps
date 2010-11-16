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
import java.util.Arrays;

import org.cryms.gogps.util.Bits;


public class Decode1012Msg implements Decode {
	public Decode1012Msg() {
		
	}
	public void decode(boolean[] bits) {
		int start = 12;
		GlonassHeader glonassh = new GlonassHeader();
		GlonassSatellite satellite = new GlonassSatellite();
		
		glonassh.setStationid(Bits.bitsToInt(Bits.subset(bits, start, 12))); // 12
		start += 12;
		glonassh.setEpochTime(Bits.bitsToInt(Bits.subset(bits, start, 27))); // 27
		start += 27;
		glonassh.setFlag(Bits.bitsToInt(Bits.subset(bits, start, 1))); // 1
		start++;
		glonassh.setNumberOfSatellites(Bits.bitsToInt(Bits.subset(bits, start,
				5))); // 5
		start += 5;
		glonassh
				.setSmoothIndicator(Bits.bitsToInt(Bits.subset(bits, start, 1))); // 1
		start += 1;
		glonassh.setSmoothInterval(Bits.bitsToInt(Bits.subset(bits, start, 3))); // 3
		start += 3;
		//System.out.println(glonassh);
		for (int i = 0; i < glonassh.getNumberOfSatellites(); i++) {
			satellite.setSatID(Bits.bitsToInt(Bits.subset(bits, start, 6)));
			start += 6;
			satellite.setL1code(Bits.bitsToInt(Bits.subset(bits, start, 1)));
			start += 1;
			satellite.setSatFrequency(Bits.bitsToInt(Bits.subset(bits, start, 5)));
			start += 5;
			satellite.setL1pseudorange(Bits.bitsTwoComplement(Bits.subset(bits,
					start, 25)));
			start += 25;
			satellite.setL1phaserange(Bits.bitsToInt(Bits.subset(bits, start, 20)));
			start += 20;
			satellite.setL1locktime(Bits.bitsTwoComplement(Bits.subset(bits, start,
					7)));
			start += 7;
			satellite.setL1psedorangemod(Bits
					.bitsToInt(Bits.subset(bits, start, 7)));
			start += 7;
			satellite.setL1CNR(Bits.bitsToInt(Bits.subset(bits, start, 8)));
			start += 8;
			satellite.setL2code(Bits.bitsToInt(Bits.subset(bits, start, 2)));
			start += 2;
			satellite.setL2l1psedorangeDif(Bits.bitsTwoComplement(Bits.subset(bits,
					start, 14)));
			start += 14;
			satellite.setL2l1phaserangeDif(Bits.bitsTwoComplement(Bits.subset(bits,
					start, 20)));
			start += 20;
			satellite.setL2locktime(Bits.bitsToInt(Bits.subset(bits, start, 7)));
			start += 7;
			satellite.setL2CNR(Bits.bitsToInt(Bits.subset(bits, start, 8)));
			start += 8;
		}
	}


//	public String toString() {
//		return "Decode1012Msg [bits=" + Arrays.toString(bits) + ", glonassh=" + glonassh + ", start=" + start
//				+ "]";
//	}
}
