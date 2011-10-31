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

package org.gogpsproject;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.parser.rtcm3.RTCM3Client;
import org.gogpsproject.parser.ublox.UBXSerialConnection;
import org.gogpsproject.producer.rinex.RinexV2Producer;

@SuppressWarnings("restriction")
public class TestRTCM3 {

	/**
	 * @param args
	 */



	public static void main(String[] args) {



		try {
			RTCM3Client rtcm = RTCM3Client.getInstance(args[0], Integer.parseInt(args[1]), args[2],args[3], args[4]);
			//RTCM3Client rtcm = RTCM3Client.getInstance("ntrip.jenoba.jp", 80, args[0],args[1], "JVR30");
			rtcm.setDebug(true);
			// Ntrip-GAA: $GPGGA,183836,3435.524,N,13530.231,E,4,10,1,164,M,1,M,3,0*69
			// CH Manno
			Coordinates coordinates = Coordinates.globalXYZInstance(4382366.510741806,687718.046802147,4568060.791344867);
			// JP Osaka
			//Coordinates coordinates = Coordinates.globalXYZInstance(-3749314.940644724,3684015.867703885,3600798.5084946174);
			rtcm.setVirtualReferenceStationPosition(coordinates);
			rtcm.init();

			// log rinex format
			RinexV2Producer rinexOut = new RinexV2Producer("./data/test-rinex.11o",true);
			rtcm.addStreamEventListener(rinexOut);


			ObservationsBuffer ob = new ObservationsBuffer(rtcm,"./data/test-rtcm.dat");
			ob.init();


			Thread.sleep(60*1000);

			rtcm.release(true, 10*1000);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

//		System.out.println(computeNMEACheckSum("$GPGGA,200530,4600,N,00857,E,4,10,1,200,M,1,M,3,0"));
//
//		File f = new File("./data/rtcm.out");
//		try {
//			FileInputStream fis = new FileInputStream(f);
//			RTCM3Client cl = new RTCM3Client(null);
//			cl.go = true;
//			cl.debug = true;
//			cl.readLoop(fis);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}


	}

}
