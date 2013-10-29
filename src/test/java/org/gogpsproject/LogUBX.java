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
		parser.addArgument("-e", "--eph")
				//.choices("true", "false").setDefault("false")
				.help("Request and log ephemerides");
		parser.addArgument("-i", "--ion")
				//.choices("true", "false").setDefault("false")
				.help("Request and log ionospheric parameters");
		parser.addArgument("port").nargs("*")
				.help("Ports connected to u-blox receivers");
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		try{
			
//			if(args.length<1){
//				System.out.println("Usage example: logubx -lognav -lognmea <COM10> <COM16>");
//				
//				UBXSerialConnection.getPortList();
//				return;
//			}
			
			for (String portId : ns.<String> getList("port")) {
				UBXSerialConnection ubxSerialConn = new UBXSerialConnection(portId, 9600);
				ubxSerialConn.init();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
