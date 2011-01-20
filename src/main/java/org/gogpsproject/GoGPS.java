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
import java.util.*;
import java.text.*;

import org.gogpsproject.parser.rinex.RinexNavigation;
import org.gogpsproject.parser.rinex.RinexNavigationParser;
import org.gogpsproject.parser.rinex.RinexObservationParser;
import org.gogpsproject.parser.rtcm3.RTCM3Client;
import org.gogpsproject.parser.sp3.SP3Navigation;
import org.gogpsproject.parser.ublox.BufferedUBXRover;
import org.gogpsproject.parser.ublox.SerialConnection;
import org.gogpsproject.parser.ublox.UBXFileReader;

// TODO: Auto-generated Javadoc
/**
 * The Class GoGPS.
 *
 * @author ege, Cryms.com
 */
public class GoGPS {

	/** The f. */
	private static DecimalFormat f = new DecimalFormat("0.000");

	/** The g. */
	private static DecimalFormat g = new DecimalFormat("0.00000000");

	// Frequency selector
	/** The Constant FREQ_L1. */
	public final static int FREQ_L1 = ObservationSet.L1;

	/** The Constant FREQ_L2. */
	public final static int FREQ_L2 = ObservationSet.L2;

	/** The freq. */
	private int freq = FREQ_L1;

	// Double-frequency flag
	/** The dual freq. */
	private boolean dualFreq = false;

	// Weighting strategy
	// 0 = same weight for all observations
	// 1 = weight based on satellite elevation
	// 2 = weight based on signal-to-noise ratio
	// 3 = weight based on combined elevation and signal-to-noise ratio
	/** The Constant WEIGHT_EQUAL. */
	public final static int WEIGHT_EQUAL = 0;

	/** The Constant WEIGHT_SAT_ELEVATION. */
	public final static int WEIGHT_SAT_ELEVATION = 1;

	/** The Constant WEIGHT_SIGNAL_TO_NOISE_RATIO. */
	public final static int WEIGHT_SIGNAL_TO_NOISE_RATIO = 2;

	/** The Constant WEIGHT_COMBINED_ELEVATION_SNR. */
	public final static int WEIGHT_COMBINED_ELEVATION_SNR = 3;

	/** The weights. */
	private int weights = WEIGHT_SIGNAL_TO_NOISE_RATIO;


	/** The Constant DYN_MODEL_STATIC. */
	public final static int DYN_MODEL_STATIC = 1;

	/** The Constant DYN_MODEL_CONST_SPEED. */
	public final static int DYN_MODEL_CONST_SPEED = 2;

	/** The Constant DYN_MODEL_CONST_ACCELERATION. */
	public final static int DYN_MODEL_CONST_ACCELERATION = 3;
	// Kalman filter parameters
	/** The dynamic model. */
	private int dynamicModel = DYN_MODEL_CONST_SPEED;

	/** The st dev init. */
	private double stDevInit = 3;

	/** The st dev e. */
	private double stDevE = 0.5;

	/** The st dev n. */
	private double stDevN = 0.5;

	/** The st dev u. */
	private double stDevU = 0.1;

	/** The st dev code c. */
	private double stDevCodeC = 3;

	/** The st dev code p. */
	private double[] stDevCodeP;

	/** The st dev phase. */
	private double stDevPhase = 0.03;

	/** The st dev ambiguity. */
	private double stDevAmbiguity = 10;

	/** The min num sat. */
	private int minNumSat = 2;

	/** The cycle slip threshold. */
	private double cycleSlipThreshold = 3;

	/** The Elevation cutoff. */
	private double cutoff = 15; // Elevation cutoff

	/** The navigation. */
	private NavigationProducer navigation;

	/** The rover in. */
	private ObservationsProducer roverIn;

	/** The master in. */
	private ObservationsProducer masterIn;

