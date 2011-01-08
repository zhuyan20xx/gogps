/*
 * Copyright (c) 2011, Lorenzo Patocchi. All Rights Reserved.
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
package org.gogpsproject.parser.sp3;

import java.util.Calendar;
import java.util.SimpleTimeZone;

import org.gogpsproject.NavigationProducer;
import org.gogpsproject.Time;
/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */
import org.gogpsproject.SatellitePosition;

/**
 * @author Lorenzo
 *
 */
public class SP3Navigation implements NavigationProducer {


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2009);
		c.set(Calendar.MONTH, 2);
		c.set(Calendar.DAY_OF_MONTH, 21);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.setTimeZone(new SimpleTimeZone(0,""));

		Time t = new Time(c.getTimeInMillis());

		System.out.println("ts: "+t.getMsec());
		System.out.println("week: "+t.getGpsWeek());
		System.out.println("week sec: "+t.getGpsWeekSec());
		System.out.println("week day: "+t.getGpsWeekDay());

		System.out.println("ts2: "+(new Time(t.getGpsWeek(),t.getGpsWeekSec())).getMsec());

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long time, int satID,
			double range) {

		return null;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getIono(int)
	 */
	@Override
	public double getIono(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#release()
	 */
	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

}
