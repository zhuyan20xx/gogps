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


public class MsgConfiguration {

	private int header1 = 0xB5;
	private int header2 = 0x62;
	private int ID2 = 0x01;
	private int ID1 = 0x06;
	private int classid;
	private int msgval;
	private MessageType msgid;
	private int length1 = 0;
	private int length2 = 3;
	private int rate;
	private int CK_A;
	private int CK_B;
	private Vector<Integer> msg;

	public MsgConfiguration(String classtype, String msgtype, int _mode) {
		msgid = new MessageType(classtype, msgtype);
		classid = msgid.getClassOut();
		msgval = msgid.getIdOut();
		// System.out.println("ID 1 >>:" + ID1 + "ID 2 >>:" + ID2);
		if (_mode == 1)
			rate = 0x01;
		else
			rate = 0x00;
		msg = new Vector<Integer>();
		msg.add(header1);
		msg.add(header2);
		msg.add(ID1);
		msg.add(ID2);
		msg.add(length2);
		msg.add(length1);
		msg.add(classid);
		msg.add(msgval);
		msg.add(rate);
		checkSum();
		msg.add(CK_A);
		msg.add(CK_B);
	}

	private void checkSum() {
		CK_A = 0;
		CK_B = 0;
		for (int i = 2; i < msg.size(); i++) {
			CK_A = CK_A + (Integer) msg.elementAt(i);
			CK_B = CK_B + CK_A;

		}
		CK_A = CK_A & 0xFF;
		CK_B = CK_B & 0xFF;
	}

	public byte[] getByte() {
		byte[] bytes = new byte[msg.size()];
		for (int i = 0; i < msg.size(); i++) {
			bytes[i] = UnsignedOperation.unsignedIntToByte(msg.elementAt(i));
		}
		return bytes;
	}

}
