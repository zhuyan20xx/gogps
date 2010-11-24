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


/**
 * <p>
 * Navigation data and related methods
 * </p>
 *
 * @author ege, Cryms.com
 */
public interface NavigationProducer {


	public SatellitePosition getGpsSatPosition(long time, int satID, double range);

	public void init();
	public void release();

	public double getIono(int i);
	/**
	 * @return the a0
	 */
	public double getA0();
	/**
	 * @return the a1
	 */
	public double getA1();
	/**
	 * @return the t
	 */
	public double getT();
	/**
	 * @return the w
	 */
	public double getW();
	/**
	 * @return the leaps
	 */
	public int getLeaps();
}
