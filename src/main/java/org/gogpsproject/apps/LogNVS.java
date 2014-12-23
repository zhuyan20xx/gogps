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
package org.gogpsproject.apps;
import java.util.Locale;
import java.util.Vector;

import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.parser.nvs.NVSSerialConnection;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * @author Eugenio Realini, Cryms.com
 *
 */
public class LogNVS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//force dot as decimal separator
		Locale.setDefault(new Locale("en", "US"));
		
		ArgumentParser parser = ArgumentParsers.newArgumentParser("LogNVS")
				.defaultHelp(true)
				.description("Log binary streams from one or more NVS receivers connected to COM ports.");
		parser.addArgument("-s", "--showCOMports")
				.action(Arguments.storeTrue())
				.help("display available COM ports");
		parser.addArgument("-r", "--rate")
				.choices(1, 2, 5, 10).setDefault(1)
				.type(Integer.class)
				.help("set the measurement rate (in Hz)");
		parser.addArgument("-t", "--timetag")
				.action(Arguments.storeTrue())
				.help("log the system time when RXM-RAW messages are received");
		parser.addArgument("-xo", "--rinexobs")
		        .action(Arguments.storeTrue())
		        .help("write a RINEX observation file while logging");
		parser.addArgument("-d", "--debug")
                .action(Arguments.storeTrue())
                .help("show warning messages for debugging purposes");
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
				NVSSerialConnection.getPortList(true);
				return;
			} else if (ns.<String> getList("port").isEmpty()) {
				parser.printHelp();
				return;
			}
			
			Vector<String> availablePorts = NVSSerialConnection.getPortList(false);
			for (String portId : ns.<String> getList("port")) {
				if (!availablePorts.contains(portId)) {
					System.out.println("Error: port "+portId+" is not available.");
					NVSSerialConnection.getPortList(true);
					return;
				}
			}
			
			for (String portId : ns.<String> getList("port")) {
				
				NVSSerialConnection nvsSerialConn = new NVSSerialConnection(portId, 115200);
				
				nvsSerialConn.setMeasurementRate((Integer) ns.get("rate"));
				nvsSerialConn.enableTimetag(ns.getBoolean("timetag"));
				nvsSerialConn.enableRinexObs(ns.getBoolean("rinexobs"));
				nvsSerialConn.enableDebug(ns.getBoolean("debug"));
				nvsSerialConn.init();
				
				new ObservationsBuffer(nvsSerialConn, null);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
