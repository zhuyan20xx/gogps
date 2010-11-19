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

	/* Locarno, Switzerland */
//	static File roverFileObs = new File("./data/locarno1_rover_RINEX.obs");
//	static File masterFileObs = new File("./data/VirA061N.10o");
//	static File fileNav = new File("./data/VirA061N.10n");

	/* Manno, Switzerland */
	static File roverFileObs = new File("./data/1009843592969.obs");
	static File masterFileObs = new File("./data/VirA323B.10o");
	static File fileNav = new File("./data/VirA323B.10n");

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
	private int minNumSat = 2;
	private double alpha = 3;
	
	private Navigation navigation;
	
	public GoGPS(){
		navigation = new Navigation();
	}

	public void runCodeStandalone() {

		// Create new objects for rover and master RINEX files
		RinexFiles roverRinexIn = new RinexFiles(roverFileObs, fileNav);

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Open file streams
		roverRinexIn.open();

		// Parse RINEX navigation file
		roverRinexIn.parseHeaderNav(navigation); /* Header */
		roverRinexIn.parseDataNav(navigation); /* Get Data */

		// Parse rover RINEX observation header
		roverRinexIn.parseHeaderObs(); /* Header */

		try {
			while (roverRinexIn.hasMoreObservations()) { // buffStreamObs.ready()

				// Parse one observation epoch
				roverRinexIn.parseEpochObs();

				// Parse one observation dataset
				roverRinexIn.parseDataObs();

				// If there are at least four satellites
				if (roverRinexIn.getObs().getGpsSize() >= 4) { // gps.length

					// Compute approximate positioning by Bancroft algorithm
					roverPos.bancroft(roverRinexIn.getObs());

					// If an approximate position was computed
					if (roverPos.getCoord().isValid()) {

						// Select satellites available for double differences
						roverPos.selectSatellitesStandalone(roverRinexIn.getObs());

						// Compute code stand-alone positioning (epoch-by-epoch
						// solution)
						roverPos.codeStandalone(roverRinexIn.getObs());

						try {
							System.out.println("Code standalone positioning:");
							System.out.println("GPS time:	" + roverRinexIn.getObs().getRefTime().getGpsTime());
							System.out.println("Lon:		" + g.format(roverPos.getCoord().getGeodeticLongitude())); // geod.get(0)
							System.out.println("Lat:		" + g.format(roverPos.getCoord().getGeodeticLatitude())); // geod.get(1)
							System.out.println("h:		    " + f.format(roverPos.getCoord().getGeodeticHeight())); // geod.get(2)
						} catch (NullPointerException e) {
							System.out.println("Error: rover approximate position not computed");
						}
						System.out.println("--------------------");
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Close file streams
		roverRinexIn.close();
	}

	public void runCodeDoubleDifferences() {

		// Create new objects for rover and master RINEX files
		RinexFiles roverRinexIn = new RinexFiles(roverFileObs, fileNav);
		RinexFiles masterRinexIn = new RinexFiles(masterFileObs);

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Open file streams
		roverRinexIn.open();
		masterRinexIn.open();

		// Parse RINEX navigation file
		roverRinexIn.parseHeaderNav(navigation); /* Header */
		roverRinexIn.parseDataNav(navigation); /* Get Data */

		// Parse RINEX observation headers
		roverRinexIn.parseHeaderObs(); /* Header */
		masterRinexIn.parseHeaderObs(); /* Header */

		try {
			while (roverRinexIn.hasMoreObservations() //buffStreamObs.ready()
					&& masterRinexIn.hasMoreObservations()) { // buffStreamObs.ready()

				// Parse one observation epoch
				roverRinexIn.parseEpochObs();
				masterRinexIn.parseEpochObs();

				// Discard master epochs if correspondent rover epochs are
				// not available

				while (roverRinexIn.getObs().getRefTime().getMsec() > masterRinexIn.getObs().getRefTime().getMsec()) {
					masterRinexIn.skipDataObs();
					masterRinexIn.parseEpochObs();
				}

				// Discard rover epochs if correspondent master epochs are
				// not available
				while (roverRinexIn.getObs().getRefTime().getMsec() < masterRinexIn.getObs().getRefTime().getMsec()) {
					roverRinexIn.skipDataObs();
					roverRinexIn.parseEpochObs();
				}

				// Parse one observation dataset
				roverRinexIn.parseDataObs();
				masterRinexIn.parseDataObs();

				// If there are at least four satellites
				if (roverRinexIn.getObs().getGpsSize() >= 4) {

					// Compute approximate positioning by Bancroft algorithm
					roverPos.bancroft(roverRinexIn.getObs());

					// If an approximate position was computed
					if (roverPos.getCoord().isValid()) {

						// Select satellites available for double differences
						roverPos.selectSatellitesDoubleDiff(roverRinexIn.getObs(),
								masterRinexIn.getObs(), masterRinexIn.getApproxPos());

						// Compute code double differences positioning
						// (epoch-by-epoch solution)
						roverPos.codeDoubleDifferences(roverRinexIn.getObs(),
								masterRinexIn.getObs(), masterRinexIn.getApproxPos());

						try {
							System.out.println("Code double difference positioning:");
							System.out.println("GPS time: " + roverRinexIn.getObs().getRefTime().getGpsTime());
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

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Close file streams
		roverRinexIn.close();
		masterRinexIn.close();
	}

	public void runKalmanFilter() {

		long timeRead = System.currentTimeMillis();
		long depRead = 0;

		long timeProc = 0;
		long depProc = 0;

		// Create new objects for rover and master RINEX files
		RinexFiles roverRinexIn = new RinexFiles(roverFileObs, fileNav);
		RinexFiles masterRinexIn = new RinexFiles(masterFileObs);

		// Create a new object for the rover position
		ReceiverPosition roverPos = new ReceiverPosition(this);

		// Open file streams
		roverRinexIn.open();
		masterRinexIn.open();

		// Parse RINEX navigation file
		roverRinexIn.parseHeaderNav(navigation); /* Header */
		roverRinexIn.parseDataNav(navigation); /* Get Data */

		// Parse RINEX observation headers
		roverRinexIn.parseHeaderObs(); /* Header */
		masterRinexIn.parseHeaderObs(); /* Header */

		// Flag to check if Kalman filter has been initialized
		boolean kalmanInitialized = false;

		// Name KML file name using Timestamp
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String date1 = sdf1.format(date);
		String outPath = "./test/" + date1 + ".kml";
		// String outPath = "./test/output.kml";

		try {

			FileWriter out = new FileWriter(outPath);
			// Write KML header part
			out.write("<kml xmlns=\"http://earth.google.com/kml/2.0\">"
							+ "<Folder><name>Track Log Export</name><description>Exported on "
							+ date
							+ " </description>"
							+ "<Style id=\"line\"><LineStyle><color>7fff0000</color><width>5</width></LineStyle></Style>"
							+ "<Placemark><name>Raw Data</name><description>by Daisuke Yoshida, Tezukayama Gakuin University, JAPAN</description>"
							+ "<styleUrl>#line</styleUrl><altitudeMode>relativeToGround</altitudeMode><LineString><coordinates>");

			timeRead = System.currentTimeMillis() - timeRead;
			depRead = depRead + timeRead;

			while (roverRinexIn.hasMoreObservations() // buffStreamObs.ready()
					&& masterRinexIn.hasMoreObservations()) { // buffStreamObs.ready()

				timeRead = System.currentTimeMillis();

				// Parse one observation epoch
				roverRinexIn.parseEpochObs();
				masterRinexIn.parseEpochObs();

				// Discard master epochs if correspondent rover epochs are
				// not available
				while (roverRinexIn.getObs().getRefTime().getGpsTime() > masterRinexIn.getObs().getRefTime().getGpsTime()) {
					masterRinexIn.skipDataObs();
					masterRinexIn.parseEpochObs();
				}

				// Discard rover epochs if correspondent master epochs are
				// not available
				while (roverRinexIn.getObs().getRefTime().getGpsTime() < masterRinexIn.getObs().getRefTime().getGpsTime()) {
					roverRinexIn.skipDataObs();
					roverRinexIn.parseEpochObs();
				}

				// Parse one observation dataset
				roverRinexIn.parseDataObs();
				masterRinexIn.parseDataObs();

				timeRead = System.currentTimeMillis() - timeRead;
				depRead = depRead + timeRead;

				timeProc = System.currentTimeMillis();

				// If Kalman filter was not initialized and if there are at
				// least four satellites
				if (!kalmanInitialized && roverRinexIn.getObs().getGpsSize() >= 4) {

					// Compute approximate positioning by Bancroft algorithm
					roverPos.bancroft(roverRinexIn.getObs());

					// If an approximate position was computed
					if (roverPos.getCoord().isValid()) {

						// Initialize Kalman filter
						roverPos.kalmanFilterInit(roverRinexIn.getObs(), masterRinexIn.getObs(), masterRinexIn.getApproxPos());

						kalmanInitialized = true;

					}
				} else if (kalmanInitialized) {

					// Do a Kalman filter loop
					roverPos.kalmanFilterLoop(roverRinexIn.getObs(),masterRinexIn.getObs(), masterRinexIn.getApproxPos());
				}

				timeProc = System.currentTimeMillis() - timeProc;
				depProc = depProc + timeProc;

				try {
					System.out.println("Positioning by Kalman filter on code and phase double differences:");
					System.out.println("GPS time: " + roverRinexIn.getObs().getRefTime().getGpsTime());
					System.out.println("Lon:      " + g.format(roverPos.getCoord().getGeodeticLongitude()));//geod.get(0)
					System.out.println("Lat:      " + g.format(roverPos.getCoord().getGeodeticLatitude()));//geod.get(1)
					System.out.println("h:        " + f.format(roverPos.getCoord().getGeodeticHeight()));//geod.get(2)
					out.write(g.format(roverPos.getCoord().getGeodeticLongitude()) + ", " // geod.get(0)
							+ g.format(roverPos.getCoord().getGeodeticLatitude()) + ", " // geod.get(1)
							+ f.format(roverPos.getCoord().getGeodeticHeight()) + ","); // geod.get(2)
				} catch (NullPointerException e) {
					System.out.println("Error: rover position not computed");
				}
				System.out.println("--------------------");
			}
			// Write KML footer part
			// out.write("</coordinates><altitudeMode>absolute</altitudeMode></LineString></Placemark></Folder></kml>");
			out.write("</coordinates></LineString></Placemark></Folder></kml>");
			// Close FileWriter
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Close file streams
		roverRinexIn.close();
		masterRinexIn.close();

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

		// Get current time
		long start = System.currentTimeMillis();

		GoGPS goGPS = new GoGPS();
		// goGPS.runCodeStandalone();
		// goGPS.runCodeDoubleDifferences();
		goGPS.runKalmanFilter();

		// Get and display elapsed time
		int elapsedTimeSec = (int) Math.floor((System.currentTimeMillis() - start) / 1000);
		int elapsedTimeMillisec = (int) ((System.currentTimeMillis() - start) - elapsedTimeSec * 1000);
		System.out.println("\nElapsed time (read + proc + display + write): "
				+ elapsedTimeSec + " seconds " + elapsedTimeMillisec
				+ " milliseconds.");
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
