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
 * Observations class
 * </p>
 *
 * @author ege, Cryms.com
 */
public class Observations {
	private Time refTime; /* Reference time of the dataset */
	private int eventFlag; /* Event flag */
//	private ArrayList<Integer> gpsSat; /* Ordered list of visible GPS satellites IDs */
//	private ArrayList<Integer> gloSat; /* Ordered list of visible GLONASS satellites IDs */
//	private ArrayList<Integer> sbsSat; /* Ordered list of visible SBAS satellites IDs */

	// TODO
	private ArrayList<ObservationSet> gps; /* GPS observations */
	private ArrayList<ObservationSet> glo; /* GLONASS observations */
	private ArrayList<ObservationSet> sbs; /* SBAS observations */


	public Observations(Time time, int flag){
		this.refTime = time;
		this.eventFlag = flag;
	}

	public int getGpsSize(){
		return gps==null?-1:gps.size();
	}
	public ObservationSet getGpsByIdx(int idx){
		return gps.get(idx);
	}
	public ObservationSet getGpsByID(Integer satID){
		if(gps == null) return null;
		for(int i=0;i<gps.size();i++)
			if(gps.get(i)!=null && gps.get(i).getSatID()==satID) return gps.get(i);
		return null;
	}
	public Integer getGpsSatID(int idx){
		return getGpsByIdx(idx).getSatID();
	}
	public boolean containsGpsSatID(Integer id){
		return getGpsByID(id) != null;
	}


	/**
	 * @return the refTime
	 */
	public Time getRefTime() {
		return refTime;
	}

	/**
	 * @param refTime the refTime to set
	 */
	public void setRefTime(Time refTime) {
		this.refTime = refTime;
	}

	/**
	 * Epoch flag
	 * 0: OK
	 * 1: power failure between previous and current epoch
	 * >1: Special event
	 *  2: start moving antenna
     *  3: new site occupation
     *  (end of kinem. data)
     * (at least MARKER NAME record
     * follows)
     * 4: header information follows
     * 5: external event (epoch is significant)
     * 6: cycle slip records follow
     * to optionally report detected
     * and repaired cycle slips
     * (same format as OBSERVATIONS
     * records; slip instead of observation;
     * LLI and signal strength blank)
     *
	 * @return the eventFlag
	 */
	public int getEventFlag() {
		return eventFlag;
	}

	/**
	 * @param eventFlag the eventFlag to set
	 */
	public void setEventFlag(int eventFlag) {
		this.eventFlag = eventFlag;
	}

//	public void init(int nGps, int nGlo, int nSbs){
//		gpsSat = new ArrayList<Integer>(nGps);
//		gloSat = new ArrayList<Integer>(nGlo);
//		sbsSat = new ArrayList<Integer>(nSbs);
//
//		// Allocate array of observation objects
//		if (nGps > 0) gps = new ObservationSet[nGps];
//		if (nGlo > 0) glo = new ObservationSet[nGlo];
//		if (nSbs > 0) sbs = new ObservationSet[nSbs];
//	}

	public void setGps(int i, ObservationSet os ){
		if(gps==null) gps = new ArrayList<ObservationSet>(i+1);
		if(i==gps.size()){
			gps.add(os);
		}else{
			int c=gps.size();
			while(c++<=i) gps.add(null);
			gps.set(i,os);
		}
		//gps[i] = os;
		//gpsSat.add(os.getSatID());
	}
}
