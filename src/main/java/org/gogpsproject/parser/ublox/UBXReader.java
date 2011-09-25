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
package org.gogpsproject.parser.ublox;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
/**
 * <p>
 * Read and parse UBX messages
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */
public class UBXReader implements StreamEventProducer {
	private InputStream in;
	private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();
//	private StreamEventListener streamEventListener;

	public UBXReader(InputStream is){
		this(is,null);
	}
	public UBXReader(InputStream is, StreamEventListener eventListener){
		this.in = is;
		addStreamEventListener(eventListener);
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
					if (data == 0x10) { // RXM
						// RMX-RAW
						DecodeRXMRAW decodegps = new DecodeRXMRAW(in);

						Observations o = decodegps.decode(null);
						if(streamEventListeners!=null && o!=null){
							for(StreamEventListener sel:streamEventListeners){
								// TODO clone o
								sel.addObservations(o);
							}
						}
						return o;
					}
				}else
				if (data == 0x0B) { // AID
					data = in.read(); // ID
					try{
						if (data == 0x02) { // HUI
							// AID-HUI (sat. Health / UTC / Ionosphere)
							DecodeAIDHUI decodegps = new DecodeAIDHUI(in);

							IonoGps iono = decodegps.decode();
							if(streamEventListeners!=null && iono!=null){
								for(StreamEventListener sel:streamEventListeners){
									// TODO clone iono
									sel.addIonospheric(iono);
								}
							}
							return iono;
						}else
						if (data == 0x31) { // EPH
							// AID-EPH (ephemerides)
							DecodeAIDEPH decodegps = new DecodeAIDEPH(in);

							EphGps eph = decodegps.decode();
							if(streamEventListeners!=null && eph!=null){
								for(StreamEventListener sel:streamEventListeners){
									// TODO clone eph
									sel.addEphemeris(eph);
								}
							}
							return eph;

						}
					}catch(UBXException ubxe){
						//System.out.println(ubxe);
						parsed = true;
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
			}else{
				System.out.println("Wrong Sync char 2 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
			}
//		}else{
//			//no warning, may be NMEA
//			//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
//		}
			return null;
	}
	/**
	 * @return the streamEventListener
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Vector<StreamEventListener> getStreamEventListeners() {
		return (Vector<StreamEventListener>)streamEventListeners.clone();
	}
	/**
	 * @param streamEventListener the streamEventListener to set
	 */
	@Override
	public void addStreamEventListener(StreamEventListener streamEventListener) {
		if(streamEventListener==null) return;
		if(!streamEventListeners.contains(streamEventListener))
			this.streamEventListeners.add(streamEventListener);
	}
	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventProducer#removeStreamEventListener(org.gogpsproject.StreamEventListener)
	 */
	@Override
	public void removeStreamEventListener(
			StreamEventListener streamEventListener) {
		if(streamEventListener==null) return;
		if(streamEventListeners.contains(streamEventListener))
			this.streamEventListeners.remove(streamEventListener);
	}

}
