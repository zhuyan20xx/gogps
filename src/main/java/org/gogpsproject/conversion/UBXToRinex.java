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
import java.util.Calendar;

import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.parser.ublox.UBXFileReader;
import org.gogpsproject.producer.rinex.RinexV2Producer;

/**
 * @author Lorenzo Patocchi, cryms.com
 *
 * Converts UBX binary file to RINEX
 *
 */
public class UBXToRinex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length<1){
			System.out.println("UBXToRinex <ubx file>");
			return;
		}

		int p=0;
		String inFile = args[p++];
		String outFile = inFile.indexOf(".ubx")>0?inFile.substring(0, inFile.indexOf(".ubx"))+".obs":inFile+".obs";

		System.out.println("in :"+inFile);
		System.out.println("out:"+outFile);

		ObservationsProducer roverIn = new UBXFileReader(new File(inFile));
		try {
			roverIn.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Started writing RINEX file...");
		RinexV2Producer rp = new RinexV2Producer(outFile, false, true);
		rp.setDefinedPosition(roverIn.getDefinedPosition());

		Observations o = roverIn.getNextObservations();
		while(o!=null){
			rp.addObservations(o);
			o = roverIn.getNextObservations();
		}
		rp.streamClosed();
		System.out.println("END");

	}

}
