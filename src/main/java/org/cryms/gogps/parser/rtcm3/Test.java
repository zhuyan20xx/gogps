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

	
	public static void main(String[] args) {

		Test test = new Test();
		if (args.length < 4) {
			System.out.println("Test [host] [port] [user] [pass] ([mountpoint])");
			System.exit(0);
		}
		
		
		try {
			RTCM3Client client = RTCM3Client.getInstance(args[0],Integer.parseInt(args[1]),args[2],args[3],args.length>4?args[4]:null);
			if(client==null){
				System.exit(0);
			}
			
			client.start();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

	public Test() {

	}

}
