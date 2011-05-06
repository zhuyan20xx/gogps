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
import org.gogpsproject.parser.ublox.UBXAssistNow;
import org.gogpsproject.parser.ublox.UBXFileReader;
import org.gogpsproject.producer.KmlProducer;

/**
 * @author ege, Cryms.com
 *
 */
public class LiveTracking {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int dynamicModel = GoGPS.DYN_MODEL_CONST_SPEED;
		try{
			// Get current time
			long start = System.currentTimeMillis();

			// Realtime
			if(args.length<2){
				System.out.println("GoGPS <rtcm_user> <rtcm_pass> [<ubx_user> <ubx_pass>]");
				return;
			}

			/******************************************
			 * ROVER & NOVIGATION uBlox
			 */
			BufferedRover roverIn = new BufferedRover();
			NavigationProducer navigationIn = roverIn;
			roverIn.init();
			SerialConnection serialConn = new SerialConnection(roverIn);
			serialConn.connect("COM10", 9600);

			if(args.length>3){
				String cmd="aid";
				String lon="8.92";
				String lat="46.03";
				navigationIn = new UBXAssistNow( args[2], args[3], cmd, lon, lat);
				try {
					navigationIn.init();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Use UBXAssistNow as Navigation");
			}
			// wait for some data to buffer
			Thread.sleep(5000);

			/******************************************
			 * compute approx position in stand-alone mode
			 */
			GoGPS goGPSstandalone = new GoGPS(navigationIn, roverIn, null);
			goGPSstandalone.setDynamicModel(dynamicModel);
			// retrieve initial position, do not need to be precise
			Coordinates initialPosition = goGPSstandalone.runCodeStandalone(10);

			/******************************************
			 * MASTER RTCM/RINEX
			 */
			RTCM3Client masterIn = RTCM3Client.getInstance("www3.swisstopo.ch", 8080, args[0].trim(), args[1].trim(), "swiposGISGEO_LV03LN02");
			//navigationIn = new RinexNavigation(RinexNavigation.IGN_NAVIGATION_HOURLY_ZIM2);
			masterIn.setApproxPosition(initialPosition);
			masterIn.init();

			/******************************************
			 * compute precise position in Kalman filter mode
			 */
			GoGPS goGPS = new GoGPS(navigationIn, roverIn, masterIn);
			goGPSstandalone.setDynamicModel(dynamicModel);

			// set Output
			Date date = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
			String date1 = sdf1.format(date);
			String outPath = "./test/" + date1 + ".kml";
			KmlProducer kml = new KmlProducer(outPath);
			goGPS.addPositionConsumerListener(kml);

			// goGPS.runCodeDoubleDifferences();
			// run blocking (never exit in live-tracking)
			// goGPS.runKalmanFilter();

			// run in background
			goGPS.runThreadMode(GoGPS.RUN_MODE_KALMAN_FILTER);

			// wait for 1 minutes
			Thread.sleep(10*1000);

			System.out.println("OK give up ---------------------------------------------");

			/******************************************
			 * END
			 */
			try{
				System.out.println("Stop Rover");
				serialConn.disconnect();
				roverIn.release(true,10000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}
			try{
				System.out.println("Stop Master");
				masterIn.release(true,10000);
			}catch(InterruptedException ie){
				ie.printStackTrace();
			}
			try{
				System.out.println("Stop Navigation");
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
