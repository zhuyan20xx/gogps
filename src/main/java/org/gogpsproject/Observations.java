/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * <p>
 * Observations class
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class Observations implements Streamable {

	SimpleDateFormat sdfHeader = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
	DecimalFormat dfX4 = new DecimalFormat("0.0000");


	private final static int STREAM_V = 1;

	private Time refTime; /* Reference time of the dataset */
	private int eventFlag; /* Event flag */
//	private ArrayList<Integer> gpsSat; /* Ordered list of visible GPS satellites IDs */
//	private ArrayList<Integer> gloSat; /* Ordered list of visible GLONASS satellites IDs */
//	private ArrayList<Integer> sbsSat; /* Ordered list of visible SBAS satellites IDs */

	// TODO
	private ArrayList<ObservationSet> gps; /* GPS observations */
	private ArrayList<ObservationSet> glo; /* GLONASS observations */
	private ArrayList<ObservationSet> sbs; /* SBAS observations */

	public Object clone(){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.write(new DataOutputStream(baos));
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
			baos.reset();
			dis.readUTF();
			return new Observations(dis, false);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return null;
	}

	public Observations(Time time, int flag){
		this.refTime = time;
		this.eventFlag = flag;
	}
	public Observations(DataInputStream dai, boolean oldVersion) throws IOException{
		read(dai, oldVersion);
	}
	public void cleanObservations(){
		if(gps != null)
			for (int i=gps.size()-1;i>=0;i--)
				if(gps.get(i)==null || Double.isNaN(gps.get(i).getPseudorange(0)))
					gps.remove(i);
		if(glo != null)
			for (int i=glo.size()-1;i>=0;i--)
				if(glo.get(i)==null || Double.isNaN(glo.get(i).getPseudorange(0)))
					glo.remove(i);
		if(sbs != null)
			for (int i=sbs.size()-1;i>=0;i--)
				if(sbs.get(i)==null || Double.isNaN(sbs.get(i).getPseudorange(0)))
					sbs.remove(i);
	}
	public int getGpsSize(){
		if(gps == null) return 0;
		int nsat = 0;
		for(int i=0;i<gps.size();i++)
			if(gps.get(i)!=null) nsat++;
		return gps==null?-1:nsat;
	}
	public int getGloSize(){
		if(glo == null) return 0;
		int nsat = 0;
		for(int i=0;i<glo.size();i++)
			if(glo.get(i)!=null) nsat++;
		return glo==null?-1:nsat;
	}
	public int getSbsSize(){
		if(sbs == null) return 0;
		int nsat = 0;
		for(int i=0;i<sbs.size();i++)
			if(sbs.get(i)!=null) nsat++;
		return sbs==null?-1:nsat;
	}
	public ObservationSet getGpsByIdx(int idx){
		return gps.get(idx);
	}
	public ObservationSet getGpsByID(Integer satID){
		if(gps == null || satID==null) return null;
		for(int i=0;i<gps.size();i++)
			if(gps.get(i)!=null && gps.get(i).getSatID()==satID.intValue()) return gps.get(i);
		return null;
	}
	public ObservationSet getGpsByID(Integer satID, char satType){
		if(gps == null || satID==null) return null;
		for(int i=0;i<gps.size();i++)
			if(gps.get(i)!=null && gps.get(i).getSatID()==satID.intValue() && gps.get(i).getSatType()==satType) return gps.get(i);
		return null;
	}
	public Integer getGpsSatID(int idx){
		return getGpsByIdx(idx).getSatID();
	}
	public char getGnssSatType(int idx){
		return getGpsByIdx(idx).getSatType();
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

	public int write(DataOutputStream dos) throws IOException{
		dos.writeUTF(MESSAGE_OBSERVATIONS); // 5
		dos.writeInt(STREAM_V); // 4
		dos.writeLong(refTime==null?-1:refTime.getMsec()); // 13
		dos.write(eventFlag); // 14
		dos.write(gps==null?0:gps.size()); // 15
		int size=19;
		if(gps!=null){
			for(int i=0;i<gps.size();i++){
				size += ((ObservationSet)gps.get(i)).write(dos);
			}
		}
		return size;
	}
	public String toString(){

		String lineBreak = System.getProperty("line.separator");

		String out= " GPS Time:"+getRefTime().getGpsTime()+" "+sdfHeader.format(new Date(getRefTime().getMsec()))+" evt:"+eventFlag+lineBreak;
		for(int i=0;i<getGpsSize();i++){
			ObservationSet os = getGpsByIdx(i);
			out+="satType:"+ os.getSatType() +"  satID:"+os.getSatID()+"\tC:"+fd(os.getCodeC(0))
				+" cP:"+fd(os.getCodeP(0))
				+" Ph:"+fd(os.getPhase(0))
				+" Dp:"+fd(os.getDoppler(0))
				+" Ss:"+fd(os.getSignalStrength(0))
				+" LL:"+fd(os.getLossLockInd(0))
				+" LL2:"+fd(os.getLossLockInd(1))
				+lineBreak;
		}
		return out;
	}

	private String fd(double n){
		return Double.isNaN(n)?"NaN":dfX4.format(n);
	}
	/* (non-Javadoc)
	 * @see org.gogpsproject.Streamable#read(java.io.DataInputStream)
	 */
	@Override
	public void read(DataInputStream dai, boolean oldVersion) throws IOException {
		int v=1;
		if(!oldVersion) v=dai.readInt();

		if(v==1){
			refTime = new Time(dai.readLong());
			eventFlag = dai.read();
			int size = dai.read();
			gps = new ArrayList<ObservationSet>(size);

			for(int i=0;i<size;i++){
				if(!oldVersion) dai.readUTF();
				ObservationSet os = new ObservationSet(dai, oldVersion);
				gps.add(os);
			}
		}else{
			throw new IOException("Unknown format version:"+v);
		}
	}

}
