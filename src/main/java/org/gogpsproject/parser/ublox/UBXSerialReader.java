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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.gogpsproject.util.InputStreamCounter;

/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

public class UBXSerialReader implements Runnable {

	private InputStreamCounter in;
	private OutputStream out;
	//private boolean end = false;
	private Thread t = null;
	private boolean stop = false;
	private UBXEventListener eventListener;
	private UBXReader reader;

	public UBXSerialReader(InputStream in,OutputStream out,UBXEventListener el) {
		FileOutputStream fos= null;
		try {
			fos = new FileOutputStream("./data/ubx.out");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.in = new InputStreamCounter(in,fos);
		this.out = out;
		this.eventListener = el;
		this.reader = new UBXReader(this.in, el);
	}

	public void start()  throws IOException{
		t = new Thread(this);
		t.setName("UBXSerialReader");
		t.start();

		//System.out.println("1");
		int nmea[] = { MessageType.NMEA_GGA, MessageType.NMEA_GLL, MessageType.NMEA_GSA, MessageType.NMEA_GSV, MessageType.NMEA_RMC, MessageType.NMEA_VTG, MessageType.NMEA_GRS,
				MessageType.NMEA_GST, MessageType.NMEA_ZDA, MessageType.NMEA_GBS, MessageType.NMEA_DTM };
		for (int i = 0; i < nmea.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_NMEA, nmea[i], false);
			out.write(msgcfg.getByte());
			out.flush();
		}
		//System.out.println("2");
		int pubx[] = { MessageType.PUBX_A, MessageType.PUBX_B, MessageType.PUBX_C, MessageType.PUBX_D };
		for (int i = 0; i < pubx.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_PUBX, pubx[i], false);
			out.write(msgcfg.getByte());
			out.flush();
		}
		// outputStream.write(clear.getBytes());
		// outputStream.flush();
		MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_RXM, MessageType.RXM_RAW, true);
		out.write(msgcfg.getByte());
		out.flush();
		msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, true);
		out.write(msgcfg.getByte());
		out.flush();
		msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, true);
		out.write(msgcfg.getByte());
		out.flush();

		//System.out.println("3");
	}
	public void stop(){
		stop = true;
	}
	public void run() {

		int data = 0;
		long aidEphTS = System.currentTimeMillis();
		long aidHuiTS = System.currentTimeMillis();


		try {
			int msg[] = {};
			System.out.println("Poll AID HUI");
			MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
			out.write(msgcfg.getByte());
			out.flush();
			System.out.println("Poll AID EPH");
			msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
			out.write(msgcfg.getByte());
			out.flush();

			//System.out.println();
			in.start();
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
				long curTS = System.currentTimeMillis();
				if(curTS-aidEphTS > 10*1000){
					System.out.println("Poll AID EPH");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidEphTS = curTS;

					System.out.println("BPS:"+in.getCurrentBps()+" bytes:"+in.getCounter());
				}
				if(curTS-aidHuiTS > 60*1000){
					System.out.println("Poll AID HUI");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidHuiTS = curTS;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		eventListener.streamClosed();
	}

}
