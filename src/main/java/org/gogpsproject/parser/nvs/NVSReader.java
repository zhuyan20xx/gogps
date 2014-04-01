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
	private InputStream in;
//	private BufferedInputStream in;
	private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();
//	private StreamEventListener streamEventListener;

	public NVSReader(InputStream is, int leng){
		this(is,null);
		
		
	}
	public NVSReader(InputStream is, StreamEventListener eventListener){
		this.in = is;
//		this.in = (BufferedInputStream) is;
		addStreamEventListener(eventListener);
	}

	//public Object readMessagge() throws IOException, NVSException{
	public Object readMessagge(InputStream in) throws IOException, NVSException{
//	public Object readMessagge(BufferedInputStream in) throws IOException, NVSException{

			int data = in.read();
//			boolean parsed = false;
			
			if(data == 0xf7){

				DecodeF7 decodeF7 = new DecodeF7(in);
//				parsed = true;
				
				EphGps eph = decodeF7.decode();
				if(streamEventListeners!=null && eph!=null){
					for(StreamEventListener sel:streamEventListeners){
						sel.addEphemeris(eph);
					}
				}
//				System.out.println("F7h");
				return eph;
						
			}else
			if (data == 0xf5){
				
				 in.mark(0); // To rewind in.read point 
				 int leng = 0;	
				 int nsv ;
				 int leng1 = in.available();
				 
				    while(in.available()>0){			
						 data = in.read();
						if(data == 0x10){  // <DLE>
							data = in.read(); 
							if(data == 0x03){  // <ETX>
								data = in.read();
								if(data == 0x10){  // <DLE>
										int leng2 = this.in.available();
//										System.out.println("leng2: " + leng2 );
										leng = (leng1 - leng2) * 8 ;
										nsv = (leng - 224) / 240;  // To calculate the number of satellites
										/* 28*8 bits = 224, 30*8 bits = 240 */
//										System.out.println("leng: " + leng );
//										System.out.println("Num of Satellite: "+ nsv);
										break;
								}					
							}							
						}	
				  }	
				in.reset(); // To return to in.mark point  
				DecodeF5 decodeF5 = new DecodeF5(in, leng);										
//				parsed = true;
				
				Observations o = decodeF5.decode();
				if(streamEventListeners!=null && o!=null){
					for(StreamEventListener sel:streamEventListeners){
						Observations oc = (Observations)o.clone();
						sel.addObservations(oc);
					}
				}
//				System.out.println("F5h");
				return o;
				
			}else
			if (data == 0x4a){
				Decode4A decode4A = new Decode4A(in);
//				parsed = true;
				
				IonoGps iono = decode4A.decode();
				if(streamEventListeners!=null && iono!=null){
					for(StreamEventListener sel:streamEventListeners){
						sel.addIonospheric(iono);
					}
				}
//				System.out.println("4Ah");
				return iono;

				
			}else
			if (data == 0x62){
				
//				System.out.println("62h");
				
			}else{
//				System.out.println("else");
				//System.out.println("Wrong Sync char 2 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
			}

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
