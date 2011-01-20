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
import java.util.ArrayList;
import org.ejml.data.SimpleMatrix;

/**
 * <p>
 * Receiver position class
 * </p>
 *
 * @author ege, Cryms.com
 */
public class ReceiverPosition extends Coordinates{

	/* Satellites */
	private double cutoff; /* Elevation cutoff */
	private int pivot; /* Index of the satellite with highest elevation in satAvail list */
	private ArrayList<Integer> satAvail; /* List of satellites available for processing */
	private ArrayList<Integer> satAvailPhase; /* List of satellites available for processing */
	private SatellitePosition[] pos; /* Absolute position of all visible satellites (ECEF) */

	//private Coordinates coord; /* Receiver coordinates */
	private SimpleMatrix covariance; /* Covariance matrix of the estimation error */
	private double receiverClockError; /* Clock error */

	// Fields for satellite selection
	private ObservationSet[] masterOrdered;
	private TopocentricCoordinates[] roverTopo;
	private TopocentricCoordinates[] masterTopo;

	// Fields for Kalman filter
	int o1, o2, o3;
	int i1, i2, i3;
	int nN;
	private SimpleMatrix T;
	private SimpleMatrix H;
	private SimpleMatrix y0;
	private SimpleMatrix Cvv;
	private SimpleMatrix Cee;
	private SimpleMatrix Cnn;
	private SimpleMatrix KFstate;
	private SimpleMatrix KFprediction;

	// Fields for keeping track of satellite configuration changes
	private ArrayList<Integer> satOld;
	private int pivotOldId;

	private GoGPS goGPS;

	public ReceiverPosition(GoGPS goGPS){
		super();
		this.goGPS = goGPS;
		this.setXYZ(0.0, 0.0, 0.0);
		this.receiverClockError = 0.0;
	}

	/**
	 * @param obs
	 */
	public void bancroft(Observations obs) {

		//this.coord = null;
		//this.coord = Coordinates.globalXYZInstance(0.0, 0.0, 0.0);

		double travelTime = 0;
		double angle;
		double a, b, c;
		double root;
		double[] r, omc;
		double cdt, calc;
		double rho;

		// Define matrices
		SimpleMatrix Binit;
		SimpleMatrix B;
		SimpleMatrix BBB;
		SimpleMatrix BBBe;
		SimpleMatrix BBBalpha;
		SimpleMatrix e;
		SimpleMatrix alpha;
		SimpleMatrix possiblePosA;
		SimpleMatrix possiblePosB;

		// Allocate vectors
		r = new double[2];
		omc = new double[2];

		// Number of GPS observations
		int nObs = obs.getGpsSize();

		// Allocate an array to store GPS satellite positions
		pos = new SatellitePosition[nObs];

		// Allocate a 2D array to store Bancroft matrix data
		double[][] dataB = new double[nObs][4];

		int p=0;
		for (int i = 0; i < nObs; i++) {

			// Create new satellite position object
			//pos[i] = new SatellitePosition(obs.getRefTime().getGpsTime(), obs.getGpsSatID(i), obs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()));

			// Compute clock-corrected satellite position
			//pos[i].computePositionGps(goGPS.getNavigation());

			double obsPseudorange = obs.getGpsByIdx(i).getPseudorange(goGPS.getFreq());
			pos[i] = goGPS.getNavigation().getGpsSatPosition(obs.getRefTime().getMsec(), obs.getGpsSatID(i), obsPseudorange);

			try {
				System.out.println("SatPos "+obs.getGpsSatID(i)+" x:"+pos[i].getX()+" y:"+pos[i].getY()+" z:"+pos[i].getZ());
				// Store Bancroft matrix data (X, Y, Z and clock-corrected
				// range)
				dataB[p][0] = pos[i].getX();
				dataB[p][1] = pos[i].getY();
				dataB[p][2] = pos[i].getZ();
				dataB[p][3] = obsPseudorange + Constants.SPEED_OF_LIGHT * pos[i].getSatelliteClockError();
				p++;
			} catch (NullPointerException u) {
				System.out.println("Error: satellite positions not computed");
				//return; // don't break eggs so quickly :-)
			}
		}
		if(p==0) return;
		if(dataB.length != p){
			double[][] dataB1 = new double[p][4];
			for(int i=0;i<p;i++){
				dataB1[i]=dataB[i];
			}
			dataB = dataB1;
		}
		// Allocate matrices
		BBB = new SimpleMatrix(4, dataB.length);
		BBBe = new SimpleMatrix(4, 1);
		BBBalpha = new SimpleMatrix(4, 1);
		e = new SimpleMatrix(dataB.length, 1);
		alpha = new SimpleMatrix(dataB.length, 1);
		possiblePosA = new SimpleMatrix(4, 1);
		possiblePosB = new SimpleMatrix(4, 1);

		// Allocate initial B matrix
		Binit = new SimpleMatrix(dataB);

		// Make two iterations
		for (int iter = 0; iter < 2; iter++) {

			// Allocate B matrix
			B = new SimpleMatrix(Binit);

			for (int i = 0; i < dataB.length; i++) {

				double x = B.get(i, 0);
				double y = B.get(i, 1);

				if (iter == 0) {
					travelTime = Constants.GPS_APPROX_TRAVEL_TIME;
				} else {
					double z = B.get(i, 2);
					rho = Math.pow((x - this.getX()), 2)
							+ Math.pow((y - this.getY()), 2)
							+ Math.pow((z - this.getZ()), 2);
					travelTime = Math.sqrt(rho) / Constants.SPEED_OF_LIGHT;
				}
				angle = travelTime * Constants.EARTH_ANGULAR_VELOCITY;
				B.set(i, 0, Math.cos(angle) * x + Math.sin(angle) * y);
				B.set(i, 1, -Math.sin(angle) * x + Math.cos(angle) * y);
			}

			if (dataB.length > 4) {
				BBB = B.transpose().mult(B).solve(B.transpose());
			} else {
				BBB = B.invert();
			}

			e.set(1);
			for (int i = 0; i < dataB.length; i++) {

				alpha.set(i, 0, lorentzInnerProduct(B.extractMatrix(i, i, 0, 3), B
						.extractMatrix(i, i, 0, 3)) / 2);
			}

			BBBe = BBB.mult(e);
			BBBalpha = BBB.mult(alpha);
			a = lorentzInnerProduct(BBBe, BBBe);
			b = lorentzInnerProduct(BBBe, BBBalpha) - 1;
			c = lorentzInnerProduct(BBBalpha, BBBalpha);
			root = Math.sqrt(b * b - a * c);
			r[0] = (-b - root) / a;
			r[1] = (-b + root) / a;
			possiblePosA = BBBalpha.plus(r[0], BBBe);
			possiblePosB = BBBalpha.plus(r[1], BBBe);
			possiblePosA.set(3, 0, -possiblePosA.get(3, 0));
			possiblePosB.set(3, 0, -possiblePosB.get(3, 0));
			for (int i = 0; i < dataB.length; i++) {
				cdt = possiblePosA.get(3, 0);
				calc = B.extractMatrix(i, i, 0, 2).transpose().minus(
						possiblePosA.extractMatrix(0, 2, 0, 0)).normF()
						+ cdt;
				omc[0] = B.get(i, 3) - calc;
				cdt = possiblePosB.get(3, 0);
				calc = B.extractMatrix(i, i, 0, 2).transpose().minus(
						possiblePosB.extractMatrix(0, 2, 0, 0)).normF()
						+ cdt;
				omc[1] = B.get(i, 3) - calc;
			}

			// Discrimination between roots (choose one of the possible
			// positions)
			if (Math.abs(omc[0]) > Math.abs(omc[1])) {
				//this.coord.ecef = possiblePosB.extractMatrix(0, 2, 0, 0); // new SimpleMatrix(
				SimpleMatrix sm = possiblePosB.extractMatrix(0, 2, 0, 0);
				this.setXYZ(sm.get(0),sm.get(1),sm.get(2));
				// Clock offset
				this.receiverClockError = possiblePosB.get(3, 0) / Constants.SPEED_OF_LIGHT;
			} else {
				//this.coord.ecef = possiblePosA.extractMatrix(0, 2, 0, 0); // new SimpleMatrix(
				SimpleMatrix sm = possiblePosA.extractMatrix(0, 2, 0, 0);
				this.setXYZ(sm.get(0),sm.get(1),sm.get(2));
				// Clock offset
				this.receiverClockError = possiblePosA.get(3, 0) / Constants.SPEED_OF_LIGHT;
			}
		}

		// Compute Bancroft's positioning in geodetic coordinates
		this.computeGeodetic();
	}

