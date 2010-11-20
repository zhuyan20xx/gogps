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
import java.io.OutputStream;

/**
 * <p>
 * 
 * </p>
 * 
 * @author Lorenzo Patocchi cryms.com
 */

public class UBXSerialReader implements Runnable {

	private InputStream in;
	private OutputStream out;
	//private boolean end = false;
	private Thread t = null;
	private boolean stop = false;
	private UBXEventListener eventListener;
	private UBXReader reader;
	
	public UBXSerialReader(InputStream in,OutputStream out,UBXEventListener el) {
		this.in = in;
		this.out = out;
		this.eventListener = el;
		this.reader = new UBXReader(in, el);
	}

	public void start()  throws IOException{
		t = new Thread(this);
		t.start();
		
		System.out.println("1");
		String nmea[] = { "GGA", "GLL", "GSA", "GSV", "RMC", "VTG", "GRS",
				"GST", "ZDA", "GBS", "DTM" };
		for (int i = 0; i < nmea.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration("NMEA", nmea[i], 0);
			out.write(msgcfg.getByte());
			out.flush();
		}
		System.out.println("2");
		String pubx[] = { "A", "B", "C", "D" };
		for (int i = 0; i < pubx.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration("PUBX", pubx[i], 0);
			out.write(msgcfg.getByte());
			out.flush();
		}
		// outputStream.write(clear.getBytes());
		// outputStream.flush();
		MsgConfiguration msgcfg = new MsgConfiguration("RXM", "RAW", 1);
		out.write(msgcfg.getByte());
		out.flush();
		System.out.println("3");
	}
	public void stop(){
		stop = true;
	}
	public void run() {

		int data = 0;

		try {
			//System.out.println();
			while (!stop) {
				if(in.available()>0){
					try{
						data = in.read();
						if(data == 0xB5){
							reader.readMessagge();
						}else{
							//no warning, may be NMEA
							//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
						}
					}catch(UBXException ubxe){
						ubxe.printStackTrace();
					}
				}else{
					// no bytes to read, wait a while
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		eventListener.streamClosed();
	}

}
