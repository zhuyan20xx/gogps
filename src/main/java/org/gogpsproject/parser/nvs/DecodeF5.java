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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.Time;
import org.gogpsproject.util.Bits;
import org.gogpsproject.util.UnsignedOperation;


public class DecodeF5 {
	
	private InputStream in;

	int nsv;
	int leng;	
	int[] data;
	
	public DecodeF5(InputStream in) throws IOException {
		this.in = in;
		int leng1 = in.available();
//		System.out.println("leng1: " + leng1 );

		// To calculate the number of satellites
		if(in.markSupported()){
		    
		    in.mark(0); // To rewind in.read point 

		    while(in.available()>0){
			
				int data = in.read();
				if(data == 0x10){  // <DLE>
					data = in.read(); 
					if(data == 0x03){  // <ETX>
						data = in.read();
						if(data == 0x10){  // <DLE>
//							System.out.println("mark is ok");

								int leng2 = this.in.available();
//								System.out.println("leng2: " + leng2 );

	//							System.out.println("Length2: "+ leng2);					
								leng = (leng1 - leng2) * 8 ;
								nsv = (leng - 224) / 240;  // To calculate the number of satellites
								/* 28*8 bits = 224, 30*8 bits = 240 */
//								System.out.println("leng: " + leng );
//								System.out.println("Num of Satellite: "+ nsv);
								break;
						}					
					}
					
				}	
		    }	
		
		}else{
				System.out.println("mark is not supported, use BufferedInputStream");
		}
		   
	    in.reset(); // To return to in.mark point  
		
	}

	public Observations decode(OutputStream logos) throws IOException, NVSException {
		
		byte bytes[];
		
		boolean[] bits;
		int indice;
		boolean[] temp1;
		
		//System.out.println("Num of Satellite: "+ nsv);	
		
		/*  TOW_UTC, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double utc = Bits.byteToIEEE754Double(bytes);
		long tow = (long)utc;
	
		/*  Week Number, 2 bytes  */
		bytes = new byte[2];
		in.read(bytes, 0, bytes.length);
		long weekN = Bits.byteToLong(bytes);
		
		/*  GPS Time Shift, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double gpsTimeShift = Bits.byteToIEEE754Double(bytes);
		
		/*  GLONASS Time Shift, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double glonassTimeShift = Bits.byteToIEEE754Double(bytes);
		
		/* Time Correction, 1 bytes */
		//bytes = new byte[1];
		//in.read(bytes, 0, bytes.length);	
		int timeCorrection = in.read();
		
		long gmtTS = getGMTTS(tow, weekN);
		Observations o = new Observations(new Time(gmtTS),0);
		
//		System.out.println("+----------------  Start of F5  ------------------+");
//
//		System.out.println("TOW_UTC: "+ utc);			        
//		System.out.println("Week No.: " + weekN);
//		System.out.println("GPS-UTC TimeShift: "+ gpsTimeShift);		
//		System.out.println("GLONASS-UTC TimeShift: "+ glonassTimeShift);	
//		System.out.println("Time_Correction: "+ timeCorrection);	
		
		
		
		data = new int[leng -224];
		
		for (int i = 0; i < leng - 224; i++) {
			data[i] = in.read();
			if(logos!=null) logos.write(data[i]);			
		}
		
		
		for (int k = 0; k < nsv ; k++) {
		
				ObservationSet os = new ObservationSet();			
				int offset = k * 30;
				
				/* Signal Type, 1 byte */
				bits = new boolean[8]; // INT8U
				indice = 0;
				temp1 = Bits.intToBits(data[offset], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				int signalType = (int)Bits.bitsToUInt(bits);
								
				/* Satellite Number, 1 byte */
				bits = new boolean[8]; // INT8U
				indice = 0;
				temp1 = Bits.intToBits(data[offset + 1], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				int satID = (int)Bits.bitsToUInt(bits);
				os.setSatID(satID);
				
				/* A carrier Number for GLONASS, 1 bytes */
				bits = new boolean[8]; // INT8U
				indice = 0;
				temp1 = Bits.intToBits(data[offset + 1 + 1 ], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				int carrierNum = (int)Bits.bitsToUInt(bits);
					
				/* SNR (dB-Hz), 1 byte */
				bits = new boolean[8]; // INT8U
				indice = 0;
				temp1 = Bits.intToBits(data[offset + 1 + 1 + 1], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				int snr = (int)Bits.bitsToUInt(bits);
				os.setSignalStrength(ObservationSet.L1, snr);
				
				/*  Carrier Phase (cycles), 8 bytes  */		
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = offset + 1 + 1 + 1 + 8 ; j >=  1 + 1 + 1 + 1 + offset; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double carrierPhase = UnsignedOperation.toDouble(Bits.tobytes(bits));
				os.setPhase(ObservationSet.L1, carrierPhase);
				
				/* C/A Pseudo Range (ms), 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = offset + 1 + 1 + 1 + 8 + 8 ; j >=  1 + 1 + 1 + 1 + 8 + offset; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double pseudoRange = UnsignedOperation.toDouble(Bits.tobytes(bits));
				os.setCodeC(ObservationSet.L1, pseudoRange);

				/*  Doppler Frequency(Hz), 8 bytes  */
				bits = new boolean[8 * 8]; // FP64
				indice = 0;
				for (int j = offset + 1 + 1 + 1 + 8 + 8 + 8 ; j >=  1 + 1 + 1 + 1 + 8 + 8 + offset; j--) {
					temp1 = Bits.intToBits(data[j], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				}
				double dopperFrequency = UnsignedOperation.toDouble(Bits.tobytes(bits));
				float d1 = (float)dopperFrequency; 
				os.setDoppler(ObservationSet.L1, d1);

				/* Raw Data Flags, 1 byte */
				bits = new boolean[8]; // INT8U
				indice = 0;
				temp1 = Bits.intToBits(data[offset + 1 + 1 + 1 + 1 + 8 + 8 + 8], 8);
					for (int i = 0; i < 8; i++) {
						bits[indice] = temp1[i];
						indice++;
					}
				int rawDataFlags = (int)Bits.bitsToUInt(bits);
		
				o.setGps(k, os);
			
//				System.out.println("##### Satellite:  "+ k );
//				System.out.println("Signal_Type: "+ signalType);
//				System.out.println("Satellite Number: "+ satID);
//				System.out.println("Carrier Number: "+ carrierNum);
//				System.out.println("SNR: "+ snr);
//				System.out.println("Carrier Phase: "+ carrierPhase);	
//				System.out.println("Pseudo Range: "+ pseudoRange);
//				System.out.println("Doppler Frequency: "+ d1);
//				System.out.println("Raw Data Flags: "+ rawDataFlags);
//				System.out.println("			");

		}
//		System.out.println("+-----------------  End of F5  -------------------+");


		return o;
		
		
	}

	private long getGMTTS(long tow, long week) {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(c.getTimeZone());
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
