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

	public SatellitePosition getGpsSatPosition(long utcTime, int satID, double range, double receiverClockError);

	public void init() throws Exception;

	public void release(boolean waitForThread, long timeoutMs) throws InterruptedException;

	public IonoGps getIono(long utcTime);

}
