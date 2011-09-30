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
package org.gogpsproject;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * <p>
 * This class receive data from streaming source and keep it buffered for navigation and observation consumer.
 * It does not yet release consumed data.
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */
public class ObservationsBuffer
	extends EphemerisSystem
	implements StreamEventListener, ObservationsProducer, NavigationProducer {

    class EphSet{
        public Time refTime;
        public HashMap<Integer,EphGps> ephs = new HashMap<Integer,EphGps>();
        public EphSet(Time refTime){
            this.refTime = refTime;
        }
    }

    private Coordinates definedPosition = null;

    private boolean waitForData=true;

    private Vector<Observations> timeOrderedObs = new Vector<Observations>();
    private Vector<EphSet> timeOrderedEphs = new Vector<EphSet>();
    private Vector<IonoGps> timeOrderedIono = new Vector<IonoGps>();

    private int ionoCursor = 0;
    private int obsCursor = 0;
    private HashMap<Integer,Integer> ephCursors = new HashMap<Integer, Integer>();

    private StreamResource streamResource;

    private String fileNameOutLog = null;
    private FileOutputStream fosOutLog = null;
    private DataOutputStream outLog = null;//new XMLEncoder(os);

    private String id = null;

    private long timeoutNextObsWait = -1;

    /**
    *
    */
   public ObservationsBuffer() {
	   //this(null);
   }
    /**
     * @throws FileNotFoundException
     *
     */
    public ObservationsBuffer(StreamResource streamResource, String fileNameOutLog) throws FileNotFoundException {
    	// 1st define outlog
    	this.setFileNameOutLog(fileNameOutLog);
    	// 2nd attach source, in case of Master source it will push Master Position into outstream
    	this.setStreamSource(streamResource);
    }

    public void setStreamSource(StreamResource streamResource){
    	if(this.streamResource!=null && this.streamResource instanceof StreamEventProducer){
    		((StreamEventProducer)streamResource).removeStreamEventListener(this);
    	}
    	this.streamResource = streamResource;
    	// if resource produces also events register for it
    	if(streamResource!=null && streamResource instanceof StreamEventProducer){
    		((StreamEventProducer)streamResource).addStreamEventListener(this);
    	}
    }

    private int getEphCursor(int satID){
    	Integer ID = new Integer(satID);
    	if(!ephCursors.containsKey(ID)) return 0;
    	return ephCursors.get(ID).intValue();
    }
    private void setEphCursor(int satID, int cur){
    	ephCursors.put(new Integer(satID), new Integer(cur));
    }
    /* (non-Javadoc)
     * @see org.gogpsproject.parser.ublox.UBXEventListener#addEphemeris(org.gogpsproject.EphGps)
     */
    @Override
    public void addEphemeris(EphGps eph) {
        int c = getEphCursor(eph.getSatID());
        // trim to minutes
        while(c<timeOrderedEphs.size() && timeOrderedEphs.elementAt(c).refTime.getMsec()/60000!=eph.getRefTime().getMsec()/60000) c++;

        if(c<timeOrderedEphs.size()){
            //System.out.println("found existing EphSet for "+eph.getSatID()+" @ "+(eph.getRefTime().getMsec()/60000));
            // existing set
            timeOrderedEphs.elementAt(c).ephs.put(new Integer(eph.getSatID()), eph);
        }else{
            //System.out.println("new EphSet for "+eph.getSatID()+" @ "+(eph.getRefTime().getMsec()/60000));
            // new set
            EphSet es = new EphSet(eph.getRefTime());
            es.ephs.put(new Integer(eph.getSatID()), eph);
            timeOrderedEphs.add(es);
        }
        if(outLog!=null){

        	try {
        		eph.write(outLog);
        		outLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.parser.ublox.UBXEventListener#addIonospheric(org.gogpsproject.IonoGps)
     */
    @Override
    public void addIonospheric(IonoGps iono) {
    	int c = ionoCursor;
    	// trim to minute
        while(c<timeOrderedIono.size() && timeOrderedIono.elementAt(c).getRefTime().getMsec()/60000!=iono.getRefTime().getMsec()/60000) c++;

        if(c<timeOrderedIono.size()){
        	//System.out.println("found existing Iono @ "+(iono.getRefTime().getMsec()/60000));
        	timeOrderedIono.set(c, iono);
        }else{
        	//System.out.println("new Iono @ "+(iono.getRefTime().getMsec()/60000));
        	timeOrderedIono.add(iono);
        }
        if(outLog!=null){

        	try {
        		iono.write(outLog);
        		outLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.parser.ublox.UBXEventListener#addObservations(org.gogpsproject.Observations)
     */
    @Override
    public void addObservations(Observations o) {
    	//System.out.println("\tR# > obs "+o.getGpsSize()+" time "+o.getRefTime().getMsec());
        // TODO test if ref time observetions is not already present
        this.timeOrderedObs.add(o);
        if(outLog!=null){

        	try {
        		o.write(outLog);
        		outLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }

    }

    /* (non-Javadoc)
     * @see org.gogpsproject.parser.ublox.UBXEventListener#streamClosed()
     */
    @Override
    public void streamClosed() {
    	// TODO implement reconnection policy, i.e. if(streamResource!=null && !waitForData) streamResource.reconnect();
    	waitForData = false;
    }





    /* (non-Javadoc)
     * @see org.gogpsproject.ObservationsProducer#getCurrentObservations()
     */
    @Override
    public Observations getCurrentObservations() {
    	long begin=System.currentTimeMillis();

    	while(waitForData && (timeOrderedObs.size()==0 || obsCursor>=timeOrderedObs.size()) && (timeoutNextObsWait==-1|| System.currentTimeMillis()-begin<timeoutNextObsWait)){
			//System.out.print("r");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}

    	if(timeOrderedObs.size()>0 && obsCursor<timeOrderedObs.size()){
    		return timeOrderedObs.get(obsCursor);
    	}else{
    		return null;
    	}
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.ObservationsProducer#init()
     */
    @Override
    public void init() throws Exception {
    	// Stream should have been already initialized.
    	// if(streamResource!=null) streamResource.init();


    }

    /* (non-Javadoc)
     * @see org.gogpsproject.ObservationsProducer#nextObservations()
     */
    @Override
    public Observations nextObservations() {

    	long begin=System.currentTimeMillis();

    	while(waitForData && (timeOrderedObs.size()==0 || (obsCursor+1)>=timeOrderedObs.size()) && (timeoutNextObsWait==-1|| System.currentTimeMillis()-begin<timeoutNextObsWait)){
			//System.out.println((id!=null?id:"")+"\tlook for :"+(obsCursor+1)+" pool size is:"+timeOrderedObs.size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}

        if(timeOrderedObs.size()>0 && (obsCursor+1) < timeOrderedObs.size()){
        	Observations o = timeOrderedObs.get(++obsCursor);

            //System.out.println((id!=null?id:"")+"\tread obs "+o.getRefTime().getMsec());
            return o;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.ObservationsProducer#release()
     */
    @Override
    public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
    	// make the request to nextObservations() return null as end of stream
    	waitForData = false;

    	if(outLog!=null){
    		try{
    			outLog.flush();
    			outLog.close();
    		}catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	if(fosOutLog!=null){
    		try{
    			fosOutLog.close();
    		}catch (Exception e) {
				e.printStackTrace();
			}
    	}


    	//if(streamResource!=null) streamResource.release(waitForThread, timeoutMs);
    	if(streamResource!=null && streamResource instanceof StreamEventProducer){
    		((StreamEventProducer)streamResource).removeStreamEventListener(this);
    	}

    }

    /* (non-Javadoc)
     * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
     */
    @Override
    public SatellitePosition getGpsSatPosition(long utcTime, int satID, double range, double receiverClockError) {
    	if(timeOrderedEphs.size()==0 ||
                utcTime < timeOrderedEphs.elementAt(0).refTime.getMsec()
                ){
    		//System.out.println("\tR: sat pos not found for "+satID);
    		return null;
    	}
        EphSet closer = null;// timeOrderedEphs.elementAt(ephCursor);

        Integer ID = new Integer(satID);

        // temp cursor
        int c = getEphCursor(satID);
        while(c<timeOrderedEphs.size()){
        	EphSet tester = timeOrderedEphs.elementAt(c);
            if((closer == null || utcTime-closer.refTime.getMsec() > utcTime-tester.refTime.getMsec()) &&
            		utcTime-tester.refTime.getMsec()>0){
                //tester is closer and before utcTime, keep as new closer and update cursor
            	if(tester.ephs.containsKey(ID)){
            		closer = tester;
            		setEphCursor(satID, c);
            	}
                c++;
            }else{
                // tester is not closer or not before utcTime
                break;
            }
        }
        if(closer !=null){
        	EphGps eph = closer.ephs.get(ID);

        	SatellitePosition sp = computePositionGps(utcTime,satID, eph, range, receiverClockError);
        	//System.out.println("\tR: < sat pos "+ID);
			return sp;
        }
        //System.out.println("\tR: < sat pos not found for "+ID);
        return null;
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.NavigationProducer#getIono(long)
     */
    @Override
    public IonoGps getIono(long utcTime) {
        if(timeOrderedIono.size()==0 ||
                utcTime < timeOrderedIono.elementAt(0).getRefTime().getMsec()
                ) return null;
        IonoGps closer = timeOrderedIono.elementAt(ionoCursor);

        // temp cursor
        int c = ionoCursor;
        while(c<timeOrderedIono.size()){
            IonoGps tester = timeOrderedIono.elementAt(c);
            if(utcTime-closer.getRefTime().getMsec() > utcTime-tester.getRefTime().getMsec() && utcTime-tester.getRefTime().getMsec()>0){
                //tester is closer and before utcTime, keep as new closer and update cursor
                closer = tester;
                ionoCursor = c;
                c++;
            }else{
                // tester is not closer or not before utcTime
            	//System.out.println("\t\tR: < Iono1");
                return closer;
            }
        }
        //System.out.println("\t\tR: < Iono2");
        return closer;
    }

    /* (non-Javadoc)
     * @see org.gogpsproject.ObservationsProducer#getApproxPosition()
     */
	@Override
    public Coordinates getDefinedPosition() {
        return definedPosition;
    }
	/**
	 * @param fileNameOutLog the fileNameOutLog to set
	 * @throws FileNotFoundException
	 */
	public void setFileNameOutLog(String fileNameOutLog) throws FileNotFoundException {
		this.fileNameOutLog = fileNameOutLog;
		if(fileNameOutLog!=null){
    		fosOutLog = new FileOutputStream(fileNameOutLog,true);
    		outLog = new DataOutputStream(fosOutLog);
    	}
	}
	/**
	 * @return the fileNameOutLog
	 */
	public String getFileNameOutLog() {
		return fileNameOutLog;
	}
	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#setDefinedPosition(org.gogpsproject.Coordinates)
	 */
	@Override
	public void setDefinedPosition(Coordinates definedPosition) {
		//System.out.println((id!=null?id:"")+" got defined position: "+definedPosition);

		this.definedPosition = definedPosition;

		if(outLog!=null){

        	try {
        		definedPosition.write(outLog);
        		outLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param timeoutNextObsWait the timeoutNextObsWait to set
	 */
	public void setTimeoutNextObsWait(long timeoutNextObsWait) {
		this.timeoutNextObsWait = timeoutNextObsWait;
	}
	/**
	 * @return the timeoutNextObsWait
	 */
	public long getTimeoutNextObsWait() {
		return timeoutNextObsWait;
	}
}
