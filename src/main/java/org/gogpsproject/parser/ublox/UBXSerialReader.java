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
package org.gogpsproject.parser.ublox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
import org.gogpsproject.util.InputStreamCounter;

/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com, Eugenio Realini
 */

public class UBXSerialReader implements Runnable,StreamEventProducer {

	private InputStreamCounter in;
	private OutputStream out;
	//private boolean end = false;
	private Thread t = null;
	private boolean stop = false;
	private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();
	//private StreamEventListener streamEventListener;
	private UBXReader reader;
	private String COMPort;
	private int measRate = 1;
	private boolean MsgAidEphEnabled = false;
	private boolean MsgAidHuiEnabled = false;
	private boolean SysTimeLogEnabled = false;
	private List<String> RequestedNmeaMsgs;

	public UBXSerialReader(InputStream in,OutputStream out, String COMPort) {
		this(in,out,COMPort,null);
		this.COMPort = padCOMSpaces(COMPort);
	}
	
	public UBXSerialReader(InputStream in,OutputStream out,String COMPort,StreamEventListener streamEventListener) {
		
		FileOutputStream fos_ubx= null;
		COMPort = padCOMSpaces(COMPort);
		
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		
		try {
			fos_ubx = new FileOutputStream("./test/"+ COMPort.trim()+ "_" + date1 + ".ubx");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.in = new InputStreamCounter(in,fos_ubx);
		this.out = out;
		this.reader = new UBXReader(this.in,streamEventListener);
	}

	public void start()  throws IOException{
		t = new Thread(this);
		t.setName("UBXSerialReader");
		t.start();
		
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date1 = sdf1.format(date);
		
		System.out.println(date1+" - "+COMPort+" - Setting measurement rate at "+measRate+" Hz");
		RateConfiguration ratecfg = new RateConfiguration(1000/measRate, 1, 1);
		out.write(ratecfg.getByte());
		out.flush();

		int nmeaAll[] = { MessageType.NMEA_GGA, MessageType.NMEA_GLL, MessageType.NMEA_GSA, MessageType.NMEA_GSV, MessageType.NMEA_RMC, MessageType.NMEA_VTG, MessageType.NMEA_GRS,
				MessageType.NMEA_GST, MessageType.NMEA_ZDA, MessageType.NMEA_GBS, MessageType.NMEA_DTM };
		for (int i = 0; i < nmeaAll.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_NMEA, nmeaAll[i], false);
			out.write(msgcfg.getByte());
			out.flush();
		}

		int nmeaRequested[];
		try {
			if (RequestedNmeaMsgs.isEmpty()) {
				System.out.println(date1+" - "+COMPort+" - Disabling NMEA messages");
			} else {
				nmeaRequested = new int[RequestedNmeaMsgs.size()];
				for (int n = 0; n < RequestedNmeaMsgs.size(); n++) {
					MessageType msgtyp = new MessageType("NMEA", RequestedNmeaMsgs.get(n));
					nmeaRequested[n] = msgtyp.getIdOut();
				}
				for (int i = 0; i < nmeaRequested.length; i++) {
					System.out.println(date1+" - "+COMPort+" - Enabling NMEA "+RequestedNmeaMsgs.get(i)+" messages");
					MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_NMEA, nmeaRequested[i], true);
					out.write(msgcfg.getByte());
					out.flush();
				}
			}
		} catch (NullPointerException e) {
		}

