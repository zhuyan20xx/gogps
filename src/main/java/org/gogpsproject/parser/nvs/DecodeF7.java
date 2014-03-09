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
		
		
		if (satType == 1){ 
		
				System.out.println("GPS PRN: " + satId);  
			
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
		        
		        
		        /*  Omega0, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double omega0 = Bits.byteToIEEE754Double(bytes);
				System.out.println("Omega0: "+ omega0);
		        
				
				/*  Cis, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longCis = Bits.byteToLong(bytes);
				String binCis = Long.toBinaryString(longCis);
				binCis = String.format("%32s",binCis).replace(' ', '0');
				
				signStr = binCis.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binCis.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binCis.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double cis = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Cis: "+ cis);
		
		        
		        /*  I0, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double i0 = Bits.byteToIEEE754Double(bytes);
				System.out.println("I0: "+ i0);
		
				
				/*  Crc, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longCrc = Bits.byteToLong(bytes);
				String binCrc = Long.toBinaryString(longCrc);
				binCrc = String.format("%32s",binCrc).replace(' ', '0');
				
				signStr = binCrc.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binCrc.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binCrc.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double crc = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Crc: "+ crc);
		        
		        
		        /*  W, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double w = Bits.byteToIEEE754Double(bytes);
				System.out.println("W: "+ w);
				
				
				/*  OmegaR, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double omegaR = Bits.byteToIEEE754Double(bytes);
				System.out.println("OmegaR: "+ omegaR);
		        
				
				/*  IDOT, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double idot = Bits.byteToIEEE754Double(bytes);
				System.out.println("IDOT: "+ idot);
				
				
				/*  Tgd, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longTgd = Bits.byteToLong(bytes);
				String binTgd = Long.toBinaryString(longTgd);
				binTgd = String.format("%32s",binTgd).replace(' ', '0');
				
				signStr = binTgd.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binTgd.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binTgd.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double tgd = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Tgd: "+ tgd);
		        
		        
		        /*  Toc, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double toc = Bits.byteToIEEE754Double(bytes);
				System.out.println("toc: "+ toc);
		        
				
				/*  Af2, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longAf2 = Bits.byteToLong(bytes);
				String binAf2 = Long.toBinaryString(longAf2);
				binAf2 = String.format("%32s",binAf2).replace(' ', '0');
				
				signStr = binAf2.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binAf2.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binAf2.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double af2 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Af2: "+ af2);
				
		        
		        /*  Af1, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longAf1 = Bits.byteToLong(bytes);
				String binAf1 = Long.toBinaryString(longAf1);
				binAf2 = String.format("%32s",binAf1).replace(' ', '0');
				
				signStr = binAf1.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binAf1.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binAf1.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double af1 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Af1: "+ af1);
		        
		        
		        /*  Af0, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				long longAf0 = Bits.byteToLong(bytes);
				String binAf0 = Long.toBinaryString(longAf0);
				binAf0 = String.format("%32s",binAf0).replace(' ', '0');
				
				signStr = binAf0.substring(0,1);
		        signInt = Integer.parseInt(signStr, 2);
		        espStr = binAf0.substring(1,9);
		        espInt = Integer.parseInt(espStr, 2);
		        mantStr = binAf0.substring(9);
		        mantInt = Integer.parseInt(mantStr, 2);
		        mantInt2 = mantInt / Math.pow(2, 23);
		        double af0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Af0: "+ af0);
        
		        
		        /*  URA(svaccur), 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longURA = Bits.byteToLong(bytes);
				System.out.println("URA: " + longURA);
				
				
				/*  IODE, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longIODE = Bits.byteToLong(bytes);
				System.out.println("IODE: " + longIODE);
				
				
				/*  IODC, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longIODC = Bits.byteToLong(bytes);
				System.out.println("IODC: " + longIODC);
				
				
				/*  CodeL2, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longCodeL2 = Bits.byteToLong(bytes);
				System.out.println("CodeL2: " + longCodeL2);
				
				
				/*  L2_Pdata_flag, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longL2flag = Bits.byteToLong(bytes);
				System.out.println("L2_Pdata_flag: " + longL2flag);
				
				
				/*  WeekN, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long longWeekN = Bits.byteToLong(bytes);
				System.out.println("WeekN: " + longWeekN);
				System.out.println("+-----------------------------------+");
						        
        
		}else{ 

				System.out.println("GLONASS PRN: " + satId);

		}
        

			return null;
	}


}
