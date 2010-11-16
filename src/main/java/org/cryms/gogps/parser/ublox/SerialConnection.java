/*
 * Copyright (c) 2010, Cryms.com . All Rights Reserved.
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

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

public class SerialConnection {
	private class SerialReader implements Runnable {
		InputStream in;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void run() {
			// byte[] buffer = new byte[1024];
			// int len = -1, i, temp;
			int raw1 = 0x02;
			int raw2 = 0x10;
			boolean raw = false;
			boolean parse = false;

			int pos = 0;
			int pos1 = 0;
			int data = 0;

			try {
				System.out.println();
				while (!end) {

					data = in.read();
					System.out.print("0x" + Integer.toHexString(data) + " ");
					if (raw1 == data) {
						data = in.read();
						System.out
								.print("0x" + Integer.toHexString(data) + " ");
						if (raw2 == data) {
							GpsDecode decodegps = new GpsDecode(in);
							decodegps.decode();
						}
					}

					// if(raw){
					// if(data==raw2 && pos1+1==pos){
					// parse = true;
					// gpsDecode decodegps = new gpsDecode(in);
					// decodegps.decode();
					// raw=false;
					// }
					// raw=false;
					// pos1=0;
					//						
					// }
					// if(raw1==data){
					// pos1=pos;
					// raw = true;
					// }
					//						
					// pos++;

				}
			} catch (IOException e) {
				end = true;
				try {
					outputStream.close();
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				serialPort.close();
				connected = false;
				System.out.println(id);
				System.out.println(id + " connection has been interrupted");
			}
		}
	}

	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean connected = false;
	private Thread reader;
	private SerialPort serialPort;
	private boolean end = false;
	private int divider;
	private int[] tempBytes;
	int numTempBytes = 0, numTotBytes = 0;
	private int id;
	public String[] nmea = { "GGA", "GLL", "GSA", "GSV", "RMC", "VTG", "GRS",
			"GST", "ZDA", "GBS", "DTM" };
	public String[] pubx = { "A", "B", "C", "D" };

	public SerialConnection(int id) {
		this(id, 255);
	}

	public SerialConnection(int id, int divider) {

		this.divider = divider;
		if (this.divider > 255)
			this.divider = 255;
		if (this.divider < 0)
			this.divider = 0;
		// this.id = id;
		tempBytes = new int[1024];
	}

	public boolean connect(String portName) {
		return connect(portName, 9600);
	}

	public boolean connect(String portName, int speed) {
		CommPortIdentifier portIdentifier;
		// OutputMessage msg = new OutputMessage();
		MsgConfiguration msgcfg;
		OutputMessage clear = new OutputMessage();
		boolean conn = false;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println(id + " Error: Port is currently in use");
			} else {
				serialPort = (SerialPort) portIdentifier.open("Serial", 2000);
				serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				inputStream = serialPort.getInputStream();
				outputStream = serialPort.getOutputStream();

				reader = (new Thread(new SerialReader(inputStream)));
				end = false;
				reader.start();
				connected = true;
				System.out.println(id + " connection on " + portName
						+ " established");
				conn = true;
				for (int i = 0; i < nmea.length; i++) {
					msgcfg = new MsgConfiguration("NMEA", nmea[i], 0);
					outputStream.write(msgcfg.getByte());
					outputStream.flush();
				}
				for (int i = 0; i < pubx.length; i++) {
					msgcfg = new MsgConfiguration("PUBX", pubx[i], 0);
					outputStream.write(msgcfg.getByte());
					outputStream.flush();
				}
				// outputStream.write(clear.getBytes());
				// outputStream.flush();
				msgcfg = new MsgConfiguration("RXM", "RAW", 1);
				outputStream.write(msgcfg.getByte());
				outputStream.flush();
			}
		} catch (NoSuchPortException e) {
			System.out.println(id + " the connection could not be made");
			e.printStackTrace();
		} catch (PortInUseException e) {
			System.out.println(id + " the connection could not be made");
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			System.out.println(id + " the connection could not be made");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(id + "the connection could not be made");
			e.printStackTrace();
		}
		return conn;
	}

	// serial reader

	public boolean disconnect() {
		boolean disconn = true;
		end = true;
		try {
			reader.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			disconn = false;
		}
		try {
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			disconn = false;
		}
		serialPort.close();
		connected = false;
		System.out.println(id);
		System.out.println(id + " connection disconnected");
		return disconn;
	}

	// Close connection

	public Vector<String> getPortList() {
		Enumeration<CommPortIdentifier> portList;
		Vector<String> portVect = new Vector<String>();
		portList = CommPortIdentifier.getPortIdentifiers();

		CommPortIdentifier portId;
		while (portList.hasMoreElements()) {
			portId = portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portVect.add(portId.getName());
			}
		}
		System.out.println(id + "found the following ports:");
		for (int i = 0; i < portVect.size(); i++) {
			System.out.println(id + portVect.elementAt(i));
		}

		return portVect;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean writeSerial(String message) {
		boolean success = false;
		if (isConnected()) {
			try {
				outputStream.write(message.getBytes());
				success = true;
			} catch (IOException e) {
				disconnect();
			}
		} else {
			System.out.println("Debug : " + id + " No port is connected.");
		}
		return success;
	}

}