		int pubx[] = { MessageType.PUBX_A, MessageType.PUBX_B, MessageType.PUBX_C, MessageType.PUBX_D };
		for (int i = 0; i < pubx.length; i++) {
			MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_PUBX, pubx[i], false);
			out.write(msgcfg.getByte());
			out.flush();
		}

		System.out.println(date1+" - "+COMPort+" - Enabling RXM-RAW messages");
		MsgConfiguration msgcfg = new MsgConfiguration(MessageType.CLASS_RXM, MessageType.RXM_RAW, true);
		out.write(msgcfg.getByte());
		out.flush();
	}
	public void stop(boolean waitForThread, long timeoutMs){
		stop = true;
		if(waitForThread && t!=null && t.isAlive()){
			try {
				t.join(timeoutMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void run() {

		int data = 0;
		long aidEphTS = System.currentTimeMillis();
		long aidHuiTS = System.currentTimeMillis();
		long sysOutTS = System.currentTimeMillis();
		MsgConfiguration msgcfg = null;
		FileOutputStream fos_tim = null;
		PrintStream ps = null;

		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		String date2 = sdf2.format(date);
		
		if (this.SysTimeLogEnabled) {
			System.out.println(date1+" - "+COMPort+" - System time logging enabled");
			try {
				fos_tim = new FileOutputStream("./test/"+ COMPort.trim()+ "_" + date2 + "_systime.txt");
				ps = new PrintStream(fos_tim);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(date1+" - "+COMPort+" - System time logging disabled");
		}

		try {
			int msg[] = {};
			if (this.MsgAidHuiEnabled) {
				System.out.println(date1+" - "+COMPort+" - AID-HUI message polling enabled");
				msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
				out.write(msgcfg.getByte());
				out.flush();
			}
			if (this.MsgAidEphEnabled) {
				System.out.println(date1+" - "+COMPort+" - AID-EPH message polling enabled");
				msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
				out.write(msgcfg.getByte());
				out.flush();
			}

			in.start();
			while (!stop) {
				if(in.available()>0){
					data = in.read();
					try{
						if(data == 0xB5){
							Object o = reader.readMessage();
							try {
								if(this.SysTimeLogEnabled && o.getClass().toString().equals("class org.gogpsproject.Observations")) {
									date = new Date();
									sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
									date1 = sdf1.format(date);
									ps.println(date1);
								}
							} catch (NullPointerException e) {
							}
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
				
				date = new Date();
				sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				date1 = sdf1.format(date);
				
				if(this.MsgAidEphEnabled && curTS-aidEphTS >= 30*1000){
					System.out.println(date1+" - "+COMPort+" - Polling AID-EPH message");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidEphTS = curTS;
				}
				if(this.MsgAidHuiEnabled && curTS-aidHuiTS >= 120*1000){
					System.out.println(date1+" - "+COMPort+" - Polling AID-HUI message");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidHuiTS = curTS;
				}
				if (curTS-sysOutTS >= 1*1000) {
					System.out.println(date1+" - "+COMPort+" - Logging at "+in.getCurrentBps()+" Bps -- Total: "+in.getCounter()+" bytes");
					sysOutTS = curTS;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(StreamEventListener sel:streamEventListeners){
			sel.streamClosed();
		}
		//if(streamEventListener!=null) streamEventListener.streamClosed();
	}

//	/**
//	 * @return the streamEventListener
//	 */
//	public StreamEventListener getStreamEventListener() {
//		return streamEventListener;
//	}

//	/**
//	 * @param streamEventListener the streamEventListener to set
//	 */
//	public void setStreamEventListener(StreamEventListener streamEventListener) {
//		this.streamEventListener = streamEventListener;
//		if(this.reader!=null) this.reader.setStreamEventListener(streamEventListener);
//	}

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
		if(this.reader!=null)
			this.reader.addStreamEventListener(streamEventListener);
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
		this.reader.removeStreamEventListener(streamEventListener);
	}
	
	public void setRate(int measRate) {
		this.measRate = measRate;
	}
	
	public void enableAidEphMsg(Boolean enableEph) {
		this.MsgAidEphEnabled = enableEph;
	}
	
	public void enableAidHuiMsg(Boolean enableIon) {
		this.MsgAidHuiEnabled = enableIon;
	}
	
	public void enableSysTimeLog(Boolean enableTim) {
		this.SysTimeLogEnabled = enableTim;
	}
	
	public void enableNmeaMsg(List<String> nmeaList) {
		this.RequestedNmeaMsgs = nmeaList;
	}
	
	private String padCOMSpaces(String COMPortIn) {
		if (COMPortIn.substring(0, 3).equals("COM") && COMPortIn.length() == 4) {
			COMPortIn = COMPortIn + " ";
		}
		return COMPortIn;
	}
}
