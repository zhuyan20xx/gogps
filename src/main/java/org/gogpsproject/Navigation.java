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

/**
 * <p>
 * Navigation data and related methods
 * </p>
 * 
 * @author ege, Cryms.com
 */
public interface Navigation {
	

	
	
	/**
	 * @param time
	 * @param satID
	 * @return Reference ephemeris set for given time and satellite
	 */
	public EphGps findEph(long time, int satID);
	
	public void init();
	public void release();
	
	
	//public int getEphSize();
	
	//public void addEph(EphGps eph);
	
	//public void setIono(int i, double val);
	public double getIono(int i);
	/**
	 * @return the a0
	 */
	public double getA0();
	/**
	 * @param a0 the a0 to set
	 */
	//public void setA0(double a0);
	/**
	 * @return the a1
	 */
	public double getA1();
	/**
	 * @param a1 the a1 to set
	 */
	//public void setA1(double a1);
	/**
	 * @return the t
	 */
	public double getT();
	/**
	 * @param t the t to set
	 */
	//public void setT(double t);
	/**
	 * @return the w
	 */
	public double getW();
	/**
	 * @param w the w to set
	 */
	//public void setW(double w);
	/**
	 * @return the leaps
	 */
	public int getLeaps();
	/**
	 * @param leaps the leaps to set
	 */
	//public void setLeaps(int leaps);
}
