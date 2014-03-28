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
package org.gogpsproject.parser.rinex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;
import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.EphemerisSystem;
import org.gogpsproject.IonoGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.SatellitePosition;
import org.gogpsproject.StreamResource;
import org.gogpsproject.Time;

/**
 * <p>
 * Class for parsing RINEX navigation files
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class RinexNavigationParser extends EphemerisSystem implements NavigationProducer{

	private File fileNav;
	private FileInputStream streamNav;
	private InputStreamReader inStreamNav;
	private BufferedReader buffStreamNav;

	private FileOutputStream cacheOutputStream;
	private OutputStreamWriter cacheStreamWriter;

	public static String newline = System.getProperty("line.separator");

	private ArrayList<EphGps> eph = new ArrayList<EphGps>(); /* GPS broadcast ephemerides */
	//private double[] iono = new double[8]; /* Ionosphere model parameters */
	private IonoGps iono = null; /* Ionosphere model parameters */
//	private double A0; /* Delta-UTC parameters: A0 */
//	private double A1; /* Delta-UTC parameters: A1 */
//	private double T; /* Delta-UTC parameters: T */
//	private double W; /* Delta-UTC parameters: W */
//	private int leaps; /* Leap seconds */


	// RINEX Read constructors
	public RinexNavigationParser(File fileNav) {
		this.fileNav = fileNav;
	}

	// RINEX Read constructors
	public RinexNavigationParser(InputStream is, File cache) {
		this.inStreamNav = new InputStreamReader(is);
		if(cache!=null){
			File path = cache.getParentFile();
			if(!path.exists()) path.mkdirs();
			try {
				cacheOutputStream = new FileOutputStream(cache);
				cacheStreamWriter = new OutputStreamWriter(cacheOutputStream);
			} catch (FileNotFoundException e) {
				System.err.println("Exception writing "+cache);
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.Navigation#init()
	 */
	@Override
	public void init() {
		open();
		if(parseHeaderNav()){
			parseDataNav();
		}
		close();
	}



	/* (non-Javadoc)
	 * @see org.gogpsproject.Navigation#release()
	 */
	@Override
	public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {

	}

	/**
	 *
	 */
	public void open() {
		try {

			if(fileNav!=null) streamNav = new FileInputStream(fileNav);
			if(streamNav!=null) inStreamNav = new InputStreamReader(streamNav);
			if(inStreamNav!=null) buffStreamNav = new BufferedReader(inStreamNav);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public void close() {
		try {
			if(cacheStreamWriter!=null){
				cacheStreamWriter.flush();
				cacheStreamWriter.close();
			}
			if(cacheOutputStream!=null){
				cacheOutputStream.flush();
				cacheOutputStream.close();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {

			if(buffStreamNav!=null) buffStreamNav.close();
			if(inStreamNav!=null) inStreamNav.close();
			if(streamNav!=null) streamNav.close();


		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 *
	 */
	public boolean parseHeaderNav() {

		//Navigation.iono = new double[8];
		String sub;

		try {

			while (buffStreamNav.ready()) {

				try {
					String line = buffStreamNav.readLine();
					if(cacheStreamWriter!=null){
						cacheStreamWriter.write(line);
						cacheStreamWriter.write(newline);
					}

					String typeField = line.substring(60, line.length());
					typeField = typeField.trim();

					if (typeField.equals("RINEX VERSION / TYPE")) {

						if (!line.substring(20, 21).equals("N")) {

							// Error if navigation file identifier was not found
							System.err.println("Navigation file identifier is missing in file " + fileNav.toString() + " header");
							return false;
						}

					} else if (typeField.equals("ION ALPHA")) {

						float a[] = new float[4];
						sub = line.substring(3, 14).replace('D', 'e');
						//Navigation.iono[0] = Double.parseDouble(sub.trim());
						a[0] = Float.parseFloat(sub.trim());

						sub = line.substring(15, 26).replace('D', 'e');
						//Navigation.iono[1] = Double.parseDouble(sub.trim());
						a[1] = Float.parseFloat(sub.trim());

						sub = line.substring(27, 38).replace('D', 'e');
						//Navigation.iono[2] = Double.parseDouble(sub.trim());
						a[2] = Float.parseFloat(sub.trim());

						sub = line.substring(39, 50).replace('D', 'e');
						//Navigation.iono[3] = Double.parseDouble(sub.trim());
						a[3] = Float.parseFloat(sub.trim());

						if(iono==null) iono = new IonoGps();
						iono.setAlpha(a);

					} else if (typeField.equals("ION BETA")) {

						float b[] = new float[4];

						sub = line.substring(3, 14).replace('D', 'e');
						//Navigation.iono[4] = Double.parseDouble(sub.trim());
						//setIono(4, Double.parseDouble(sub.trim()));
						b[0] = Float.parseFloat(sub.trim());


						sub = line.substring(15, 26).replace('D', 'e');
						//Navigation.iono[5] = Double.parseDouble(sub.trim());
						//setIono(5, Double.parseDouble(sub.trim()));
						b[1] = Float.parseFloat(sub.trim());

						sub = line.substring(27, 38).replace('D', 'e');
						//Navigation.iono[6] = Double.parseDouble(sub.trim());
						//setIono(6, Double.parseDouble(sub.trim()));
						b[2] = Float.parseFloat(sub.trim());

						sub = line.substring(39, 50).replace('D', 'e');
						//Navigation.iono[7] = Double.parseDouble(sub.trim());
						//setIono(7, Double.parseDouble(sub.trim()));
						b[3] = Float.parseFloat(sub.trim());

						if(iono==null) iono = new IonoGps();
						iono.setBeta(b);

					} else if (typeField.equals("DELTA-UTC: A0,A1,T,W")) {

						if(iono==null) iono = new IonoGps();

						sub = line.substring(3, 22).replace('D', 'e');
						//setA0(Double.parseDouble(sub.trim()));
						iono.setUtcA0(Double.parseDouble(sub.trim()));

						sub = line.substring(22, 41).replace('D', 'e');
						//setA1(Double.parseDouble(sub.trim()));
						iono.setUtcA1(Double.parseDouble(sub.trim()));

						sub = line.substring(41, 50).replace('D', 'e');
						//setT(Integer.parseInt(sub.trim()));
						// TODO need check
						iono.setUtcWNT(Integer.parseInt(sub.trim()));

						sub = line.substring(50, 59).replace('D', 'e');
						//setW(Integer.parseInt(sub.trim()));
						// TODO need check
						iono.setUtcTOW(Integer.parseInt(sub.trim()));

					} else if (typeField.equals("LEAP SECONDS")) {
						if(iono==null) iono = new IonoGps();
						sub = line.substring(0, 6).trim().replace('D', 'e');
						//setLeaps(Integer.parseInt(sub.trim()));
						// TODO need check
						iono.setUtcLS(Integer.parseInt(sub.trim()));

					} else if (typeField.equals("END OF HEADER")) {

						return true;
					}
				} catch (StringIndexOutOfBoundsException e) {
					// Skip over blank lines
				}
			}

			// Display an error if END OF HEADER was not reached
			System.err.println("END OF HEADER was not found in file "
					+ fileNav.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Read all navigation data
	 */
	public void parseDataNav() {

		try {

			// Resizable array
			//Navigation.eph = new ArrayList<EphGps>();

			int j = 0;

			EphGps eph = null;

			while (buffStreamNav.ready()) {

				String sub;

				// read 8 lines
				for (int i = 0; i < 8; i++) {

					String line = buffStreamNav.readLine();
					if(cacheStreamWriter!=null){
						cacheStreamWriter.write(line);
						cacheStreamWriter.write(newline);
					}

					try {

						int len = line.length();

						if (len != 0) {

							if (i == 0) { // LINE 1

								//Navigation.eph.get(j).refTime = new Time();

								eph = new EphGps();
								//Navigation.eph.add(eph);
								addEph(eph);

								// Get satellite ID
								sub = line.substring(0, 2).trim();
								eph.setSatID(Integer.parseInt(sub));

								// Get and format date and time string
								String dT = line.substring(2, 22);
								dT = dT.replace("  ", " 0").trim();
								dT = "20" + dT;

								try {
									//Time timeEph = new Time(dT);
									// Convert String to UNIX standard time in
									// milliseconds
									//timeEph.msec = Time.dateStringToTime(dT);
									Time toc = new Time(dT);
									eph.setRefTime(toc);
									eph.setToc(toc.getGpsWeekSec());

									// sets Iono reference time
									if(iono!=null && iono.getRefTime()==null) iono.setRefTime(new Time(dT));

								} catch (ParseException e) {
									System.err.println("Time parsing failed");
								}

								sub = line.substring(22, 41).replace('D', 'e');
								eph.setAf0(Double.parseDouble(sub.trim()));

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setAf1(Double.parseDouble(sub.trim()));

								sub = line.substring(60, len).replace('D', 'e');
								eph.setAf2(Double.parseDouble(sub.trim()));

							} else if (i == 1) { // LINE 2

								sub = line.substring(3, 22).replace('D', 'e');
								double iode = Double.parseDouble(sub.trim());
								// TODO check double -> int conversion ?
								eph.setIode((int) iode);

								sub = line.substring(22, 41).replace('D', 'e');
								eph.setCrs(Double.parseDouble(sub.trim()));

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setDeltaN(Double.parseDouble(sub.trim()));

								sub = line.substring(60, len).replace('D', 'e');
								eph.setM0(Double.parseDouble(sub.trim()));

							} else if (i == 2) { // LINE 3

								sub = line.substring(0, 22).replace('D', 'e');
								eph.setCuc(Double.parseDouble(sub.trim()));

								sub = line.substring(22, 41).replace('D', 'e');
								eph.setE(Double.parseDouble(sub.trim()));

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setCus(Double.parseDouble(sub .trim()));

								sub = line.substring(60, len).replace('D', 'e');
								eph.setRootA(Double.parseDouble(sub.trim()));

							} else if (i == 3) { // LINE 4

								sub = line.substring(0, 22).replace('D', 'e');
								eph.setToe(Double.parseDouble(sub.trim()));

								sub = line.substring(22, 41).replace('D', 'e');
								eph.setCic(Double.parseDouble(sub.trim()));

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setOmega0(Double.parseDouble(sub.trim()));

								sub = line.substring(60, len).replace('D', 'e');
								eph.setCis(Double.parseDouble(sub.trim()));

							} else if (i == 4) { // LINE 5

								sub = line.substring(0, 22).replace('D', 'e');
								eph.setI0(Double.parseDouble(sub.trim()));

								sub = line.substring(22, 41).replace('D', 'e');
								eph.setCrc(Double.parseDouble(sub.trim()));

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setOmega(Double.parseDouble(sub.trim()));

								sub = line.substring(60, len).replace('D', 'e');
								eph.setOmegaDot(Double.parseDouble(sub.trim()));

							} else if (i == 5) { // LINE 6

								sub = line.substring(0, 22).replace('D', 'e');
								eph.setiDot(Double.parseDouble(sub.trim()));

								sub = line.substring(22, 41).replace('D', 'e');
								double L2Code = Double.parseDouble(sub.trim());
								eph.setL2Code((int) L2Code);

								sub = line.substring(41, 60).replace('D', 'e');
								double week = Double.parseDouble(sub.trim());
								eph.setWeek((int) week);

								sub = line.substring(60, len).replace('D', 'e');
								double L2Flag = Double.parseDouble(sub.trim());
								eph.setL2Flag((int) L2Flag);

							} else if (i == 6) { // LINE 7

								sub = line.substring(0, 22).replace('D', 'e');
								double svAccur = Double.parseDouble(sub.trim());
								eph.setSvAccur((int) svAccur);

								sub = line.substring(22, 41).replace('D', 'e');
								double svHealth = Double.parseDouble(sub.trim());
								eph.setSvHealth((int) svHealth);

								sub = line.substring(41, 60).replace('D', 'e');
								eph.setTgd(Double.parseDouble(sub.trim()));

								sub = line.substring(60, len).replace('D', 'e');
								double iodc = Double.parseDouble(sub.trim());
								eph.setIodc((int) iodc);

							} else if (i == 7) { // LINE 8

								sub = line.substring(0, 22).replace('D', 'e');
								eph.setTom(Double.parseDouble(sub.trim()));

								if (len > 22) {
									sub = line.substring(22, 41).replace('D', 'e');
									eph.setFitInt(Double.parseDouble(sub.trim()));

								} else {
									eph.setFitInt(0);
								}
							}
						} else {
							i--;
						}
					} catch (NullPointerException e) {
						// Skip over blank lines
					}
				}

				// Increment array index
				j++;
				// Store the number of ephemerides
				//Navigation.n = j;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param unixTime
	 * @param satID
	 * @return Reference ephemeris set for given time and satellite
	 */
	public EphGps findEph(long unixTime, int satID) {

		long dt = 0;
		long dtMin = 0;
		EphGps refEph = null;

		//long gpsTime = (new Time(unixTime)).getGpsTime();

		for (int i = 0; i < eph.size(); i++) {
			// Find ephemeris sets for given satellite
			if (eph.get(i).getSatID() == satID) {
				// Compare current time and ephemeris reference time
				dt = Math.abs(eph.get(i).getRefTime().getMsec() - unixTime /*getGpsTime() - gpsTime*/);
				// If it's the first round, set the minimum time difference and
				// select the first ephemeris set candidate
				if (refEph == null) {
					dtMin = dt;
					refEph = eph.get(i);
					// Check if the current ephemeris set is closer in time than
					// the previous candidate; if yes, select new candidate
				} else if (dt < dtMin) {
					dtMin = dt;
					refEph = eph.get(i);
				}
			}
		}
		
		//check satellite health
//		temporary comment out by Yoshida, since NVS does not include health value
		if (refEph != null && refEph.getSvHealth() != 0) {
			refEph = null;
		}
		
		return refEph;
	}

	public int getEphSize(){
		return eph.size();
	}

	public void addEph(EphGps eph){
		this.eph.add(eph);
	}

//	public void setIono(int i, double val){
//		this.iono[i] = val;
//	}
	public IonoGps getIono(long unixTime){
		return iono;
	}
//	/**
//	 * @return the a0
//	 */
//	public double getA0() {
//		return A0;
//	}
//	/**
//	 * @param a0 the a0 to set
//	 */
//	public void setA0(double a0) {
//		A0 = a0;
//	}
//	/**
//	 * @return the a1
//	 */
//	public double getA1() {
//		return A1;
//	}
//	/**
//	 * @param a1 the a1 to set
//	 */
//	public void setA1(double a1) {
//		A1 = a1;
//	}
//	/**
//	 * @return the t
//	 */
//	public double getT() {
//		return T;
//	}
//	/**
//	 * @param t the t to set
//	 */
//	public void setT(double t) {
//		T = t;
//	}
//	/**
//	 * @return the w
//	 */
//	public double getW() {
//		return W;
//	}
//	/**
//	 * @param w the w to set
//	 */
//	public void setW(double w) {
//		W = w;
//	}
//	/**
//	 * @return the leaps
//	 */
//	public int getLeaps() {
//		return leaps;
//	}
//	/**
//	 * @param leaps the leaps to set
//	 */
//	public void setLeaps(int leaps) {
//		this.leaps = leaps;
//	}

	public boolean isTimestampInEpocsRange(long unixTime){
		return eph.size()>0 &&
		eph.get(0).getRefTime().getMsec() <= unixTime /*&&
		unixTime <= eph.get(eph.size()-1).getRefTime().getMsec() missing interval +epochInterval*/;
	}


	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long unixTime, int satID, double range, double receiverClockError) {
		EphGps eph = findEph(unixTime, satID);

		if (eph != null) {
			SatellitePosition sp = computePositionGps(unixTime,satID, eph, range, receiverClockError);
			//if(receiverPosition!=null) earthRotationCorrection(receiverPosition, sp);
			return sp;// new SatellitePosition(eph, unixTime, satID, range);
		}
		return null;
	}

}
