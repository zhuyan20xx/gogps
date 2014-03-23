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
import org.gogpsproject.parser.ublox.UBXReader;

/**
 * <p>
 * Read an NVS File and implement Observation and Navigation producer (if 4A and F7 has been recorded)
 * </p>
 *
 * @author Daisuke Yoshida (Osaka City University), Lorenzo Patocchi (cryms.com)
 */

//public class NVSFileReader  {
public class NVSFileReader extends EphemerisSystem implements ObservationsProducer,NavigationProducer {

	private InputStream in;
	private NVSReader reader;
	private File file;
	private Observations obs = null;
	private IonoGps iono = null;
	// TODO support past times, now keep only last broadcast data
	private HashMap<Integer,EphGps> ephs = new HashMap<Integer,EphGps>();
	String file2 = "./data/output.txt";
	
	BufferedInputStream in2;

		
	public NVSFileReader(File file) {
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
		this.in = new FileInputStream(file);

		this.reader = new NVSReader(in, null);
	}
	
	public void initNVS() throws Exception {
		this.in = new FileInputStream(file);
		
		FileOutputStream outf = null;
			try {
				outf = new FileOutputStream(file2);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		BufferedOutputStream out = new BufferedOutputStream(outf);	

		while(in.available()>0){   // To remove double <DLE> 
			int contents = in.read();
		    out.write(contents);
			if(contents == 0x10){
				contents = in.read();
				if(contents == 0x10){
					continue;	
				}else{
					out.write(contents);								

				}										
			}	
		}		
		out.close();		
		
		FileInputStream ins = new FileInputStream(file2);
		in2 = new BufferedInputStream(ins);
		
		this.reader = new NVSReader(in2, null);	
		
	}
	
	/* (non-Javadoc)
	 * @see org.gogpsproject.ObservationsProducer#init()
	 */
	@Override   // need to comment if you want to use main method 
	public Observations getNextObservations() {
//	public static void main(String[] args) throws FileNotFoundException {
//	 	String file = "./data/rc.rin"; 
//	 	String file = "./data/output.txt"; 
//	 	String file = "./data/131021_1430_NVSANT_UBXREC_2NVSREC_KIN_BINR3_rover_00.bin";
			
//	    String file2 = "./data/output.txt";  // after deleting double <DLE> data
	    //String file = "./data/131021_1300_NVSANT_UBXREC_2NVSREC_BINR2_rover_00.bin";

	    /* for deleting double <DLE> data  */
//		FileInputStream ins0 = null;
//		try {
//			ins0 = new FileInputStream(file);
//		} catch (FileNotFoundException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		
//		BufferedInputStream in0 = new BufferedInputStream(ins0);
    
		
	    
	    /* for deleting double <DLE> data  */
//	    FileOutputStream outf = null;
//		try {
//			outf = new FileOutputStream(file2);
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	//	DataOutputStream out = new DataOutputStream(outf);	
//		BufferedOutputStream out = new BufferedOutputStream(outf);	
		
		try{		

//			while(in0.available()>0){   // To remove double <DLE> 
//						int contents = in0.read();
//					    out.write(contents);
//						if(contents == 0x10){
//							contents = in0.read();
//							if(contents == 0x10){
//								continue;	
//							}else{
//								out.write(contents);								
//
//							}										
//						}	
//			}		
			
//			while(in.available()>0){   // To remove double <DLE> 
//				int contents = in.read();
//			    out.write(contents);
//				if(contents == 0x10){
//					contents = in.read();
//					if(contents == 0x10){
//						continue;	
//					}else{
//						out.write(contents);								
//
//					}										
//				}	
//			}		
//			out.close();
			
		    /* after deleting double <DLE> data */
//			FileInputStream ins = new FileInputStream(file2);
//			@SuppressWarnings("resource")
//			BufferedInputStream in2 = new BufferedInputStream(ins);
				
//????			    NVSReader reader = new NVSReader(in, null);		
//			System.out.println(in2.available());

			while(in2.available()>0){
				try{
					int data = in2.read();
//					System.out.println("<DLE>");

					if(data == 0x10){
//						System.out.println("<DLE>");
//						Object o = reader.readMessagge();
						Object o = reader.readMessagge(in2);

						if(o instanceof Observations){
//							System.out.println("Observations OK");
							return (Observations)o;
						}else
						if(o instanceof IonoGps){
//							System.out.println("IonoGps OK");
							iono = (IonoGps)o;
						}
						if(o instanceof EphGps){
//							System.out.println("EphGps OK");
							EphGps e = (EphGps)o;
							ephs.put(new Integer(e.getSatID()), e);
						}
						
						
					}else{
						//System.out.println("else");
						//no warning, may be NMEA
						//System.out.println("Wrong Sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
					}
				}catch(NVSException nvse){
					System.err.println(nvse);
//					ubxe.printStackTrace();
				}
			}
			
		//	in.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;   // need to comment if you want to use main method 
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

	
	

