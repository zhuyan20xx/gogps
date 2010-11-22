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

import org.cryms.gogps.parser.ublox.UBXFileReader;
import org.gogpsproject.parser.rinex.RinexFileNavigation;
import org.gogpsproject.parser.rinex.RinexFileObservation;

/**
 * @author ege, Cryms.com
 * 
 */
public class GoGPS {

	/* Como, Italy */
//	static File roverFileObs = new File("./data/perim2.08o");
//	static File masterFileObs = new File("./data/COMO1190.08o");
//	static File fileNav = new File("./data/COMO1190.08n");

	/* Sardinia, Italy */
	// static File roverFileObs = new File("./data/goCerchio_rover.obs");
	// static File masterFileObs = new File("./data/sard0880.10o");
	// static File fileNav = new File("./data/sard0880.10n");

//	/* Locarno, Switzerland */
//	static File roverFileObs = new File("./data/locarno1_rover_RINEX.obs");
//	static File masterFileObs = new File("./data/VirA061N.10o");
//	static File fileNav = new File("./data/VirA061N.10n");

	/* Manno, Switzerland */
//	static File roverFileObs = new File("./data/1009843324860.obs");
//	// 2 static File roverFileObs = new File("./data/1009843888879.obs");
//	// 3 static File roverFileObs = new File("./data/1009844950228.obs");
//	static File masterFileObs = new File("./data/VirFaido19112010b.10o");
//	static File fileNav = new File("./data/VirFaido19112010b.10n");

	/* Osaka, Japan (static) */
//	static File roverFileObs = new File("./data/COM10_100617_rover.obs");
//	static File masterFileObs = new File("./data/vrs2.10o");
//	static File fileNav = new File("./data/vrs2.10n");

	/* Como, Italy (static) */
	// static File roverFileObs = new File("./data/como_pillar_rover.obs");
	// static File masterFileObs = new File("./data/como_pillar_master.10o");
	// static File fileNav = new File("./data/como_pillar_rover.nav");

	private static DecimalFormat f = new DecimalFormat("0.000");
	private static DecimalFormat g = new DecimalFormat("0.00000000");

	// Frequency selector
	public final static int FREQ_L1 = ObservationSet.L1;
	public final static int FREQ_L2 = ObservationSet.L2;
	private int freq = FREQ_L1;

	// Double-frequency flag
	private boolean dualFreq = false;

	// Elevation cutoff
	private double cutoff = 15;

	// Weighting strategy
	// 0 = same weight for all observations
	// 1 = weight based on satellite elevation
	// 2 = weight based on signal-to-noise ratio
	// 3 = weight based on combined elevation and signal-to-noise ratio
	public final static int WEIGHT_EQUAL = 0;
	public final static int WEIGHT_SAT_ELEVATION = 1;
	public final static int WEIGHT_SIGNAL_TO_NOISE_RATIO = 2;
	public final static int WEIGHT_COMBINED_ELEVATION_SNR = 3;
	
	private int weights = WEIGHT_SIGNAL_TO_NOISE_RATIO;

	// Kalman filter parameters
	private int order = 2;
	private double stDevInit = 3;
	private double stDevX = 0.3;
	private double stDevY = 0.3;
	private double stDevZ = 0.3;
	private double stDevCode = 3;
	private double stDevPhase = 0.03;
	private double stDevN = 10;
	private int minNumSat = 4;
	private double alpha = 3;
	
	private Navigation navigation;
	private ObservationsProducer roverIn;
	private ObservationsProducer masterIn;
	
	public GoGPS(Navigation navigation, ObservationsProducer roverIn, ObservationsProducer masterIn){
		this.navigation = navigation;
		this.roverIn = roverIn;
		this.masterIn = masterIn;
	}

