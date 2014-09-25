/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland, Daisuke Yoshida. All Rights Reserved. All Rights Reserved.
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
package org.gogpsproject.parser.rinex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.StreamResource;
import org.gogpsproject.Time;

/**
 * <p>
 * Class for parsing RINEX observation files
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class RinexObservationParser implements ObservationsProducer{

	private File fileObs;
	private FileInputStream streamObs;
	private InputStreamReader inStreamObs;
	private BufferedReader buffStreamObs;

	private int nTypes; /* Number of observation types */
	private int[] typeOrder; /* Order of observation data */
	private boolean hasS1Field = false; /* S1 field (SNR) is present */
	private boolean hasS2Field = false; /* S2 field (SNR) is present */
	private Time timeFirstObs; /* Time of first observation set */

	private Coordinates approxPos; /* Approximate position (X, Y, Z) [m] */
	private double[] antDelta; /* Antenna delta (E, N, U) [m] */

	private Observations obs = null; /* Current observation data sets */

	// Private fields useful to keep track of values between epoch parsing and
	// data parsing
	private int nGps;
	private int nGlo;
	private int nSbs;
	private int nSat;
	private char[] sysOrder;
	private int[] satOrder;
	private int ver ;


	public RinexObservationParser(File fileObs) {
		this.fileObs = fileObs;
	}

	/**
	 *
	 */
	public void open() throws FileNotFoundException{
		streamObs = new FileInputStream(fileObs);
		inStreamObs = new InputStreamReader(streamObs);
		buffStreamObs = new BufferedReader(inStreamObs);
	}

	public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
		try {
			streamObs.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			inStreamObs.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			buffStreamObs.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 *
	 */
	public int parseHeaderObs() {
		try {
			boolean foundTypeObs = false;

			while (buffStreamObs.ready()) {
				String line = buffStreamObs.readLine();
				String typeField = line.substring(60, line.length());
				typeField = typeField.trim();

				if (typeField.equals("RINEX VERSION / TYPE")) {

						if (!line.substring(20, 21).equals("O")) {
	
							// Error if observation file identifier was not found
							System.err
									.println("Observation file identifier is missing in file "
											+ fileObs.toString() + " header");
							return ver = 0;
							
						} else if (line.substring(5, 9).equals("3.01")){							
							ver = 3;							
						} else if (line.substring(5, 9).equals("2.12")){							
							ver = 3;							
						} else {							
							ver = 2;							
						}						
				}
				
				switch (ver){ 	
				/* In case of RINEX ver. 2 */
				case 2: 
						if (typeField.equals("# / TYPES OF OBSERV")) {
							parseTypes(line);
							foundTypeObs = true;
						}		
						else if (typeField.equals("TIME OF FIRST OBS")) {
							parseTimeFirstObs(line);
						}		
						else if (typeField.equals("APPROX POSITION XYZ")) {
							parseApproxPos(line);
						}		
						else if (typeField.equals("ANTENNA: DELTA H/E/N")) {
							parseAntDelta(line);
						}		
						else if (typeField.equals("END OF HEADER")) {
							if (!foundTypeObs) {
								// Display an error if TIME OF FIRST OBS was not found
								System.err.println("Critical information"
										+ "(TYPES OF OBSERV) is missing in file "
										+ fileObs.toString() + " header");
							}
							return ver;
						}		
				break;
				
				/* In case of RINEX ver. 3 */
				case 3:  						
						if (typeField.equals("SYS / # / OBS TYPES")) {		
							
							String satType = line.substring(0,1);
//							System.out.println("sys: " + sys);
							
							if(satType.equals("G")){
								parseTypesV3(line);
								foundTypeObs = true;
							}
							
						}
						else if (typeField.equals("TIME OF FIRST OBS")) {
							parseTimeFirstObsV3(line);
						}		
						else if (typeField.equals("APPROX POSITION XYZ")) {
							parseApproxPos(line);
						}	
						else if (typeField.equals("ANTENNA: DELTA H/E/N")) {
							parseAntDelta(line);
						}
						else if (typeField.equals("END OF HEADER")) {
							if (!foundTypeObs) {
								// Display an error if TIME OF FIRST OBS was not found
								System.err.println("Critical information"
										+ "(TYPES OF OBSERV) is missing in file "
										+ fileObs.toString() + " header");
							}
							return ver;
						}							
				break;
						
				
				} // End of switch
				
			} // End of while 

			
			// Display an error if END OF HEADER was not reached
			System.err.println("END OF HEADER was not found in file "
					+ fileObs.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (StringIndexOutOfBoundsException e) {
			// Skip over blank lines
			e.printStackTrace();
		}
		
		return 0;
		
	}

	/**
	 * Parse one observation epoch single/double line
	 */
	public Observations getNextObservations() {

		try {
			
			/* In case of RINEX ver. 2 */
			if (ver == 2){ 	

					if(!hasMoreObservations()) return null;
					String line = buffStreamObs.readLine();
					int len = line.length();
		
					// Parse date and time
					String dateStr = "20" + line.substring(1, 22);
		
					// Parse event flag
					String eFlag = line.substring(28, 30).trim();
					int eventFlag = Integer.parseInt(eFlag);
		
					// Parse available satellites string
					String satAvail = line.substring(30, len);
		
					// Parse number of available satellites
					String numOfSat = satAvail.substring(0, 2).trim();
					nSat = Integer.parseInt(numOfSat);
		
					// Arrays to store satellite order
					satOrder = new int[nSat];
					sysOrder = new char[nSat];
		
					nGps = 0;
					nGlo = 0;
					nSbs = 0;
		
					// If number of satellites <= 12, read only one line...
					if (nSat <= 12) {
		
						// Parse satellite IDs
						int j = 2;
						for (int i = 0; i < nSat; i++) {
		
							String satType = satAvail.substring(j, j + 1);
							String satID = satAvail.substring(j + 1, j + 3);
							if (satType.equals("G") || satType.equals(" ")) {
								sysOrder[i] = 'G';
								satOrder[i] = Integer.parseInt(satID.trim());
								nGps++;
							} else if (satType.equals("R")) {
								sysOrder[i] = 'R';
								satOrder[i] = Integer.parseInt(satID.trim());
								nGlo++;
							} else if (satType.equals("S")) {
								sysOrder[i] = 'S';
								satOrder[i] = Integer.parseInt(satID.trim());
								nSbs++;
							}
							j = j + 3;
						}
					} else { // ... otherwise, read two lines
		
						// Parse satellite IDs
						int j = 2;
						for (int i = 0; i < 12; i++) {
		
							String satType = satAvail.substring(j, j + 1);
							String satID = satAvail.substring(j + 1, j + 3);
							if (satType.equals("G") || satType.equals(" ")) {
								sysOrder[i] = 'G';
								satOrder[i] = Integer.parseInt(satID.trim());
								nGps++;
							} else if (satType.equals("R")) {
								sysOrder[i] = 'R';
								satOrder[i] = Integer.parseInt(satID.trim());
								nGlo++;
							} else if (satType.equals("S")) {
								sysOrder[i] = 'S';
								satOrder[i] = Integer.parseInt(satID.trim());
								nSbs++;
							}
							j = j + 3;
						}
						// Get second line
						satAvail = buffStreamObs.readLine().trim();
		
						// Number of remaining satellites
						int num = nSat - 12;
		
						// Parse satellite IDs
						int k = 0;
						for (int i = 0; i < num; i++) {
		
							String satType = satAvail.substring(k, k + 1);
							String satID = satAvail.substring(k + 1, k + 3);
							if (satType.equals("G") || satType.equals(" ")) {
								sysOrder[i + 12] = 'G';
								satOrder[i + 12] = Integer.parseInt(satID.trim());
								nGps++;
							} else if (satType.equals("R")) {
								sysOrder[i + 12] = 'R';
								satOrder[i + 12] = Integer.parseInt(satID.trim());
								nGlo++;
							} else if (satType.equals("S")) {
								sysOrder[i + 12] = 'S';
								satOrder[i + 12] = Integer.parseInt(satID.trim());
								nSbs++;
							}
							k = k + 3;
						}
					}
		
					obs = new Observations(new Time(dateStr), eventFlag);
		
					// Convert date string to standard UNIX time in milliseconds
					//long time = Time.dateStringToTime(dateStr);
		
					// Store time
					//obs.refTime = new Time(dateStr);
					//obs.refTime.msec = time;
		
					// Store event flag
					//obs.eventFlag = eventFlag;
		
					parseDataObs();
		
					obs.cleanObservations();
		
					return obs;
					
					
			/* In case of RINEX ver. 3 */
			} else {
								
				if(!hasMoreObservations()) return null;
				String line = buffStreamObs.readLine();
	
				// Parse date and time
				String dateStr = line.substring(2, 25);

				// Parse event flag
				String eFlag = line.substring(30, 32).trim();
				
				int eventFlag = Integer.parseInt(eFlag);
	
				// Parse available satellites string
				String satAvail = line.substring(33, 35).trim();
	
				// Parse number of available satellites
//				String numOfSat = satAvail.substring(0, 2).trim();
				nSat = Integer.parseInt(satAvail);
	
				// Arrays to store satellite order
//				satOrder = new int[nSat];
//				sysOrder = new char[nSat];
	
				nGps = 0;
				nGlo = 0;
				nSbs = 0;
				
				obs = new Observations(new Time(dateStr), eventFlag);
	
				// Convert date string to standard UNIX time in milliseconds
				//long time = Time.dateStringToTime(dateStr);
	
				// Store time
				//obs.refTime = new Time(dateStr);
				//obs.refTime.msec = time;
	
				// Store event flag
				//obs.eventFlag = eventFlag;
	
				parseDataObsV3();
	
				obs.cleanObservations();
	
				return obs;
					
						
			} // End of if 
					
			
					
		} catch (ParseException e) {
			// Skip over unexpected observation lines
			e.printStackTrace();
		} catch (StringIndexOutOfBoundsException e) {
			// Skip over blank lines
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parse one observation epoch
	 */
	private void parseDataObs() {

		try {

			//obs.init(nGps, nGlo, nSbs);

			// Arrays to store satellite list for each system
//			obs.gpsSat = new ArrayList<Integer>(nGps);
//			obs.gloSat = new ArrayList<Integer>(nGlo);
//			obs.sbsSat = new ArrayList<Integer>(nSbs);
//
//			// Allocate array of observation objects
//			if (nGps > 0)
//				obs.gps = new ObservationSet[nGps];
//			if (nGlo > 0)
//				obs.glo = new ObservationSet[nGlo];
//			if (nSbs > 0)
//				obs.sbs = new ObservationSet[nSbs];

			// Loop through observation lines
			for (int i = 0; i < nSat; i++) {

				// Read line of observations
				String line = buffStreamObs.readLine();

				if (sysOrder[i] == 'G') {
					// Create observation object
					ObservationSet os = new ObservationSet();
					os.setSatType('G');
					os.setSatID(satOrder[i]);
					obs.setGps(i, os);
//					obs.gps[i] = os;// new ObservationSet();
//					obs.gps[i].C = 0;
//					obs.gps[i].P = new double[2];
//					obs.gps[i].L = new double[2];
//					obs.gps[i].S = new float[2];
//					obs.gps[i].D = new float[2];

					// Store satellite ID
					//obs.gps[i].setSatID(satOrder[i]);
//					obs.gpsSat.add(satOrder[i]);

					if (nTypes <= 5) { // If the number of observation
						// types
						// is <= 5, they are all on one line ...

						// Parse observation data according to typeOrder
						int j = 0;
						for (int k = 0; k < nTypes; k++) {

							assignTypes(line, k, j, i);
							j = j + 16;
						}

					} else { // ... otherwise, they are on two lines

						// Parse observation data according to typeOrder
						// (first line <-> 5 observation columns)
						int j = 0;
						for (int k = 0; k < 5; k++) {

							assignTypes(line, k, j, i);
							j = j + 16;
						}

						// Get second line
						line = buffStreamObs.readLine();

						// Parse observation data according to typeOrder
						// (second line)
						j = 0;
						for (int k = 5; k < nTypes; k++) {

							assignTypes(line, k, j, i);
							j = j + 16;
						}
					}
				} else if (nTypes > 5){
					// Skip additional observation line for GLO and SBS
					line = buffStreamObs.readLine();
				}
			}

		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			// Skip over blank lines
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void parseDataObsV3() {

		try {

			//obs.init(nGps, nGlo, nSbs);

			// Arrays to store satellite list for each system
//			obs.gpsSat = new ArrayList<Integer>(nGps);
//			obs.gloSat = new ArrayList<Integer>(nGlo);
//			obs.sbsSat = new ArrayList<Integer>(nSbs);
//
//			// Allocate array of observation objects
//			if (nGps > 0)
//				obs.gps = new ObservationSet[nGps];
//			if (nGlo > 0)
//				obs.glo = new ObservationSet[nGlo];
//			if (nSbs > 0)
//				obs.sbs = new ObservationSet[nSbs];

			// Loop through observation lines
//			System.out.println("nSat: " + nSat);

			for (int i = 0; i < nSat; i++) {
				
				// Read line of observations
				String line = buffStreamObs.readLine();

				String satType = line.substring(0, 1);
				String satNum = line.substring(1, 3);
				
				int satID = Integer.parseInt(satNum.trim());
					
				
				if (satType.equals("G") || satType.equals("J")) {
					
					// Create observation object
					ObservationSet os = new ObservationSet();
					if (satType.equals("G")) os.setSatType('G');
					if (satType.equals("J")) os.setSatType('J');
					os.setSatID(satID);
					obs.setGps(i, os);
//					obs.gps[i] = os;// new ObservationSet();
//					obs.gps[i].C = 0;
//					obs.gps[i].P = new double[2];
//					obs.gps[i].L = new double[2];
//					obs.gps[i].S = new float[2];
//					obs.gps[i].D = new float[2];

					// Store satellite ID
					//obs.gps[i].setSatID(satOrder[i]);
//					obs.gpsSat.add(satOrder[i]);
					

//					if (nTypes <= 5) { // If the number of observation
						// types
						// is <= 5, they are all on one line ...

						line = line.substring(3);
						// Parse observation data according to typeOrder
						int j = 0;
						for (int k = 0; k < nTypes; k++) {
							assignTypes(line, k, j, i);
							j = j + 16;
						}
						
//				} else if (nTypes > 5){
//					// Skip additional observation line for GLO and SBS
//					line = buffStreamObs.readLine();
				} else if (satType.equals("R")){
				
					// Create observation object
					ObservationSet os = new ObservationSet();
					os.setSatType('R');
					os.setSatID(satID);
					obs.setGps(i, os);
					
					
					line = line.substring(3);
					// Parse observation data according to typeOrder
					int j = 0;
					for (int k = 0; k < nTypes; k++) {
						assignTypes(line, k, j, i);
						j = j + 16;
					}
								
							
				} // End of if
				
			}  // End of for

		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			// Skip over blank lines
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Skip one observation epoch
//	 */
//	public Observations skipDataObs() {
//
//		try {
//			// Loop through observation lines
//			for (int i = 0; i < nSat; i++) {
//
//				// Read line of observations
//				buffStreamObs.readLine();
//
//				if (nTypes > 5) { // ... otherwise, they are on two lines
//
//					// Get second line
//					buffStreamObs.readLine();
//				}
//			}
//
//		} catch (StringIndexOutOfBoundsException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return obs;
//	}

	/**
	 * Assign observation data according to type order
	 */
	private void assignTypes(String line, int k, int j, int i) {

		try {
			ObservationSet o = obs.getGpsByIdx(i);
			
			if (typeOrder[k] == 0) { // ** C1 code

				String codeC = line.substring(j, j + 14).trim();
				if(codeC.trim().length()>0)o.setCodeC(0,Double.parseDouble(codeC));

			} else if (typeOrder[k] == 1) { // ** C2 code

				String codeC = line.substring(j, j + 14).trim();
				if(codeC.trim().length()>0)o.setCodeC(1,Double.parseDouble(codeC));

			} else if (typeOrder[k] == 2) { // ** P1 code

				String codeP = line.substring(j, j + 14).trim();
//				System.out.println("codeP: " + codeP);
				if (codeP.length() != 0) {
					o.setCodeP(0,Double.parseDouble(codeP));
				}
			} else if (typeOrder[k] == 3) { // ** P2 code

				String codeP = line.substring(j, j + 14).trim();
				if (codeP.length() != 0) {
					o.setCodeP(1,Double.parseDouble(codeP));
				}
			} else if (typeOrder[k] == 4) { // ** L1 phase

				String phaseL = line.substring(j, j + 14);
//				System.out.println("phaseL: " + phaseL);
				phaseL = phaseL.trim();
				try {
					if (phaseL.length() != 0) {
						o.setPhase(0,Double.parseDouble(phaseL));
						try{
							// Loss of Lock
							int lli = Integer.parseInt(line.substring(j+14, j + 15));
							o.setLossLockInd(0,lli);
						}catch(Exception ignore){}
						try{
							// Signal Strength
							int ss = Integer.parseInt(line.substring(j+15, j + 16));
							o.setSignalStrengthInd(0, ss);
							if (!hasS1Field)
								o.setSignalStrength(0,ss * 6);
						}catch(Exception ignore){}
					}
				} catch (NumberFormatException e) {
				}
			} else if (typeOrder[k] == 5) { // ** L2 phase

				String phaseL = line.substring(j, j + 14).trim();
				try {
					if (phaseL.length() != 0) {
						o.setPhase(1,Double.parseDouble(phaseL));

						try{
							// Loss of Lock
							int lli = Integer.parseInt(line.substring(j+14, j + 15));
							o.setLossLockInd(1,lli);
						}catch(Exception ignore){}
						try{
							// Signal Strength
							int ss = Integer.parseInt(line.substring(j+15, j + 16));
							o.setSignalStrengthInd(1, ss);
							if (!hasS2Field)
								o.setSignalStrength(1,ss * 6);
						}catch(Exception ignore){}
					}
				} catch (NumberFormatException e) {
				}
			} else if (typeOrder[k] == 6) { // S1 ** SNR on L1

				String snrS = line.substring(j, j + 14).trim();
//				System.out.println("snrS: " + snrS);

				if (snrS.length() != 0) {
					o.setSignalStrength(0,Float.parseFloat(snrS));
				}
			} else if (typeOrder[k] == 7) { // S2 ** SNR on L2

				String snrS = line.substring(j, j + 14).trim();
				if (snrS.length() != 0) {
					o.setSignalStrength(1,Float.parseFloat(snrS));
				}
			} else if (typeOrder[k] == 8) { // ** D1 doppler

				String dopplerD = line.substring(j, j + 14).trim();
//				System.out.println("dopplerD: " + dopplerD);

				if (dopplerD.length() != 0) {
					o.setDoppler(0,Float.parseFloat(dopplerD));
				}
			} else if (typeOrder[k] == 9) { // ** D2 doppler

				String dopplerD = line.substring(j, j + 14).trim();
				if (dopplerD.length() != 0) {
					o.setDoppler(1,Float.parseFloat(dopplerD));
				}
			
			/*  NEED to improve below codes  */ 
				
				
			} else if (typeOrder[k] == 10) { // ** D2 doppler
	
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 11) { // ** D2 doppler
		
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 12) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 13) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 14) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 15) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 16) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 17) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 18) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 19) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 20) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 21) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 22) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 23) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			
			} else if (typeOrder[k] == 24) { // ** D2 doppler
				
//				String dopplerD = line.substring(j, j + 14).trim();
//				if (dopplerD.length() != 0) {
//					o.setDoppler(1,Float.parseFloat(dopplerD));
//				}
			}
			
			
			
		} catch (StringIndexOutOfBoundsException e) {
			// Skip over blank slots
		}
	}

	/**
	 * @param line
	 */
	private void parseTypes(String line) {

		// Extract number of available data types
		nTypes = Integer.parseInt(line.substring(0, 6).trim());

		// Allocate the array that stores data type order
		typeOrder = new int[nTypes];

		/*
		 * Parse data types and store order (internal order: C1 P1 P2 L1 L2 S1
		 * S2 D1 D2)
		 */
		for (int i = 0; i < nTypes; i++) {
			String type = line.substring(6 * (i + 2) - 2, 6 * (i + 2));
			if (type.equals("C1")) {
				typeOrder[i] = 0;
			} else if (type.equals("C2")) {
				typeOrder[i] = 1;
			} else if (type.equals("P1")) {
				typeOrder[i] = 2;
			} else if (type.equals("P2")) {
				typeOrder[i] = 3;
			} else if (type.equals("L1")) {
				typeOrder[i] = 4;
			} else if (type.equals("L2")) {
				typeOrder[i] = 5;
			} else if (type.equals("S1")) {
				typeOrder[i] = 6;
				hasS1Field = true;
			} else if (type.equals("S2")) {
				typeOrder[i] = 7;
				hasS2Field = true;
			} else if (type.equals("D1")) {
				typeOrder[i] = 8;
			} else if (type.equals("D2")) {
				typeOrder[i] = 9;
			}
		}
	}
	
	
	/**
	 * @param line
	 * @throws IOException 
	 */
	private void parseTypesV3(String line) throws IOException {

		// Extract number of available data types
		nTypes = Integer.parseInt(line.substring(1, 6).trim());

		// Allocate the array that stores data type order
		typeOrder = new int[nTypes];

		if(nTypes > 13){  // In case of more than 13 Types, it will two lines
							
			/*
			 * Parse data types and store order 
			 * (internal order: C1C L1C D1C S1C C1W L1W S1W C2X L2X D2X S2X C2W L2W 
             *   D2W S2W C5X L5X D5X S5X    // NEED to check this order !!
			 *
			 */
			
			for (int i = 0; i <= 12; i++) {
				String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);

				if (type.equals("C1C")) {
					typeOrder[i] = 0;
				} else if (type.equals("C2C")) {
					typeOrder[i] = 1;
				} else if (type.equals("P1C")) {
					typeOrder[i] = 2;
				} else if (type.equals("P2C")) {
					typeOrder[i] = 3;
				} else if (type.equals("L1C")) {
					typeOrder[i] = 4;
				} else if (type.equals("L2C")) {
					typeOrder[i] = 5;
				} else if (type.equals("S1C")) {
					typeOrder[i] = 6;
					hasS1Field = true;
				} else if (type.equals("S2C")) {
					typeOrder[i] = 7;
					hasS2Field = true;
				} else if (type.equals("D1C")) {
					typeOrder[i] = 8;
				} else if (type.equals("D2C")) {
					typeOrder[i] = 9;
					
				} else if (type.equals("C1W")) {
					typeOrder[i] = 10;
				} else if (type.equals("L1W")) {
					typeOrder[i] = 11;
				} else if (type.equals("S1W")) {
					typeOrder[i] = 12;
					
				} else if (type.equals("C2X")) {
					typeOrder[i] = 13;
				} else if (type.equals("L2X")) {
					typeOrder[i] = 14;			
				} else if (type.equals("D2X")) {
					typeOrder[i] = 15;
				} else if (type.equals("S2X")) {
					typeOrder[i] = 16;
				
				} else if (type.equals("C2W")) {
					typeOrder[i] = 17;
				} else if (type.equals("L2W")) {
					typeOrder[i] = 18;			
				} else if (type.equals("D2W")) {
					typeOrder[i] = 19;
				} else if (type.equals("S2W")) {
					typeOrder[i] = 20;		
				
				} else if (type.equals("C5X")) {
					typeOrder[i] = 21;
				} else if (type.equals("L5X")) {
					typeOrder[i] = 22;			
				} else if (type.equals("D5X")) {
					typeOrder[i] = 23;
				} else if (type.equals("S5X")) {
					typeOrder[i] = 24;			
				}	
				
				}
			
			line = buffStreamObs.readLine();   // read the second line
			
				int j = 0;
			for (int i = 13; i <= nTypes  ; i++) {
				
				String type = line.substring(4 * (j + 3) -5 , 4 * (j + 3) -2);

				if (type.equals("C1C")) {
					typeOrder[i] = 0;
				} else if (type.equals("C2C")) {
					typeOrder[i] = 1;
				} else if (type.equals("P1C")) {
					typeOrder[i] = 2;
				} else if (type.equals("P2C")) {
					typeOrder[i] = 3;
				} else if (type.equals("L1C")) {
					typeOrder[i] = 4;
				} else if (type.equals("L2C")) {
					typeOrder[i] = 5;
				} else if (type.equals("S1C")) {
					typeOrder[i] = 6;
					hasS1Field = true;
				} else if (type.equals("S2C")) {
					typeOrder[i] = 7;
					hasS2Field = true;
				} else if (type.equals("D1C")) {
					typeOrder[i] = 8;
				} else if (type.equals("D2C")) {
					typeOrder[i] = 9;
					
				} else if (type.equals("C1W")) {
					typeOrder[i] = 10;
				} else if (type.equals("L1W")) {
					typeOrder[i] = 11;
				} else if (type.equals("S1W")) {
					typeOrder[i] = 12;
					
				} else if (type.equals("C2X")) {
					typeOrder[i] = 13;
				} else if (type.equals("L2X")) {
					typeOrder[i] = 14;			
				} else if (type.equals("D2X")) {
					typeOrder[i] = 15;
				} else if (type.equals("S2X")) {
					typeOrder[i] = 16;
				
				} else if (type.equals("C2W")) {
					typeOrder[i] = 17;
				} else if (type.equals("L2W")) {
					typeOrder[i] = 18;			
				} else if (type.equals("D2W")) {
					typeOrder[i] = 19;
				} else if (type.equals("S2W")) {
					typeOrder[i] = 20;		
				
				} else if (type.equals("C5X")) {
					typeOrder[i] = 21;
				} else if (type.equals("L5X")) {
					typeOrder[i] = 22;			
				} else if (type.equals("D5X")) {
					typeOrder[i] = 23;
				} else if (type.equals("S5X")) {
					typeOrder[i] = 24;			
				}
				
				j++ ;
				
			}

			
		} else {  // less than 14 types, it will be one line.  
		
		/*
		 * Parse data types and store order (internal order: C1 P1 P2 L1 L2 S1
		 * S2 D1 D2)
		 *  
		 */
				for (int i = 0; i < nTypes; i++) {
					String type = line.substring(4 * (i + 3) -5 , 4 * (i + 3) -2);
		
					if (type.equals("C1C")) {
						typeOrder[i] = 0;
					} else if (type.equals("C2C")) {
						typeOrder[i] = 1;
					} else if (type.equals("P1C")) {
						typeOrder[i] = 2;
					} else if (type.equals("P2C")) {
						typeOrder[i] = 3;
					} else if (type.equals("L1C")) {
						typeOrder[i] = 4;
					} else if (type.equals("L2C")) {
						typeOrder[i] = 5;
					} else if (type.equals("S1C")) {
						typeOrder[i] = 6;
						hasS1Field = true;
					} else if (type.equals("S2C")) {
						typeOrder[i] = 7;
						hasS2Field = true;
					} else if (type.equals("D1C")) {
						typeOrder[i] = 8;
					} else if (type.equals("D2C")) {
						typeOrder[i] = 9;
					}
				}
		
		
		}
	}

	/**
	 * @param line
	 */
	private void parseTimeFirstObs(String line) {

		// Format date string according to DateStringToTime required format
		String dateStr = line.substring(0, 42).trim().replace("    ", " ") .replace("   ", " ");

		// Create time object
		//timeFirstObs = new Time();

		// Convert date string to standard UNIX time in milliseconds
		try {
			timeFirstObs = new Time(dateStr); //Time.dateStringToTime(dateStr);

		} catch (ParseException e) {
			// Display an error if END OF HEADER was not reached
			System.err.println("TIME OF FIRST OBS parsing failed in file "
					+ fileObs.toString());
		}
	}

	/**
	 * @param line
	 */
	private void parseTimeFirstObsV3(String line) {

		// Format date string according to DateStringToTime required format
//		String dateStr = line.substring(0, 43).trim().replace("    ", " ") .replace("   ", " ");
		String dateStr = line.substring(0, 43).trim().replace("    ", " ") .replace("   ", " ");
//		System.out.println(dateStr);

		
		// Create time object
		//timeFirstObs = new Time();

		// Convert date string to standard UNIX time in milliseconds
		try {
			timeFirstObs = new Time(dateStr); //Time.dateStringToTime(dateStr);
//			System.out.println("TIME OF FIRST OBS: " + timeFirstObs);


		} catch (ParseException e) {
			// Display an error if END OF HEADER was not reached
			System.err.println("TIME OF FIRST OBS parsing failed in file "
					+ fileObs.toString());
		}
	}
	
	
	/**
	 * @param line
	 */
	private void parseApproxPos(String line) {

		// Allocate the vector that stores the approximate position (X, Y, Z)
		//approxPos = Coordinates.globalXYZInstance(new SimpleMatrix(3, 1));
//		approxPos.ecef = new SimpleMatrix(3, 1);

		// Read approximate position coordinates
//		approxPos.ecef.set(0, 0, Double.valueOf(line.substring(0, 14).trim())
//				.doubleValue());
//		approxPos.ecef.set(1, 0, Double.valueOf(line.substring(14, 28).trim())
//				.doubleValue());
//		approxPos.ecef.set(2, 0, Double.valueOf(line.substring(28, 42).trim())
//				.doubleValue());
//
		approxPos = Coordinates.globalXYZInstance(Double.valueOf(line.substring(0, 14).trim()), Double.valueOf(line.substring(14, 28).trim()), Double.valueOf(line.substring(28, 42).trim()) );

		// Convert the approximate position to geodetic coordinates
		approxPos.computeGeodetic();
	}

	/**
	 * @param line
	 */
	private void parseAntDelta(String line) {

		// Allocate the array that stores the approximate position
		antDelta = new double[3];

		// Read approximate position coordinates (E, N, U)
		antDelta[2] = Double.valueOf(line.substring(0, 14).trim())
				.doubleValue();
		antDelta[0] = Double.valueOf(line.substring(14, 28).trim())
				.doubleValue();
		antDelta[1] = Double.valueOf(line.substring(28, 42).trim())
				.doubleValue();
	}

	/**
	 * @return the approxPos
	 */
	public Coordinates getDefinedPosition() {
		return approxPos;
	}

	/**
	 * @return the obs
	 */
	public Observations getCurrentObservations() {
		return obs;
	}

	public boolean hasMoreObservations() throws IOException{
		return buffStreamObs.ready();
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	@Override
	public void init() throws Exception {
		// Open file streams
		open();

		// Parse RINEX observation headers
		parseHeaderObs(); /* Header */

	}

}
