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

import java.io.File;
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

import org.gogpsproject.Observations;
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
	private boolean SysTimeLogEnabled = false;
	private List<String> RequestedNmeaMsgs;
	private String dateFile;
	private String outputDir = "./out";
	private int msgAidEphRate = 0; //seconds
	private int msgAidHuiRate = 0; //seconds

	public UBXSerialReader(InputStream in,OutputStream out, String COMPort) {
		this(in,out,COMPort,null);
		this.COMPort = padCOMSpaces(COMPort);
	}
	
	public UBXSerialReader(InputStream in,OutputStream out,String COMPort,StreamEventListener streamEventListener) {
		
		FileOutputStream fos_ubx= null;
		COMPort = padCOMSpaces(COMPort);
		
		String COMPortStr = COMPort;
		String [] tokens = COMPort.split("/");
		if (tokens.length > 0) {
			COMPortStr = tokens[tokens.length-1].trim();	//for Linux /dev/tty* ports
		}

		File file = new File(outputDir);
		if(!file.exists() || !file.isDirectory()){
		    boolean wasDirectoryMade = file.mkdirs();
		    if(wasDirectoryMade)System.out.println("Directory "+outputDir+" created");
		    else System.out.println("Could not create directory "+outputDir);
		}
		
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date1 = sdf1.format(date);
		SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		dateFile = sdfFile.format(date);
		
		try {
			System.out.println(date1+" - "+COMPort+" - Logging UBX stream in "+outputDir+"/"+ COMPortStr+ "_" + dateFile + ".ubx");
			fos_ubx = new FileOutputStream(outputDir+"/"+COMPortStr+ "_" + dateFile + ".ubx");
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
		
		System.out.println(date1+" - "+COMPort+" - Measurement rate set at "+measRate+" Hz");
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
				System.out.println(date1+" - "+COMPort+" - NMEA messages disabled");
			} else {
				nmeaRequested = new int[RequestedNmeaMsgs.size()];
				for (int n = 0; n < RequestedNmeaMsgs.size(); n++) {
					MessageType msgtyp = new MessageType("NMEA", RequestedNmeaMsgs.get(n));
					nmeaRequested[n] = msgtyp.getIdOut();
				}
				for (int i = 0; i < nmeaRequested.length; i++) {
					System.out.println(date1+" - "+COMPort+" - NMEA "+RequestedNmeaMsgs.get(i)+" messages enabled");
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

		System.out.println(date1+" - "+COMPort+" - RXM-RAW messages enabled");
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
		//long sysOutTS = System.currentTimeMillis();
		MsgConfiguration msgcfg = null;
		FileOutputStream fos_tim = null;
		FileOutputStream fos_nmea = null;
		PrintStream psSystime = null;
		PrintStream psNmea = null;

		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date1 = sdf1.format(date);
		
		String COMPortStr = COMPort;
		String [] tokens = COMPort.split("/");
		if (tokens.length > 0) {
			COMPortStr = tokens[tokens.length-1].trim();	//for Linux /dev/tty* ports
		}
		
		if (SysTimeLogEnabled) {
			System.out.println(date1+" - "+COMPort+" - System time logging enabled");
			try {
				System.out.println(date1+" - "+COMPort+" - Logging system time in "+outputDir+"/"+COMPortStr+ "_" + dateFile + "_systime.txt");
				fos_tim = new FileOutputStream(outputDir+"/"+COMPortStr+ "_" + dateFile + "_systime.txt");
				psSystime = new PrintStream(fos_tim);
				psSystime.println("GPS time                      System time");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(date1+" - "+COMPort+" - System time logging disabled");
		}
		
		if (!RequestedNmeaMsgs.isEmpty()) {
			try {
				System.out.println(date1+" - "+COMPort+" - Logging NMEA sentences in "+outputDir+"/"+COMPortStr+ "_" + dateFile + "_NMEA.txt");
				fos_nmea = new FileOutputStream(outputDir+"/"+COMPortStr+ "_" + dateFile + "_NMEA.txt");
				psNmea = new PrintStream(fos_nmea);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		try {
			int msg[] = {};
			if (msgAidHuiRate > 0) {
				System.out.println(date1+" - "+COMPort+" - AID-HUI message polling enabled (rate: "+msgAidHuiRate+"s)");
				msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
				out.write(msgcfg.getByte());
				out.flush();
			}
			if (msgAidEphRate > 0) {
				System.out.println(date1+" - "+COMPort+" - AID-EPH message polling enabled (rate: "+msgAidEphRate+"s)");
				msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
				out.write(msgcfg.getByte());
				out.flush();
			}

			in.start();
			sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String dateSys = null;
			String dateGps = null;
			boolean msgReceived = false;
			boolean truncatedNmea = false;
			while (!stop) {
				if(in.available()>0){
					dateSys = sdf1.format(new Date());
					if (!truncatedNmea) {
						data = in.read();
					}else{
						truncatedNmea = false;
					}
					try{
						if(data == 0xB5){
							Object o = reader.readMessage();
							try {
								if(o instanceof Observations){
									if(streamEventListeners!=null && o!=null){
										for(StreamEventListener sel:streamEventListeners){
											Observations co = sel.getCurrentObservations();
										    sel.pointToNextObservations();

										    msgReceived = true;

										    if (this.SysTimeLogEnabled) {
										    	dateGps = sdf1.format(new Date(co.getRefTime().getMsec()));
										    	psSystime.println(dateGps +"       "+dateSys);
										    }
										}
									}
								}
							} catch (NullPointerException e) {
							}
						}else if(data == 0x24 && !RequestedNmeaMsgs.isEmpty()){
							psNmea.print((char) data);
							data = in.read();
							if(data == 0x47) {
								psNmea.print((char) data);
								data = in.read();
								if(data == 0x50) {
									psNmea.print((char) data);
									data = in.read();
									while (data != 0x0A && data != 0xB5) {
										//System.out.print((char) data);
										psNmea.print((char) data);
										data = in.read();
									}
									psNmea.print((char) 0x0A);
									if (data == 0xB5) {
										truncatedNmea = true;
									}
								}
							}
							//no warning, may be NMEA
							//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
						}
					}catch(UBXException ubxe){
						ubxe.printStackTrace();
					}
				}else{
					// no bytes to read, wait 1 msec
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {}
				}
				long curTS = System.currentTimeMillis();
				
				if(msgAidEphRate > 0 && curTS-aidEphTS >= msgAidEphRate*1000){
					System.out.println(dateSys+" - "+COMPort+" - Polling AID-EPH message");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_EPH, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidEphTS = curTS;
				}
				if(msgAidHuiRate > 0 && curTS-aidHuiTS >= msgAidHuiRate*1000){
					System.out.println(dateSys+" - "+COMPort+" - Polling AID-HUI message");
					msgcfg = new MsgConfiguration(MessageType.CLASS_AID, MessageType.AID_HUI, msg);
					out.write(msgcfg.getByte());
					out.flush();
					aidHuiTS = curTS;
				}
				if (msgReceived/*curTS-sysOutTS >= 1*1000*/) {
					int bps = in.getCurrentBps();
					if (bps != 0) {
						System.out.println(dateSys+" - "+COMPort+" - Logging at "+String.format("%4d", bps)+" Bps -- Total: "+in.getCounter()+" bytes");
					} else {
						System.out.println(dateSys+" - "+COMPort+" - Log starting...     -- Total: "+in.getCounter()+" bytes");
					}
					//sysOutTS = curTS;
					msgReceived = false;
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
	
	public void enableAidEphMsg(Integer ephRate) {
		this.msgAidEphRate = ephRate;
	}
	
	public void enableAidHuiMsg(Integer ionRate) {
		this.msgAidHuiRate = ionRate;
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