	/**
	 * Instantiates a new go gps.
	 *
	 * @param navigation the navigation
	 * @param roverIn the rover in
	 * @param masterIn the master in
	 */
	public GoGPS(NavigationProducer navigation, ObservationsProducer roverIn, ObservationsProducer masterIn){

		stDevCodeP = new double[2];
		stDevCodeP[0] = 0.6;
		stDevCodeP[1] = 0.4;

		this.navigation = navigation;
		this.roverIn = roverIn;
		this.masterIn = masterIn;
	}

	/**
	 * Run code standalone.
	 */
	public void runCodeStandalone() {
		runCodeStandalone(-1);
	}

	/**
	 * Run code standalone.
	 *
	 * @param getNthPosition the get nth position
	 * @return the coordinates
	 */
	public Coordinates runCodeStandalone(int getNthPosition) {

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Name KML file name using Timestamp
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		String outPath = "./test/" + date1 + ".kml";

		try {
			SimpleDateFormat timeKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			FileWriter out = new FileWriter(outPath);
			// Write KML header part
			//			out.write("<kml xmlns=\"http://earth.google.com/kml/2.0\">"
			//							+ "<Folder><name>Track Log Export</name><description>Exported on "
			//							+ date
			//							+ " </description>"
			//							+ "<Style id=\"line\"><LineStyle><color>7fff0000</color><width>5</width></LineStyle></Style>"
			//							+ "<Placemark><name>Raw Data</name><description>by Daisuke Yoshida, Tezukayama Gakuin University, JAPAN</description>"
			//							+ "<styleUrl>#line</styleUrl><altitudeMode>relativeToGround</altitudeMode><LineString><coordinates>");
			String timeline = "<Folder><open>1</open><Style><ListStyle><listItemType>checkHideChildren</listItemType></ListStyle></Style>";

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
					"<Document xmlns:kml=\"http://earth.google.com/kml/2.1\">"+
					"  <Style id=\"SMExport_3_ff0000e6_fffefefe\"><LineStyle><color>ff0000e6</color><width>3</width></LineStyle><PolyStyle><color>fffefefe</color></PolyStyle></Style>"+
					"  <Style id=\"dot-icon\"><IconStyle><Icon><href>http://www.eriadne.org/icons/MapPointer.png</href></Icon></IconStyle></Style>"+
					"  <Placemark>"+
					"    <name></name>"+
					"    <description></description>"+
					"    <styleUrl>#SMExport_3_ff0000e6_fffefefe</styleUrl>"+
					"    <LineString>"+
					"      <tessellate>1</tessellate>"+
			"      <coordinates>");
			out.flush();

			Observations obsR = roverIn.nextObservations();
			while (obsR!=null) { // buffStreamObs.ready()

				try{
					// If there are at least four satellites
					if (roverIn.getCurrentObservations().getGpsSize() >= 4) { // gps.length
						System.out.println("OK "+roverIn.getCurrentObservations().getGpsSize()+" satellites");
						// Compute approximate positioning by Bancroft algorithm
						roverPos.bancroft(roverIn.getCurrentObservations());

						// If an approximate position was computed
						System.out.println("has valid position? "+roverPos.isValidXYZ()+" x:"+roverPos.getX()+" y:"+roverPos.getY()+" z:"+roverPos.getZ());
						if (roverPos.isValidXYZ()) {

							// Select satellites available for double differences
							roverPos.selectSatellitesStandalone(roverIn.getCurrentObservations());

							// Compute code stand-alone positioning (epoch-by-epoch
							// solution)
							roverPos.codeStandalone(roverIn.getCurrentObservations());

							try {
								System.out.println("Code standalone positioning:");
								System.out.println("GPS time:	" + roverIn.getCurrentObservations().getRefTime().getGpsTime());
								System.out.println("Lon:		" + g.format(roverPos.getGeodeticLongitude())); // geod.get(0)
								System.out.println("Lat:		" + g.format(roverPos.getGeodeticLatitude())); // geod.get(1)
								System.out.println("h:		" + f.format(roverPos.getGeodeticHeight())); // geod.get(2)
								out.write(g.format(roverPos.getGeodeticLongitude()) + "," // geod.get(0)
										+ g.format(roverPos.getGeodeticLatitude()) + "," // geod.get(1)
										+ f.format(roverPos.getGeodeticHeight()) + " \n"); // geod.get(2)
								out.flush();
							} catch (NullPointerException e) {
								System.out.println("Error: rover approximate position not computed");
							}
							System.out.println("-------------------- "+getNthPosition);
							if(getNthPosition>0){
								getNthPosition--;
								if(getNthPosition==0)return roverPos;
							}
						}
					}
				}catch(Exception e){
					System.out.println("Could not complete due to "+e);
				}
				obsR = roverIn.nextObservations();
			}
			// Write KML footer part
			// out.write("</coordinates><altitudeMode>absolute</altitudeMode></LineString></Placemark></Folder></kml>");
			out.write("</coordinates></LineString></Placemark>"+timeline+"</Folder></Document>\n");
			// Close FileWriter
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return roverPos;
	}

