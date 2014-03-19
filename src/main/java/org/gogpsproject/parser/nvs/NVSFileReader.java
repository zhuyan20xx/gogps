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

public class NVSFileReader  {
	

	private static InputStream in;
	private NVSReader reader;
//	private File file;
	private Observations obs = null;
	private static IonoGps iono = null;
	// TODO support past times, now keep only last broadcast data
	private static HashMap<Integer,EphGps> ephs = new HashMap<Integer,EphGps>();

	
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	public static void main(String[] args) throws FileNotFoundException {
		//public void init() throws Exception {

	    
	    String file = "./data/rc.rin"; 
	    String file2 = "./data/output.txt";  // after deleting double <DLE> data
	    //String file = "./data/131021_1300_NVSANT_UBXREC_2NVSREC_BINR2_rover_00.bin";

	    /* for deleting double <DLE> data  */
		FileInputStream ins0 = new FileInputStream(file);
	    BufferedInputStream in0 = new BufferedInputStream(ins0);
//	    DataInputStream in0 = new DataInputStream(inb0);
	    
//	    FileInputStream ins = new FileInputStream(file);
//	    BufferedInputStream in = new BufferedInputStream(ins);
//	    DataInputStream in = new DataInputStream(inb);
//	    NVSReader reader = new NVSReader(in, null);
	    
	    /* for deleting double <DLE> data  */
	    FileOutputStream outf = new FileOutputStream(file2);
	//	DataOutputStream out = new DataOutputStream(outf);	
		BufferedOutputStream out = new BufferedOutputStream(outf);	
		
		try{		
			while(in0.available()>0){   // To remove double <DLE> 
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
			
		        /* after deleting double <DLE> data */
				FileInputStream ins = new FileInputStream(file2);
			    BufferedInputStream in = new BufferedInputStream(ins);
				
			    NVSReader reader = new NVSReader(in, null);		
		 
			while(in.available()>0){
				try{
					int data = in.read();
					if(data == 0x10){
						//System.out.println("<DLE>");
						Object o = reader.readMessagge();
				
						
						if(o instanceof Observations){
							//return (Observations)o;
						}else
						if(o instanceof IonoGps){
							iono = (IonoGps)o;
						}
						if(o instanceof EphGps){

							EphGps e = (EphGps)o;
							ephs.put(new Integer(e.getSatID()), e);
						}
						
						
					}else{
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
		//return null;
	}
		
		
		

}
/*	
	public NVSFileReader(File file) {
		this.file = file;
	}
*/
	
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#getApproxPosition()
	 */
	/*
	@Override
	
	public Coordinates getDefinedPosition() {
		Coordinates coord = Coordinates.globalXYZInstance(0.0, 0.0, 0.0); //new Coordinates(new SimpleMatrix(3, 1));
		//coord.setXYZ(0.0, 0.0, 0.0 );
		coord.computeGeodetic();
		// TODO should return null?
		return coord;
	}
*/
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#getCurrentObservations()
	 */
	/*
	@Override
	public Observations getCurrentObservations() {
		return obs;
	}
*/
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#hasMoreObservations()
	 */
	/*
	public boolean hasMoreObservations() throws IOException {
		return in.available()>0;
	}
*/
	

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#nextObservations()
	 */
	//@Override
	//public Observations getNextObservations() {
//	public void getNextObservations() {
//
//		try{
//			while(in.available()>0){
//				try{
//					int data = in.read();
//					if(data == 0xf7){
//						System.out.println("f7");
//						Object o = reader.readMessagge();
//						
//						if(o instanceof Observations){
//							//return (Observations)o;
//						}else
//						if(o instanceof IonoGps){
//							iono = (IonoGps)o;
//						}
//						if(o instanceof EphGps){
//
//							EphGps e = (EphGps)o;
//							ephs.put(new Integer(e.getSatID()), e);
//						}
//					}else{
//						//no warning, may be NMEA
//						//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
//					}
//				}catch(NVSException ubxe){
//					System.err.println(ubxe);
////					ubxe.printStackTrace();
//				}
//			}
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		//return null;
//	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#release()
	 */
	/*
	@Override
	public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/
	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getGpsSatPosition(long, int, double)
	 */
	/*
	@Override
	public SatellitePosition getGpsSatPosition(long unixTime, int satID, double range, double receiverClockError) {
		EphGps eph = ephs.get(new Integer(satID));

		if (eph != null) {
			SatellitePosition sp = computePositionGps(unixTime,satID, eph, range, receiverClockError);
			return sp;
		}
		return null ;
	}
*/
	/* (non-Javadoc)
	 * @see org.gogpsproject.NavigationProducer#getIono(long)
	 */
//	
//	@Override
//	public IonoGps getIono(long unixTime) {
//		return iono;
//	}
//
//	@Override
//	public void init() throws Exception {
//		// TODO Auto-generated method stub
//		
//	}


	
	

