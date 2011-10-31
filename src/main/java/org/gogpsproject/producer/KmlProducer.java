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
package org.gogpsproject.producer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gogpsproject.Coordinates;
import org.gogpsproject.PositionConsumer;
import org.gogpsproject.RoverPosition;
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

	private boolean goodDop = false;
	private double goodDopThreshold = 0.0;
	private int timeSapleDelaySec = 1;

	public KmlProducer(String filename, double goodDopTreshold, int timeSapleDelaySec) throws IOException{
		this.filename = filename;
		this.goodDopThreshold = goodDopTreshold;
		this.timeSapleDelaySec = timeSapleDelaySec;
	}

	/* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#addCoordinate(org.gogpsproject.Coordinates)
	 */
	@Override
	public void addCoordinate(RoverPosition coord) {
		if(out==null) return;
		try {
			boolean prevDopResponse = goodDop;
			goodDop = coord.getpDop()<goodDopThreshold;
			if(prevDopResponse != goodDop){
				out.write("</coordinates></LineString></Placemark>\n"+
						"  <Placemark>\n"+
						"    <name></name>\n"+
						"    <description></description>\n"+
						"    <styleUrl>#SMExport_3_"+(goodDop?"ff00ff00":"ff0000e6")+"_fffefefe</styleUrl>\n"+
						"    <LineString>\n"+
						"      <tessellate>1</tessellate>\n"+
						"      <coordinates>\n");
			}

			String lon = g.format(coord.getGeodeticLongitude());
			String lat = g.format(coord.getGeodeticLatitude());
			String h = f.format(coord.getGeodeticHeight());

			out.write(lon + "," // geod.get(0)
					+ lat + "," // geod.get(1)
					+ h + " \n"); // geod.get(2)
			out.flush();

			String t = timeKML.format(new Date(coord.getRefTime().getMsec()));
			System.out.print("T:" + t);
//			System.out.print(" Lon:" + lon);//geod.get(0)
//			System.out.print(" Lat:" + lat);//geod.get(1)
			String dopLabel = "DOP";
			if (coord.getDopType() == RoverPosition.DOP_TYPE_KALMAN)
				dopLabel = "KDOP";
			System.out.println("Lon:"+lon + " " // geod.get(0)
					+"Lat:"+ lat + " " // geod.get(1)
					+"H:"+ h + "\t" + dopLabel + " " // geod.get(2)
					+"P:"+ coord.getpDop()+" "
					+"H:"+ coord.gethDop()+" "
					+"V:"+ coord.getvDop()+" ");//geod.get(2)
			if((num++)%timeSapleDelaySec==0){

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
				"  <Style id=\"SMExport_3_ff00ff00_fffefefe\"><LineStyle><color>ff00ff00</color><width>3</width></LineStyle><PolyStyle><color>fffefefe</color></PolyStyle></Style>\n"+
				"  <Style id=\"dot-icon\"><IconStyle><Icon><href>http://www.eriadne.org/icons/MapPointer.png</href></Icon></IconStyle></Style>\n"+
				"  <Placemark>\n"+
				"    <name></name>\n"+
				"    <description></description>\n"+
				"    <styleUrl>#SMExport_3_"+(goodDop?"ff00ff00":"ff0000e6")+"_fffefefe</styleUrl>\n"+
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

	void GenCircle(double centerlat_form, double centerlong_form,
			int num_points, double radius_form, String outputFile) {
		double lat1, long1, lat2, long2;
		double dlat, dlong, d_rad;
		double a, c, d;
		double delta_pts;
		double radial, lat_rad, dlon_rad, lon_rad;

		double degreeToRadian = Math.PI / 180.0;

		// convert coordinates to radians
		lat1 = Math.toRadians(centerlat_form);
		long1 = Math.toRadians(centerlong_form);

		// Earth measures
		// Year Name a (meters) b (meters) 1/f Where Used
		// 1980 International 6,378,137 6,356,752 298.257 Worldwide
		d = radius_form;
		d_rad = d / 6378137;

		try {
			File fileOutput = new File(outputFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					fileOutput));

			writer.write("<Document>\n<name>$Document</name>\n<Folder>\n<name>$Folder</name>\n<visibility>1</visibility>\n<Placemark>\n<name>$Circle_name</name>\n<visibility>$visibility</visibility>\n<Style>\n<geomColor>$geomColor1$geomColor2</geomColor>\n<geomScale>$geomScale</geomScale></Style>\n<LineString>\n<coordinates>\n");
			// System.out.write(c);
			// System.out.println(c);

			// loop through the array and write path linestrings
			for (int i = 0; i <= num_points; i++) {
				// delta_pts = 360/(double)num_points;
				// radial = Math.toRadians((double)i*delta_pts);
				radial = Math.toRadians((double) i);

				// This algorithm is limited to distances such that dlon <pi/2
				lat_rad = Math.asin(Math.sin(lat1) * Math.cos(d_rad)
						+ Math.cos(lat1) * Math.sin(d_rad) * Math.cos(radial));
				dlon_rad = Math.atan2(Math.sin(radial) * Math.sin(d_rad)
						* Math.cos(lat1), Math.cos(d_rad) - Math.sin(lat1)
						* Math.sin(lat_rad));
				lon_rad = ((long1 + dlon_rad + Math.PI) % (2 * Math.PI))
						- Math.PI;

				// write results
				writer.write(Math.toDegrees(lon_rad) + ", ");
				writer.write(Math.toDegrees(lat_rad) + ", 0");
				writer.write('\n');
			}
			// output footer
			writer.write("</coordinates>\n</LineString>\n</Placemark>\n</Folder>\n</Document>");

			writer.close();
		} catch (IOException e) {
			return;
		}
	}
}
