/*
 * Copyright (c) 2010, Lorenzo Patocchi. All Rights Reserved.
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

import org.gogpsproject.Observations;
/**
 * <p>
 * Read and parse UBX messages
 * </p>
 * 
 * @author Lorenzo Patocchi cryms.com
 */
public class UBXReader {
	private InputStream in;
	private UBXEventListener eventListener;

	public UBXReader(InputStream is){
		this(is,null);
	}
	public UBXReader(InputStream is, UBXEventListener eventListener){
		this.in = is;
		this.eventListener = eventListener;
	}
	
	public Object readMessagge() throws IOException, UBXException{
		
//		int data = in.read();
//		if(data == 0xB5){
			int data = in.read(); 
			if(data == 0x62){

				data = in.read(); // Class
				boolean parsed = false;
				if (data == 0x02) {
					data = in.read(); // ID
					if (data == 0x10) {
						// RMX-RAW
						DecodeRXMRAW decodegps = new DecodeRXMRAW(in);
						
						Observations o = decodegps.decode();
						if(eventListener!=null) eventListener.addObservations(o);
						return o;
					}
				}else{
					in.skip(1); // ID
				}
				if(!parsed){
					
					// read non parsed message length
					int[] length = new int[2];
					length[1] = in.read();
					length[0] = in.read();
					
					int len = length[0]*256+length[1];
					//System.out.println("skip "+len);
					in.skip(len+2);
					
				}
//					if (data == 0x0B) {
//						data = in.read();
//						if (data == 0x31) {
//							// AID-EPH
//	//						DecodeRMXRAW decodegps = new DecodeRMXRAW(in);
//	//						decodegps.decode();
//						}
//					}
			}else{
				System.out.println("Wrong Sync char 2 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
			}
//		}else{
//			//no warning, may be NMEA
//			//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
//		}
			return null;
	}
}
