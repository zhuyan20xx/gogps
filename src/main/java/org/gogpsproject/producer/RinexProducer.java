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
package org.gogpsproject.producer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;
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
public class RinexProducer implements StreamEventListener {

	private String outFilename;
	private boolean headerWritten;

	private Coordinates approxPosition = null;

	private Vector<Observations> observations = new Vector<Observations>();

	private boolean needApproxPos=false;

	private FileOutputStream fos = null;
	private PrintStream ps = null;

	public RinexProducer(String outFilename, boolean needApproxPos){
		this.outFilename = outFilename;
		this.needApproxPos = needApproxPos;

		try {
			fos = new FileOutputStream(outFilename, false);
			ps = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addEphemeris(org.gogpsproject.EphGps)
	 */
	@Override
	public void addEphemeris(EphGps eph) {

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addIonospheric(org.gogpsproject.IonoGps)
	 */
	@Override
	public void addIonospheric(IonoGps iono) {

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#addObservations(org.gogpsproject.Observations)
	 */
	@Override
	public void addObservations(Observations o) {
		synchronized (this) {
			if(!headerWritten){
				observations.add(o);
				if(needApproxPos && approxPosition==null){
					return;
				}

				try {
					writeHeader(approxPosition, observations.firstElement());
				} catch (IOException e) {
					e.printStackTrace();
				}

				for(Observations obs:observations){
					try {
						writeObservation(obs);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				observations.removeAllElements();
				headerWritten = true;
			}else{
				try {
					writeObservation(o);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#setDefinedPosition(org.gogpsproject.Coordinates)
	 */
	@Override
	public void setDefinedPosition(Coordinates definedPosition) {
		synchronized (this) {
			this.approxPosition = definedPosition;
		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamEventListener#streamClosed()
	 */
	@Override
	public void streamClosed() {
		try {
			ps.close();
		} catch (Exception e) {

		}
		try {
			fos.close();
		} catch (Exception e) {

		}
	}

	private void writeHeader(Coordinates approxPosition,Observations firstObservation) throws IOException{
		SimpleDateFormat sdfHeader = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
		DecimalFormat dfX7 = new DecimalFormat("0.0000000");
		DecimalFormat dfX4 = new DecimalFormat("0.0000");
		DecimalFormat dfX = new DecimalFormat("0");
//	          2              OBSERVATION DATA    G (GPS)             RINEX VERSION / TYPE
//	     CCRINEXO V2.4.1 LH  Bernese             28-APR-08 17:51     PGM / RUN BY / DATE
//	     TPS2RIN 1.40        GEOMATICA/IREALP    28-APR-08 12:59     COMMENT
//	     BUILD FEB  4 2004 (C) TOPCON POSITIONING SYSTEMS            COMMENT
//	     D:\GPSCOMO\TB01H\2008\04\28\CO6C79~1.JPS                    COMMENT
//	     SE TPS 00000000                                             COMMENT
//	     COMO                                                        MARKER NAME
//	     12761M001                                                   MARKER NUMBER
//	     GEOMATICA/IREALP    MILANO POLYTECHNIC                      OBSERVER / AGENCY
//	     8PRM6AZ2EBK         TPS ODYSSEY_E       3.1 JAN,24,2007 P1  REC # / TYPE / VERS
//	     217-0400            TPSCR3_GGD      CONE                    ANT # / TYPE
//	       4398306.2809   704149.8723  4550154.6777                  APPROX POSITION XYZ
//	             0.2134        0.0000        0.0000                  ANTENNA: DELTA H/E/N
//	          1     1                                                WAVELENGTH FACT L1/2
//	          7    C1    P1    P2    L1    L2    D1    D2            # / TYPES OF OBSERV
//	          1                                                      INTERVAL
//	       2008     4    28    12     0    0.000000                  TIME OF FIRST OBS
//	                                                                 END OF HEADER
		writeLine (sf("",5)+sf("2",15)+sf("OBSERVATION DATA",20)+sf("G (GPS)",20)+se("RINEX VERSION / TYPE",20), false);
		appendLine(sf("goGPS-java",20)+sf("",20)+sf(sdfHeader.format(new Date()).toUpperCase(),20)+se("PGM / RUN BY / DATE",20));
		appendLine(sf("",20*3)+se("MARKER NAME",20));
		appendLine(sf("",20*3)+se("MARKER NUMBER",20));
		appendLine(sf("",20*3)+se("OBSERVER / AGENCY",20));
		if(approxPosition != null){
			appendLine(sp(dfX4.format(approxPosition.getX()),14,1)+sp(dfX4.format(approxPosition.getY()),14,1)+sp(dfX4.format(approxPosition.getZ()),14,1)+sf("",18)+se("APPROX POSITION XYZ",20));
		}else{
			appendLine(sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sf("",18)+se("APPROX POSITION XYZ",20));
		}
		appendLine(sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sp(dfX4.format(0.0),14,1)+sf("",18)+se("ANTENNA: DELTA H/E/N",20));
		appendLine(sp(dfX.format(1),6,1)+sp(dfX.format(1),6,1)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",6)+sf("",12)+se("WAVELENGTH FACT L1/2",20));
		appendLine(sp(dfX.format(7),6,1)+sp("C1",6,1)+sp("P1",6,1)+sp("P2",6,1)+sp("L1",6,1)+sp("L2",6,1)+sp("D1",6,1)+sp("D2",6,1)+sp("S1",6,1)+sp("S2",6,1)+se("# / TYPES OF OBSERV",20));
		appendLine(sp(dfX.format(1),6,1)+sf("",60-1*6)+se("INTERVAL",20));

		if(firstObservation!=null){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(firstObservation.getRefTime().getMsec());
			appendLine(sp(dfX.format(c.get(Calendar.YEAR)),6,1)
					+sp(dfX.format(c.get(Calendar.MONTH)+1),6,1)
					+sp(dfX.format(c.get(Calendar.DATE)),6,1)
					+sp(dfX.format(c.get(Calendar.HOUR_OF_DAY)),6,1)
					+sp(dfX.format(c.get(Calendar.MINUTE)),6,1)
					+sp(dfX7.format(c.get(Calendar.SECOND)+c.get(Calendar.MILLISECOND)/1000.0),13,1)
					+sp("GPS",8,1)+sf("",9)+se("TIME OF FIRST OBS",20));
		}

		appendLine(sf("",60)+se("END OF HEADER",20));
	}


//	 10  3  2 13 20  0.0000000  0 12G11G13G32G04G20G17G23R15R20R21R05R04
//	  24501474.376                    24501481.324   128756223.92705 100329408.23106
//	        42.000          21.000
//	  20630307.428                    20630311.680   108413044.43906  84477784.53807
//	        47.000          39.000
//	  23383145.918                    23383151.148   122879286.75106  95750102.77806
//	        43.000          27.000
//	  21517480.617                    21517485.743   113075085.94506  88110522.62107
//	        46.000          37.000
//	  20912191.322                    20912194.474   109894342.71907  85631979.97507
//	        51.000          39.000
//	  23257094.649                    23257100.632   122216783.06806  95233917.17406
//	        43.000          27.000
//	  20161355.093                    20161358.227   105948754.59007  82557486.35607
//	        51.000          40.000
//	  20509222.093    20509221.179    20509227.404   109595309.53707  85240851.57508
//	        49.000          44.000
//	  23031155.571    23031156.184    23031167.285   123157678.29405  95789331.13107
//	        41.000          38.000
//	  21173954.694    21173954.486    21173960.635   113305812.57507  88126783.03008
//	        49.000          45.000
//	  20128038.579    20128037.431    20128044.524   107596015.91107  83685880.30708
//	        52.000          46.000
//	  23792524.319    23792523.288    23792533.414   127408153.57805  99095296.49707
//	        40.000          37.000
	private void writeObservation(Observations o) throws IOException{
		//System.out.println(o);
		DecimalFormat dfX3 = new DecimalFormat("0.000");
		DecimalFormat dfX5 = new DecimalFormat("0.00000");
		DecimalFormat dfX7 = new DecimalFormat("0.0000000");
		DecimalFormat dfX = new DecimalFormat("0");
		DecimalFormat dfXX = new DecimalFormat("00");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(o.getRefTime().getMsec());

		String line = "";
		line += sp(dfX.format(c.get(Calendar.YEAR)-2000),3,1);
		line += sp(dfX.format(c.get(Calendar.MONTH)+1),3,1);
		line += sp(dfX.format(c.get(Calendar.DATE)),3,1);
		line += sp(dfX.format(c.get(Calendar.HOUR_OF_DAY)),3,1);
		line += sp(dfX.format(c.get(Calendar.MINUTE)),3,1);
		line += sp(dfX7.format(c.get(Calendar.SECOND)+c.get(Calendar.MILLISECOND)/1000.0),11,1);
		line += sp(dfX.format(o.getEventFlag()),3,1);
		int gpsSize = o.getGpsSize();
		line += sp(dfX.format(gpsSize),3,1);
		for(int i=0;i<gpsSize;i++){
			if(i==12){
				writeLine(line, true);
				line = "";
			}
			line += "G"+dfXX.format(o.getGpsSatID(i));
		}
		writeLine(line, true);

		// C1    P1    P2    L1    L2    D1    D2   S1   S2
		for(int i=0;i<gpsSize;i++){
			ObservationSet os = o.getGpsByIdx(i);
			line = "";
			line += Double.isNaN(os.getCodeC(0))?sf("",16):sp(dfX3.format(os.getCodeC(0)),14,1)+"  "; // C1
			line += Double.isNaN(os.getCodeP(0))?sf("",16):sp(dfX3.format(os.getCodeP(0)),14,1)+"  "; // P1
			line += Double.isNaN(os.getCodeP(1))?sf("",16):sp(dfX3.format(os.getCodeP(1)),14,1)+"  "; // P2

			line += Double.isNaN(os.getPhase(0))?sf("",14):sp(dfX3.format(os.getPhase(0)),14,1); // L1
			line += os.getLossLockInd(0)<0?" ":dfX.format(os.getLossLockInd(0)); // L1 Loss of Lock
			line += " ";//Float.isNaN(os.getSignalStrength(0))?" ":sp(dfX.format(os.getSignalStrength(0)/6),1,0); // L1 signal strengt

			line += Double.isNaN(os.getPhase(1))?sf("",14):sp(dfX3.format(os.getPhase(1)),14,1); // L2
			line += os.getLossLockInd(1)<0?" ":dfX.format(os.getLossLockInd(1)); // L2 Loss of Lock
			line += " ";//Float.isNaN(os.getSignalStrength(1))?" ":sp(dfX.format(os.getSignalStrength(1)/6),1,0); // L2 signal strengt

			writeLine(line, true);
			line = "";

			line += Float.isNaN(os.getDoppler(0))?sf("",16):sp(dfX3.format(os.getDoppler(0)),14,1)+"  "; // D1
			line += Float.isNaN(os.getDoppler(1))?sf("",16):sp(dfX3.format(os.getDoppler(1)),14,1)+"  "; // D2
			line += Float.isNaN(os.getSignalStrength(0))?sf("",16):sp(dfX3.format(os.getSignalStrength(0)),14,1)+"  "; // S1
			line += Float.isNaN(os.getSignalStrength(1))?sf("",16):sp(dfX3.format(os.getSignalStrength(1)),14,1)+"  "; // S2

			writeLine(line, true);
		}

	}
	// space end
	private String se(String in, int max){
		return sf(in,max,0);
	}
	// space fill with 1 space margin
	private String sf(String in, int max){
		return sf(in,max,1);
	}
	// space fill with margin
	private String sf(String in, int max,int margin){
		if(in.length()==max-margin){
			while(in.length()<max) in +=" ";
			return in;
		}
		if(in.length()>max-margin){
			return in.substring(0, max-margin)+" ";
		}
		while(in.length()<max) in +=" ";

		return in;
	}
	// space prepend with margin
	private String sp(String in, int max,int margin){
		if(in.length()==max-margin){
			while(in.length()<max) in =" "+in;
			return in;
		}
		if(in.length()>max-margin){
			return in.substring(0, max-margin)+" ";
		}
		while(in.length()<max) in =" "+in;

		return in;
	}

	private void appendLine(String line) throws IOException{
		writeLine(line, true);
	}
	private void writeLine(String line, boolean append) throws IOException{

		FileOutputStream fos = this.fos;
		PrintStream ps = this.ps;
		if(this.fos == null){
			fos = new FileOutputStream(outFilename, append);
			ps = new PrintStream(fos);
		}

		ps.println(line);
		//System.out.println(line);

		ps.flush();
		if(this.fos == null){
			ps.close();
			fos.close();
		}


	}
}
