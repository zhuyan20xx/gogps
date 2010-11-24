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
package org.gogpsproject.parser.rinex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;

import org.gogpsproject.Constants;
import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.SatellitePosition;
import org.gogpsproject.Time;

/**
 * <p>
 * Class for parsing RINEX files
 * </p>
 *
 * @author ege, Cryms.com
 */
public class RinexFileNavigation implements NavigationProducer{

	private File fileNav;
	private FileInputStream streamNav;
	private InputStreamReader inStreamNav;
	private BufferedReader buffStreamNav;

	private ArrayList<EphGps> eph; /* GPS broadcast ephemerides */
	private double[] iono; /* Ionosphere model parameters */
	private double A0; /* Delta-UTC parameters: A0 */
	private double A1; /* Delta-UTC parameters: A1 */
	private double T; /* Delta-UTC parameters: T */
	private double W; /* Delta-UTC parameters: W */
	private int leaps; /* Leap seconds */


	// RINEX Read constructors
	public RinexFileNavigation(File fileNav) {
		this.fileNav = fileNav;
		eph = new ArrayList<EphGps>();
		iono = new double[8];
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.Navigation#init()
	 */
	@Override
	public void init() {
		open();
		parseHeaderNav();
		parseDataNav();
		close();
	}



	/* (non-Javadoc)
	 * @see org.gogpsproject.Navigation#release()
	 */
	@Override
	public void release() {

	}

	/**
	 *
	 */
	public void open() {
		try {

			streamNav = new FileInputStream(fileNav);
			inStreamNav = new InputStreamReader(streamNav);
			buffStreamNav = new BufferedReader(inStreamNav);

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public void close() {
		try {

			streamNav.close();
			streamNav.close();
			buffStreamNav.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void parseHeaderNav() {

		//Navigation.iono = new double[8];
		String sub;

		try {

			while (buffStreamNav.ready()) {

				try {
					String line = buffStreamNav.readLine();
					String typeField = line.substring(60, line.length());
					typeField = typeField.trim();

					if (typeField.equals("RINEX VERSION / TYPE")) {

						if (!line.substring(20, 21).equals("N")) {

							// Error if navigation file identifier was not found
							System.err.println("Navigation file identifier is missing in file " + fileNav.toString() + " header");
						}

					} else if (typeField.equals("ION ALPHA")) {

						sub = line.substring(3, 14).replace('D', 'e');
						//Navigation.iono[0] = Double.parseDouble(sub.trim());
						setIono(0, Double.parseDouble(sub.trim()));

						sub = line.substring(15, 26).replace('D', 'e');
						//Navigation.iono[1] = Double.parseDouble(sub.trim());
						setIono(1, Double.parseDouble(sub.trim()));

						sub = line.substring(27, 38).replace('D', 'e');
						//Navigation.iono[2] = Double.parseDouble(sub.trim());
						setIono(2, Double.parseDouble(sub.trim()));

						sub = line.substring(39, 50).replace('D', 'e');
						//Navigation.iono[3] = Double.parseDouble(sub.trim());
						setIono(3, Double.parseDouble(sub.trim()));


					} else if (typeField.equals("ION BETA")) {

						sub = line.substring(3, 14).replace('D', 'e');
						//Navigation.iono[4] = Double.parseDouble(sub.trim());
						setIono(4, Double.parseDouble(sub.trim()));


						sub = line.substring(15, 26).replace('D', 'e');
						//Navigation.iono[5] = Double.parseDouble(sub.trim());
						setIono(5, Double.parseDouble(sub.trim()));


						sub = line.substring(27, 38).replace('D', 'e');
						//Navigation.iono[6] = Double.parseDouble(sub.trim());
						setIono(6, Double.parseDouble(sub.trim()));


						sub = line.substring(39, 50).replace('D', 'e');
						//Navigation.iono[7] = Double.parseDouble(sub.trim());
						setIono(7, Double.parseDouble(sub.trim()));


					} else if (typeField.equals("DELTA-UTC: A0,A1,T,W")) {

						sub = line.substring(3, 22).replace('D', 'e');
						setA0(Double.parseDouble(sub.trim()));

						sub = line.substring(22, 41).replace('D', 'e');
						setA1(Double.parseDouble(sub.trim()));

						sub = line.substring(41, 50).replace('D', 'e');
						setT(Integer.parseInt(sub.trim()));

						sub = line.substring(50, 59).replace('D', 'e');
						setW(Integer.parseInt(sub.trim()));

					} else if (typeField.equals("LEAP SECONDS")) {

						sub = line.substring(0, 6).trim().replace('D', 'e');
						setLeaps(Integer.parseInt(sub.trim()));

					} else if (typeField.equals("END OF HEADER")) {

						return;
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
					int len = line.length();
					// Skip blank lines
					while (len == 0) {
						line = buffStreamNav.readLine();
						len = line.length();
					}

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
							eph.setRefTime(new Time(dT));

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
						eph.setToc(Double.parseDouble(sub.trim()));

						if (len > 22) {
							sub = line.substring(22, 41).replace('D', 'e');
							eph.setFitInt(Double.parseDouble(sub.trim()));

						} else {
							eph.setFitInt(0);
						}
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
	 * @param time
	 * @param satID
	 * @return Reference ephemeris set for given time and satellite
	 */
	public EphGps findEph(long time, int satID) {

		long dt = 0;
		long dtMin = 0;
		EphGps refEph = null;

		for (int i = 0; i < eph.size(); i++) {
			// Find ephemeris sets for given satellite
			if (eph.get(i).getSatID() == satID) {
				// Compare current time and ephemeris reference time
				dt = Math.abs(eph.get(i).getRefTime().getGpsTime() - time);
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
		return refEph;
	}

	public int getEphSize(){
		return eph.size();
	}

	public void addEph(EphGps eph){
		this.eph.add(eph);
	}

	public void setIono(int i, double val){
		this.iono[i] = val;
	}
	public double getIono(int i){
		return iono[i];
	}
	/**
	 * @return the a0
	 */
	public double getA0() {
		return A0;
	}
	/**
	 * @param a0 the a0 to set
	 */
	public void setA0(double a0) {
		A0 = a0;
	}
	/**
	 * @return the a1
	 */
	public double getA1() {
		return A1;
	}
	/**
	 * @param a1 the a1 to set
	 */
	public void setA1(double a1) {
		A1 = a1;
	}
	/**
	 * @return the t
	 */
	public double getT() {
		return T;
	}
	/**
	 * @param t the t to set
	 */
	public void setT(double t) {
		T = t;
	}
	/**
	 * @return the w
	 */
	public double getW() {
		return W;
	}
	/**
	 * @param w the w to set
	 */
	public void setW(double w) {
		W = w;
	}
	/**
	 * @return the leaps
	 */
	public int getLeaps() {
		return leaps;
	}
	/**
	 * @param leaps the leaps to set
	 */
	public void setLeaps(int leaps) {
		this.leaps = leaps;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long time, int satID, double range) {
		EphGps eph = findEph(time, satID);

		if (eph != null) {
			return new SatellitePosition(eph, time, satID, range);
		}
		return null;
	}

}
