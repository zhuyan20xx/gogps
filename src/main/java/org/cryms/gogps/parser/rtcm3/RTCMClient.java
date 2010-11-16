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

package org.cryms.gogps.parser.rtcm3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

public class RTCMClient extends InputDevice implements Runnable {

	private GPSNetSettings settings;
	private Thread dataThread;
	/** Indicates if the end of the data file loop has been reached */
	boolean loopend;

	// public GPSNet() {
	//
	// }

	public RTCMClient(GPSNetSettings settings) {
		super();
		go = false;
		loopend = true;
		this.settings = settings;
	}

	@Override
	public String getDeviceId() {
		return null;
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
					while (!s.substring(0, 4).equals("RTCM")) {
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
	public int ready() {
		// TODO Auto-generated method stub
		return 0;
	}

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
			System.out.println(" \n %%%%%%%%%%%%%%%%%%%%% \n password >>> "
					+ settings.getAuthbase64());
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
			while (go && state != 5) {
				int c = in.read();
				if (c < 0)
					break;
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
				System.out
						.println(settings.getSource()
								+ "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n >>>> Header success: OK");
			} else {
				// showErrorMessage(settings.getSource(), "Error");
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
			// tester.println("<" + settings.getSource() +
			// "%%%%%%%%%%%% >>>> Connection Error  : Data is empty");
			System.out.println("<" + settings.getSource()
					+ "%%%%%%%%%%%% >>>> Connection Error  : Data is empty");
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

	@Override
	public void start() {
		dataThread = new Thread(this);
		dataThread.start();
	}

	/** stops the execution of this thread */
	@Override
	public void stop() {
		go = false;
		// tester.println("<" + settings.deviceId + ">stopping..");
	}

	/** returns true if the data thread still is alive */
	@Override
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

}
