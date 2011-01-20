/*
 * Copyright (c) 2010, Lorenzo Patocchi. All Rights Reserved.
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
package org.gogpsproject.parser.ublox;

import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.Observations;
/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

/**
 * @author Lorenzo
 *
 */
public interface UBXEventListener {

	public void streamClosed();
	public void addObservations(Observations o);
	public void addIonospheric(IonoGps iono);
	public void addEphemeris(EphGps eph);

}
