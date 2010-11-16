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
import java.io.InputStream;
import java.util.HashMap;

import org.cryms.gogps.util.Bits;


public abstract class InputDevice {

	protected boolean go;
	HashMap<Integer, Decode> map = new HashMap<Integer, Decode>();
	/** Optinal message handler for showing error messages. */

	public boolean header = true;

	public int messagelength = 0;

	public int switchboolean;

	int[] buffer;

	boolean[] bits;

	boolean[] rollbits;

	boolean downloadlength = false;

	public InputDevice() {
		go = false;

	}

	abstract public String getDeviceId();

	/**
	 * reads data from an InputStream while go is true
	 * 
	 * @param in
	 *            input stream to read from
	 */
	protected void readLoop(InputStream in) throws IOException {
		int c;
		int index;
		while (go) {
			c = in.read();
			System.out.println("Header : " + c);
			if (c < 0)
				break;
			if (header) {
				if (c == 211) { // header
					index = 0;
					buffer = new int[2];
					buffer[0] = in.read();
					buffer[1] = in.read();
					bits = new boolean[buffer.length * 8];
					rollbits = new boolean[8];
					for (int i = 0; i < buffer.length; i++) {
						rollbits = Bits.rollByteToBits(buffer[i]);
						for (int j = 0; j < rollbits.length; j++) {
							bits[index] = rollbits[j];
							index++;
						}
					}
					messagelength = Bits.bitsToInt(Bits.subset(bits, 6, 10));
					System.out.println("Debug message length : "
							+ messagelength);
					header = false;
					// downloadlength = true;
				}
			}

			if (messagelength > 0) {
				setBits(in, messagelength);
				int msgtype = Bits.bitsToInt(Bits.subset(bits, 0, 12));

				System.out.println("message type : " + msgtype);
				messagelength = 0;
				map.put(new Integer(1004), new Decode1004Msg(bits));
				map.put(new Integer(1005), new Decode1005Msg(bits));
				map.put(new Integer(1007), new Decode1007Msg(bits));
				map.put(new Integer(1012), new Decode1012Msg(bits));
				
					Decode dec = map.get(new Integer(msgtype));
					if(dec!=null){
						dec.decode();
					}			
//					switch (msgtype) {
//					case 1007:
//						Decode1007Msg decode1007 = new Decode1007Msg(bits);
//						decode1007.decode();
//						break;
//					case 1012:
//						Decode1012Msg decode1012 = new Decode1012Msg(bits);
//						decode1012.decode();
//						break;
//					case 1005:
//						Decode1005Msg decode1005 = new Decode1005Msg(bits);
//						decode1005.decode();
//						break;
//					case 1004:
//						Decode1004Msg decode1004 = new Decode1004Msg(bits);
//						decode1004.decode();
//						break;
//					default:
//						break;
//					}
				
				// Decode1007Msg decode = new Decode1007Msg(bits);

				// CRC
				setBits(in, 3);
			
				header = true;
				// setBits(in,1);
				// System.out.println(" dati :" + Bits.bitsToStr(bits));
			}
		}
	}

	/** returns the number of messages ready to read */
	abstract public int ready();

	private void setBits(InputStream in, int bufferlength) throws IOException {
		int index = 0;
		buffer = new int[bufferlength];
		bits = new boolean[buffer.length * 8];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = in.read();
		}
		// index = 0;
		for (int i = 0; i < buffer.length; i++) {
			rollbits = Bits.rollByteToBits(buffer[i]);
			for (int j = 0; j < 8; j++) {
				bits[index] = rollbits[j];
				index++;
			}
		}
	}

	abstract public void start();

	abstract public void stop();

	abstract public boolean stopped();
}
