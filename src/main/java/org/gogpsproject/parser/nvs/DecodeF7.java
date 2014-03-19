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


		int[] data;
		boolean[] bits;
		int indice;
		boolean[] temp1;
		
		EphGps eph = new EphGps();
		int satType = in.read();  // satType 1 = GPS, 2 = GLONASS		
		int satId = in.read();
		
		
		if (satType == 1){   // GPS: 138 bytes 
		
				data = new int[138];
				
				for (int i = 0; i < 138 ; i++) {
					data[i] = in.read();
				}
			
				eph.setSatID((int)satId);

								
				/*  Crs, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 3; j >= 0; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float crs = UnsignedOperation.toFloat(Bits.tobytes(bits));
		        eph.setCrs(crs);		        
				
				/*  deltaN, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 7; j >= 4; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float deltaN = UnsignedOperation.toFloat(Bits.tobytes(bits));
		        eph.setDeltaN(deltaN);		        
				
				/*  M0, 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 15; j >= 8; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double m0 = UnsignedOperation.toDouble(Bits.tobytes(bits));
				eph.setM0(m0);
			
				/*  Cuc, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 19; j >= 16; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float cuc = UnsignedOperation.toFloat(Bits.tobytes(bits));
		        eph.setCuc(cuc);

				  
		        /*  E, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 27; j >= 20; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double e = UnsignedOperation.toDouble(Bits.tobytes(bits));
				eph.setE(e);   
				
				/*  Cus, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 31; j >= 28; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float cus = UnsignedOperation.toFloat(Bits.tobytes(bits));
		        eph.setCus(cus);
	  	        		        
		        /*  SqrtA, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 39; j >= 32; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double rootA = UnsignedOperation.toDouble(Bits.tobytes(bits));	      		 
				eph.setRootA(rootA);
					        
		        /*  Toe, 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 47; j >= 40; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double toe = UnsignedOperation.toDouble(Bits.tobytes(bits));	      		 				
				eph.setToe(toe);
							  	        
		        /*  Cic, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 51; j >= 48; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float cic = UnsignedOperation.toFloat(Bits.tobytes(bits));	      		 							
		        eph.setCic(cic);	
		           	        
		        /*  Omega0, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 59; j >= 52; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double omega0 = UnsignedOperation.toDouble(Bits.tobytes(bits));	 		        
				eph.setOmega0(omega0);	
								
				/*  Cis, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 63; j >= 60; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float cis = UnsignedOperation.toFloat(Bits.tobytes(bits));								
		        eph.setCis(cis);	        
	        
		        /*  I0, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 71; j >= 64; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double i0 = UnsignedOperation.toDouble(Bits.tobytes(bits));	
		        eph.setI0(i0);
				
				/*  Crc, 4 bytes  */
		        bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 75; j >= 72; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float crc = UnsignedOperation.toFloat(Bits.tobytes(bits));								 
		        eph.setCrc(crc);	   
		        
		        /*  W, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 83; j >= 76; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double omega = UnsignedOperation.toDouble(Bits.tobytes(bits));	      
				eph.setOmega(omega);
				
				/*  OmegaR(OmegaDot), 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
					indice = 0;
					for (int j = 91; j >= 84; j--) {
						temp1 = Bits.intToBits(data[j], 8);
						for (int i = 0; i < 8; i++) {
							bits[indice] = temp1[i];
							indice++;
						}
					}
				double omegaDot = UnsignedOperation.toDouble(Bits.tobytes(bits));	      			
				eph.setOmegaDot(omegaDot);
							
				/*  IDOT, 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 99; j >= 92; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double iDot = UnsignedOperation.toDouble(Bits.tobytes(bits));	 				
				eph.setiDot(iDot);	
				
				/*  Tgd, 4 bytes  */
				bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 103; j >= 100; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float tgd = UnsignedOperation.toFloat(Bits.tobytes(bits));				
		        eph.setTgd(tgd);        
		        
		        /*  Toc, 8 bytes  */
		        bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = 111; j >= 104; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double toc = UnsignedOperation.toDouble(Bits.tobytes(bits));
		        eph.setTgd(toc);        
    			  
				/*  Af2, 4 bytes  */
		        bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 115; j >= 112; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}			
				//System.out.println("af2: " + Bits.tobytes(bits)) ;
				
				float af2 = (float) UnsignedOperation.toFloat(Bits.tobytes(bits));
				eph.setAf2(af2);				
		        
		        /*  Af1, 4 bytes  */
		        bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 119; j >= 116; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float af1 = UnsignedOperation.toFloat(Bits.tobytes(bits));
		        eph.setAf1(af1);        
		        
		        /*  Af0, 4 bytes  */
		        bits = new boolean[8 * 4]; // FP32
				indice = 0;
				for (int j = 123; j >= 120; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				float af0 = UnsignedOperation.toFloat(Bits.tobytes(bits));	        
		        eph.setAf0(af0);
		        		        	        
		        /*  URA(svaccur), 2 bytes  */	
		        bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 125; j >= 124; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int svAccur = (int)Bits.bitsToUInt(bits);		        
				eph.setSvAccur(svAccur);			
				
				/*  IODE, 2 bytes  */
				bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 127; j >= 126; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int iode = (int)Bits.bitsToUInt(bits);		
				eph.setSvAccur((int)iode);			
				
				/*  IODC, 2 bytes  */
				bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 129; j >= 128; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int iodc = (int)Bits.bitsToUInt(bits);			
				eph.setIodc(iodc);			
				
				/*  CodeL2, 2 bytes  */
				bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 131; j >= 130; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int l2Code = (int)Bits.bitsToUInt(bits);					
				eph.setL2Code(l2Code);
				
				/*  L2_Pdata_flag, 2 bytes  */
				bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 133; j >= 132; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int l2Flag = (int)Bits.bitsToUInt(bits);									
				eph.setL2Flag(l2Flag);		
				
				/*  WeekN, 2 bytes  */
				bits = new boolean[8 * 2]; 
				indice = 0;
				for (int j = 135; j >= 134; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				int week = (int)Bits.bitsToUInt(bits);													
				eph.setWeek(week);
			
				System.out.println("+----------------  Start of F7  ------------------+");
				System.out.println("satType: " + satType);  
				System.out.println("GPS PRN: " + satId);  
		        System.out.println("Crs: "+ crs);		
		        System.out.println("deltaN: "+ deltaN);	   
				System.out.println("M0: "+ m0);			        
		        System.out.println("Cuc: "+ cuc);	        
				System.out.println("E: "+ e);				
		        System.out.println("Cus: "+ cus);        
				System.out.println("SqrtA: "+ rootA);		
				System.out.println("Toe: "+ toe);			
		        System.out.println("Cis: "+ cis);
		        System.out.println("Cic: "+ cic);        
				System.out.println("Omega0: "+ omega0);			
				System.out.println("I0: "+ i0);			
		        System.out.println("Crc: "+ crc);  
				System.out.println("W: "+ omega);	
				System.out.println("OmegaR: "+ omegaDot);				
				System.out.println("IDOT: "+ iDot);	
		        System.out.println("Tgd: "+ tgd);
				System.out.println("toc: "+ toc);		
		        System.out.println("Af2: "+ af2);
		        System.out.println("Af1: "+ af1);	        
		        System.out.println("Af0: "+ af0);  
				System.out.println("URA: " + svAccur);
				System.out.println("IODE: " + iode);
				System.out.println("IODC: " + iodc);
				System.out.println("CodeL2: " + l2Code);
				System.out.println("L2_Pdata_flag: " + l2Flag);
				System.out.println("WeekN: " + week);
				System.out.println("+-----------------  End of F7  -------------------+");
						        
        
		}else{   // GLONASS: 93 bytes

				System.out.println("GLONASS PRN: " + satId);

		}
        

			return null;
	}


}
