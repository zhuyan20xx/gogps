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

import org.cryms.gogps.util.Bits;

public class Decode1007Msg implements Decode {



	public Decode1007Msg() {
		
	}

	public void decode(boolean[] bits, long referenceTS) {
		AntennaDescriptor antenna = new AntennaDescriptor();
		int start = 12;
		String desc = "";
		antenna.setStationID(Bits.bitsToInt(Bits.subset(bits, start, 12)));
		start += 12;
		antenna.setDecriptorCounterN(Bits.bitsToInt(Bits.subset(bits, start, 8)));
		start += 8;
		for (int i = 0; i < antenna.getDecriptorCounterN(); i++) {
			char value = (char) Bits.bitsToInt(Bits.subset(bits, start, 8));
			desc += Character.toString(value);
			start += 8;
		}
		antenna.setAntennaDescriptor(desc);
		antenna.setSetupID(Bits.bitsToInt(Bits.subset(bits, start, 8)));
		start += 8;
		//System.out.println(antenna);
		//System.out.println(start);
	}

}