	public void runCodeStandalone() {
		
		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		Observations obsR = roverIn.nextObservations();
		while (obsR!=null) { // buffStreamObs.ready()

			// If there are at least four satellites
			Observations obs = roverIn.nextObservations();
			if (obs.getGpsSize() >= 4) { // gps.length

				// Compute approximate positioning by Bancroft algorithm
				roverPos.bancroft(roverIn.getCurrentObservations());

				// If an approximate position was computed
				if (roverPos.getCoord().isValid()) {

					// Select satellites available for double differences
					roverPos.selectSatellitesStandalone(roverIn.getCurrentObservations());

					// Compute code stand-alone positioning (epoch-by-epoch
					// solution)
					roverPos.codeStandalone(roverIn.getCurrentObservations());

					try {
						System.out.println("Code standalone positioning:");
						System.out.println("GPS time:	" + roverIn.getCurrentObservations().getRefTime().getGpsTime());
						System.out.println("Lon:		" + g.format(roverPos.getCoord().getGeodeticLongitude())); // geod.get(0)
						System.out.println("Lat:		" + g.format(roverPos.getCoord().getGeodeticLatitude())); // geod.get(1)
						System.out.println("h:		    " + f.format(roverPos.getCoord().getGeodeticHeight())); // geod.get(2)
					} catch (NullPointerException e) {
						System.out.println("Error: rover approximate position not computed");
					}
					System.out.println("--------------------");
				}
			}
			
			obsR = roverIn.nextObservations();
		}

	}

	public void runCodeDoubleDifferences() {

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);


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
					if (roverPos.getCoord().isValid()) {

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
							System.out.println("Lon:      " + g.format(roverPos.getCoord().getGeodeticLongitude()));//geod.get(0)
							System.out.println("Lat:      " + g.format(roverPos.getCoord().getGeodeticLatitude())); // geod.get(1)
							System.out.println("h:        " + f.format(roverPos.getCoord().getGeodeticHeight())); // geod.get(2)
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

	}

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
			timeRead = System.currentTimeMillis() - timeRead;
			depRead = depRead + timeRead;

