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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
/**
 * <p>
 * Read and parse NVS messages
 * </p>
 *
 * @author Daisuke Yoshida (Osaka City University), Lorenzo Patocchi (cryms.com)
 */
public class NVSReader implements StreamEventProducer {
	private InputStream is;
	private BufferedInputStream bis;
	private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();
	private Boolean debugModeEnabled = false;
	
	boolean gpsEnable = true;  // enable GPS data reading
	boolean qzsEnable = true;  // enable QZSS data reading
    boolean gloEnable = true;  // enable GLONASS data reading	
    boolean galEnable = true;  // enable Galileo data reading
    boolean bdsEnable = true;  // enable BeiDou data reading

    private Boolean[] multiConstellation = {gpsEnable, qzsEnable, gloEnable, galEnable, bdsEnable};
//	private StreamEventListener streamEventListener;

//	public NVSReader(InputStream is){
//		this(is,null);		
//	}
	//TODO
	public NVSReader(BufferedInputStream is, Boolean[] multiConstellation){
		this(is,null, null);		
	}
	
	public NVSReader(InputStream is, StreamEventListener eventListener){
		this.is = is;
//		this.in = (BufferedInputStream) is;
		addStreamEventListener(eventListener);
	}
	
	public NVSReader(BufferedInputStream is, Boolean[] multiConstellation, StreamEventListener eventListener){
		this.is = is;
//		this.in = (BufferedInputStream) is;
		this.multiConstellation = multiConstellation;
		addStreamEventListener(eventListener);
	}

	public Object readMessage() throws IOException, NVSException{
//	public Object readMessage(InputStream in) throws IOException, NVSException{
//	public Object readMessage(BufferedInputStream in) throws IOException, NVSException{

			int data = is.read();
			@SuppressWarnings("unused")
			boolean parsed = false;
			
			if(data == 0xf7){ // F7

				DecodeF7 decodeF7 = new DecodeF7(is, multiConstellation);
				parsed = true;
				
				EphGps eph = decodeF7.decode();
				if(streamEventListeners!=null && eph!=null){
					for(StreamEventListener sel:streamEventListeners){
//						sel.addEphemeris(eph);
					}
				}

				return eph;
						
			}else
			if (data == 0xf5){  // F5
				
				int leng1 = is.available();  // for Total data
				int leng2 = 0;	 			  // for <DLE><ETX><DLE> position
				is.mark(leng1); 			// To rewind in.read point 

				/* To calculate the number of satellites */
				while(is.available()>0){			
					data = is.read();
					if(data == 0x10){  // <DLE>
						data = is.read(); 
						if(data == 0x03){  // <ETX>
							data = is.read();
							if(data == 0x10){  // <DLE>
								leng2 = this.is.available();
								leng2 = (leng1 - leng2 + 1 ) * 8 ;
								// int nsv = (leng2 - 224) / 240;  
								/* 28*8 bits = 224, 30*8 bits = 240 */
								// System.out.println("leng: " + leng );
								// System.out.println("Num of Satellite: "+ nsv);
								break;
							}					
						}							
					}	
				}
				    
				if(leng2 != 0){
						is.reset(); // To return to in.mark point  
						DecodeF5 decodeF5 = new DecodeF5(is, multiConstellation);										
						parsed = true;
						
						Observations o = decodeF5.decode(null, leng2);
						if(streamEventListeners!=null && o!=null){
							for(StreamEventListener sel:streamEventListeners){
								Observations oc = (Observations)o.clone();
								sel.addObservations(oc);
							}
						}

						return o;
				}else{
						return null;
				}
				
				
			}else
			if (data == 0x4a){ // 4A
				Decode4A decode4A = new Decode4A(is);
				parsed = true;
				
				IonoGps iono = decode4A.decode();
				if(streamEventListeners!=null && iono!=null){
					for(StreamEventListener sel:streamEventListeners){
//						sel.addIonospheric(iono);
					}
				}

				return iono;

				
			}else
			if (data == 0x62){
				
				findMessageEnd();
				
			}else
			if (data == 0x70){
				
				findMessageEnd();
				
			}else
			if (data == 0x4b){
				
				findMessageEnd();
					
			}else
			if (data == 0xF6){
					
				findMessageEnd();
						
			}
			else
			if (debugModeEnabled) {

				System.out.println("Warning: wrong sync char 2 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
			}

			return null;
	}
	
	private void findMessageEnd() {
		int data = 0;
		try {
			while(is.available()>0){			
				data = is.read();
				if(data == 0x10){  // <DLE>
					data = is.read(); 
					if(data == 0x03){  // <ETX>
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public void enableDebugMode(Boolean enableDebug) {
		this.debugModeEnabled = enableDebug;
	}
}
