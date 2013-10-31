/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
 *
 */
package org.gogpsproject;
import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * @author Eugenio Realini, Cryms.com
 *
 */
public class LogUBX {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("LogUBX")
				.defaultHelp(true)
				.description("Log binary streams from one or more u-blox receivers connected to COM ports.");
		parser.addArgument("-s", "--showCOMports")
				.action(Arguments.storeTrue())
				.help("Display available COM ports");
		parser.addArgument("-e", "--ephemeris")
				.action(Arguments.storeTrue())
				.help("Request and log ephemeris (AID-EPH message)");
		parser.addArgument("-i", "--ionosphere")
				.action(Arguments.storeTrue())
				.help("Request and log ionospheric parameters (AID-HUI message)");
		parser.addArgument("-n", "--nmea")
				.choices("GGA", "GSV", "RMC").setDefault("false")
				.help("Enable and log NMEA sentences");
		parser.addArgument("port").nargs("*")
				.help("COM port(s) connected to u-blox receivers (e.g. COM3 COM10)");
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		try{
			
			if ((Boolean) ns.get("showCOMports")) {
				UBXSerialConnection.getPortList();
				return;
			} else if (ns.<String> getList("port").isEmpty()) {
				parser.printHelp();
				return;
			}
			
			for (String portId : ns.<String> getList("port")) {
				UBXSerialConnection ubxSerialConn = new UBXSerialConnection(portId, 9600);
				ubxSerialConn.enableEphemeris((Boolean) ns.get("ephemeris"));
				ubxSerialConn.enableIonoParam((Boolean) ns.get("ionosphere"));
				ubxSerialConn.init();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
