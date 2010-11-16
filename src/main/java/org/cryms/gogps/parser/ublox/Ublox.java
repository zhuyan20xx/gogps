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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

public class Ublox {

	public static int speed = 9600;
	private static SerialConnection network;
	private static boolean resend_active = false;

	/**
	 * @param args
	 */

	public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts
					.nextElement();
			switch (com.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				try {
					CommPort thePort = com.open("CommUtil", 50);
					thePort.close();
					h.add(com);
				} catch (PortInUseException e) {
					System.out.println("Port, " + com.getName()
							+ ", is in use.");
				} catch (Exception e) {
					System.err.println("Failed to open port " + com.getName());
					e.printStackTrace();
				}
			}
		}
		return h;
	}

	public static void main(String[] args) {

		network = new SerialConnection(0);
		Vector<String> ports = network.getPortList();
		if (ports.size() > 0) {
			System.out
					.println("the following serial ports have been detected:");
		} else {
			System.out
					.println("sorry, no serial ports were found on your computer\n");
			System.exit(0);
		}
		for (int i = 0; i < ports.size(); ++i) {
			System.out.println("    " + Integer.toString(i + 1) + ":  "
					+ ports.elementAt(i));
		}
		if (network.connect(ports.elementAt(2), speed)) {
			System.out.println();
		} else {
			System.out.println("sorry, there was an error connecting\n");
			System.exit(1);
		}

		// // TODO Auto-generated method stub
		// HashSet<CommPortIdentifier> ha = new HashSet<CommPortIdentifier>();
		// ha = getAvailableSerialPorts();
		// while (ha.iterator().hasNext()) {
		// System.out.println(" port : " + ha.iterator().next().getPortType());
		// }
		//
		// }

	}

}
