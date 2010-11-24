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
import org.ejml.data.SimpleMatrix;

/**
 * <p>
 * Coordinate and reference system tools
 * </p>
 *
 * @author ege, Cryms.com
 */
public class Coordinates {

	// Global systems
	private SimpleMatrix ecef = null;; /* Earth-Centered, Earth-Fixed (X, Y, Z) */
	private SimpleMatrix geod = null; /* Longitude (lam), latitude (phi), height (h) */

	// Local systems (require to specify an origin)
	private SimpleMatrix enu; /* Local coordinates (East, North, Up) */

	protected Coordinates(){

	}

	public static Coordinates globalXYZInstance(double x, double y, double z){
		Coordinates c = new Coordinates();
		c.ecef = new SimpleMatrix(3, 1);
		c.setXYZ(x, y, z);
		return c;
	}
//	public static Coordinates globalXYZInstance(SimpleMatrix ecef){
//		Coordinates c = new Coordinates();
//		c.ecef = ecef.copy();
//		return c;
//	}
	public static Coordinates globalENUInstance(SimpleMatrix ecef){
		Coordinates c = new Coordinates();
		c.enu = ecef.copy();
		return c;
	}

	public SimpleMatrix minusXYZ(Coordinates coord){
		return this.ecef.minus(coord.ecef);
	}
	/**
	 *
	 */
	public void computeGeodetic() {
		double X = this.ecef.get(0);
		double Y = this.ecef.get(1);
		double Z = this.ecef.get(2);

		this.geod = new SimpleMatrix(3, 1);

		double a = Constants.WGS84_SEMI_MAJOR_AXIS;
		double e = Constants.WGS84_ECCENTRICITY;

		// Radius computation
		double r = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2));

		// Geocentric longitude
		double lamGeoc = Math.atan2(Y, X);

		// Geocentric latitude
		double phiGeoc = Math.atan(Z / Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2)));

		// Computation of geodetic coordinates
		double psi = Math.atan(Math.tan(phiGeoc) / Math.sqrt(1 - Math.pow(e, 2)));
		double phiGeod = Math.atan((r * Math.sin(phiGeoc) + Math.pow(e, 2) * a
				/ Math.sqrt(1 - Math.pow(e, 2)) * Math.pow(Math.sin(psi), 3))
				/ (r * Math.cos(phiGeoc) - Math.pow(e, 2) * a * Math.pow(Math.cos(psi), 3)));
		double lamGeod = lamGeoc;
		double N = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(phiGeod), 2));
		double h = r * Math.cos(phiGeoc) / Math.cos(phiGeod) - N;

		this.geod.set(0, 0, Math.toDegrees(lamGeod));
		this.geod.set(1, 0, Math.toDegrees(phiGeod));
		this.geod.set(2, 0, h);
	}

	/**
	 * @param origin
	 * @return Rotation matrix from global to local reference systems
	 */
	public void computeLocal(Coordinates target) {
		if(this.geod==null) computeGeodetic();

		SimpleMatrix R = globalToLocalMatrix(this);

		enu = R.mult(target.minusXYZ(this));

	}

	public double getGeodeticLongitude(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(0);
	}
	public double getGeodeticLatitude(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(1);
	}
	public double getGeodeticHeight(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(2);
	}
	public double getX(){
		return ecef.get(0);
	}
	public double getY(){
		return ecef.get(1);
	}
	public double getZ(){
		return ecef.get(2);
	}

	public void setENU(double e, double n, double u){
		this.enu.set(0, 0, e);
		this.enu.set(1, 0, n);
		this.enu.set(2, 0, u);
	}
	public double getE(){
		return enu.get(0);
	}
	public double getN(){
		return enu.get(1);
	}
	public double getU(){
		return enu.get(2);
	}


	public void setXYZ(double x, double y, double z){
		if(this.ecef==null) this.ecef = new SimpleMatrix(3, 1);
		this.ecef.set(0, 0, x);
		this.ecef.set(1, 0, y);
		this.ecef.set(2, 0, z);
	}
	public void setPlusXYZ(SimpleMatrix sm){
		this.ecef.set(ecef.plus(sm));
	}
	public void setSMMultXYZ(SimpleMatrix sm){
		this.ecef = sm.mult(this.ecef);
	}

	public boolean isValidXYZ(){
		return this.ecef != null;
	}

	/**
	 * @param origin
	 * @return Rotation matrix from global to local reference systems
	 */
	public static SimpleMatrix globalToLocalMatrix(Coordinates origin) {

		double lam = Math.toRadians(origin.getGeodeticLongitude());
		double phi = Math.toRadians(origin.getGeodeticLatitude());

		double cosLam = Math.cos(lam);
		double cosPhi = Math.cos(phi);
		double sinLam = Math.sin(lam);
		double sinPhi = Math.sin(phi);

		double[][] data = new double[3][3];
		data[0][0] = -sinLam;
		data[0][1] = cosLam;
		data[0][2] = 0;
		data[1][0] = -sinPhi * cosLam;
		data[1][1] = -sinPhi * sinLam;
		data[1][2] = cosPhi;
		data[2][0] = cosPhi * cosLam;
		data[2][1] = cosPhi * sinLam;
		data[2][2] = sinPhi;

		SimpleMatrix R = new SimpleMatrix(data);

		return R;
	}
}
