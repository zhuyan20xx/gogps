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
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Reference.java
 *
 * Created on 21. marts 2005, 16:51
 */

/**
 * Implements InputDevice Recives data from a serial device. Settings need to
 * set at creation, but the port is first opened when the data recival is
 * started with the start() method
 * 
 * 
 * @author Ander Boeck Jensen, s031974
 * @author Bjarne Mathiesen, s022518
 */
public class SerialDevice implements SerialPortEventListener {
	// Port devices
	private CommPortIdentifier portId;
	// private Enumeration portList;
	private InputStream in;
	private OutputStream out;
	private SerialPort serialPort;
	private Thread readThread;
	private SerialSettings settings;
	boolean go = false;
	// Parametres for the port
	private int baud, databits, stopbits, parity;

	/**
	 * Creates a new instance of SerialDevice Uses default Serial port
	 * settings:<B> 9600 baud, 8 databits, stopbit = 1, none praity
	 */

	public SerialDevice(SerialSettings settings) {
		super();
		this.settings = settings;
		baud = settings.baud;
		databits = settings.databits;
		stopbits = settings.stopbits;
		parity = settings.parity;
		portId = settings.portId;
		go = false;
	}

	public SerialDevice(SerialSettings settings, boolean save) {
		// super(save);
		this.settings = settings;
		baud = settings.baud;
		databits = settings.databits;
		stopbits = settings.stopbits;
		parity = settings.parity;
		portId = settings.portId;
		go = false;
	}

	public void finialize() {
		if (serialPort != null) {
			stop();
		}
	}

	public String getDeviceId() {
		return settings.deviceId;
	}

	/**
	 * reads a full messages from the buffer<B> uses CRWBuffer.readMessage()
	 */
	// public int[] readMessage() throws BufferUnderrunException
	// {
	// return outputBuffer.readMessage();
	// }

	/**
	 * returns the number of messages ready for reading<B> uses
	 * CRWBuffer.ready()
	 */
	// public int ready()
	// {
	// return outputBuffer.ready();
	// }

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
			// tester.serialEventName(" Break interrupt - " + getDeviceId());
			break;
		case SerialPortEvent.OE:
			// tester.serialEventName(" Overrun error - " + getDeviceId());
			break;
		case SerialPortEvent.FE:
			// tester.serialEventName(" Framing error - " + getDeviceId());
			break;
		case SerialPortEvent.PE:
			// tester.serialEventName(" Parity error - " + getDeviceId());
			break;
		case SerialPortEvent.CD:
			// tester.serialEventName(" Carrier detect - " + getDeviceId());
			break;
		case SerialPortEvent.CTS:
			// tester.serialEventName(" Clear to send - " + getDeviceId());
			break;
		case SerialPortEvent.DSR:
			// tester.serialEventName(" Data set ready - " + getDeviceId());
			break;
		case SerialPortEvent.RI:
			// tester.serialEventName(" Ring indicator - " + getDeviceId());
			break;
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			// tester.serialEventName(" Output buffer empty  - "
			// + getDeviceId());
			break;
		}

		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			// try
			// {
			// //readLoop(in);
			// }
			// catch (IOException e)
			// {
			// // showErrorMessage(getDeviceId(),
			// "Enheden kunne ikke initialiserer");
			// // tester.exceptionPrint(e);
			// stop();
			//                
			// }
		}

	}

	/**
	 * starts reciving data<B> This method will open a port and start the thread
	 * for reciving data Recived data will be buffered for reading
	 * 
	 * @see #readMessage
	 * @see #ready
	 */
	public void start() {
		// The device should only start if it is not currently started
		if (!go)
			try {
				// The Serial port is opened using "dGPS Server" as owner name
				// and
				// 2000 ms as timeout.
				serialPort = (SerialPort) portId.open("dGPS Server", 2000);
				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

				// Exceptions is ignored

				// This is added as Eventlistener, so we get the data when ready
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				// The parametres are set
				serialPort
						.setSerialPortParams(baud, databits, stopbits, parity);

				// If a start text is specified then it will be send
				if (!settings.startText.equals("")) {
					out.write(settings.startText.getBytes());
					out.flush();
				}

				go = true;
			}
			// Errors caugth: PortInUseException, IOException,
			// TooManyListenersException UnsupportedCommOperationException
			catch (Exception e) {
				serialPort.close();
				serialPort = null;
			}

		// The thread for reading is started and this method returns
	}

	/** stops the execution of this thread */
	public void stop() {
		// super.stop();
		go = false;
		// tester.println("<" + settings.deviceId + ">stopping");

		// Thread is stopped
		try {
			readThread.interrupt();
		} catch (Exception e) {
		}

		if (settings.stopText != null && settings.stopText != "") {
			// If a stop text is specified then it will be send
			try {
				out.write(settings.stopText.getBytes());
				out.flush();
			} catch (IOException ex) {
			}
			// Exceptions is ignored
		}

		try {
			// Event listener is removed and the port and the streams are closed
			// If the port failed to initialize, then serialPort will be null
			// and NullPointerException is thrown. But then the were never
			// created and they need not be closed
			serialPort.removeEventListener();
			serialPort.close();
			in.close();
			out.close();
		} catch (Exception e) {
		}

		in = null;
		out = null;
		serialPort = null;
	}

	/** returns true if the device data looped are stopped */
	public boolean stopped() {
		return !go;
	}

}
