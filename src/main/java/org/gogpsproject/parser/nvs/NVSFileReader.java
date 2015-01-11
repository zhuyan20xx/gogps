/*
 * Copyright (c) 2010 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
package org.gogpsproject.parser.nvs;


import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.EphemerisSystem;
import org.gogpsproject.IonoGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.SatellitePosition;

/**
 * <p>
 * Read an NVS File and implement Observation and Navigation producer 
 * </p>
 *
 * @author Daisuke Yoshida OCU
 */

public class NVSFileReader extends EphemerisSystem implements ObservationsProducer,NavigationProducer {

	private BufferedInputStream in;
	private File file;
	private Observations obs = null;
	private NVSReader reader;
	private IonoGps iono = null;
	// TODO support past times, now keep only last broadcast data
	private HashMap<Integer,EphGps> ephs = new HashMap<Integer,EphGps>();
	
    boolean gpsEnable = true;  // enable GPS data reading
	boolean qzsEnable = true;  // enable QZSS data reading
    boolean gloEnable = true;  // enable GLONASS data reading	
    boolean galEnable = true;  // enable Galileo data reading
    boolean bdsEnable = true;  // enable BeiDou data reading

	Boolean[] multiConstellation = {gpsEnable, qzsEnable, gloEnable, galEnable, bdsEnable};
	
	public NVSFileReader(File file) {
		this.file = file;
		//this.multiConstellation[0] = true;  // enable QZSS data reading
		//this.multiConstellation[1] = true;  // enable GLONASS data reading
	}
	
	public NVSFileReader(File file, Boolean[] multiConstellation) {
		this.file = file;
		this.multiConstellation = multiConstellation;		
	}
	
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#getApproxPosition()
	 */
	@Override
	public Coordinates getDefinedPosition() {
		Coordinates coord = Coordinates.globalXYZInstance(0.0, 0.0, 0.0); //new Coordinates(new SimpleMatrix(3, 1));
		//coord.setXYZ(0.0, 0.0, 0.0 );
		coord.computeGeodetic();
		// TODO should return null?
		return coord;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#getCurrentObservations()
	 */
	@Override
	public Observations getCurrentObservations() {
		return obs;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#hasMoreObservations()
	 */
	public boolean hasMoreObservations() throws IOException {
		return in.available()>0;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	@Override
	public void init() throws Exception {
		FileInputStream ins = new FileInputStream(file);
		this.in = new BufferedInputStream(ins);
		this.reader = new NVSReader(in, multiConstellation, null);
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	@Override   // need to comment if you want to use main method 
	public Observations getNextObservations() {
		
		try{		
			while(in.available()> 1000){  // Need to adjust this value 
				
				try{
					int data = in.read();
					
					if(data == 0x10){
						Object o = reader.readMessage();
						
						if(o instanceof Observations){
							return (Observations)o;
						}else
							if(o instanceof IonoGps){
								iono = (IonoGps)o;
							}
						if(o instanceof EphGps){

							EphGps e = (EphGps)o;
							ephs.put(new Integer(e.getSatID()), e);
						}
						
						
//					}else{
						//no warning, may be NMEA
						//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
					}
				}catch(NVSException nvse){
					System.err.println(nvse);
//					ubxe.printStackTrace();
				}
			}
			
			in.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
		
		
		
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#release()
	 */
	@Override
	public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
		try {
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	@Override
	public SatellitePosition getGpsSatPosition(long unixTime, int satID, char satType, double range, double receiverClockError) {
		EphGps eph = ephs.get(new Integer(satID));

//		char satType = eph.getSatType();
		
		if (eph != null) {
			SatellitePosition sp = computePositionGps(unixTime, satID, satType, eph, range, receiverClockError);
			return sp;
		}
		return null ;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getIono(long)
	 */
	@Override
	public IonoGps getIono(long unixTime) {
		return iono;
	}
}	


	
	

