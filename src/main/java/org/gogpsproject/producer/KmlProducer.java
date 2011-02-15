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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gogpsproject.Coordinates;
/**
 * <p>
 * Produces KML file
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

public class KmlProducer implements PositionConsumer {

	/** The f. */
	private static DecimalFormat f = new DecimalFormat("0.000");

	/** The g. */
	private static DecimalFormat g = new DecimalFormat("0.00000000");

	private SimpleDateFormat timeKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private String filename = null;
	private FileWriter out = null;
	private String timeline = null;
	private int num = 0;

	public KmlProducer(String filename) throws IOException{
		this.filename = filename;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#addCoordinate(org.gogpsproject.Coordinates)
	 */
	@Override
	public void addCoordinate(Coordinates coord) {
		if(out==null) return;
		try {
			String lon = g.format(coord.getGeodeticLongitude());
			String lat = g.format(coord.getGeodeticLatitude());
			String h = f.format(coord.getGeodeticHeight());

			out.write(lon + "," // geod.get(0)
					+ lat + "," // geod.get(1)
					+ h + " \n"); // geod.get(2)
			out.flush();

			String t = timeKML.format(new Date(coord.getRefTime().getMsec()));
			System.out.print("T:" + t);
			System.out.print(" Lon:" + lon);//geod.get(0)
			System.out.print(" Lat:" + lat);//geod.get(1)
			System.out.println(" H:" + h);//geod.get(2)
			if((num++)%10==0){

				timeline += "\n";
				timeline += "<Placemark>"+
		        "<TimeStamp>"+
		        "<when>"+t+"</when>"+
		        "</TimeStamp>"+
		        "<styleUrl>#dot-icon</styleUrl>"+
		        "<Point>"+
		        "<coordinates>"+lon + ","
				+ lat + ","
				+ h + "</coordinates>"+
		        "</Point>"+
		        "</Placemark>";
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#startOfTrack()
	 */
	public void startOfTrack() {
		timeline = "<Folder><open>1</open><Style><ListStyle><listItemType>checkHideChildren</listItemType></ListStyle></Style>";
		try {
			out = new FileWriter(filename);

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				"<Document xmlns:kml=\"http://earth.google.com/kml/2.1\">\n"+
				"  <Style id=\"SMExport_3_ff0000e6_fffefefe\"><LineStyle><color>ff0000e6</color><width>3</width></LineStyle><PolyStyle><color>fffefefe</color></PolyStyle></Style>\n"+
				"  <Style id=\"dot-icon\"><IconStyle><Icon><href>http://www.eriadne.org/icons/MapPointer.png</href></Icon></IconStyle></Style>\n"+
				"  <Placemark>\n"+
				"    <name></name>\n"+
				"    <description></description>\n"+
				"    <styleUrl>#SMExport_3_ff0000e6_fffefefe</styleUrl>\n"+
				"    <LineString>\n"+
				"      <tessellate>1</tessellate>\n"+
				"      <coordinates>\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#endOfTrack()
	 */
	public void endOfTrack() {
		if(out!=null){
			// Write KML footer part
			try {
				out.write("</coordinates></LineString></Placemark>"+timeline+"</Folder></Document>\n");
				// Close FileWriter
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#event(int)
	 */
	@Override
	public void event(int event) {
		if(event == EVENT_START_OF_TRACK){
			startOfTrack();
		}
		if(event == EVENT_END_OF_TRACK){
			endOfTrack();
		}
	}

}
