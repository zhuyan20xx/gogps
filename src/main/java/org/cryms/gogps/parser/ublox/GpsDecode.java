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

import java.io.IOException;
import java.io.InputStream;

import org.cryms.gogps.util.Bits;
import org.cryms.gogps.util.UnsignedOperation;


public class GpsDecode {
	//private boolean[] bits;
	InputStream in;
	RawData data = new RawData();

	int[] fdata;
	int[] fbits;
	boolean end = true;

	// public gpsDecode(boolean[] _bits){
	// bits=_bits;
	// }
	public GpsDecode(InputStream _in) {
		in = _in;
	}

	public void decode() throws IOException {
		// parse little Endian data
		boolean[] lengthbits;
		int[] length = new int[2];
		int[] data;
		boolean[] temp = new boolean[8]; // byte
		int index = 0;
		System.out.print("\nLength : \n");
		for (int i = 1; i >= 0; i--) {
			length[i] = in.read();
			System.out.print("0x" + Integer.toHexString(length[i]) + " ");
		}
		lengthbits = new boolean[length.length * 8];

		for (int i = 0; i < length.length; i++) {
			temp = Bits.intToBits(length[i], 8);
			for (int j = 0; j < temp.length; j++) {
				lengthbits[index] = temp[j];
				index++;
			}
		}
		int len = Bits.bitsToInt(lengthbits);
		System.out.println(" %%%%%%%%%% Length : " + len);
		data = new int[8];
		int[] datatmp = new int[8];
		System.out.print("\n Header ");
		for (int i = 0; i < 8; i++) {
			data[i] = in.read();
			datatmp[i] = data[i];
			System.out.print("0x" + Integer.toHexString(data[i]) + " ");
		}
		System.out.println();
		boolean[] bits = new boolean[8 * 4];
		int indice = 0;
		for (int j = 3; j >= 0; j--) {
			boolean[] temp1 = Bits.intToBits(data[j], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
		}
		System.out.println("Gps Time of week " + Bits.bitsTwoComplement(bits)
				+ " ms");
		bits = new boolean[8 * 2];
		indice = 0;
		for (int j = 5; j >= 4; j--) {
			boolean[] temp1 = Bits.intToBits(data[j], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
		}
		bits = new boolean[8];
		indice = 0;
		boolean[] temp1 = Bits.intToBits(data[6], 8);
		for (int i = 0; i < 8; i++) {
			bits[indice] = temp1[i];
			indice++;
		}

		System.out.println("Numsv :  " + Bits.bitsToInt(bits) + " S ");

		bits = new boolean[8];
		indice = 0;
		temp1 = Bits.intToBits(data[7], 8);
		for (int i = 0; i < 8; i++) {
			bits[indice] = temp1[i];
			indice++;
		}

		System.out.println("Res :  " + Bits.bitsToInt(bits) + "  ");

		data = new int[len - 8];

		for (int i = 0; i < len - 8; i++) {
			data[i] = in.read();
			System.out.print("0x" + Integer.toHexString(data[i]) + " ");
		}
		System.out.println();

		for (int k = 0; k < (len - 8) / 24; k++) {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + k
					+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			int offset = k * 24;
			bits = new boolean[8 * 8]; // R8
			indice = 0;
			for (int j = offset + 7; j >= 0 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[indice] = temp1[i];
					indice++;
				}
			}
			System.out.println("Carrier phase :  "
					+ UnsignedOperation.toDouble(Bits.tobytes(bits)) + "  ");
			bits = new boolean[8 * 8]; // R8
			indice = 0;
			for (int j = offset + 7 + 8; j >= 8 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[indice] = temp1[i];
					indice++;
				}
			}
			System.out.println("Pseudorange :  "
					+ UnsignedOperation.toDouble(Bits.tobytes(bits)) + "  ");
			bits = new boolean[8 * 4]; // R8
			indice = 0;
			for (int j = offset + 7 + 8 + 4; j >= 8 + 8 + offset; j--) {
				temp1 = Bits.intToBits(data[j], 8);
				for (int i = 0; i < 8; i++) {
					bits[indice] = temp1[i];
					indice++;
				}
			}
			System.out.println("Doppler measurement :  "
					+ UnsignedOperation.toFloat(Bits.tobytes(bits)) + "  ");
			bits = new boolean[8];
			indice = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
			System.out.println("Space Vehicle Number :  "
					+ Bits.bitsToInt(bits) + "  ");
			bits = new boolean[8];
			indice = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
			System.out.println("Space Vehicle Number :  "
					+ Bits.bitsTwoComplement(bits) + "  ");
			System.out.println("Nav Measurements Quality:  "
					+ Bits.bitsToInt(bits) + "  ");
			bits = new boolean[8];
			indice = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
			System.out.println("Signal strength C/No. (dbHz) :  "
					+ Bits.bitsTwoComplement(bits) + "  ");
			bits = new boolean[8];
			indice = 0;
			temp1 = Bits.intToBits(data[offset + 7 + 8 + 4 + 1 + 1 + 1 + 1], 8);
			for (int i = 0; i < 8; i++) {
				bits[indice] = temp1[i];
				indice++;
			}
			System.out.println("Loss of lock indicator (RINEX definition) :  "
					+ Bits.bitsToInt(bits) + "  ");
			int total = offset + 7 + 8 + 4 + 1 + 1 + 1 + 1;
			System.out.println("Offset " + total);

		}
		// / Checksum
		int CH_A = 0;
		int CH_B = 0;
		CH_A += 0x02;
		CH_B += CH_A;

		CH_A += 0x10;
		CH_B += CH_A;
		CH_A += length[0];
		CH_B += CH_A;
		CH_A += length[1];
		CH_B += CH_A;
		for (int l = datatmp.length - 1; l >= 0; l--) {
			CH_A += datatmp[l];
			CH_B += CH_A;
		}
		for (int l = len - 8 - 1; l >= 0; l--) {
			CH_A += data[l];
			CH_B += CH_A;
		}
		CH_A = CH_A & 0xFF;
		CH_B = CH_B & 0xFF;
		System.out.println("CH_A cal " + Integer.toHexString(CH_A)
				+ " CH_K packetto " + Integer.toHexString(in.read()));
		System.out.println("CH_B cal " + Integer.toHexString(CH_B)
				+ " CH_K packetto " + Integer.toHexString(in.read()));
	}
}
