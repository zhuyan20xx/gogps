/*
 * Copyright (c) 2010 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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

package org.gogpsproject.parser.skytraq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.Time;
import org.gogpsproject.util.Bits;
import org.gogpsproject.util.UnsignedOperation;


public class DecodeRAWMEAS {
	private InputStream in;
	private Observations o;
//	private int[] fdata;
//	private int[] fbits;
//	private boolean end = true;

	public DecodeRAWMEAS(InputStream in, Observations o) {
		this.in = in;
		this.o = o;
	}

	public Observations decode(int len) throws IOException, STQException {

		boolean[] bits = new boolean[8];
		int index = 0;
		boolean[] temp1 = Bits.intToBits(in.read(), 8);
		for (int i = 0; i < 8; i++) {
			bits[index] = temp1[i];
			index++;
		}

		int IOD = (int)Bits.bitsToUInt(bits);
		//System.out.println("IOD :  " + IOD + " S ");

		bits = new boolean[8];
		index = 0;
		temp1 = Bits.intToBits(in.read(), 8);
		for (int i = 0; i < 8; i++) {
			bits[index] = temp1[i];
			index++;
		}

		int NMEAS = (int)Bits.bitsToUInt(bits);
		//System.out.println("NMEAS :  " + NMEAS + "  ");

		int[] data = new int[len - 8];

		for (int i = 0; i < len - 8; i++) {
			data[i] = in.read();
			//System.out.print("0x" + Integer.toHexString(data[i]) + " ");
		}
		//System.out.println();

		int gpsCounter = 0;

		for (int k = 0; k < (len - 8) / 24; k++) {
//			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + k
//					+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

			ObservationSet os = new ObservationSet();

			int offset = k * 24;
			bits = new boolean[8 * 8]; // R8
			index = 0;
			for (int j = offset + 7; j >= 0 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[index] = temp1[i];
					index++;
				}
			}
			os.setPhase(ObservationSet.L1, UnsignedOperation.toDouble(Bits.tobytes(bits)));
//			System.out.print(k+"\tPhase: "
//					+ os.getPhase(ObservationSet.L1) + "  ");
			bits = new boolean[8 * 8]; // R8
			index = 0;
			for (int j = offset + 7 + 8; j >= 8 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[index] = temp1[i];
					index++;
				}
			}
			os.setCodeC(ObservationSet.L1, UnsignedOperation.toDouble(Bits.tobytes(bits)));
//			System.out.print(" Code: "
//					+ os.getCodeC(ObservationSet.L1) + "  ");
			bits = new boolean[8 * 4]; // R8
			index = 0;
			for (int j = offset + 7 + 8 + 4; j >= 8 + 8 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[index] = temp1[i];
					index++;
				}
			}
			os.setDoppler(ObservationSet.L1, UnsignedOperation.toFloat(Bits.tobytes(bits)));
//			System.out.print(" Doppler: "
//					+ os.getDoppler(ObservationSet.L1) + "  ");
			bits = new boolean[8];
			index = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[index] = temp1[i];
				index++;
			}
			os.setSatID((int)Bits.bitsToUInt(bits));
//			System.out.print (" SatID: "
//					+ os.getSatID() + "  ");


			bits = new boolean[8];
			index = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[index] = temp1[i];
				index++;
			}
//			System.out.print("Nav Measurements Quality Ind.: "
//					+ Bits.bitsTwoComplement(bits) + "  ");
//			System.out.print(" QI: "
//					+ Bits.bitsToInt(bits) + "  ");
			bits = new boolean[8];
			index = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[index] = temp1[i];
				index++;
			}

			os.setSignalStrength(ObservationSet.L1, Bits.bitsTwoComplement(bits));
//			System.out.print(" SNR: " // Signal strength C/No. (dbHz)
//					+ os.getSignalStrength(ObservationSet.L1) + "  ");
			bits = new boolean[8];
			index = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[index] = temp1[i];
				index++;
			}
//			System.out.println(" Lock: "//Loss of lock indicator (RINEX definition)
//					+ Bits.bitsToInt(bits) + "  ");
			int total = offset + 7 + 8 + 4 + 1 + 1 + 1 + 1;
			//System.out.println("Offset " + total);

			if (os.getSatID() <= 32) {
				os.setSatType('G');
				o.setGps(gpsCounter, os);
				gpsCounter++;
			}
		}

		return o;
	}

	private long getGMTTS(long tow, long week) {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT Time"));
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DAY_OF_MONTH, 6);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

//		c.add(Calendar.DATE, week*7);
//		c.add(Calendar.MILLISECOND, tow/1000*1000);

		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss.SSS");
		//System.out.println(sdf.format(c.getTime()));
		//ubx.log( (c.getTime().getTime())+" "+c.getTime()+" "+week+" "+tow+"\n\r");

		return c.getTimeInMillis() + week*7*24*3600*1000 + tow;
	}
}
