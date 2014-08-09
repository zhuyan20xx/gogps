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


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.io.OutputStreamWriter;










import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;
import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.EphemerisSystem;
import org.gogpsproject.IonoGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.SatellitePosition;
import org.gogpsproject.StreamResource;

/**
 * <p>
 * Read an UBX File and implement Observation and Navigation producer (if AID-HUI and AID-EPH has been recorded)
 * </p>
 *
 * @author Daisuke Yoshida OCU
 */

public class NVSFileReader3 extends EphemerisSystem implements ObservationsProducer,NavigationProducer {
	

//	private InputStream in;
	private BufferedInputStream in;
	private File file;
	private Observations obs = null;
	private NVSReader reader;
	private IonoGps iono = null;
	// TODO support past times, now keep only last broadcast data
	private HashMap<Integer,EphGps> ephs = new HashMap<Integer,EphGps>();
	private BufferedInputStream in0;

    String file2 = "./data/output.txt";  // for storing processed data after removing double <DLE>

	
	public NVSFileReader3(File file) {
		this.file = file;
	
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
		
		/* read original data file */
		FileInputStream inf = new FileInputStream(file);
		@SuppressWarnings("resource")
		BufferedInputStream in0 = new BufferedInputStream(inf);
		
		/* write processed data */
	    FileOutputStream outf = null;
		outf = new FileOutputStream(file2);
		BufferedOutputStream out = new BufferedOutputStream(outf);	
		
		/*  remove double <DLE> into single  */
		while(in0.available()>0){   
			int contents = in0.read();
		    out.write(contents);
		    
			if(contents == 0x10){
				contents = in0.read();
				if(contents == 0x10){
					continue;	
				}else{
					out.write(contents);								
				}										
			}	
			
		}								
		out.close();	
		
		
		/* read processed data file */
		FileInputStream ins = new FileInputStream(file2);
	    this.in = new BufferedInputStream(ins);	    		
		this.reader = new NVSReader(in, null);
	    		
	}
	
	
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	@Override   // need to comment if you want to use main method 
	public Observations getNextObservations() {
//	public static void main(String[] args) throws FileNotFoundException {
		//public void init() throws Exception {  
		
		try{		
			while(in.available()> 1000){  // Need to adjust this value 
//				int leng1 = in.available();
//				System.out.println("leng1: " + leng1);
				
				
				try{
					int data = in.read();
					
					if(data == 0x10){
//						System.out.println("<DLE>");
						Object o = reader.readMessagge();
						
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
	public SatellitePosition getGpsSatPosition(long unixTime, int satID, double range, double receiverClockError) {
		EphGps eph = ephs.get(new Integer(satID));

		if (eph != null) {
			SatellitePosition sp = computePositionGps(unixTime,satID, eph, range, receiverClockError);
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


	
	
