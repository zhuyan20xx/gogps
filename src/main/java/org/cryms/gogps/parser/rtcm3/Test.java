/*
 * Copyright (c) 2010, Cryms.com . All Rights Reserved.
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

package org.cryms.gogps.parser.rtcm3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class Test {

	private static GPSNetSettings gpsnetDefault;
	private ArrayList<String> mountpoints;
	private static RTCMClient net;
	private static SaveMessage dati;
	//private static int index = -1;

	public static void main(String[] args) {

		Test test = new Test();
		if (args.length < 4) {
			System.out.println("Test [host] [port] [user] [pass] ([mountpoint])");
			System.exit(0);
		}
		
		
		try {
			if(!test.getSources(args[0],Integer.parseInt(args[1]),args[2],args[3],args.length>4?args[4]:null)){
				System.exit(0);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dati = new SaveMessage(net, test);
		dati.start();
	}

	public Test() {

	}

	public boolean getSources(String _host, int _port, String _username,
			String _password, String _mountpoint) throws Exception{

		ArrayList<String> s = new ArrayList<String>();
		gpsnetDefault = new GPSNetSettings(_host, _port, _username, _password);
		mountpoints = new ArrayList<String>();
		net = new RTCMClient(gpsnetDefault);
		try {
			s = net.getSources();
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		for (int j = 1; j < s.size(); j++) {
			if (j % 2 == 0){
				mountpoints.add(s.get(j));
			}
		}
		if(_mountpoint == null){
			System.out.println("Available Mountpoints:");
		}
		for (int j = 0; j < mountpoints.size(); j++) {
			if(_mountpoint == null){
				System.out.print("\t[" + mountpoints.get(j)+"]");
			}else{
				System.out.print("\t[" + mountpoints.get(j)+"]["+_mountpoint+"]");
				if(_mountpoint.equalsIgnoreCase(mountpoints.get(j))){
					gpsnetDefault.setSource(mountpoints.get(j));
					System.out.print(" found");
				}
			}
			System.out.println();
		}
		if(gpsnetDefault.getSource() == null){
			System.out.println("Select a valid mountpoint!");
			return false;
		}
		return true;
	}

	public void reciverStopped() {
		dati = null;
	}
}
