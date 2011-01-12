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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;

import org.ejml.data.SimpleMatrix;
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
public class RinexNavigationParser implements NavigationProducer{

	private File fileNav;
	private FileInputStream streamNav;
	private InputStreamReader inStreamNav;
	private BufferedReader buffStreamNav;

	private FileOutputStream cacheOutputStream;
	private OutputStreamWriter cacheStreamWriter;

	public static String newline = System.getProperty("line.separator");

	private ArrayList<EphGps> eph = new ArrayList<EphGps>(); /* GPS broadcast ephemerides */
	private double[] iono = new double[8]; /* Ionosphere model parameters */
	private double A0; /* Delta-UTC parameters: A0 */
	private double A1; /* Delta-UTC parameters: A1 */
	private double T; /* Delta-UTC parameters: T */
	private double W; /* Delta-UTC parameters: W */
	private int leaps; /* Leap seconds */


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
	public void release() {

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
	 * @param utcTime
	 * @param satID
	 * @return Reference ephemeris set for given time and satellite
	 */
	public EphGps findEph(long utcTime, int satID) {

		long dt = 0;
		long dtMin = 0;
		EphGps refEph = null;

		//long gpsTime = (new Time(utcTime)).getGpsTime();

		for (int i = 0; i < eph.size(); i++) {
			// Find ephemeris sets for given satellite
			if (eph.get(i).getSatID() == satID) {
				// Compare current time and ephemeris reference time
				dt = Math.abs(eph.get(i).getRefTime().getMsec() - utcTime /*getGpsTime() - gpsTime*/);
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
	public double getIono(long utcTime, int i){
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

	public boolean isTimestampInEpocsRange(long utcTime){
		return eph.size()>0 &&
		eph.get(0).getRefTime().getMsec() <= utcTime &&
		utcTime <= eph.get(eph.size()-1).getRefTime().getMsec()/* missing interval +epochInterval*/;
	}


	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long utcTime, int satID, double range, Coordinates receiverPosition) {
		EphGps eph = findEph(utcTime, satID);

		if (eph != null) {
			SatellitePosition sp = computePositionGps(utcTime,satID, eph, range);
			if(receiverPosition!=null) earthRotationCorrection(receiverPosition, sp);
			return sp;// new SatellitePosition(eph, utcTime, satID, range);
		}
		return null;
	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param satID
	 * @param range
	 * @param approxPos
	 */
	private SatellitePosition computePositionGps(long utcTime,int satID, EphGps eph, double obsPseudorange) {

		double timeCorrection = getTimeCorrection(utcTime, eph, obsPseudorange);

		// Compute clock correction
		double tGPS = getClockCorrection(utcTime, timeCorrection, obsPseudorange);

		// Compute eccentric anomaly
		double Ek = eccAnomaly(tGPS, eph);

		// Semi-major axis
		double A = eph.getRootA() * eph.getRootA();

		// Time from the ephemerides reference epoch
		double tk = checkGpsTime(tGPS - eph.getToe());

		// Position computation
		double fk = Math.atan2(Math.sqrt(1 - Math.pow(eph.getE(), 2))
				* Math.sin(Ek), Math.cos(Ek) - eph.getE());
		double phi = fk + eph.getOmega();
		phi = Math.IEEEremainder(phi, 2 * Math.PI);
		double u = phi + eph.getCuc() * Math.cos(2 * phi) + eph.getCus()
				* Math.sin(2 * phi);
		double r = A * (1 - eph.getE() * Math.cos(Ek)) + eph.getCrc()
				* Math.cos(2 * phi) + eph.getCrs() * Math.sin(2 * phi);
		double ik = eph.getI0() + eph.getiDot() * tk + eph.getCic() * Math.cos(2 * phi)
				+ eph.getCis() * Math.sin(2 * phi);
		double Omega = eph.getOmega0()
				+ (eph.getOmegaDot() - Constants.EARTH_ANGULAR_VELOCITY) * tk
				- Constants.EARTH_ANGULAR_VELOCITY * eph.getToe();
		Omega = Math.IEEEremainder(Omega + 2 * Math.PI, 2 * Math.PI);
		double x1 = Math.cos(u) * r;
		double y1 = Math.sin(u) * r;

		// Coordinates
//			double[][] data = new double[3][1];
//			data[0][0] = x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega);
//			data[1][0] = x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega);
//			data[2][0] = y1 * Math.sin(ik);

		// Fill in the satellite position matrix
		//this.coord.ecef = new SimpleMatrix(data);
		//this.coord = Coordinates.globalXYZInstance(new SimpleMatrix(data));
		SatellitePosition sp = new SatellitePosition(utcTime,satID, x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
				y1 * Math.sin(ik));
		sp.setTimeCorrection(timeCorrection);

		return sp;
//		this.setXYZ(x1 * Math.cos(Omega) - y1 * Math.cos(ik) * Math.sin(Omega),
//				x1 * Math.sin(Omega) + y1 * Math.cos(ik) * Math.cos(Omega),
//				y1 * Math.sin(ik));

	}

	/**
	 * @param time
	 *            (Uncorrected GPS time)
	 * @return GPS time accounting for beginning or end of week crossover
	 */
	private static double checkGpsTime(double time) {

		// Account for beginning or end of week crossover
		if (time > Constants.SEC_IN_HALF_WEEK) {
			time = time - 2 * Constants.SEC_IN_HALF_WEEK;
		} else if (time < -Constants.SEC_IN_HALF_WEEK) {
			time = time + 2 * Constants.SEC_IN_HALF_WEEK;
		}
		return time;
	}

	/**
	 * @param traveltime
	 */
	public void earthRotationCorrection(Coordinates approxPos, Coordinates satelitePosition) {

		// Computation of signal travel time
		//SimpleMatrix diff = this.coord.ecef.minus(approxPos.ecef);
		SimpleMatrix diff = satelitePosition.minusXYZ(approxPos);//this.coord.minusXYZ(approxPos);
		double rho2 = Math.pow(diff.get(0), 2) + Math.pow(diff.get(1), 2)
				+ Math.pow(diff.get(2), 2);
		double traveltime = Math.sqrt(rho2) / Constants.SPEED_OF_LIGHT;

		// Compute rotation angle
		double omegatau = Constants.EARTH_ANGULAR_VELOCITY * traveltime;

		// Rotation matrix
		double[][] data = new double[3][3];
		data[0][0] = Math.cos(omegatau);
		data[0][1] = Math.sin(omegatau);
		data[0][2] = 0;
		data[1][0] = -Math.sin(omegatau);
		data[1][1] = Math.cos(omegatau);
		data[1][2] = 0;
		data[2][0] = 0;
		data[2][1] = 0;
		data[2][2] = 1;
		SimpleMatrix R = new SimpleMatrix(data);

		// Apply rotation
		//this.coord.ecef = R.mult(this.coord.ecef);
		//this.coord.setSMMultXYZ(R);// = R.mult(this.coord.ecef);
		satelitePosition.setSMMultXYZ(R);// = R.mult(this.coord.ecef);

	}

	/**
	 * @param eph
	 * @return Clock-corrected GPS time
	 */
	private double getClockCorrection(long utcTime, double timeCorrection, double obsPseudorange) {

		long gpsTime = (new Time(utcTime)).getGpsTime();
		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		return tRaw - timeCorrection;
	}

	/**
	 * @param eph
	 * @return Satellite clock error
	 */
	private double getTimeCorrection(long utcTime, EphGps eph, double obsPseudorange){
		long gpsTime = (new Time(utcTime)).getGpsTime();
		// Remove signal travel time from observation time
		double tRaw = (gpsTime - obsPseudorange /*this.range*/ / Constants.SPEED_OF_LIGHT);

		// Compute eccentric anomaly
		double Ek = eccAnomaly(tRaw, eph);

		// Relativistic correction term computation
		double dtr = Constants.RELATIVISTIC_ERROR_CONSTANT * eph.getE() * eph.getRootA() * Math.sin(Ek);

		// Clock error computation
		double dt = checkGpsTime(tRaw - eph.getToc());
		double timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();
		double tGPS = tRaw - timeCorrection;
		dt = checkGpsTime(tGPS - eph.getToc());
		timeCorrection = (eph.getAf2() * dt + eph.getAf1()) * dt + eph.getAf0() + dtr - eph.getTgd();

		return timeCorrection;
	}

	/**
	 * @param time
	 *            (GPS time in seconds)
	 * @param eph
	 * @return Eccentric anomaly
	 */
	private static double eccAnomaly(double time, EphGps eph) {

		// Semi-major axis
		double A = eph.getRootA() * eph.getRootA();

		// Time from the ephemerides reference epoch
		double tk = checkGpsTime(time - eph.getToe());

		// Computed mean motion [rad/sec]
		double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT / Math.pow(A, 3));

		// Corrected mean motion [rad/sec]
		double n = n0 + eph.getDeltaN();

		// Mean anomaly
		double Mk = eph.getM0() + n * tk;

		// Eccentric anomaly starting value
		Mk = Math.IEEEremainder(Mk + 2 * Math.PI, 2 * Math.PI);
		double Ek = Mk;

		int i;
		double EkOld, dEk;

		// Eccentric anomaly iterative computation
		for (i = 0; i < 10; i++) {
			EkOld = Ek;
			Ek = Mk + eph.getE() * Math.sin(Ek);
			dEk = Math.IEEEremainder(Ek - EkOld, 2 * Math.PI);
			if (Math.abs(dEk) < 1e-12)
				break;
		}

		// TODO Display/log warning message
		if (i == 10)
			System.out.println("Eccentric anomaly does not converge");

		return Ek;

	}

}
