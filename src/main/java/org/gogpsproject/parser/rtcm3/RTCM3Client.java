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

package org.gogpsproject.parser.rtcm3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.util.Bits;

public class RTCM3Client implements Runnable {

	private ConnectionSettings settings;
	private Thread dataThread;
	/** Indicates if the end of the data file loop has been reached */
	private boolean loopend;
	
	private boolean go = false;
	private HashMap<Integer, Decode> decodeMap;
	
	/** Optinal message handler for showing error messages. */
	private boolean header = true;
	private int messagelength = 0;
	private int switchboolean;
	private int[] buffer;
	private boolean[] bits;
	private boolean[] rollbits;
	private boolean downloadlength = false;
	
	private Vector<Observations> observationsBuffer = null;


	public static RTCM3Client getInstance(String _host, int _port, String _username,
			String _password, String _mountpoint) throws Exception{

		ArrayList<String> s = new ArrayList<String>();
		ConnectionSettings settings = new ConnectionSettings(_host, _port, _username, _password);
		ArrayList<String> mountpoints = new ArrayList<String>();
		RTCM3Client net = new RTCM3Client(settings);
		try {
			s = net.getSources();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		for (int j = 1; j < s.size(); j++) {
			if (j % 2 == 0){
				mountpoints.add(s.get(j));
			}
		}
		if(_mountpoint == null){
			System.out.println("Available Mountpoints:");
		}
		for (int j = 0; j < mountpoints.size(); j++) {
			if(_mountpoint == null){
				System.out.print("\t[" + mountpoints.get(j)+"]");
			}else{
				System.out.print("\t[" + mountpoints.get(j)+"]["+_mountpoint+"]");
				if(_mountpoint.equalsIgnoreCase(mountpoints.get(j))){
					settings.setSource(mountpoints.get(j));
					System.out.print(" found");
				}
			}
			System.out.println();
		}
		if(settings.getSource() == null){
			System.out.println("Select a valid mountpoint!");
			return null;
		}
		return net;
	}

	public RTCM3Client(ConnectionSettings settings) {
		super();
		go = false;
		loopend = true;
		this.settings = settings;
		
		decodeMap = new HashMap<Integer, Decode>();
		
		decodeMap.put(new Integer(1004), new Decode1004Msg(this));
		decodeMap.put(new Integer(1005), new Decode1005Msg());
		decodeMap.put(new Integer(1007), new Decode1007Msg());
		decodeMap.put(new Integer(1012), new Decode1012Msg());
	}

	public ArrayList<String> getSources() throws IOException {
		Socket sck = new Socket(settings.getHost(), settings.getPort());

		// The input and output streams are created
		PrintWriter out = new PrintWriter(sck.getOutputStream(), true);
		InputStream sckIn = sck.getInputStream();
		// A Buffered reader is created so we can read whole lines
		InputStreamReader inRead = new InputStreamReader(sckIn);
		BufferedReader in = new BufferedReader(inRead);

		// The data request containing the logon and password are send
		out.println("GET / HTTP/1.1");
		out.println("User-Agent: NTRIP goGPS");
		out.println("Authorization: Basic " + settings.getPass_base64());
		// out.println("Ntrip-GAA: $GPGGA,200530,4600,N,00857,E,4,10,1,200,M,1,M,3,0*65");
		// out.println("Accept: */*\r\nConnection: close");
		out.println();
		out.flush();

		boolean going = true;
		boolean first = true;
		Vector<String> lines = new Vector<String>();
		while (going) {
			// The next byte is read and added to the buffer
			String newLine = in.readLine();
			if (newLine == null) {
				going = false;
			} else if (first) {
				// The first line should be "SOURCETABLE 200 OK"
				if (!newLine.equals("SOURCETABLE 200 OK")) {
					going = false;
				}
				first = false;

			} else {
				lines.addElement(newLine);
			}
		}

		// Lines are parsed
		ArrayList<String> sources = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			// A new StringTokenizer is created with ";" as delimiter
			
			StringTokenizer token = new StringTokenizer(lines.elementAt(i), ";");
			try {
				if (token.countTokens() > 1 && token.nextToken().equals("STR")) {
					
					System.out.println(lines.elementAt(i));
					
					// We excpect the correct source to be the first token after
					// "STR" to through the token wich specifies the RTCM
					// version
					// starting with "RTCM "
					// We haven't seen any specification of the sourcetable, but
					// according to what we can see from it it should be correct
					String s = token.nextToken();
					while (!s.startsWith("RTCM 3")) {
						sources.add(s);
						s = token.nextToken();
					}

				}
			} catch (NoSuchElementException ex) {/* The line is ignored */
			}
		}

		in.close();
		inRead.close();
		sckIn.close();
		out.close();

		return sources;
	}

