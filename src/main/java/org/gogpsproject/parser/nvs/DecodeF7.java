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

package org.gogpsproject.parser.nvs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.gogpsproject.EphGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.Time;
import org.gogpsproject.util.Bits;
import org.gogpsproject.util.UnsignedOperation;


public class DecodeF7 {

	private final static double TWO_P_4   = 16.0; // 2^4
	private final static double TWO_P_M5  = 0.03125; // 2^-5
	private final static double TWO_P_M19 = 0.0000019073486328125; // 2^-19
	private final static double TWO_P_M29 = 0.00000000186264514923095703125; // 2^-29
	private final static double TWO_P_M31 = 0.0000000004656612873077392578125; // 2^-31
	private final static double TWO_P_M33 = 1.16415321826934814453125e-10; // 2^-33
	private final static double TWO_P_M43 = 1.136868377216160297393798828125e-13; // 2^-43
	private final static double TWO_P_M55 = 2.7755575615628913510590791702271e-17; // 2^-55
	private final static double PI        = 3.141592653589793238462643383279502884197169399375105820974944592;

	InputStream in;

	public DecodeF7(InputStream _in) {
		in = _in;
	}

	public EphGps decode() throws IOException,NVSException {
		// parse little Endian data


		EphGps eph = new EphGps();
		int satType = in.read();  // satType 1 = GPS, 2 = GLONASS		
		System.out.println("satType: " + satType);  
		int satId = in.read();
		System.out.println("svid: " + satId);  
		
		eph.setSatID((int)satId);

		byte bytes[];
		int signInt;
		String signStr; 
		int espInt;
		String espStr;
		long mantInt;
		String mantStr; 
		double mantInt2;
		double crs;
		
			
		/*  Crs, 4 bytes  */		
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		long longCrs = Bits.byteToLong(bytes);
		//System.out.println("longCrs: "+ longCrs);
		String binCrs = Long.toBinaryString(longCrs);
		binCrs = String.format("%32s",binCrs).replace(' ', '0');
		//System.out.println("binCrs: "+ binCrs);
				
		signStr = binCrs.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCrs.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCrs.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = (double) (mantInt / Math.pow(2, 23));
        crs = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Crs: "+ crs);
	
        
        /*  Dn, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		long longDn = Bits.byteToLong(bytes);
		String binDn = Long.toBinaryString(longDn);
		binDn = String.format("%32s",binDn).replace(' ', '0');
		
		signStr = binDn.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binDn.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binDn.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double dn = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Dn: "+ dn);
   
        
        /*  M0, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double m0 = Bits.byteToIEEE754Double(bytes);
		System.out.println("M0: "+ m0);		
        
        /*  Cuc, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		long longCuc = Bits.byteToLong(bytes);
		String binCuc = Long.toBinaryString(longCuc);
		binCuc = String.format("%32s",binCuc).replace(' ', '0');
		
		signStr = binCuc.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCuc.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCuc.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cuc = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cuc: "+ cuc);
        
        
        /*  E, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);	
		double e = Bits.byteToIEEE754Double(bytes);
		System.out.println("E: "+ e);		
        
        /*  Cus, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		long longCus = Bits.byteToLong(bytes);
		String binCus = Long.toBinaryString(longCus);
		binCus = String.format("%32s",binCus).replace(' ', '0');
		
		signStr = binCus.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCus.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCus.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cus = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cus: "+ cus);
        
        
        /*  SqrtA, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double sqrtA = Bits.byteToIEEE754Double(bytes);
		System.out.println("SqrtA: "+ sqrtA);
	
        
        /*  Toe, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double toe = Bits.byteToIEEE754Double(bytes);
		System.out.println("Toe: "+ toe);
		  
        
        /*  Cic, 4 bytes  */
		bytes = new byte[4];
		in.read(bytes, 0, bytes.length);
		long longCic = Bits.byteToLong(bytes);
		String binCic = Long.toBinaryString(longCic);
		binCic = String.format("%32s",binCic).replace(' ', '0');
		
		signStr = binCic.substring(0,1);
        signInt = Integer.parseInt(signStr, 2);
        espStr = binCic.substring(1,9);
        espInt = Integer.parseInt(espStr, 2);
        mantStr = binCic.substring(9);
        mantInt = Integer.parseInt(mantStr, 2);
        mantInt2 = mantInt / Math.pow(2, 23);
        double cic = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
        System.out.println("Cic: "+ cic);
        
        
        
        
        
		/*
		byte bits[] = new byte[4];
		in.read(bits, 0, bits.length);
		double crs = Bits.byteToIEEE754Double(bits);
		System.out.println("CRS: " + crs );
		*/
		
//		long svid=0;
//		for(int i=3;i>=0;i--){
//			svid=svid<<8;
//			svid = svid | Bits.getUInt(bytes[i]);
//
//		}

		//byte[] bytes = new byte[1];
		//in.read(bytes, 0, bytes.length);
	//	int[] data;
		
	/*	
		int crs = in.read();


		boolean[] bits = new boolean[8];
		int indice = 0;
		boolean[] temp1 = Bits.intToBits(crs, 8);

	
		for (int i = 0; i < 8; i++) {
			bits[indice] = temp1[i];
			indice++;
		}
		int numSV = (int)Bits.bitsTwoComplement(bits);
		System.out.println("NumSV :  " + numSV + " S ");
		*/
//		System.out.println("crs: " + crs);  
		//float crs_bit = Bits.intToBits(crs, 8);
		//System.out.print("crs "+ crs_bit);
//		String binary = Integer.toBinaryString( crs );
//		System.out.println( binary );
		
		
//		System.out.print("svid "+svid);
//		baos.write(bytes);

//		System.out.println("IODC("+eph.getIodc()+"), IODE2("+IODE2+"), IODE3("+IODE3+") not matching for SVID "+svid);

		return null;
	}


}
