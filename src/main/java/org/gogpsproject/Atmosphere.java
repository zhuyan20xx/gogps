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
 */
package org.gogpsproject;
/**
 * <p>
 * Atmospheric correction class
 * </p>
 * 
 * @author ege, Cryms.com
 */
public class Atmosphere {

	/**
	 * @param elevation
	 * @param height
	 * @return troposphere correction value by Saastamoinen model
	 */
	public static double computeTroposphereCorrection(double elevation, double height) {

		double tropoCorr = 0;

		if (height < 5000) {

			elevation = Math.toRadians(Math.abs(elevation));
			if (elevation == 0){
				elevation = elevation + 0.01;
			}

			// Numerical constants and tables for Saastamoinen algorithm
			// (troposphere correction)
			double hr = 50.0;
			int[] ha = new int[9];
			double[] ba = new double[9];

			ha[0] = 0;
			ha[1] = 500;
			ha[2] = 1000;
			ha[3] = 1500;
			ha[4] = 2000;
			ha[5] = 2500;
			ha[6] = 3000;
			ha[7] = 4000;
			ha[8] = 5000;

			ba[0] = 1.156;
			ba[1] = 1.079;
			ba[2] = 1.006;
			ba[3] = 0.938;
			ba[4] = 0.874;
			ba[5] = 0.813;
			ba[6] = 0.757;
			ba[7] = 0.654;
			ba[8] = 0.563;

			// Saastamoinen algorithm
			double P = Constants.STANDARD_PRESSURE * Math.pow((1 - 0.0000226 * height), 5.225);
			double T = Constants.STANDARD_TEMPERATURE - 0.0065 * height;
			double H = hr * Math.exp(-0.0006396 * height);

			// If height is below zero, keep the maximum correction value
			double B = ba[0];
			// Otherwise, interpolate the tables
			if (height >= 0) {
				int i = 1;
				while (height > ha[i]) {
					i++;
				}
				double m = (ba[i] - ba[i - 1]) / (ha[i] - ha[i - 1]);
				B = ba[i - 1] + m * (height - ha[i - 1]);
			}

			double e = 0.01
					* H
					* Math.exp(-37.2465 + 0.213166 * T - 0.000256908
							* Math.pow(T, 2));

			tropoCorr = ((0.002277 / Math.sin(elevation))
					* (P - (B / Math.pow(Math.tan(elevation), 2))) + (0.002277 / Math.sin(elevation))
					* (1255 / T + 0.05) * e);
		}

		return tropoCorr;
	}

	/**
	 * @param ionoParams
	 * @param coord
	 * @param time
	 * @return ionosphere correction value by Klobuchar model
	 */
	static double computeIonosphereCorrection(Navigation navigation,
			Coordinates coord, double azimuth, double elevation, double time) {

		double ionoCorr = 0;

		double a0 = navigation.getIono(0);
		double a1 = navigation.getIono(1);
		double a2 = navigation.getIono(2);
		double a3 = navigation.getIono(3);
		double b0 = navigation.getIono(4);
		double b1 = navigation.getIono(5);
		double b2 = navigation.getIono(6);
		double b3 = navigation.getIono(7);

		elevation = Math.abs(elevation);

		// Parameter conversion to semicircles
		double lon = coord.getGeodeticLongitude() / 180; // geod.get(0)
		double lat = coord.getGeodeticLatitude() / 180; //geod.get(1)
		azimuth = azimuth / 180;
		elevation = elevation / 180;

		// Klobuchar algorithm
		double f = 1 + 16 * Math.pow((0.53 - elevation), 3);
		double psi = 0.0137 / (elevation + 0.11) - 0.022;
		double phi = lat + psi * Math.cos(azimuth * Math.PI);
		if (phi > 0.416){
			phi = 0.416;
		}
		if (phi < -0.416){
			phi = -0.416;
		}
		double lambda = lon + (psi * Math.sin(azimuth * Math.PI))
				/ Math.cos(phi * Math.PI);
		double ro = phi + 0.064 * Math.cos((lambda - 1.617) * Math.PI);
		double t = lambda * 43200 + time;
		while (t >= 86400)
			t = t - 86400;
		while (t < 0)
			t = t + 86400;
		double p = b0 + b1 * ro + b2 * Math.pow(ro, 2) + b3 * Math.pow(ro, 3);

		if (p < 72000)
			p = 72000;
		double a = a0 + a1 * ro + a2 * Math.pow(ro, 2) + a3 * Math.pow(ro, 3);
		if (a < 0)
			a = 0;
		double x = (2 * Math.PI * (t - 50400)) / p;
		if (Math.abs(x) < 1.57){
			ionoCorr = Constants.SPEED_OF_LIGHT
					* f
					* (5e-9 + a
							* (1 - (Math.pow(x, 2)) / 2 + (Math.pow(x, 4)) / 24));
		}else{
			ionoCorr = Constants.SPEED_OF_LIGHT * f * 5e-9;
		}
		return ionoCorr;
	}

}
