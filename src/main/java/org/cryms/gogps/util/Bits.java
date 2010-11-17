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
package org.cryms.gogps.util;

public class Bits {

//	public static void main(String args[]){
//		
//		System.out.println(bitsToInt(new boolean[]{true,false,true}) + " " + bitsToInt(new boolean[]{true,false,true}));
//		System.out.println(bitsToInt(new boolean[]{true,false,false}) + " " + bitsToInt(new boolean[]{true,false,false}));
//		System.out.println(bitsToInt(new boolean[]{false,false,true}) + " " + bitsToInt(new boolean[]{false,false,true}));
//		
//	}
//	public static int bitsToInt(boolean[] bits) {
//		int result = 0;
//
//		for (int i = 0; i < bits.length; i++) {
//			if (bits[i]) {
//				result = result
//						+ (int) java.lang.Math.pow(2, (bits.length - i - 1));
//			}
//		}
//
//		return result;
//	}
	public static int bitsToUInt(boolean[] bits) {
		int result = 0;


		
		int pow2 = 1;
		for (int i = bits.length; i >= 0; i--) {
			if (bits[i]) {
				result = result + pow2 ;//(int) java.lang.Math.pow(2, (bits.length - i - 1));
			}
			pow2 = pow2 * 2;
		}

		return result;
	}
	/**
	 * convert bits to String
	 * 
	 * @param b
	 *            byte to convert
	 * 
	 * @return String of 0's and 1's
	 */
	public static String bitsToStr(boolean[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				result = result.concat("1");
			} else {
				result = result.concat("0");
			}
		}

		return result;
	}

	public static int bitsTwoComplement(boolean[] bits) {
		int result;

		if (!bits[0]) {
			// If the most significant bit are 0 then the integer is positive
			result = bitsToUInt(bits);
		} else {
			// If the most significant bit are 1 then the integer is negative
			// and the bits must be inverted and added 1 in order to get the
			// correct negative integer
			boolean[] b = new boolean[bits.length];
			for (int i = 0; i < bits.length; i++) {
				b[i] = !bits[i];
			}
			result = -(bitsToUInt(b) + 1);
		}

		return result;
	}

	/**
	 * convert a integer (byte) to a bit String
	 * 
	 * @param b
	 *            integer to convert, only the first byte are used
	 */
	public static String byteToStr(int b) {
		return bitsToStr(rollByteToBits(b));
	}

	/**
	 * compares two bit arrays for idendeical length and bits
	 * 
	 * @param b1
	 *            bit array to compare
	 * @param b2
	 *            bit array to compare
	 * 
	 * @return <code>true</code> if the bit arrays are identical,
	 *         <code>false</code> otherwise
	 */
	public static boolean compare(boolean[] b1, boolean[] b2) {
		// The length of the bit arrays is first compared
		boolean result = b1.length != 0 && (b1.length == b2.length);

		// Only bit arrays of equal length are further examined
		int i = 0;
		while (result && i < b1.length) {
			result = (b1[i] == b2[i]);
			i++;
		}

		return result;
	}

	/**
	 * concatinates two bit arrays into one new
	 * 
	 * @param b1
	 *            the first bit array. Data from here are the first in the new
	 *            array
	 * @paran b2 the second bit array
	 */
	public static boolean[] concat(boolean[] b1, boolean[] b2) {
		// As there is no check of nullity an exception will be thrown by the
		// JVM if one of the arrays is null
		boolean[] result = new boolean[b1.length + b2.length];
		for (int i = 0; i < b1.length; i++) {
			result[i] = b1[i];
		}
		for (int i = 0; i < b2.length; i++) {
			result[i + b1.length] = b2[i];
		}

		return result;
	}

	/**
	 * copies an entire bit array into a new bit array
	 * 
	 * @param b
	 *            the bit array to copy
	 */
	public static boolean[] copy(boolean[] b) {
		// Function just uses subset to copy
		return subset(b, 0, b.length);
	}

	/**
	 * convert a integer to bits
	 * 
	 * @param in
	 *            integer to convert
	 * @param length
	 *            how many bits to use, must be between 1 and 32
	 */

	public static boolean[] intToBits(int in, int length) {
		boolean[] result = new boolean[length];
		for (int i = 0; i < length; i++) {
			result[length - 1 - i] = (in % 2 == 1);
			in = in / 2;
		}
		return result;
	}

	/**
	 * convert a byte (given as an integer) to bits with all bits turned
	 * 
	 * @param in
	 *            integer to convert, only the first byte are used
	 */
	public static boolean[] rollByteToBits(int in) {
		boolean[] result = new boolean[8];
		for (int i = 7; i > -1; i--) {
			result[i] = (in % 2 == 1);
			in = in / 2;
		}

		// int ct = 10000000;
		// for (int i = 0; i < 8; i++) {
		// result[i] = (in / ct == 1);
		// ct /= 10;
		// }
		// System.out.println("" + Bits.subsetBin(result, 0, 8));
		return result;

	}

	/**
	 * copies a subset from a bit array into a new bit array
	 * 
	 * @param b
	 *            bit arrray to copy from
	 * @param start
	 *            the index to start from
	 * @param length
	 *            the length of the subset
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 *             if subset succseeds the original arrays length (not
	 *             decleraed)
	 */
	public static boolean[] subset(boolean[] b, int start, int length) {
		boolean[] result;

		if (start >= b.length || start + length > b.length) {
			// Exception is thrown if the index starts before 0, or succseeds
			// the
			// length of the original array
			result = null;
			throw new ArrayIndexOutOfBoundsException(
					"Invalid subset: Succseeds length of " + b.length
							+ ":\nstart of subset: " + start
							+ ", length of subset: " + length);
		} else {
			result = new boolean[length];
			for (int i = 0; i < length; i++) {
				result[i] = b[start + i];
			}
		}

		return result;
	}

	public static String subsetBin(boolean[] b, int start, int length) {
		String result = "b://";

		if (start >= b.length || start + length > b.length) {
			// Exception is thrown if the index starts before 0, or succseeds
			// the
			// length of the original array
			result = null;
			throw new ArrayIndexOutOfBoundsException(
					"Invalid subset: Succseeds length of " + b.length
							+ ":\nstart of subset: " + start
							+ ", length of subset: " + length);
		} else {
			for (int i = 0; i < length; i++) {
				if (b[start + i])
					result += "1";
				else
					result += "0";

			}
		}

		return result;
	}

	public static byte[] tobytes(boolean[] bits) {
		byte[] bytes = new byte[bits.length / 8];
		int indice = 0;
		for (int i = 0; i < bits.length / 8; i++) {
			bytes[i] = (byte) bitsToUInt(subset(bits, indice, 8));
			indice += 8;
		}
		return bytes;
	}

}
