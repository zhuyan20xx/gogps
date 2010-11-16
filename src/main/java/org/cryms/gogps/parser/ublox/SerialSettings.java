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

//import javax.comm.* ;  // Sun serial port driver
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.util.Properties;
public class SerialSettings {

	public static SerialSettings loadSettings(Properties sets,
			CommPortIdentifier port) throws NumberFormatException {
		// The resulting settings object
		SerialSettings result = new SerialSettings();
		result.portId = port;

		// The name of the port
		String n = port.getName();

		result.deviceId = sets.getProperty(n + ".device", "");
		result.baud = Integer.parseInt(sets.getProperty(n + ".baud", "9600"));
		result.databits = Integer.parseInt(sets.getProperty(n + ".databits", ""
				+ SerialPort.DATABITS_6));
		result.stopbits = Integer.parseInt(sets.getProperty(n + ".stopbits", ""
				+ SerialPort.STOPBITS_1));
		result.parity = Integer.parseInt(sets.getProperty(n + ".parity", ""
				+ SerialPort.PARITY_NONE));
		result.useDevice = Boolean
				.valueOf(sets.getProperty(n + ".use", "true")).booleanValue();
		result.startText = sets.getProperty(n + ".starttext", "");
		result.stopText = sets.getProperty(n + ".stoptext", "");
		result.textToBytes = Boolean.valueOf(
				sets.getProperty(n + ".textToBytes", "false")).booleanValue();

		return result;
	}

	/** Name for the device connected to the port */
	public String deviceId;
	/** Port settings */
	public int baud, databits, stopbits, parity;
	/** Port identifier */
	public CommPortIdentifier portId;
	/** if true, the device should be used, otherwise it should not be used */
	public boolean useDevice;
	/** text send to start and stop the datatransmission for the device */
	public String startText, stopText;

	/** if true start and stop texts are send as bytes */
	public boolean textToBytes;

	/** For internal use only. Creates a SerialSettings with no parameters set */
	private SerialSettings() {
	}

	/** Creates a new instance of SerialSettings with start and stop texts */
	public SerialSettings(String deviceId, CommPortIdentifier portId, int baud,
			int databits, int stopbits, int parity, boolean useDevice,
			String startText, String stopText, boolean textToByte) {
		this.deviceId = deviceId;
		this.portId = portId;
		this.baud = baud;
		this.databits = databits;
		this.stopbits = stopbits;
		this.parity = parity;
		this.useDevice = useDevice;
		this.startText = startText;
		this.stopText = stopText;
		this.textToBytes = textToBytes;
	}

	/**
	 * Writes the settings to a Properties instance
	 * 
	 * @param sets
	 *            the Properties instance written to
	 */
	public void saveSettings(Properties sets) {
		String n = portId.getName();
		sets.setProperty(n + ".device", deviceId);
		sets.setProperty(n + ".baud", Integer.toString(baud));
		sets.setProperty(n + ".databits", Integer.toString(databits));
		sets.setProperty(n + ".stopbits", Integer.toString(stopbits));
		sets.setProperty(n + ".parity", Integer.toString(parity));
		sets.setProperty(n + ".use", Boolean.toString(useDevice));
		sets.setProperty(n + ".starttext", startText);
		sets.setProperty(n + ".stoptext", stopText);
		sets.setProperty(n + ".textToBytes", Boolean.toString(textToBytes));
	}

	/**
	 * sets the port. Should be used only after loading settings.
	 */
	private void setPort(CommPortIdentifier portId) {
		this.portId = portId;
	}
}