			Observations obsR = roverIn.nextObservations();
			Observations obsM = masterIn.nextObservations();
			int c=0;
			while (obsR != null && obsM != null) {

				timeRead = System.currentTimeMillis();

				// Discard master epochs if correspondent rover epochs are
				// not available
//				Observations obsR = roverIn.nextObservations();
//				Observations obsM = masterIn.nextObservations();
				long obsRtime = obsR.getRefTime().getGpsTime();
				while (obsM!=null && obsR!=null && obsRtime > obsM.getRefTime().getGpsTime()) {
//					masterIn.skipDataObs();
//					masterIn.parseEpochObs();
					obsM = masterIn.nextObservations();
				}

				// Discard rover epochs if correspondent master epochs are
				// not available
				long obsMtime = obsM.getRefTime().getGpsTime();
				while (obsM!=null && obsR!=null && obsR.getRefTime().getGpsTime() < obsMtime) {
					obsR = roverIn.nextObservations();
				}

				if(obsM!=null && obsR!=null){
					timeRead = System.currentTimeMillis() - timeRead;
					depRead = depRead + timeRead;
					timeProc = System.currentTimeMillis();
					
					// If Kalman filter was not initialized and if there are at
					// least four satellites
					boolean valid = true;
					if (!kalmanInitialized && obsR.getGpsSize() >= 4) {
	
						// Compute approximate positioning by Bancroft algorithm
						roverPos.bancroft(obsR);
	
						// If an approximate position was computed
						if (roverPos.getCoord().isValid()) {
	
							// Initialize Kalman filter
							roverPos.kalmanFilterInit(obsR, obsM, masterIn.getApproxPosition());
	
							kalmanInitialized = true;
	
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
	
					if(valid){
						try {
							String lon = g.format(roverPos.getCoord().getGeodeticLongitude());
							String lat = g.format(roverPos.getCoord().getGeodeticLatitude());
							String h = f.format(roverPos.getCoord().getGeodeticHeight());
							
							out.write(lon + "," // geod.get(0)
									+ lat + "," // geod.get(1)
									+ h + " \n"); // geod.get(2)
							if(c%1==0){
								String t = timeKML.format(new Date(obsR.getRefTime().getMsec()));
								
								//System.out.println("Positioning by Kalman filter on code and phase double differences:");
								System.out.print("T:" + t);
								System.out.print(" Lon:" + lon);//geod.get(0)
								System.out.print(" Lat:" + lat);//geod.get(1)
								System.out.println(" H:" + h);//geod.get(2)
								
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
							System.out.println("Error: rover position not computed");
						}
					}
					//System.out.println("--------------------");
					
					// get next epoch
					obsR = roverIn.nextObservations();
					obsM = masterIn.nextObservations();
					
					c++;
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
	 * @param args
	 */
	public static void main(String[] args) {

		try{
			// Get current time
			long start = System.currentTimeMillis();
			
			/* Faido */
			//ObservationsProducer roverIn = new RinexFileObservation(roverFileObs);
			//ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009843324860.ubx"));
			//ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009843888879.ubx"));
			ObservationsProducer roverIn = new UBXFileReader(new File("./data/1009844950228.ubx"));
			ObservationsProducer masterIn = new RinexFileObservation(new File("./data/VirFaido19112010b.10o"));
			Navigation navigationIn = new RinexFileNavigation(new File("./data/VirFaido19112010b.10n"));
			
//			ObservationsProducer roverIn = new UBXFileReader(new File("./data/manno-21.11.2010.ubx"));
//			ObservationsProducer masterIn = new RinexFileObservation(new File("./data/VirManno-21-11-2010.10o"));
//			Navigation navigationIn = new RinexFileNavigation(new File("./data/VirManno-21-11-2010.10n"));
			
			roverIn.init();
			masterIn.init();
			navigationIn.init();
			
			GoGPS goGPS = new GoGPS(navigationIn, roverIn, masterIn);
			// goGPS.runCodeStandalone();
			// goGPS.runCodeDoubleDifferences();
			goGPS.runKalmanFilter();
	
			roverIn.release();
			masterIn.release();
			navigationIn.release();
	
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

	/**
	 * @return the freq
	 */
	public int getFreq() {
		return freq;
	}

	/**
	 * @param freq the freq to set
	 */
	public void setFreq(int freq) {
		this.freq = freq;
	}

	/**
	 * @return the dualFreq
	 */
	public boolean isDualFreq() {
		return dualFreq;
	}

	/**
	 * @param dualFreq the dualFreq to set
	 */
	public void setDualFreq(boolean dualFreq) {
		this.dualFreq = dualFreq;
	}

	/**
	 * @return the cutoff
	 */
	public double getCutoff() {
		return cutoff;
	}

	/**
	 * @param cutoff the cutoff to set
	 */
	public void setCutoff(double cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return the stDevInit
	 */
	public double getStDevInit() {
		return stDevInit;
	}

	/**
	 * @param stDevInit the stDevInit to set
	 */
	public void setStDevInit(double stDevInit) {
		this.stDevInit = stDevInit;
	}

	/**
	 * @return the stDevX
	 */
	public double getStDevX() {
		return stDevX;
	}

	/**
	 * @param stDevX the stDevX to set
	 */
	public void setStDevX(double stDevX) {
		this.stDevX = stDevX;
	}

	/**
	 * @return the stDevY
	 */
	public double getStDevY() {
		return stDevY;
	}

	/**
	 * @param stDevY the stDevY to set
	 */
	public void setStDevY(double stDevY) {
		this.stDevY = stDevY;
	}

	/**
	 * @return the stDevZ
	 */
	public double getStDevZ() {
		return stDevZ;
	}

	/**
	 * @param stDevZ the stDevZ to set
	 */
	public void setStDevZ(double stDevZ) {
		this.stDevZ = stDevZ;
	}

	/**
	 * @return the stDevCode
	 */
	public double getStDevCode() {
		return stDevCode;
	}

	/**
	 * @param stDevCode the stDevCode to set
	 */
	public void setStDevCode(double stDevCode) {
		this.stDevCode = stDevCode;
	}

	/**
	 * @return the stDevPhase
	 */
	public double getStDevPhase() {
		return stDevPhase;
	}

	/**
	 * @param stDevPhase the stDevPhase to set
	 */
	public void setStDevPhase(double stDevPhase) {
		this.stDevPhase = stDevPhase;
	}

	/**
	 * @return the stDevN
	 */
	public double getStDevN() {
		return stDevN;
	}

	/**
	 * @param stDevN the stDevN to set
	 */
	public void setStDevN(double stDevN) {
		this.stDevN = stDevN;
	}

	/**
	 * @return the minNumSat
	 */
	public int getMinNumSat() {
		return minNumSat;
	}

	/**
	 * @param minNumSat the minNumSat to set
	 */
	public void setMinNumSat(int minNumSat) {
		this.minNumSat = minNumSat;
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the navigation
	 */
	public Navigation getNavigation() {
		return navigation;
	}

	/**
	 * @param navigation the navigation to set
	 */
	public void setNavigation(Navigation navigation) {
		this.navigation = navigation;
	}

	/**
	 * @return the weights
	 */
	public int getWeights() {
		return weights;
	}

	/**
	 * @param weights the weights to set
	 */
	public void setWeights(int weights) {
		this.weights = weights;
	}
}
