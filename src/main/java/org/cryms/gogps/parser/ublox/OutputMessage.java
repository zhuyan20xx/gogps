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

package org.cryms.gogps.parser.ublox;

import java.util.Vector;

import org.cryms.gogps.util.UnsignedOperation;


public class OutputMessage {

	private int header1 = 0xB5;
	private int header2 = 0x62;

	private int ID1 = 0x06;
	private int ID2 = 0x09;

	private int[] ACK_HEC = { 0xB5, 0x62, 0x05, 0x01, 0x02, 0x00, ID1, ID2 };

	private byte[] bytes;
	private Vector<Integer> record;
	private Vector<Integer> msg;
	private int[] save_mask = { 0x00, 0x00, 0x00, 0x00 };

	private int[] load_mask = { 0x00, 0x00, 0x00, 0x00 };
	private int[] clear_mask = { 0x00, 0x00, 0x00, 0x00 };
	private String finalmsg = "";

	private int devices = 17;

	// devices BBR, FLASH,I2C-EEPROM
	private int length1 = 0x00;
	private int length2 = 0xD;

	public OutputMessage() {
		setMaskTyoe("clear");
		loadMsg();
		msgFinal();
		loadBytes();
	}

	void addMaskToVector() {
		// msg.add(e)
	}

	void checkSum() {
		int CK_A = 0;
		int CK_B = 0;
		record = new Vector<Integer>();
		for (int i = 2; i < msg.size(); i++) {
			CK_A = CK_A + (Integer) msg.elementAt(i);
			CK_B = CK_B + CK_A;
		}
		CK_A = CK_A & 0xFF;
		CK_B = CK_B & 0xFF;
		// CK_A = CK_A % 256;
		// CK_B = CK_B % 256;

		System.out.println("CK " + CK_A + " B " + CK_B);
		msg.add(new Integer(CK_A));
		msg.add(new Integer(CK_B));
		record.add(new Integer(CK_A));
		record.add(new Integer(CK_B));
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getFinalmsg() {
		return finalmsg;
	}

	void loadBytes() {
		bytes = new byte[msg.size()];
		for (int i = 0; i < msg.size(); i++) {
			UnsignedOperation.unsignedIntToByte(msg.elementAt(i));
		}
	}

	void loadMsg() {
		msg = new Vector<Integer>();
		msg.add(new Integer(header1));
		msg.add(new Integer(header2));
		msg.add(new Integer(ID1));
		msg.add(new Integer(ID2));
		msg.add(new Integer(length2));
		msg.add(new Integer(length1));
	}

	void msgFinal() {

		for (int i = 0; i < clear_mask.length; i++) {
			msg.add(new Integer(clear_mask[i]));
		}
		for (int i = 0; i < save_mask.length; i++) {
			msg.add(new Integer(save_mask[i]));
		}
		for (int i = 0; i < load_mask.length; i++) {
			msg.add(new Integer(load_mask[i]));
		}
		msg.add(new Integer(devices));
		checkSum();
		msg.add(new Integer(record.elementAt(0)));
		msg.add(new Integer(record.elementAt(1)));
	}

	void setMaskTyoe(String command) {
		if (command.compareTo("clear") == 0) {
			clear_mask[0] = 0xFF;
			clear_mask[1] = 0xFF;
			load_mask[0] = 0xFF;
			load_mask[1] = 0xFF;
		} else if (command.compareTo("save") == 0) {
			save_mask[0] = 0xFF;
			save_mask[1] = 0xFF;
		} else if (command.compareTo("load") == 0) {
			load_mask[0] = 0xFF;
			load_mask[1] = 0xFF;
		}

	}

}
