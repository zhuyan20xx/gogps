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
	//private SimpleMatrix enu; /* Local coordinates (East, North, Up) */

	public Coordinates(SimpleMatrix sm){
		this.ecef = sm.copy();
	}
	
	public SimpleMatrix minus(Coordinates coord){
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
	public void setXYZ(double x, double y, double z){
		this.ecef.set(0, 0, x);
		this.ecef.set(1, 0, y);
		this.ecef.set(2, 0, z);
	}
	public void setPlus(SimpleMatrix sm){
		this.ecef.set(ecef.plus(sm));
	}
	public void setSMMult(SimpleMatrix sm){
		this.ecef = sm.mult(this.ecef);
	}
	
	public boolean isValid(){
		return this.ecef != null;
	}
}