	// public int[] readMessage() throws BufferUnderrunException {
	// return outputBuffer.readMessage();
	// }
	//
	// /**
	// * returns the number of messages ready for reading<B> uses
	// * CRWBuffer.ready()
	// */
	// public int ready() {
	// return outputBuffer.ready();
	// }


	@Override
	public void run() {
		Socket sck = null;
		PrintWriter out = null;
		InputStream in = null;

		try {
			go = true;
			loopend = false;
			// Socket for reciving data are created

			try {
				sck = new Socket(settings.getHost(), settings.getPort());
				System.out.println("Connected to " + settings.getHost() + ":"
						+ settings.getPort());
			} catch (Exception e) {
				go = false;
				String msg = "Connection to " + settings.getHost() + ":"
						+ settings.getPort() + " failed: \n  " + e;
				// if (messages == null) {
				// tester.println("<" + settings.getSource() + ">" + msg);
				// } else {
				// messages.showErrorMessage(settings.getSource(), msg);
				// }
				return;
			}

			// The input and output streams are created
			out = new PrintWriter(sck.getOutputStream(), true);
			in = sck.getInputStream();
			// The data request containing the logon and password are send
			out.println("GET /" + settings.getSource() + " HTTP/1.1");
			out.println("User-Agent: NTRIP goGps");
			out.println("Authorization: Basic " + settings.getAuthbase64());
			out.println("Ntrip-GAA: $GPGGA,200530,4600,N,00857,E,4,10,1,200,M,1,M,3,0*65");
			// out.println("User-Agent: NTRIP goGps");
			// out.println("Ntrip-GAA: $GPGGA,200530,4600,N,00857,E,4,10,1,200,M,1,M,3,0*65");
			// out.println("User-Agent: NTRIP GoGps");
			// out.println("Accept: */*\r\nConnection: close");
			out.println();
			out.flush();
//			System.out.println(" \n %%%%%%%%%%%%%%%%%%%%% \n password >>> "
//					+ settings.getAuthbase64());
			// *****************
			// Reading the data

			// /First we read the HTTP header using a small state machine
			// The end of the header is recived when a double end line
			// consisting
			// of a "new line" and a "carrige return" charecter has been recived
			int state = 0;
			// First the HTTP header type is read. It should be "ICY 200 OK"
			// But Since we recive integers not charecters the correct header is
			// numeric: 73 = 'I', 67 = 'C' and so on.

			int[] header = new int[11];
			int[] correctHeader = { 73, 67, 89, 32, 50, 48, 48, 32, 79, 75, 13 };
			int hindex = 0;
			// when go is changed to false the loop is stopped
			while (go && state == 0) {
				int c = in.read();
				System.out.print((char)c);
				if (c < 0)
					break;
				// tester.write(c);
				state = transition(state, c);
				if (hindex > 10) {
					// The header should only be 11 charecters long
					go = false;
				} else {
					header[hindex] = c;
					hindex++;
				}
			}
			
			for (int i = 0; i < 11 && go; i++) {
				if (header[i] != correctHeader[i]) {
					go = false;
				}
			}
			if(!go){
				for(int i=0;i<header.length;i++)
					System.out.print((char)header[i]);
				int c = in.read();
				while(c!=-1){
					System.out.print((char)c);
					c = in.read();
				}
				System.out.println();
				System.out.println(settings.getSource()+" invalid header");
				return;
			}
			
			while (state != 5) {
				int c = in.read();
				if (c < 0) break;
				// tester.write(c);
				state = transition(state, c);
			}
			// When HTTP header is read, the GPS data are recived and parsed:

			// The data is buffered as it is recived. When the buffer has size 6
			// There is a full word + a byte. The extra byte (first in buffer)
			// is
			// used for parity check.
			if (go) {
				// tester.println("<" + settings.getSource() +
				// ">Header least: OK");
				System.out.println(settings.getSource()+" connected successfully");
			} else {
				// showErrorMessage(settings.getSource(), "Error");
				System.out.println(settings.getSource()+" not connected");
				return;
			}
			// The read loop is started
			// sck.wait(1000);
			// this.dataThread.sleep(6000);
			// this.notifyAll();
			readLoop(in);
			// System.out.println("1");

		} catch (IOException ex) {

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			// Connection was either terminated or an IOError accured
			go = false;
			loopend = true;

			System.out.println(settings.getSource() + "Connection Error: Data is empty");
			// All connections are closed
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (sck != null)
					sck.close();
			} catch (IOException ex) {
			}

		}

	}

	public void start() {
		dataThread = new Thread(this);
		dataThread.start();
	}

	/** stops the execution of this thread */
	public void stop() {
		go = false;
	}

	/** returns true if the data thread still is alive */
	public boolean stopped() {
		// return true;
		return dataThread != null && !dataThread.isAlive();
	}

	public int transition(int state, int input) {
		switch (state) {
			case 0: {
				if (input == 13)
					state = 1;
				break;
			}
	
			case 1: {
				if (input == 13)
					state = 2;
				break;
			}
			case 2: {
				if (input == 10)
					state = 5;
				else
					state = 1;
				break;
			}
			case 3: {
				if (input == 13)
					state = 4;
				else
					state = 1;
				break;
			}
			case 4: {
				if (input == 10)
					state = 5;
				else
					state = 1;
				break;
			}
		}

		return state;
	}

	/**
	 * reads data from an InputStream while go is true
	 * 
	 * @param in
	 *            input stream to read from
	 */
	protected void readLoop(InputStream in) throws IOException {
		int c;
		int index;
		while (go) {
			c = in.read();
//			System.out.println("Header : " + c);
			if (c < 0)
				break;
			if (header) {
				if (c == 211) { // header
					index = 0;
					buffer = new int[2];
					buffer[0] = in.read();
					buffer[1] = in.read();
					bits = new boolean[buffer.length * 8];
					rollbits = new boolean[8];
					for (int i = 0; i < buffer.length; i++) {
						rollbits = Bits.rollByteToBits(buffer[i]);
						for (int j = 0; j < rollbits.length; j++) {
							bits[index] = rollbits[j];
							index++;
						}
					}
					messagelength = Bits.bitsToUInt(Bits.subset(bits, 6, 10));
//					System.out.println("Debug message length : "
//							+ messagelength);
					header = false;
					// downloadlength = true;
				}
			}

			if (messagelength > 0) {
				setBits(in, messagelength);
				int msgtype = Bits.bitsToUInt(Bits.subset(bits, 0, 12));

				System.out.println("message type : " + msgtype);
				messagelength = 0;

				Decode dec = decodeMap.get(new Integer(msgtype));
				if(dec!=null){
					dec.decode(bits, System.currentTimeMillis());
				}else{
					// missing message parser
				}
				
				// CRC
				setBits(in, 3);
			
				header = true;
				// setBits(in,1);
				// System.out.println(" dati :" + Bits.bitsToStr(bits));
			}
		}
	}
	
	private void setBits(InputStream in, int bufferlength) throws IOException {
		int index = 0;
		buffer = new int[bufferlength];
		bits = new boolean[buffer.length * 8];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = in.read();
		}
		// index = 0;
		for (int i = 0; i < buffer.length; i++) {
			rollbits = Bits.rollByteToBits(buffer[i]);
			for (int j = 0; j < 8; j++) {
				bits[index] = rollbits[j];
				index++;
			}
		}
	}
	
	public void addObservation(Observations o){
		if(observationsBuffer == null) observationsBuffer = new Vector<Observations>();
		observationsBuffer.add(o);
	}
	
	
}