	/**
	 * Run code double differences.
	 */
	public void runCodeDoubleDifferences() {

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Name KML file name using Timestamp
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		String outPath = "./test/" + date1 + ".kml";

		try {
			SimpleDateFormat timeKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			FileWriter out = new FileWriter(outPath);
			// Write KML header part
			//			out.write("<kml xmlns=\"http://earth.google.com/kml/2.0\">"
			//							+ "<Folder><name>Track Log Export</name><description>Exported on "
			//							+ date
			//							+ " </description>"
			//							+ "<Style id=\"line\"><LineStyle><color>7fff0000</color><width>5</width></LineStyle></Style>"
			//							+ "<Placemark><name>Raw Data</name><description>by Daisuke Yoshida, Tezukayama Gakuin University, JAPAN</description>"
			//							+ "<styleUrl>#line</styleUrl><altitudeMode>relativeToGround</altitudeMode><LineString><coordinates>");
			String timeline = "<Folder><open>1</open><Style><ListStyle><listItemType>checkHideChildren</listItemType></ListStyle></Style>";

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
					"<Document xmlns:kml=\"http://earth.google.com/kml/2.1\">"+
					"  <Style id=\"SMExport_3_ff0000e6_fffefefe\"><LineStyle><color>ff0000e6</color><width>3</width></LineStyle><PolyStyle><color>fffefefe</color></PolyStyle></Style>"+
					"  <Style id=\"dot-icon\"><IconStyle><Icon><href>http://www.eriadne.org/icons/MapPointer.png</href></Icon></IconStyle></Style>"+
					"  <Placemark>"+
					"    <name></name>"+
					"    <description></description>"+
					"    <styleUrl>#SMExport_3_ff0000e6_fffefefe</styleUrl>"+
					"    <LineString>"+
					"      <tessellate>1</tessellate>"+
			"      <coordinates>");
			out.flush();

			Observations obsR = roverIn.nextObservations();
			Observations obsM = masterIn.nextObservations();
			while (obsR != null && obsM != null) {

				// Discard master epochs if correspondent rover epochs are
				// not available
				long obsRtime = obsR.getRefTime().getGpsTime();
				while (obsM!=null && obsR!=null && obsRtime > obsM.getRefTime().getGpsTime()) {
					obsM = masterIn.nextObservations();
				}

				// Discard rover epochs if correspondent master epochs are
				// not available
				long obsMtime = obsM.getRefTime().getGpsTime();
				while (obsM!=null && obsR!=null && roverIn.getCurrentObservations().getRefTime().getGpsTime() < obsMtime) {
					obsR = roverIn.nextObservations();
				}


				// If there are at least four satellites
				if (obsM!=null && obsR!=null){
					if(roverIn.getCurrentObservations().getGpsSize() >= 4) {

						// Compute approximate positioning by Bancroft algorithm
						roverPos.bancroft(roverIn.getCurrentObservations());

						// If an approximate position was computed
						if (roverPos.isValidXYZ()) {

							// Select satellites available for double differences
							roverPos.selectSatellitesDoubleDiff(roverIn.getCurrentObservations(),
									masterIn.getCurrentObservations(), masterIn.getApproxPosition());

							// Compute code double differences positioning
							// (epoch-by-epoch solution)
							roverPos.codeDoubleDifferences(roverIn.getCurrentObservations(),
									masterIn.getCurrentObservations(), masterIn.getApproxPosition());

							try {
								System.out.println("Code double difference positioning:");
								System.out.println("GPS time: " + roverIn.getCurrentObservations().getRefTime().getGpsTime());
								System.out.println("Lon:      " + g.format(roverPos.getGeodeticLongitude()));//geod.get(0)
								System.out.println("Lat:      " + g.format(roverPos.getGeodeticLatitude())); // geod.get(1)
								System.out.println("h:        " + f.format(roverPos.getGeodeticHeight())); // geod.get(2)
								out.write(g.format(roverPos.getGeodeticLongitude()) + "," // geod.get(0)
										+ g.format(roverPos.getGeodeticLatitude()) + "," // geod.get(1)
										+ f.format(roverPos.getGeodeticHeight()) + " \n"); // geod.get(2)
								out.flush();
							} catch (NullPointerException e) {
								System.out.println("Error: rover approximate position not computed");
							}
							System.out.println("--------------------");

						}
					}
				}
				// get next epoch
				obsR = roverIn.nextObservations();
				obsM = masterIn.nextObservations();
			}
			// Write KML footer part
			// out.write("</coordinates><altitudeMode>absolute</altitudeMode></LineString></Placemark></Folder></kml>");
			out.write("</coordinates></LineString></Placemark>"+timeline+"</Folder></Document>\n");
			// Close FileWriter
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run kalman filter.
	 */
	public void runKalmanFilter() {

		long timeRead = System.currentTimeMillis();
		long depRead = 0;

		long timeProc = 0;
		long depProc = 0;

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Flag to check if Kalman filter has been initialized
		boolean kalmanInitialized = false;

		// Name KML file name using Timestamp
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		String outPath = "./test/" + date1 + ".kml";
		// String outPath = "./test/output.kml";

		try {
			SimpleDateFormat timeKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			FileWriter out = new FileWriter(outPath);
			// Write KML header part
//			out.write("<kml xmlns=\"http://earth.google.com/kml/2.0\">"
//							+ "<Folder><name>Track Log Export</name><description>Exported on "
//							+ date
//							+ " </description>"
//							+ "<Style id=\"line\"><LineStyle><color>7fff0000</color><width>5</width></LineStyle></Style>"
//							+ "<Placemark><name>Raw Data</name><description>by Daisuke Yoshida, Tezukayama Gakuin University, JAPAN</description>"
//							+ "<styleUrl>#line</styleUrl><altitudeMode>relativeToGround</altitudeMode><LineString><coordinates>");
			String timeline = "<Folder><open>1</open><Style><ListStyle><listItemType>checkHideChildren</listItemType></ListStyle></Style>";

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				"<Document xmlns:kml=\"http://earth.google.com/kml/2.1\">"+
				"  <Style id=\"SMExport_3_ff0000e6_fffefefe\"><LineStyle><color>ff0000e6</color><width>3</width></LineStyle><PolyStyle><color>fffefefe</color></PolyStyle></Style>"+
				"  <Style id=\"dot-icon\"><IconStyle><Icon><href>http://www.eriadne.org/icons/MapPointer.png</href></Icon></IconStyle></Style>"+
				"  <Placemark>"+
				"    <name></name>"+
				"    <description></description>"+
				"    <styleUrl>#SMExport_3_ff0000e6_fffefefe</styleUrl>"+
				"    <LineString>"+
				"      <tessellate>1</tessellate>"+
				"      <coordinates>");
			out.flush();
			timeRead = System.currentTimeMillis() - timeRead;
			depRead = depRead + timeRead;

			Observations obsR = roverIn.nextObservations();
			Observations obsM = masterIn.nextObservations();
			System.out.println("R:"+obsR.getRefTime().getMsec()+" M:"+obsM.getRefTime().getMsec());
			int c=0;
			while (obsR != null && obsM != null) {

				timeRead = System.currentTimeMillis();

				// Discard master epochs if correspondent rover epochs are
				// not available
//				Observations obsR = roverIn.nextObservations();
//				Observations obsM = masterIn.nextObservations();
				long obsRtime = obsR.getRefTime().getGpsTime();
				//System.out.println("look for M "+obsRtime);
				while (obsM!=null && obsR!=null && obsRtime > obsM.getRefTime().getGpsTime()) {
//					masterIn.skipDataObs();
//					masterIn.parseEpochObs();
					obsM = masterIn.nextObservations();
				}
				//System.out.println("found M "+obsRtime);

				// Discard rover epochs if correspondent master epochs are
				// not available
				long obsMtime = obsM.getRefTime().getGpsTime();
				//System.out.println("look for R "+obsMtime);
				while (obsM!=null && obsR!=null && obsR.getRefTime().getGpsTime() < obsMtime) {
					obsR = roverIn.nextObservations();
				}
				//System.out.println("found R "+obsMtime);

				if(obsM!=null && obsR!=null){
					timeRead = System.currentTimeMillis() - timeRead;
					depRead = depRead + timeRead;
					timeProc = System.currentTimeMillis();

					// If Kalman filter was not initialized and if there are at
					// least four satellites
					boolean valid = true;
					if (!kalmanInitialized && obsR.getGpsSize() >= 4) {

						System.out.print("Try to init with bancroft ");

						// Compute approximate positioning by Bancroft algorithm
						roverPos.bancroft(obsR);

						// If an approximate position was computed
						if (roverPos.isValidXYZ()) {

							// Initialize Kalman filter
							roverPos.kalmanFilterInit(obsR, obsM, masterIn.getApproxPosition());

							kalmanInitialized = true;

							System.out.println("OK");
						}else{
							System.out.println("....nope");
						}
					} else if (kalmanInitialized) {

						// Do a Kalman filter loop
						try{
							roverPos.kalmanFilterLoop(obsR,obsM, masterIn.getApproxPosition());
						}catch(Exception e){
							e.printStackTrace();
							valid = false;
						}
					}

					timeProc = System.currentTimeMillis() - timeProc;
					depProc = depProc + timeProc;

					if(kalmanInitialized & valid){
						try {
							String lon = g.format(roverPos.getGeodeticLongitude());
							String lat = g.format(roverPos.getGeodeticLatitude());
							String h = f.format(roverPos.getGeodeticHeight());

							out.write(lon + "," // geod.get(0)
									+ lat + "," // geod.get(1)
									+ h + " \n"); // geod.get(2)
							out.flush();

							String t = timeKML.format(new Date(obsR.getRefTime().getMsec()));
							System.out.print("T:" + t);
							System.out.print(" Lon:" + lon);//geod.get(0)
							System.out.print(" Lat:" + lat);//geod.get(1)
							System.out.println(" H:" + h);//geod.get(2)
							if(c%10==0){

								//System.out.println("Positioning by Kalman filter on code and phase double differences:");


								timeline += "\n";
								timeline += "<Placemark>"+
						        "<TimeStamp>"+
						        "<when>"+t+"</when>"+
						        "</TimeStamp>"+
						        "<styleUrl>#dot-icon</styleUrl>"+
						        "<Point>"+
						        "<coordinates>"+lon + ","
								+ lat + ","
								+ h + "</coordinates>"+
						        "</Point>"+
						        "</Placemark>";
							}
						} catch (NullPointerException e) {
							System.out.println("Rover position not computed");
						}
					}
					//System.out.println("--------------------");

					System.out.println("-- Get next epoch ---------------------------------------------------");
					// get next epoch
					obsR = roverIn.nextObservations();
					obsM = masterIn.nextObservations();

					c++;
				}else{
					System.out.println("Missing M or R obs ");
				}


			}
			// Write KML footer part
			// out.write("</coordinates><altitudeMode>absolute</altitudeMode></LineString></Placemark></Folder></kml>");
			out.write("</coordinates></LineString></Placemark>"+timeline+"</Folder></Document>\n");
			// Close FileWriter
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int elapsedTimeSec = (int) Math.floor(depRead / 1000);
		int elapsedTimeMillisec = (int) (depRead - elapsedTimeSec * 1000);
		System.out.println("\nElapsed time (read): " + elapsedTimeSec
				+ " seconds " + elapsedTimeMillisec + " milliseconds.");

		elapsedTimeSec = (int) Math.floor(depProc / 1000);
		elapsedTimeMillisec = (int) (depProc - elapsedTimeSec * 1000);
		System.out.println("\nElapsed time (proc): " + elapsedTimeSec
				+ " seconds " + elapsedTimeMillisec + " milliseconds.");

	}


	/**
	 * Gets the freq.
	 *
	 * @return the freq
	 */
	public int getFreq() {
		return freq;
	}

	/**
	 * Sets the freq.
	 *
	 * @param freq the freq to set
	 */
	public void setFreq(int freq) {
		this.freq = freq;
	}

	/**
	 * Checks if is dual freq.
	 *
	 * @return the dualFreq
	 */
	public boolean isDualFreq() {
		return dualFreq;
	}

	/**
	 * Sets the dual freq.
	 *
	 * @param dualFreq the dualFreq to set
	 */
	public void setDualFreq(boolean dualFreq) {
		this.dualFreq = dualFreq;
	}

	/**
	 * Gets the cutoff.
	 *
	 * @return the cutoff
	 */
	public double getCutoff() {
		return cutoff;
	}

	/**
	 * Sets the cutoff.
	 *
	 * @param cutoff the cutoff to set
	 */
	public void setCutoff(double cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return dynamicModel;
	}

	/**
	 * Sets the order.
	 *
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.dynamicModel = order;
	}

	/**
	 * Gets the st dev init.
	 *
	 * @return the stDevInit
	 */
	public double getStDevInit() {
		return stDevInit;
	}

	/**
	 * Sets the st dev init.
	 *
	 * @param stDevInit the stDevInit to set
	 */
	public void setStDevInit(double stDevInit) {
		this.stDevInit = stDevInit;
	}

	/**
	 * Gets the st dev e.
	 *
	 * @return the stDevE
	 */
	public double getStDevE() {
		return stDevE;
	}

	/**
	 * Sets the st dev e.
	 *
	 * @param stDevE the stDevE to set
	 */
	public void setStDevE(double stDevE) {
		this.stDevE = stDevE;
	}

	/**
	 * Gets the st dev n.
	 *
	 * @return the stDevN
	 */
	public double getStDevN() {
		return stDevN;
	}

	/**
	 * Sets the st dev n.
	 *
	 * @param stDevN the stDevN to set
	 */
	public void setStDevN(double stDevN) {
		this.stDevN = stDevN;
	}

	/**
	 * Gets the st dev u.
	 *
	 * @return the stDevU
	 */
	public double getStDevU() {
		return stDevU;
	}

	/**
	 * Sets the st dev u.
	 *
	 * @param stDevU the stDevU to set
	 */
	public void setStDevU(double stDevU) {
		this.stDevU = stDevU;
	}

	/**
	 * Gets the st dev code.
	 *
	 * @param roverObsSet the rover observation set
	 * @param masterObsSet the master observation set
	 * @param i the selected GPS frequency
	 * @return the stDevCode
	 */
	public double getStDevCode(ObservationSet roverObsSet, ObservationSet masterObsSet, int i) {
		return (roverObsSet.isPseudorangeP(i) & masterObsSet.isPseudorangeP(i))?stDevCodeP[i]:stDevCodeC;
	}

	/**
	 * Gets the st dev code c.
	 *
	 * @return the stDevCodeC
	 */
	public double getStDevCodeC() {
		return stDevCodeC;
	}

	/**
	 * Sets the st dev code c.
	 *
	 * @param stDevCodeC the stDevCodeC to set
	 */
	public void setStDevCodeC(double stDevCodeC) {
		this.stDevCodeC = stDevCodeC;
	}

	/**
	 * Gets the st dev code p.
	 *
	 * @param i the selected GPS frequency
	 * @return the stDevCodeP
	 */
	public double getStDevCodeP(int i) {
		return stDevCodeP[i];
	}

	/**
	 * Sets the st dev code p.
	 *
	 * @param stDevCodeP the stDevCodeP to set
	 * @param i the selected GPS frequency
	 */
	public void setStDevCodeP(double stDevCodeP, int i) {
		this.stDevCodeP[i] = stDevCodeP;
	}

	/**
	 * Gets the st dev phase.
	 *
	 * @return the stDevPhase
	 */
	public double getStDevPhase() {
		return stDevPhase;
	}

	/**
	 * Sets the st dev phase.
	 *
	 * @param stDevPhase the stDevPhase to set
	 */
	public void setStDevPhase(double stDevPhase) {
		this.stDevPhase = stDevPhase;
	}

	/**
	 * Gets the st dev ambiguity.
	 *
	 * @return the stDevAmbiguity
	 */
	public double getStDevAmbiguity() {
		return stDevAmbiguity;
	}

	/**
	 * Sets the st dev ambiguity.
	 *
	 * @param stDevAmbiguity the stDevAmbiguity to set
	 */
	public void setStDevAmbiguity(double stDevAmbiguity) {
		this.stDevAmbiguity = stDevAmbiguity;
	}

	/**
	 * Gets the min num sat.
	 *
	 * @return the minNumSat
	 */
	public int getMinNumSat() {
		return minNumSat;
	}

	/**
	 * Sets the min num sat.
	 *
	 * @param minNumSat the minNumSat to set
	 */
	public void setMinNumSat(int minNumSat) {
		this.minNumSat = minNumSat;
	}

	/**
	 * Gets the cycle slip threshold.
	 *
	 * @return the cycle slip threshold
	 */
	public double getCycleSlipThreshold() {
		return cycleSlipThreshold;
	}

	/**
	 * Sets the cycle slip threshold.
	 *
	 * @param csThreshold the cycle slip threshold to set
	 */
	public void setCycleSlipThreshold(double csThreshold) {
		this.cycleSlipThreshold = csThreshold;
	}

	/**
	 * Gets the navigation.
	 *
	 * @return the navigation
	 */
	public NavigationProducer getNavigation() {
		return navigation;
	}

	/**
	 * Sets the navigation.
	 *
	 * @param navigation the navigation to set
	 */
	public void setNavigation(NavigationProducer navigation) {
		this.navigation = navigation;
	}

	/**
	 * Gets the weights.
	 *
	 * @return the weights
	 */
	public int getWeights() {
		return weights;
	}

	/**
	 * Sets the weights.
	 *
	 * @param weights the weights to set
	 */
	public void setWeights(int weights) {
		this.weights = weights;
	}

	/**
	 * Gets the dynamic model.
	 *
	 * @return the dynamicModel
	 */
	public int getDynamicModel() {
		return dynamicModel;
	}

	/**
	 * Sets the dynamic model.
	 *
	 * @param dynamicModel the dynamicModel to set
	 */
	public void setDynamicModel(int dynamicModel) {
		this.dynamicModel = dynamicModel;
	}
}
