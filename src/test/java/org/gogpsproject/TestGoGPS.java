/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni. All Rights Reserved.
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
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.*;

import org.gogpsproject.parser.rinex.RinexNavigation;
import org.gogpsproject.parser.rinex.RinexNavigationParser;
import org.gogpsproject.parser.rinex.RinexObservationParser;
import org.gogpsproject.parser.rtcm3.RTCM3Client;
import org.gogpsproject.parser.sp3.SP3Navigation;
import org.gogpsproject.parser.ublox.SerialConnection;
import org.gogpsproject.parser.ublox.UBXFileReader;
import org.gogpsproject.producer.KmlProducer;

/**
 * @author ege, Cryms.com
 *
 */
public class TestGoGPS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int dynamicModel = GoGPS.DYN_MODEL_CONST_SPEED;
		try{
			// Get current time
			long start = System.currentTimeMillis();
			/* Como */
//			ObservationsProducer roverIn = new RinexObservationParser(new File("./data/perim2.08o"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/COMO1190.08o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/COMO1190.08n"));
//			//NavigationProducer navigationIn = new SP3Navigation(SP3Navigation.IGN_FR_FINAL);
//			//NavigationProducer navigationIn = new RinexNavigation(RinexNavigation.GARNER_NAVIGATION_AUTO);

			/* Como, Italy (static) */
//			dynamicModel = GoGPS.DYN_MODEL_STATIC;
//			ObservationsProducer roverIn = new RinexObservationParser(new File("./data/como_pillar_rover.obs"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/como_pillar_master.10o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/como_pillar_rover.nav"));

			/* Sardinia, Italy */
//			ObservationsProducer roverIn = new RinexObservationParser(new File("./data/goCerchio_rover.obs"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/sard0880.10o"));
//			//NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/sard0880.10n"));
//			NavigationProducer navigationIn = new RinexNavigation(RinexNavigation.GARNER_NAVIGATION_ZIM2);

			/* Osaka, Japan (static) */
//			dynamicModel = GoGPS.DYN_MODEL_STATIC;
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/COM10_100608_024314.ubx"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/vrs.10o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/vrs.10n"));

//			dynamicModel = GoGPS.DYN_MODEL_STATIC;
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/COM10_100617_025543.ubx"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/vrs2.10o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/vrs2.10n"));
//			NavigationProducer navigationIn = new RinexNavigation(RinexNavigation.GARNER_NAVIGATION_AUTO);

//			/* Locarno, Switzerland */
			ObservationsProducer roverIn = new RinexObservationParser(new File("./data/locarno1_rover_RINEX.obs"));
			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/VirA061N.10o"));
			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/VirA061N.10n"));
			//NavigationProducer navigationIn = new RinexNavigation(RinexNavigation.GARNER_NAVIGATION_AUTO);

			/* Faido */
			//ObservationsProducer roverIn = new RinexObservationParser(roverFileObs);
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009843324860.ubx"));
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009843888879.ubx"));
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009844950228.ubx"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/VirFaido19112010b.10o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/VirFaido19112010b.10n"));
//			NavigationProducer navigationIn = new RinexNavigation(RinexNavigation.GARNER_NAVIGATION_AUTO);

			/* Manno, Switzerland (static)*/
//			dynamicModel = GoGPS.DYN_MODEL_STATIC;
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/manno-21.11.2010.ubx"));
//			ObservationsProducer masterIn = new RinexObservationParser(new File("./data/VirManno-21-11-2010.10o"));
//			NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/VirManno-21-11-2010.10n"));

			// 1st init
			navigationIn.init();
			roverIn.init();
			masterIn.init();

			// Name KML file name using Timestamp
			Date date = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
			String date1 = sdf1.format(date);
			String outPath = "./test/" + date1 + ".kml";
			KmlProducer kml = new KmlProducer(outPath);

			GoGPS goGPS = new GoGPS(navigationIn, roverIn, masterIn);
			goGPS.addPositionConsumerListener(kml);
			goGPS.setDynamicModel(dynamicModel);
			// goGPS.runCodeStandalone();
			// goGPS.runCodeDoubleDifferences();
			goGPS.runKalmanFilter();

			try{
				roverIn.release(true,10000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}
			try{
				masterIn.release(true,10000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}
			try{
				navigationIn.release(true,10000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}

			// Get and display elapsed time
			int elapsedTimeSec = (int) Math.floor((System.currentTimeMillis() - start) / 1000);
			int elapsedTimeMillisec = (int) ((System.currentTimeMillis() - start) - elapsedTimeSec * 1000);
			System.out.println("\nElapsed time (read + proc + display + write): "
					+ elapsedTimeSec + " seconds " + elapsedTimeMillisec
					+ " milliseconds.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
