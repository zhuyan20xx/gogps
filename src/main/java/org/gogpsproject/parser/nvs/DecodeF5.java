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

//	private int[] fdata;
//	private int[] fbits;
//	private boolean end = true;

	int nsv;
	
	public DecodeF5(InputStream in) throws IOException {
		this.in = in;
		int leng = this.in.available();
		System.out.println("Length: "+ leng);
		
		if(in.markSupported()){
		    
		    in.mark(0); // To rewind in.read point 

		    while(in.available()>0){
				
				int data = in.read();
				if(data == 0x10){
					data = in.read();
					if(data == 0xf7 || data == 0xf5 || data == 0x4a || data == 0x62 || data == 0xf6 || data == 0xd5 || data == 0xe5 || data == 0x6f || data == 0x63 || data == 0x64 || data == 0x69 || data == 0x6a || data == 0x6b  ){
						int leng2 = this.in.available();
						System.out.println("Length2: "+ leng2);
						
						leng = (leng - leng2) * 8 ;
						nsv = (leng - 216) / 240;  // To calculate the number of satellites
						// 27*8 bits = 216, 30*8 bits = 240
						System.out.println("Num of Satellite: "+ nsv);
						
					break;
					}
				}	
		    }	
		
		}
		else
		    System.out.println("mark is not supported, use BufferedInputStream");
		
	    in.reset(); // To return to in.mark point  
		
	}

	public Observations decode() throws IOException, NVSException {
		
		byte bytes[];
		
		
		//System.out.println("Num of Satellite: "+ nsv);
		
		
		/*  TOW_UTC, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double utc = Bits.byteToIEEE754Double(bytes);
		System.out.println("TOW_UTC: "+ utc);			        
	
		/*  Week Number, 2 bytes  */
		bytes = new byte[2];
		in.read(bytes, 0, bytes.length);
		long weekN = Bits.byteToLong(bytes);
		System.out.println("Week No.: " + weekN);
		
		/*  GPS Time Shift, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double gpsTimeShift = Bits.byteToIEEE754Double(bytes);
		System.out.println("GPS-UTC TimeShift: "+ gpsTimeShift);		
		
		/*  GLONASS Time Shift, 8 bytes  */
		bytes = new byte[8];
		in.read(bytes, 0, bytes.length);
		double glonassTimeShift = Bits.byteToIEEE754Double(bytes);
		System.out.println("GLONASS-UTC TimeShift: "+ glonassTimeShift);	
		
		/* Time Correction, 2 bytes */
		int timeCorrection = in.read();
		System.out.println("Time_Correction: "+ timeCorrection);	
		
		for(int i=0; i< nsv; i++){
		
				/* Signal Type, 2 bytes, 01: GLONASS, 02: GPS, 04: SBAS */
				int signalType = in.read();
				System.out.println("Signal_Type: "+ signalType);
		
				/* Satellite Number, 2 bytes */
				int satID = in.read();
				System.out.println("Satellite Number: "+ satID);
				
				/* A carrier Number for GLONASS, 2 bytes */
				int carrierNum = in.read();
				System.out.println("Carrier Number: "+ carrierNum);
				
				/* SNR (dB-Hz) */
				int snr = in.read();
				System.out.println("SNR: "+ snr);
				
				/*  Carrier Phase (cycles), 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double carrierPhase = Bits.byteToIEEE754Double(bytes);
				System.out.println("Carrier Phase: "+ carrierPhase);	
				
				/*  Pseudo Range (ms), 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double pseudoRange = Bits.byteToIEEE754Double(bytes);
				System.out.println("Pseudo Range: "+ pseudoRange);
				
				/*  Doppler Frequency(Hz), 8 bytes  */
				bytes = new byte[8];
				in.read(bytes, 0, bytes.length);
				double dopperFrequency = Bits.byteToIEEE754Double(bytes);
				System.out.println("Doppler Frequency: "+ dopperFrequency);
				
				/* Raw Data Flags */
				int rawDataFlags = in.read();
				System.out.println("Raw Data Flags: "+ rawDataFlags);
				
				/* Reserved */
				int reserved = in.read();
				System.out.println("reserved: "+ reserved);
				
				
		}
		
		return null;
		
		
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