	/**
	 * @param roverObs
	 */
	public void codeStandalone(Observations roverObs) {

		NavigationProducer navigation = goGPS.getNavigation();
		
		// Number of GPS observations
		int nObs = roverObs.getGpsSize();

		// Number of unknown parameters
		int nUnknowns = 4;

		// Define least squares matrices
		SimpleMatrix A;
		SimpleMatrix b;
		SimpleMatrix y0;
		SimpleMatrix Q;
		SimpleMatrix x;
		SimpleMatrix vEstim;
		SimpleMatrix tropoCorr;
		SimpleMatrix ionoCorr;

		// Number of available satellites (i.e. observations)
		int nObsAvail = satAvail.size();

		// Least squares design matrix
		A = new SimpleMatrix(nObsAvail, nUnknowns);

		// Vector for approximate pseudoranges
		b = new SimpleMatrix(nObsAvail, 1);

		// Vector for observed pseudoranges
		y0 = new SimpleMatrix(nObsAvail, 1);

		// Cofactor matrix
		Q = new SimpleMatrix(nObsAvail, nObsAvail);

		// Solution vector
		x = new SimpleMatrix(nUnknowns, 1);

		// Vector for observation error
		vEstim = new SimpleMatrix(nObsAvail, 1);

		// Vectors for troposphere and ionosphere corrections
		tropoCorr = new SimpleMatrix(nObsAvail, 1);
		ionoCorr = new SimpleMatrix(nObsAvail, 1);

		// Counter for available satellites
		int k = 0;

		// Initialize the cofactor matrix
		Q.set(1);

		// Second loop, using only satellites above the cutoff to set up the
		// least squares matrices
		for (int i = 0; i < nObs; i++) {

			if (pos[i]!=null && satAvail.contains(pos[i].getSatID())) {

				// Compute receiver-satellite approximate pseudorange
				//SimpleMatrix diff = this.coord.ecef.minus(pos[i].getCoord().ecef);
				SimpleMatrix diff = this.minusXYZ(pos[i]);
				double appRange = Math.sqrt(Math.pow(diff.get(0), 2)
						+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

				// Fill in one row in the design matrix
				A.set(k, 0, diff.get(0) / appRange); /* X */
				A.set(k, 1, diff.get(1) / appRange); /* Y */
				A.set(k, 2, diff.get(2) / appRange); /* Z */
				A.set(k, 3, 1); /* clock error */

				// Add the approximate pseudorange value to b
				b.set(k, 0, appRange - pos[i].getSatelliteClockError() * Constants.SPEED_OF_LIGHT);

				// Add the clock-corrected observed pseudorange value to y0
				y0.set(k, 0, roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()));

				// Correct approximate pseudorange for troposphere
				double elevation = roverTopo[i].getElevation();
				double height = this.getGeodeticHeight();//geod.get(2);
				double roverSatelliteTropoCorr = ComputingToolbox.computeTroposphereCorrection(elevation, height);

				// Correct approximate pseudorange for ionosphere
				double azimuth = roverTopo[i].getAzimuth();
				double roverSatelliteIonoCorr = ComputingToolbox
						.computeIonosphereCorrection(navigation,this, azimuth, elevation, roverObs.getRefTime());

				// Fill in troposphere and ionosphere double differenced
				// corrections
				tropoCorr.set(k, 0, roverSatelliteTropoCorr);
				ionoCorr.set(k, 0, roverSatelliteIonoCorr);

				// Fill in the cofactor matrix
				double weight = Q.get(k, k)
						+ computeWeight(elevation,
								roverObs.getGpsByIdx(i).getSignalStrength(goGPS.getFreq()));
				Q.set(k, k, weight);

				// Increment available satellites counter
				k++;
			}
		}

		// Apply troposphere and ionosphere correction
		b = b.plus(tropoCorr);
		b = b.plus(ionoCorr);

		// Least squares solution x = ((A'*Q^-1*A)^-1)*A'*Q^-1*(y0-b);
		x = A.transpose().mult(Q.invert()).mult(A).invert().mult(A.transpose())
				.mult(Q.invert()).mult(y0.minus(b));

		// Receiver position
		//this.coord.ecef.set(this.coord.ecef.plus(x.extractMatrix(0, 2, 0, 0)));
		this.setPlusXYZ(x.extractMatrix(0, 2, 0, 0));
		
		// Receiver clock error
		this.receiverClockError = x.get(3);

		// Estimation of the variance of the observation error
		vEstim = y0.minus(A.mult(x).plus(b));
		double varianceEstim = (vEstim.transpose().mult(Q.invert())
				.mult(vEstim)).get(0)
				/ (nObsAvail - nUnknowns);

		// Covariance matrix of the estimation error
		if (nObsAvail > nUnknowns)
			this.covariance = A.transpose().mult(Q.invert()).mult(A).invert()
					.scale(varianceEstim);
		else
			this.covariance = null;

		// Compute positioning in geodetic coordinates
		this.computeGeodetic();

	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 */
	public void codeDoubleDifferences(Observations roverObs,Observations masterObs, Coordinates masterPos) {

		NavigationProducer navigation = goGPS.getNavigation();
		// Number of GPS observations
		int nObs = roverObs.getGpsSize();

		// Number of unknown parameters
		int nUnknowns = 3;

		// Define least squares matrices
		SimpleMatrix A;
		SimpleMatrix b;
		SimpleMatrix y0;
		SimpleMatrix Q;
		SimpleMatrix x;
		SimpleMatrix vEstim;
		SimpleMatrix tropoCorr;
		SimpleMatrix ionoCorr;

		// Number of available satellites (i.e. observations)
		int nObsAvail = satAvail.size();

		// Double differences with respect to pivot satellite reduce
		// observations by 1
		nObsAvail--;

		//System.out.println(nObsAvail+" "+nUnknowns);
		// Least squares design matrix
		A = new SimpleMatrix(nObsAvail, nUnknowns);

		// Vector for approximate pseudoranges
		b = new SimpleMatrix(nObsAvail, 1);

		// Vector for observed pseudoranges
		y0 = new SimpleMatrix(nObsAvail, 1);

		// Cofactor matrix
		Q = new SimpleMatrix(nObsAvail, nObsAvail);

		// Solution vector
		x = new SimpleMatrix(nUnknowns, 1);

		// Vector for observation error
		vEstim = new SimpleMatrix(nObsAvail, 1);

		// Vectors for troposphere and ionosphere corrections
		tropoCorr = new SimpleMatrix(nObsAvail, 1);
		ionoCorr = new SimpleMatrix(nObsAvail, 1);

		// Counter for available satellites
		int k = 0;

		// Store rover-pivot and master-pivot observed pseudoranges
		double roverPivotObs = roverObs.getGpsByIdx(pivot).getPseudorange(goGPS.getFreq());
		double masterPivotObs = masterOrdered[pivot].getPseudorange(goGPS.getFreq());

		// Computation of rover-pivot approximate pseudoranges
		//SimpleMatrix diffRoverPivot = this.coord.ecef.minus(pos[pivot].getCoord().ecef);
		SimpleMatrix diffRoverPivot = this.minusXYZ(pos[pivot]);
		double roverPivotAppRange = Math.sqrt(Math
				.pow(diffRoverPivot.get(0), 2)
				+ Math.pow(diffRoverPivot.get(1), 2)
				+ Math.pow(diffRoverPivot.get(2), 2));

		// Computation of master-pivot approximate pseudoranges
		//SimpleMatrix diff = masterPos.ecef.minus(pos[pivot]].getCoord().ecef);
		SimpleMatrix diff = masterPos.minusXYZ(pos[pivot]);
		double masterPivotAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
				+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

		// Computation of rover-pivot troposphere correction
		double roverElevation = roverTopo[pivot].getElevation();
		double roverHeight = this.getGeodeticHeight();//geod.get(2);
		double roverPivotTropoCorr = ComputingToolbox.computeTroposphereCorrection(
				roverElevation, roverHeight);

		// Computation of master-pivot troposphere correction
		double masterElevation = masterTopo[pivot].getElevation();
		double masterHeight = masterPos.getGeodeticHeight();//geod.get(2);
		double masterPivotTropoCorr = ComputingToolbox.computeTroposphereCorrection(
				masterElevation, masterHeight);

		// Computation of rover-pivot ionosphere correction
		double roverAzimuth = roverTopo[pivot].getAzimuth();
		double roverPivotIonoCorr = ComputingToolbox.computeIonosphereCorrection(
				navigation, this, roverAzimuth, roverElevation, roverObs.getRefTime());

		// Computation of master-pivot ionosphere correction
		double masterAzimuth = masterTopo[pivot].getAzimuth();
		double masterPivotIonoCorr = ComputingToolbox.computeIonosphereCorrection(
				navigation, masterPos, masterAzimuth, masterElevation,
				roverObs.getRefTime());

		// Compute rover-pivot and master-pivot weights
		double roverPivotWeight = computeWeight(roverElevation,
				roverObs.getGpsByIdx(pivot).getSignalStrength(goGPS.getFreq()));
		double masterPivotWeight = computeWeight(masterElevation,
				masterOrdered[pivot].getSignalStrength(goGPS.getFreq()));
		Q.set(roverPivotWeight + masterPivotWeight);

		// Second loop, using only satellites above the cutoff to set up the
		// least squares matrices
		for (int i = 0; i < nObs; i++) {


			if (pos[i] !=null && satAvail.contains(pos[i].getSatID())
					&& i != pivot) {

				// Compute rover-satellite approximate pseudorange
				//SimpleMatrix diffRoverSat = this.coord.ecef.minus(pos[i].getCoord().ecef);
				SimpleMatrix diffRoverSat = this.minusXYZ(pos[i]);
				double roverSatAppRange = Math.sqrt(Math.pow(diffRoverSat.get(0), 2)
						+ Math.pow(diffRoverSat.get(1), 2)
						+ Math.pow(diffRoverSat.get(2), 2));

				// Compute master-satellite approximate pseudorange
				//diff = masterPos.ecef.minus(pos[i].ecef);
				diff = masterPos.minusXYZ(pos[i]);
				double masterSatAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
						+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

				// Fill in one row in the design matrix
				A.set(k, 0, diffRoverSat.get(0) / roverSatAppRange
						- diffRoverPivot.get(0) / roverPivotAppRange); /* X */

				A.set(k, 1, diffRoverSat.get(1) / roverSatAppRange
						- diffRoverPivot.get(1) / roverPivotAppRange); /* Y */

				A.set(k, 2, diffRoverSat.get(2) / roverSatAppRange
						- diffRoverPivot.get(2) / roverPivotAppRange); /* Z */

				// Add the differenced approximate pseudorange value to b
				b.set(k, 0, (roverSatAppRange - masterSatAppRange)
						- (roverPivotAppRange - masterPivotAppRange));

				// Add the differenced observed pseudorange value to y0
				y0.set(k, 0, (roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()) - masterOrdered[i].getPseudorange(goGPS.getFreq()))
						- (roverPivotObs - masterPivotObs));

				// Computation of rover-satellite troposphere correction
				roverElevation = roverTopo[i].getElevation();
				roverHeight = this.getGeodeticHeight();//geod.get(2);
				double roverSatTropoCorr = ComputingToolbox
						.computeTroposphereCorrection(roverElevation,
								roverHeight);

				// Computation of master-satellite troposphere correction
				masterElevation = masterTopo[i].getElevation();
				masterHeight = masterPos.getGeodeticHeight();//geod.get(2);
				double masterSatTropoCorr = ComputingToolbox
						.computeTroposphereCorrection(masterElevation,
								masterHeight);

				// Computation of rover-satellite ionosphere correction
				roverAzimuth = roverTopo[i].getAzimuth();
				double roverSatIonoCorr = ComputingToolbox
						.computeIonosphereCorrection(navigation,
								this, roverAzimuth, roverElevation, roverObs.getRefTime());

				// Computation of master-satellite ionosphere correction
				masterAzimuth = masterTopo[i].getAzimuth();
				double masterSatIonoCorr = ComputingToolbox
						.computeIonosphereCorrection(navigation,
								masterPos, masterAzimuth, masterElevation, roverObs.getRefTime());

				// Fill in troposphere and ionosphere double differenced
				// corrections
				tropoCorr.set(k, 0, (roverSatTropoCorr - masterSatTropoCorr)
						- (roverPivotTropoCorr - masterPivotTropoCorr));
				ionoCorr.set(k, 0, (roverSatIonoCorr - masterSatIonoCorr)
						- (roverPivotIonoCorr - masterPivotIonoCorr));

				// Fill in the cofactor matrix
				double roverSatWeight = computeWeight(roverElevation,
						roverObs.getGpsByIdx(i).getSignalStrength(goGPS.getFreq()));
				double masterSatWeight = computeWeight(masterElevation,
						masterOrdered[i].getSignalStrength(goGPS.getFreq()));
				Q.set(k, k, Q.get(k, k) + roverSatWeight + masterSatWeight);

				// Increment available satellites counter
				k++;
			}
		}

		// Apply troposphere and ionosphere correction
		b = b.plus(tropoCorr);
		b = b.plus(ionoCorr);

		// Least squares solution x = ((A'*Q^-1*A)^-1)*A'*Q^-1*(y0-b);
		x = A.transpose().mult(Q.invert()).mult(A).invert().mult(A.transpose())
				.mult(Q.invert()).mult(y0.minus(b));

		// Receiver position
		//this.coord.ecef.set(this.coord.ecef.plus(x));
		this.setPlusXYZ(x);

		// Estimation of the variance of the observation error
		vEstim = y0.minus(A.mult(x).plus(b));
		double varianceEstim = (vEstim.transpose().mult(Q.invert())
				.mult(vEstim)).get(0)
				/ (nObsAvail - nUnknowns);

		// Covariance matrix of the estimation error
		if (nObsAvail > nUnknowns){
			this.covariance = A.transpose().mult(Q.invert()).mult(A).invert()
					.scale(varianceEstim);
		}else{
			this.covariance = null;
		}
		// Compute positioning in geodetic coordinates
		this.computeGeodetic();
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 *
	 */
	public void kalmanFilterInit(Observations roverObs, Observations masterObs,
			Coordinates masterPos) {

		// Order-related quantities
		o1 = goGPS.getOrder();
		o2 = goGPS.getOrder() * 2;
		o3 = goGPS.getOrder() * 3;

		// Order-related indices
		i1 = o1 - 1;
		i2 = o2 - 1;
		i3 = o3 - 1;

		// Set number of ambiguities
		if (goGPS.isDualFreq())
			nN = 64;
		else
			nN = 32;

		// Allocate matrices
		T = SimpleMatrix.identity(o3 + nN);
		KFstate = new SimpleMatrix(o3 + nN, 1);
		KFprediction = new SimpleMatrix(o3 + nN, 1);
		Cvv = new SimpleMatrix(o3 + nN, o3 + nN);
		Cee = new SimpleMatrix(o3 + nN, o3 + nN);

		// System dynamics
		int j = 0;
		for (int i = 0; i < o3; i++) {
			if (j < (o1 - 1)) {
				T.set(i, i + 1, 1);
				j++;
			} else {
				j = 0;
			}
		}

		// Model error covariance matrix
		Cvv.zero();
		Cvv.set(i1, i1, Math.pow(goGPS.getStDevE(), 2));
		Cvv.set(i2, i2, Math.pow(goGPS.getStDevN(), 2));
		Cvv.set(i3, i3, Math.pow(goGPS.getStDevU(), 2));

		// Improve approximate position accuracy by applying
		// twice code double differences
		for (int i = 0; i < 2; i++) {

			selectSatellitesDoubleDiff(roverObs, masterObs, masterPos);

			codeDoubleDifferences(roverObs, masterObs, masterPos);
		}

		// If it was not possible to compute the covariance matrix
		if (this.covariance == null) {
			this.covariance = SimpleMatrix.identity(3).scale(
					Math.pow(goGPS.getStDevInit(), 2));
		}

		// Estimate phase ambiguities by comparing observed code and phase
		estimateAmbiguitiesObserv(roverObs, masterObs);

		// Initial state
		KFstate.set(0, 0, this.getX());
		KFstate.set(i1 + 1, 0, this.getY());
		KFstate.set(i2 + 1, 0, this.getZ());

		// Prediction
		KFprediction = T.mult(KFstate);

		// Covariance matrix of the initial state
		Cee.set(0, 0, this.covariance.get(0, 0));
		Cee.set(i1 + 1, i1 + 1, this.covariance.get(1, 1));
		Cee.set(i2 + 1, i2 + 1, this.covariance.get(2, 2));
		for (int i = 1; i < o1; i++) {
			Cee.set(i, i, Math.pow(goGPS.getStDevInit(), 2));
			Cee.set(i + i1 + 1, i + i1 + 1, Math.pow(goGPS.getStDevInit(), 2));
			Cee.set(i + i2 + 1, i + i2 + 1, Math.pow(goGPS.getStDevInit(), 2));
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 *
	 */
	public void kalmanFilterLoop(Observations roverObs, Observations masterObs, Coordinates masterPos) {

		// Save previous list of available satellites with phase
		satOld = satAvailPhase;

		// Save the ID of the previous pivot satellite
		pivotOldId = pos[pivot].getSatID();

		// Select satellites
		selectSatellitesDoubleDiff(roverObs, masterObs, masterPos);

		// Number of observations (code and phase)
		int nObs = satAvail.size();

		// Double differences with respect to pivot satellite reduce
		// number of observations by 1
		nObs = nObs - 1;

		if (satAvailPhase.size() != 0) {

			// Add number of satellites with phase (minus 1 for double diff)
			nObs = nObs + satAvailPhase.size() - 1;
		}

		if (satAvail.size() >= goGPS.getMinNumSat()) {

			// Allocate transformation matrix
			H = new SimpleMatrix(nObs, o3 + nN);

			// Allocate observation vector
			y0 = new SimpleMatrix(nObs, 1);

			// Allocate observation error covariance matrix
			Cnn = new SimpleMatrix(nObs, nObs);

			// Allocate K and G matrices
			SimpleMatrix K = new SimpleMatrix(o3 + nN, o3 + nN);
			SimpleMatrix G = new SimpleMatrix(o3 + nN, nObs);

			// Re-initialization of the model error covariance matrix
			Cvv.zero();

			// Set variances only if dynamic model is not static
			if (o1 != 1) {
				// Allocate and build rotation matrix
				SimpleMatrix R = new SimpleMatrix(3, 3);
				R = Coordinates.rotationMatrix(this);

				// Build 3x3 diagonal matrix with variances
				SimpleMatrix diagonal = new SimpleMatrix(3, 3);
				diagonal.zero();
				diagonal.set(0, 0, Math.pow(goGPS.getStDevE(), 2));
				diagonal.set(1, 1, Math.pow(goGPS.getStDevN(), 2));
				diagonal.set(2, 2, Math.pow(goGPS.getStDevU(), 2));

				// Propagate local variances to global variances
				diagonal = R.transpose().mult(diagonal).mult(R);

				// Set global variances in the model error covariance matrix
				Cvv.set(i1, i1, diagonal.get(0, 0));
				Cvv.set(i2, i2, diagonal.get(1, 1));
				Cvv.set(i3, i3, diagonal.get(2, 2));
			}

			// Set linearization point (approximate coordinates by KF
			// prediction at previous step)
			this.setXYZ(KFprediction.get(0), KFprediction.get(i1 + 1), KFprediction.get(i2 + 1));
//			this.coord.ecef.set(0, 0, KFprediction.get(0));
//			this.coord.ecef.set(1, 0, KFprediction.get(i1 + 1));
//			this.coord.ecef.set(2, 0, KFprediction.get(i2 + 1));

			// Fill in Kalman filter transformation matrix, observation
			// vector and observation error covariance matrix
			setupKalmanFilterInput(roverObs, masterObs, masterPos);

			// Check if satellite configuration changed since the previous epoch
			checkSatelliteConfiguration(roverObs, masterObs, masterPos);

			// Identity matrix
			SimpleMatrix I = SimpleMatrix.identity(o3 + nN);

			// Kalman filter equations
			K = T.mult(Cee).mult(T.transpose()).plus(Cvv);
			G = K.mult(H.transpose()).mult(
					H.mult(K).mult(H.transpose()).plus(Cnn).invert());
			KFstate = I.minus(G.mult(H)).mult(KFprediction).plus(G.mult(y0));
			KFprediction = T.mult(KFstate);
			Cee = I.minus(G.mult(H)).mult(K);

		} else {

			// Positioning only by system dynamics
			KFstate = KFprediction;
			KFprediction = T.mult(KFstate);
			Cee = T.mult(Cee).mult(T.transpose());
		}

		// Set receiver position to current state estimate
		this.setXYZ(KFstate.get(0), KFstate.get(i1 + 1), KFstate.get(i2 + 1));
//		this.coord.ecef.set(0, 0, KFstate.get(0));
//		this.coord.ecef.set(1, 0, KFstate.get(i1 + 1));
//		this.coord.ecef.set(2, 0, KFstate.get(i2 + 1));

		// Compute positioning in geodetic coordinates
		this.computeGeodetic();
	}

	/**
	 * @param roverObs
	 */
	public void selectSatellitesStandalone(Observations roverObs) {
		
		NavigationProducer navigation = goGPS.getNavigation();

		// Retrieve options from goGPS class
		double cutoff = goGPS.getCutoff();

		// Number of GPS observations
		int nObs = roverObs.getGpsSize();
		
		// Allocate an array to store GPS satellite positions
		pos = new SatellitePosition[nObs];

		// Create a list for available satellites after cutoff
		satAvail = new ArrayList<Integer>(0);

		// Create a list for available satellites with phase
		satAvailPhase = new ArrayList<Integer>(0);

		// Allocate array of topocentric coordinates
		roverTopo = new TopocentricCoordinates[nObs];

		// First loop to compute topocentric coordinates and 
		// select satellites above the cutoff level
		for (int i = 0; i < nObs; i++) {
			
			// Compute GPS satellite positions
			pos[i] = navigation.getGpsSatPosition(roverObs.getRefTime().getMsec(), roverObs.getGpsSatID(i), roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()));

			if(pos[i]!=null){

				// Compute azimuth, elevation and distance for each satellite
				roverTopo[i] = new TopocentricCoordinates();
				roverTopo[i].computeTopocentric(this, pos[i]);

				// Check if satellite elevation is higher than cutoff
				if (roverTopo[i].getElevation() > cutoff) {

					satAvail.add(pos[i].getSatID());
					System.out.println("Available sat "+pos[i].getSatID());

					// Check if also phase is available
					if (roverObs.getGpsByIdx(i).getPhase(goGPS.getFreq()) != 0) {
						System.out.println("Available sat phase "+pos[i].getSatID());
						satAvailPhase.add(pos[i].getSatID());
					}
				}else{
					System.out.println("Not available sat "+roverTopo[i].getElevation()+" < "+cutoff);
				}
			}
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 */
	public void selectSatellitesDoubleDiff(Observations roverObs,
			Observations masterObs, Coordinates masterPos) {
		
		NavigationProducer navigation = goGPS.getNavigation();

		// Retrieve options from goGPS class
		double cutoff = goGPS.getCutoff();

		// Number of GPS observations
		int nObs = roverObs.getGpsSize();
		
		// Allocate an array to store GPS satellite positions
		pos = new SatellitePosition[nObs];

		// Create a list for available satellites
		satAvail = new ArrayList<Integer>(0);

		// Create a list for available satellites with phase
		satAvailPhase = new ArrayList<Integer>(0);

		// Allocate arrays of topocentric coordinates
		roverTopo = new TopocentricCoordinates[nObs];
		masterTopo = new TopocentricCoordinates[nObs];

		// Variables to store highest elevation
		double maxElevCode = 0;
		double maxElevPhase = 0;

		// Variables for code pivot and phase pivot
		int pivotCode = -1;
		int pivotPhase = -1;

		// Array to store re-ordered master observations
		masterOrdered = new ObservationSet[nObs];

		// First loop to compute topocentric coordinates and 
		// select satellites above the cutoff level
		for (int i = 0; i < nObs; i++) {
			
			// Compute GPS satellite positions
			pos[i] = navigation.getGpsSatPosition(roverObs.getRefTime().getMsec() /*getGpsTime()*/, roverObs.getGpsSatID(i), roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()));

			if(pos[i]!=null){

				// Compute azimuth, elevation and distance for each satellite from
				// rover
				roverTopo[i] = new TopocentricCoordinates();
				roverTopo[i].computeTopocentric(this, pos[i]);

				// Compute azimuth, elevation and distance for each satellite from
				// master
				masterTopo[i] = new TopocentricCoordinates();
				masterTopo[i].computeTopocentric(masterPos, pos[i]);

				// Check if satellite is available for double differences, after
				// cutoff
				if (masterObs.containsGpsSatID(roverObs.getGpsSatID(i)) // gpsSat.get( // masterObs.gpsSat.contains(roverObs.getGpsSatID(i)
						&& roverTopo[i].getElevation() > cutoff) {

					// Store master observations according to the order of rover
					// observations
					masterOrdered[i] = new ObservationSet();
					masterOrdered[i] = masterObs.getGpsByID(roverObs.getGpsSatID(i)); //masterObs.getGps(masterObs.gpsSat.indexOf(roverObs.gpsSat.get(i)));

					// Find code pivot satellite (with highest elevation)
					if (roverTopo[i].getElevation() > maxElevCode) {
						pivotCode = i;
						maxElevCode = roverTopo[i].getElevation();
					}

					satAvail.add(pos[i].getSatID());

					// Check if also phase is available for both rover and master
					if (roverObs.getGpsByIdx(i).getPhase(goGPS.getFreq()) != 0
							&& masterOrdered[i].getPhase(goGPS.getFreq()) != 0) {

						// Find code pivot satellite (with highest elevation)
						if (roverTopo[i].getElevation() > maxElevPhase) {
							pivotPhase = i;
							maxElevPhase = roverTopo[i].getElevation();
						}

						satAvailPhase.add(pos[i].getSatID());
					}
				}
			}
		}

		// Select best pivot satellite
		if (pivotPhase != -1){
			pivot = pivotPhase;
		}else{
			pivot = pivotCode;
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 */
	private void setupKalmanFilterInput(Observations roverObs,
			Observations masterObs, Coordinates masterPos) {

		// Number of GPS observations
		int nObs = roverObs.getGpsSize();

		// Number of available satellites (i.e. observations)
		int nObsAvail = satAvail.size();

		// Double differences with respect to pivot satellite reduce
		// observations by 1
		nObsAvail--;

		// Counter for available satellites
		int k = 0;

		// Counter for satellites with phase available
		int p = 0;

		// Set lambda according to GPS frequency
		double lambda;
		if (goGPS.getFreq() == 0) {
			lambda = Constants.LAMBDA_1;
		} else {
			lambda = Constants.LAMBDA_2;
		}

		// Store rover-pivot and master-pivot observed pseudoranges
		double roverPivotCodeObs = roverObs.getGpsByIdx(pivot).getPseudorange(goGPS.getFreq());
		double masterPivotCodeObs = masterOrdered[pivot].getPseudorange(goGPS.getFreq());

		// Compute and store rover-pivot and master-pivot observed phase ranges
		double roverPivotPhaseObs = lambda
				* roverObs.getGpsByIdx(pivot).getPhase(goGPS.getFreq());
		double masterPivotPhaseObs = lambda
				* masterOrdered[pivot].getPhase(goGPS.getFreq());

		// Computation of rover-pivot approximate pseudoranges
		//SimpleMatrix diffRoverPivot = this.coord.ecef.minus(pos[pivot].getCoord().ecef);
		SimpleMatrix diffRoverPivot = this.minusXYZ(pos[pivot]);
		double roverPivotAppRange = Math.sqrt(Math
				.pow(diffRoverPivot.get(0), 2)
				+ Math.pow(diffRoverPivot.get(1), 2)
				+ Math.pow(diffRoverPivot.get(2), 2));

		// Computation of master-pivot approximate pseudoranges
		//SimpleMatrix diff = masterPos.ecef.minus(pos[pivot].getCoord().ecef);
		SimpleMatrix diff = masterPos.minusXYZ(pos[pivot]);
		double masterPivotAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
				+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

		// Compute rover-pivot and master-pivot weights
		double roverElevation = roverTopo[pivot].getElevation();
		double masterElevation = masterTopo[pivot].getElevation();
		double roverPivotWeight = computeWeight(roverElevation,
				roverObs.getGpsByIdx(pivot).getSignalStrength(goGPS.getFreq()));
		double masterPivotWeight = computeWeight(masterElevation,
				masterOrdered[pivot].getSignalStrength(goGPS.getFreq()));

		// Start filling in the observation error covariance matrix
		Cnn.zero();
		int nSatAvail = satAvail.size() - 1;
		int nSatAvailPhase = satAvailPhase.size() - 1;
		for (int i = 0; i < nSatAvail + nSatAvailPhase; i++) {
			for (int j = 0; j < nSatAvail + nSatAvailPhase; j++) {

				if (i < nSatAvail && j < nSatAvail)
					Cnn.set(i, j, Math.pow(goGPS.getStDevCode(roverObs.getGpsByIdx(pivot), masterOrdered[pivot], goGPS.getFreq()), 2)
							* (roverPivotWeight + masterPivotWeight));
				else if (i >= nSatAvail && j >= nSatAvail)
					Cnn.set(i, j, Math.pow(goGPS.getStDevPhase(), 2)
							* (roverPivotWeight + masterPivotWeight));
			}
		}

		for (int i = 0; i < nObs; i++) {

			if (satAvail.contains(pos[i].getSatID())
					&& i != pivot) {

				// Compute rover-satellite approximate pseudorange
				//SimpleMatrix diffRoverSat = this.coord.ecef.minus(pos[i].ecef);
				SimpleMatrix diffRoverSat = this.minusXYZ(pos[i]);
				double roverSatAppRange = Math.sqrt(Math.pow(diffRoverSat
						.get(0), 2)
						+ Math.pow(diffRoverSat.get(1), 2)
						+ Math.pow(diffRoverSat.get(2), 2));

				// Compute master-satellite approximate pseudorange
				//diff = masterPos.ecef.minus(pos[i].ecef);
				diff = masterPos.minusXYZ(pos[i]);
				double masterSatAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
						+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

				// Compute parameters obtained from linearization of observation
				// equations
				double alphaX = diffRoverSat.get(0) / roverSatAppRange
						- diffRoverPivot.get(0) / roverPivotAppRange;
				double alphaY = diffRoverSat.get(1) / roverSatAppRange
						- diffRoverPivot.get(1) / roverPivotAppRange;
				double alphaZ = diffRoverSat.get(2) / roverSatAppRange
						- diffRoverPivot.get(2) / roverPivotAppRange;

				// Approximate code double difference
				double ddcApp = (roverSatAppRange - masterSatAppRange)
						- (roverPivotAppRange - masterPivotAppRange);

				// Observed code double difference
				double ddcObs = (roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq()) - masterOrdered[i].getPseudorange(goGPS.getFreq()))
						- (roverPivotCodeObs - masterPivotCodeObs);

				// Observed phase double difference
				double ddpObs = (lambda * roverObs.getGpsByIdx(i).getPhase(goGPS.getFreq()) - lambda
						* masterOrdered[i].getPhase(goGPS.getFreq()))
						- (roverPivotPhaseObs - masterPivotPhaseObs);

				// Fill in one row in the design matrix (for code)
				H.set(k, 0, alphaX);
				H.set(k, i1 + 1, alphaY);
				H.set(k, i2 + 1, alphaZ);

				// Fill in one element of the observation vector (for code)
				y0.set(k, 0, ddcObs - ddcApp + alphaX * this.getX()
						+ alphaY * this.getY() + alphaZ
						* this.getZ());

				// Fill in the observation error covariance matrix (for code)
				double roverSatWeight = computeWeight(roverElevation,
						roverObs.getGpsByIdx(i).getSignalStrength(goGPS.getFreq()));
				double masterSatWeight = computeWeight(masterElevation,
						masterOrdered[i].getSignalStrength(goGPS.getFreq()));
				double CnnBase = Cnn.get(k, k);
				Cnn.set(k, k, CnnBase + Math.pow(goGPS.getStDevCode(roverObs.getGpsByIdx(i), masterOrdered[i], goGPS.getFreq()), 2)
						* (roverSatWeight + masterSatWeight));

				if (satAvailPhase.contains(pos[i].getSatID())) {

					// Fill in one row in the design matrix (for phase)
					H.set(nObsAvail + p, 0, alphaX);
					H.set(nObsAvail + p, i1 + 1, alphaY);
					H.set(nObsAvail + p, i2 + 1, alphaZ);
					H.set(nObsAvail + p, i3 + pos[i].getSatID(), -lambda);

					// Fill in one element of the observation vector (for phase)
					y0.set(nObsAvail + p, 0, ddpObs - ddcApp + alphaX
							* this.getX() + alphaY
							* this.getY() + alphaZ
							* this.getZ());

					// Fill in the observation error covariance matrix (for
					// phase)
					CnnBase = Cnn.get(nObsAvail + p, nObsAvail + p);
					Cnn.set(nObsAvail + p, nObsAvail + p, CnnBase
							+ Math.pow(goGPS.getStDevPhase(), 2)
							* (roverSatWeight + masterSatWeight));

					// Increment satellites with phase counter
					p++;
				}

				// Increment available satellites counter
				k++;
			}
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 */
	private void checkSatelliteConfiguration(Observations roverObs,
			Observations masterObs, Coordinates masterPos) {

		// Check if satellites were lost since the previous epoch
		if (satAvailPhase.size() < satOld.size()) {

			System.out.println("Lost satellite(s)");

			for (int i = 0; i < satOld.size(); i++) {

				// Set ambiguity of lost satellites to zero
				if (!satAvailPhase.contains(satOld.get(i))) {
					KFprediction.set(i3 + satOld.get(i), 0, 0);
				}
			}
		}

		// Check if new satellites are available since the previous epoch
		if (satAvailPhase.size() > satOld.size()) {

			System.out.println("New satellite(s)");

			for (int i = 0; i < pos.length; i++) {

				if (satAvailPhase.contains(pos[i].getSatID())
						&& !satOld.contains(pos[i].getSatID())) {

					// Estimate ambiguity for new satellites
					estimateAmbiguitiesApprox(roverObs, masterObs, masterPos, i, pivot, false);
				}
			}
		}

		// Check if pivot satellite changed since the previous epoch
		if (pivotOldId != pos[pivot].getSatID()) {

			System.out.println("Pivot change");

			// Matrix construction to manage the change of pivot satellite
			SimpleMatrix A = new SimpleMatrix(o3 + nN, o3 + nN);

			int pivotIndex = i3 + pos[pivot].getSatID();
			int pivotOldIndex = i3 + pivotOldId;
			for (int i = 0; i < o3; i++) {
				for (int j = 0; j < o3; j++) {
					if (i == j)
						A.set(i, j, 1);
				}
			}
			for (int i = 0; i < satAvailPhase.size(); i++) {
				for (int j = 0; j < satAvailPhase.size(); j++) {
					int satIndex = i3 + satAvailPhase.get(i);
					if (i == j) {
						A.set(satIndex, satIndex, 1);
					}
					A.set(satIndex, pivotIndex, -1);
				}
			}
			A.set(pivotOldIndex, pivotOldIndex, 0);
			A.set(pivotIndex, pivotIndex, 0);

			// Update predicted state
			KFprediction = A.mult(KFprediction);

			// Re-computation of the Cee covariance matrix at the previous epoch
			Cee = A.mult(Cee).mult(A.transpose());
		}

		// Check cycle-slips
		for (int i = 0; i < pos.length; i++) {

			if (satAvailPhase.contains(pos[i].getSatID())) {

				// Estimate ambiguity and check for cycle slips
				estimateAmbiguitiesApprox(roverObs, masterObs, masterPos, i, pivot, true);
			}
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 */
	private void estimateAmbiguitiesObserv(Observations roverObs, Observations masterObs) {

		// Number of GPS observations
		int nObs = roverObs.getGpsSize();

		// Rover-pivot and master-pivot observed code
		double roverPivotCodeObs = roverObs.getGpsByIdx(pivot).getPseudorange(goGPS.getFreq());
		double masterPivotCodeObs = masterOrdered[pivot].getPseudorange(goGPS.getFreq());

		// Rover-pivot and master-pivot observed phase
		double roverPivotPhaseObs = roverObs.getGpsByIdx(pivot).getPhase(goGPS.getFreq());
		double masterPivotPhaseObs = masterOrdered[pivot].getPhase(goGPS.getFreq());

		// Variables to store rover-satellite and master-satellite observed code
		double roverSatCodeObs;
		double masterSatCodeObs;

		// Variables to store rover-satellite and master-satellite observed
		// phase
		double roverSatPhaseObs;
		double masterSatPhaseObs;

		// Variables to store code and phase double differences
		double codeDoubleDiffObserv;
		double phaseDoubleDiffObserv;

		// Set lambda according to GPS frequency
		double lambda;
		if (goGPS.getFreq() == 0) {
			lambda = Constants.LAMBDA_1;
		} else {
			lambda = Constants.LAMBDA_2;
		}

		for (int i = 0; i < nObs; i++) {

			if (pos[i]!=null && satAvailPhase.contains(pos[i].getSatID())) {

				// Rover-satellite and master-satellite observed code
				roverSatCodeObs = roverObs.getGpsByIdx(i).getPseudorange(goGPS.getFreq());
				masterSatCodeObs = masterOrdered[i].getPseudorange(goGPS.getFreq());

				// Rover-satellite and master-satellite observed phase
				roverSatPhaseObs = roverObs.getGpsByIdx(i).getPhase(goGPS.getFreq());
				masterSatPhaseObs = masterOrdered[i].getPhase(goGPS.getFreq());

				// Observed code double difference
				codeDoubleDiffObserv = (roverSatCodeObs - masterSatCodeObs)
						- (roverPivotCodeObs - masterPivotCodeObs);

				// Observed phase double difference
				phaseDoubleDiffObserv = (roverSatPhaseObs - masterSatPhaseObs)
						- (roverPivotPhaseObs - masterPivotPhaseObs);

				// Estimated ambiguity
				KFstate.set(i3 + pos[i].getSatID(), 0,
						codeDoubleDiffObserv / lambda - phaseDoubleDiffObserv);

				// Store the variance of the estimated ambiguity
				Cee.set(i3 + pos[i].getSatID(), i3
						+ pos[i].getSatID(), 4
						* Math.pow(goGPS.getStDevCode(roverObs.getGpsByIdx(i), masterOrdered[i], goGPS.getFreq()), 2) / Math.pow(lambda, 2));
			}
		}
	}

	/**
	 * @param roverObs
	 * @param masterObs
	 * @param masterPos
	 */
	private void estimateAmbiguitiesApprox(Observations roverObs,
			Observations masterObs, Coordinates masterPos, int satIndex,
			int pivotIndex, boolean checkCycleSlip) {

		// Computation of rover-pivot approximate pseudoranges
		//SimpleMatrix diffRoverPivot = this.coord.ecef.minus(pos[pivotIndex].getCoord().ecef);
		SimpleMatrix diffRoverPivot = this.minusXYZ(pos[pivotIndex]);
		double roverPivotAppRange = Math.sqrt(Math
				.pow(diffRoverPivot.get(0), 2)
				+ Math.pow(diffRoverPivot.get(1), 2)
				+ Math.pow(diffRoverPivot.get(2), 2));

		// Computation of master-pivot approximate pseudoranges
		//SimpleMatrix diff = masterPos.ecef.minus(pos[pivotIndex].getCoord().ecef);
		SimpleMatrix diff = masterPos.minusXYZ(pos[pivotIndex]);
		double masterPivotAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
				+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

		// Rover-pivot and master-pivot observed phase
		double roverPivotPhaseObs = roverObs.getGpsByIdx(pivotIndex).getPhase(goGPS.getFreq());
		double masterPivotPhaseObs = masterOrdered[pivotIndex].getPhase(goGPS.getFreq());

		// Variables to store rover-satellite and master-satellite observed
		// code
		double roverSatCodeAppRange;
		double masterSatCodeAppRange;

		// Variables to store rover-satellite and master-satellite observed
		// phase
		double roverSatPhaseObs;
		double masterSatPhaseObs;

		// Variables to store code and phase double differences
		double codeDoubleDiffApprox;
		double phaseDoubleDiffObserv;

		// Set lambda according to GPS frequency
		double lambda;
		if (goGPS.getFreq() == 0) {
			lambda = Constants.LAMBDA_1;
		} else {
			lambda = Constants.LAMBDA_2;
		}

		// Compute rover-satellite approximate pseudorange
		//SimpleMatrix diffRoverSat = this.coord.ecef.minus(pos[satIndex].getCoord().ecef);
		SimpleMatrix diffRoverSat = this.minusXYZ(pos[satIndex]);
		roverSatCodeAppRange = Math.sqrt(Math.pow(diffRoverSat.get(0), 2)
				+ Math.pow(diffRoverSat.get(1), 2)
				+ Math.pow(diffRoverSat.get(2), 2));

		// Compute master-satellite approximate pseudorange
		//diff = masterPos.ecef.minus(pos[satIndex].getCoord().ecef);
		diff = masterPos.minusXYZ(pos[satIndex]);
		masterSatCodeAppRange = Math.sqrt(Math.pow(diff.get(0), 2)
				+ Math.pow(diff.get(1), 2) + Math.pow(diff.get(2), 2));

		// Rover-satellite and master-satellite observed phase
		roverSatPhaseObs = roverObs.getGpsByIdx(satIndex).getPhase(goGPS.getFreq());
		masterSatPhaseObs = masterOrdered[satIndex].getPhase(goGPS.getFreq());

		// Estimated code pseudorange double differences
		codeDoubleDiffApprox = (roverSatCodeAppRange - masterSatCodeAppRange)
				- (roverPivotAppRange - masterPivotAppRange);

		// Observed phase double differences
		phaseDoubleDiffObserv = (roverSatPhaseObs - masterSatPhaseObs)
				- (roverPivotPhaseObs - masterPivotPhaseObs);

		// Estimated ambiguity
		double estimAmbiguity = codeDoubleDiffApprox / lambda
				- phaseDoubleDiffObserv;

		if (!checkCycleSlip) {

			// Estimated ambiguity
			KFprediction.set(i3 + pos[satIndex].getSatID(), 0,
					estimAmbiguity);

			// Store the variance of the estimated ambiguity
			Cvv.set(i3 + pos[satIndex].getSatID(), i3
					+ pos[satIndex].getSatID(), 4
					* Math.pow(goGPS.getStDevCode(roverObs.getGpsByIdx(satIndex), masterOrdered[satIndex], goGPS.getFreq()), 2) / Math.pow(lambda, 2));
		} else {

			// Ambiguity predicted by Kalman filter
			double kalmanAmbiguity = KFprediction.get(i3
					+ pos[satIndex].getSatID());

			if (satIndex != pivotIndex
					&& Math.abs(kalmanAmbiguity - estimAmbiguity) > goGPS.getCycleSlipThreshold()) {

				System.out.println("Cycle slip");

				// Estimated ambiguity
				KFprediction.set(i3 + pos[satIndex].getSatID(), 0,
						estimAmbiguity);

				// Store the variance of the estimated ambiguity
				Cvv.set(i3 + pos[satIndex].getSatID(), i3
						+ pos[satIndex].getSatID(), Math.pow(
						goGPS.getStDevAmbiguity(), 2));
			}
		}
	}

	/**
	 * @param elevation
	 * @param snr
	 * @return weight computed according to the variable "goGPS.weights"
	 */
	private double computeWeight(double elevation, float snr) {

		double weight = 1;
		float Sa = Constants.SNR_a;
		float SA = Constants.SNR_A;
		float S0 = Constants.SNR_0;
		float S1 = Constants.SNR_1;

		switch (goGPS.getWeights()) {

			// Weight based on satellite elevation
			case GoGPS.WEIGHT_SAT_ELEVATION:
				weight = 1 / Math.pow(Math.sin(elevation * Math.PI / 180), 2);
				break;

			// Weight based on signal-to-noise ratio
			case GoGPS.WEIGHT_SIGNAL_TO_NOISE_RATIO:
				if (snr >= S1) {
					weight = 1;
				} else {
					weight = Math.pow(10, -(snr - S1) / Sa)
							* ((SA / Math.pow(10, -(S0 - S1) / Sa) - 1) / (S0 - S1)
									* (snr - S1) + 1);
				}
				break;

			// Weight based on combined elevation and signal-to-noise ratio
			case GoGPS.WEIGHT_COMBINED_ELEVATION_SNR:
				if (snr >= S1) {
					weight = 1;
				} else {
					double weightEl = 1 / Math.pow(Math.sin(elevation * Math.PI / 180), 2);
					double weightSnr = Math.pow(10, -(snr - S1) / Sa)
							* ((SA / Math.pow(10, -(S0 - S1) / Sa) - 1) / (S0 - S1) * (snr - S1) + 1);
					weight = weightEl * weightSnr;
				}
				break;

			// Same weight for all observations or default
			case GoGPS.WEIGHT_EQUAL:
			default:
				weight = 1;
		}

		return weight;
	}

	/**
	 * @param x
	 * @param y
	 * @return Lorentz inner product
	 */
	private static double lorentzInnerProduct(SimpleMatrix x, SimpleMatrix y) {

		double prod = x.get(0) * y.get(0) + x.get(1) * y.get(1) + x.get(2) * y.get(2) - x.get(3) * y.get(3);

		return prod;
	}

//	/**
//	 * @return the coord
//	 */
//	public Coordinates getCoord() {
//		return coord;
//	}
//
//	/**
//	 * @param coord the coord to set
//	 */
//	public void setCoord(Coordinates coord) {
//		this.coord = coord;
//	}

	/**
	 * @return the covariance
	 */
	public SimpleMatrix getCovariance() {
		return covariance;
	}

	/**
	 * @param covariance the covariance to set
	 */
	public void setCovariance(SimpleMatrix covariance) {
		this.covariance = covariance;
	}

	/**
	 * @return the receiver clock error
	 */
	public double getReceiverClockError() {
		return receiverClockError;
	}

	/**
	 * @param receiverClockError the receiver clock error to set
	 */
	public void setReceiverClockError(double dt) {
		this.receiverClockError = dt;
	}
}
