/*
 * Copyright (c) 2011 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
package org.gogpsproject.conversion;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.Time;
import org.gogpsproject.parser.rtcm3.RTCM3FileReader;
import org.gogpsproject.producer.rinex.RinexV2Producer;
import org.gogpsproject.producer.rinex.RinexV3Producer;

/**
 * @author Lorenzo Patocchi, cryms.com
 *
 * Converts NVS binary file to RINEX
 *
 */
public class RTCM3ToRinex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//force dot as decimal separator
		Locale.setDefault(new Locale("en", "US"));
		
		boolean singleFreq = false;

		if(args.length<2){
			System.out.println("RTCM3ToRinex <RTCM3 file> <marker name> <starting GPS week>");
			return;
		}

		int p=0;
		String inFile = args[p++];
		String marker = args[p++];
		int week =Integer.parseInt(args[p++]);

		System.out.println("in :"+inFile);

		ObservationsProducer masterIn = new RTCM3FileReader(new File(inFile), week);
		try {
			masterIn.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Observations o = masterIn.getNextObservations();
		while(o==null){
			o = masterIn.getNextObservations();
		}
		
		//First daily RINEX file
		Time epoch = o.getRefTime();
		int DOY = epoch.getDayOfYear();
		int year = epoch.getYear2c();
		String outFile = "./test/" + marker + String.format("%03d", DOY) + "0." + year + "o";
        
		System.out.println("Started writing RINEX file "+outFile);

//		RinexV3Producer rp = new RinexV3Producer(outFile, false, singleFreq);
		RinexV2Producer rp = new RinexV2Producer(outFile, false, singleFreq);
		rp.setDefinedPosition(masterIn.getDefinedPosition());

		int DOYold = DOY;
		
		while(o!=null){
			rp.addObservations(o);
			o = masterIn.getNextObservations();
			
			if (o!=null) {
				//check if the day changes; if yes, a new daily RINEX file must be started
				epoch = o.getRefTime();
				DOY = epoch.getDayOfYear();

				if (DOY != DOYold) {
					rp.streamClosed();

					year = epoch.getYear2c();
					outFile = "./test/" + marker + String.format("%03d", DOY) + "0." + year + "o";

					System.out.println("Started writing RINEX file "+outFile);
//					rp = new RinexV3Producer(outFile, false, singleFreq);
					rp = new RinexV2Producer(outFile, false, singleFreq);
					rp.setDefinedPosition(masterIn.getDefinedPosition());

					DOYold = DOY;
				}
			}
		}
		rp.streamClosed();
		
		System.out.println("END");
	}
}
