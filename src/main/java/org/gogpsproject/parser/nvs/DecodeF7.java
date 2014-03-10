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
			
			
				/*  Crs, 4 bytes  */		
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float crs = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Crs: "+ crs);		
		        eph.setCrs(crs);		        
		        
		        /*  deltaN, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float deltaN = Bits.byteToIEEE754Float(bytes);
		        System.out.println("deltaN: "+ deltaN);	   
		        eph.setDeltaN(deltaN);		        
		        
		        /*  M0, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double m0 = Bits.byteToIEEE754Double(bytes);
				System.out.println("M0: "+ m0);			        
				eph.setM0(m0);
								
		        /*  Cuc, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float cuc = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Cuc: "+ cuc);	        
		        eph.setCuc(cuc);
		        
		        /*  E, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);	
				double e = Bits.byteToIEEE754Double(bytes);
				System.out.println("E: "+ e);				
				eph.setE(e);   
							
		        /*  Cus, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float cus = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Cus: "+ cus);        
		        eph.setCus(cus);
		        		        
		        /*  SqrtA, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double rootA = Bits.byteToIEEE754Double(bytes);
				System.out.println("SqrtA: "+ rootA);		
				eph.setRootA(rootA);
					        
		        /*  Toe, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double toe = Bits.byteToIEEE754Double(bytes);
				System.out.println("Toe: "+ toe);			
				eph.setToe(toe);
				  	        
		        /*  Cic, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float cic = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Cic: "+ cic);        
		        eph.setCic(cic);	        
		        
		        /*  Omega0, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double omega0 = Bits.byteToIEEE754Double(bytes);
				System.out.println("Omega0: "+ omega0);			
				eph.setOmega0(omega0);	        
				
				/*  Cis, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float cis = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Cis: "+ cis);
		        eph.setCis(cis);	        
		        
		        /*  I0, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double i0 = Bits.byteToIEEE754Double(bytes);
				System.out.println("I0: "+ i0);			
				eph.setI0(i0);
				
				/*  Crc, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float crc = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Crc: "+ crc);  
		        eph.setCrc(crc);	   
		        
		        /*  W, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double omega = Bits.byteToIEEE754Double(bytes);
				System.out.println("W: "+ omega);	
				eph.setOmega(omega);
				
				/*  OmegaR(OmegaDot), 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double omegaDot = Bits.byteToIEEE754Double(bytes);
				System.out.println("OmegaR: "+ omegaDot);				
				eph.setOmegaDot(omegaDot);
				
				/*  IDOT, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double iDot = Bits.byteToIEEE754Double(bytes);
				System.out.println("IDOT: "+ iDot);	
				eph.setiDot(iDot);	
				
				/*  Tgd, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				float tgd = Bits.byteToIEEE754Float(bytes);
		        System.out.println("Tgd: "+ tgd);
		        eph.setTgd(tgd);        
		        
		        /*  Toc, 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double toc = Bits.byteToIEEE754Double(bytes);
				System.out.println("toc: "+ toc);		
				eph.setToc(toc);
		        
				
				/*  Af2, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
//				float af2 = Bits.byteToIEEE754Float(bytes);
								
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
		        
		        eph.setAf2(af2);
				
		        
		        /*  Af1, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				
				float af1 = Bits.byteToIEEE754Float(bytes);

//				long longAf1 = Bits.byteToLong(bytes);
//				String binAf1 = Long.toBinaryString(longAf1);
//				binAf1 = String.format("%32s",binAf1).replace(' ', '0');
//				
//				signStr = binAf1.substring(0,1);
//		        signInt = Integer.parseInt(signStr, 2);
//		        espStr = binAf1.substring(1,9);
//		        espInt = Integer.parseInt(espStr, 2);
//		        mantStr = binAf1.substring(9);
//		        mantInt = Integer.parseInt(mantStr, 2);
//		        mantInt2 = mantInt / Math.pow(2, 23);
//		        double af1 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Af1: "+ af1);
		        
		        eph.setAf1(af1);
		        
		        
		        /*  Af0, 4 bytes  */
				bytes = new byte[4];
				in.read(bytes, 0, bytes.length);
				
				float af0 = Bits.byteToIEEE754Float(bytes);
//		
//				long longAf0 = Bits.byteToLong(bytes);
//				String binAf0 = Long.toBinaryString(longAf0);
//				binAf0 = String.format("%32s",binAf0).replace(' ', '0');
//				
//				signStr = binAf0.substring(0,1);
//		        signInt = Integer.parseInt(signStr, 2);
//		        espStr = binAf0.substring(1,9);
//		        espInt = Integer.parseInt(espStr, 2);
//		        mantStr = binAf0.substring(9);
//		        mantInt = Integer.parseInt(mantStr, 2);
//		        mantInt2 = mantInt / Math.pow(2, 23);
//		        double af0 = (double) (Math.pow(-1, signInt) * Math.pow(2, (espInt - 127)) * (1 + mantInt2));  //FP32
		        System.out.println("Af0: "+ af0);
        
		        eph.setAf0(af0);
		        
		        
		        /*  URA(svaccur), 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long svAccur = Bits.byteToLong(bytes);
				System.out.println("URA: " + svAccur);
				
				eph.setSvAccur((int)svAccur);
				
				
				/*  IODE, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long iode = Bits.byteToLong(bytes);
				System.out.println("IODE: " + iode);
				
				eph.setSvAccur((int)iode);
				
				
				/*  IODC, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long iodc = Bits.byteToLong(bytes);
				System.out.println("IODC: " + iodc);
				
				eph.setIodc((int)iodc);
				
				
				/*  CodeL2, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long l2Code = Bits.byteToLong(bytes);
				System.out.println("CodeL2: " + l2Code);
				
				eph.setL2Code((int)l2Code);
				
				
				/*  L2_Pdata_flag, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long l2Flag = Bits.byteToLong(bytes);
				System.out.println("L2_Pdata_flag: " + l2Flag);
				
				eph.setL2Flag((int)l2Flag);
				
				
				/*  WeekN, 2 bytes  */
				bytes = new byte[2];
				in.read(bytes, 0, bytes.length);
				long week = Bits.byteToLong(bytes);
				System.out.println("WeekN: " + week);
				
				eph.setWeek((int)week);
				
				System.out.println("+-----------------------------------+");
						        
        
		}else{ 

				System.out.println("GLONASS PRN: " + satId);

		}
        

			return null;
	}


}
