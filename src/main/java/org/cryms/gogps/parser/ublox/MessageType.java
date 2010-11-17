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

public class MessageType {
	public enum ack {
		ACK, NAK;
	}

	public enum aid {
		REQ, DATA, INI, HUI, ALM, EPH;
	}

	public enum cfg {
		PRT, USB, MSG, NMEA, RATE, CFG, TP, NAV2, DAT, INF, RST, RXM, ANT, FXN, SBAS, LIC, TM, TM2, TMODE, EKF;
	}

	public enum inf {
		ERROR, WARNING, NOTICE, TEST, DEBUG, USER;
	}

	public enum mon {
		SCHD, IO, MSGPP, RXBUF, TXBUF, HW, IPC, USB, VER, EXCEPT;
	}

	public enum msgclass {
		NAV, RXM, INF, ACK, CFG, UPD, MON, AID, TIM, NMEA, PUBX;
	}

	public enum nav {
		POSECEF, POSLLH, POSUTM, DOP, STATUS, SOL, VELECEF, VELNED, TIMEGPS, TIMEUTC, CLOCK, SVINFO, DGPS, SBAS, EKFSTATUS;
	}

	public enum nmea {
		GGA, GLL, GSA, GSV, RMC, VTG, GRS, GST, ZDA, GBS, DTM;
	}

	public enum pubx {
		A, B, C, D;
	}

	public enum rxm {
		RAW, SVSI, SFRB, ALM, EPH, POSREQ;
	}

	public enum tim {
		TM, TM2, TP, SVIN;
	}

	public enum upd {
		DOWNL, UPLOAD, EXEC, MEMCPY;
	}

	private static int classOut = 0;

	private static int idOut = 0;

	private static void setClassOut(int classOut) {
		MessageType.classOut = classOut;
	}

	private static void setIdOut(int idOut) {
		MessageType.idOut = idOut;
	}

	public MessageType(String _mclass, String _msgtype) {
		getMsg(_mclass, _msgtype);
	}

	public int getClassOut() {
		return classOut;
	}

	public int getIdOut() {
		return idOut;
	}

	private int getMsg(String mclass, String msgtype) {
		try {
			switch (msgclass.valueOf(mclass)) {

			case NAV:
				setClassOut(0x01);
				switch (nav.valueOf(msgtype)) {
				case POSECEF:
					setIdOut(0x01);
					return 0;
				case POSLLH:
					setIdOut(0x02);
					return 0;
				case POSUTM:
					setIdOut(0x08);
					return 0;
				case DOP:
					setIdOut(0x04);
					return 0;
				case STATUS:
					setIdOut(0x03);
					return 0;
				case SOL:
					setIdOut(0x06);
					return 0;
				case VELECEF:
					setIdOut(0x11);
					return 0;
				case VELNED:
					setIdOut(0x12);
					return 0;
				case TIMEGPS:
					setIdOut(0x20);
					return 0;
				case TIMEUTC:
					setIdOut(0x21);
					return 0;
				case CLOCK:
					setIdOut(0x22);
					return 0;
				case SVINFO:
					setIdOut(0x30);
					return 0;
				case DGPS:
					setIdOut(0x31);
					return 0;
				case SBAS:
					setIdOut(0x32);
					return 0;
				case EKFSTATUS:
					setIdOut(0x40);
					return 0;
				}

			case RXM:
				setClassOut(0x02);
				switch (rxm.valueOf(msgtype)) {
				case RAW:
					setIdOut(0x10);
					return 0;
				case SVSI:
					setIdOut(0x20);
					return 0;
				case SFRB:
					setIdOut(0x11);
					return 0;
				case ALM:
					setIdOut(0x30);
					return 0;
				case EPH:
					setIdOut(0x31);
					return 0;
				case POSREQ:
					setIdOut(0x40);
					return 0;
				}

			case INF:
				setClassOut(0x04);
				switch (inf.valueOf(msgtype)) {
				case ERROR:
					setIdOut(0x00);
					return 0;
				case WARNING:
					setIdOut(0x01);
					return 0;
				case NOTICE:
					setIdOut(0x02);
					return 0;
				case TEST:
					setIdOut(0x03);
					return 0;
				case DEBUG:
					setIdOut(0x04);
					return 0;
				case USER:
					setIdOut(0x07);
					return 0;
				}
			case ACK:
				setClassOut(0x05);
				switch (ack.valueOf(msgtype)) {
				case ACK:
					setIdOut(0x01);
					return 0;
				case NAK:
					setIdOut(0x00);
					return 0;
				}

			case CFG:
				setClassOut(06);
				switch (cfg.valueOf(msgtype)) {
				case PRT:
					setIdOut(0x00);
					return 0;
				case USB:
					setIdOut(0x1B);
					return 0;
				case MSG:
					setIdOut(0x01);
					return 0;
				case NMEA:
					setIdOut(0x17);
					return 0;
				case RATE:
					setIdOut(0x08);
					return 0;
				case CFG:
					setIdOut(0x09);
					return 0;
				case TP:
					setIdOut(0x07);
					return 0;
				case NAV2:
					setIdOut(0x1A);
					return 0;
				case DAT:
					setIdOut(0x06);
					return 0;
				case INF:
					setIdOut(0x02);
					return 0;
				case RST:
					setIdOut(0x04);
					return 0;
				case RXM:
					setIdOut(0x11);
					return 0;
				case ANT:
					setIdOut(0x13);
					return 0;
				case FXN:
					setIdOut(0x0E);
					return 0;
				case SBAS:
					setIdOut(0x16);
					return 0;
				case LIC:
					setIdOut(0x80);
					return 0;
				case TM:
					setIdOut(0x10);
					return 0;
				case TM2:
					setIdOut(0x19);
					return 0;
				case TMODE:
					setIdOut(0x1D);
					return 0;
				case EKF:
					setIdOut(0x12);
					return 0;
				}

			case UPD:
				setClassOut(0x09);
				switch (upd.valueOf(msgtype)) {
				case DOWNL:
					setIdOut(0x01);
					return 0;
				case UPLOAD:
					setIdOut(0x02);
					return 0;
				case EXEC:
					setIdOut(0x03);
					return 0;
				case MEMCPY:
					setIdOut(0x04);
					return 0;
				}

			case MON:
				setClassOut(0x0A);
				switch (mon.valueOf(msgtype)) {
				case SCHD:
					setIdOut(0x01);
					return 0;
				case IO:
					setIdOut(0x02);
					return 0;
				case MSGPP:
					setIdOut(0x06);
					return 0;
				case RXBUF:
					setIdOut(0x07);
					return 0;
				case TXBUF:
					setIdOut(0x08);
					return 0;
				case HW:
					setIdOut(0x09);
					return 0;
				case IPC:
					setIdOut(0x03);
					return 0;
				case USB:
					setIdOut(0x0A);
					return 0;
				case VER:
					setIdOut(0x04);
					return 0;
				case EXCEPT:
					setIdOut(0x05);
					return 0;
				}

			case AID:
				setClassOut(0x0B);
				switch (aid.valueOf(msgtype)) {
				case REQ:
					setIdOut(0x00);
					return 0;
				case DATA:
					setIdOut(0x10);
					return 0;
				case INI:
					setIdOut(0x01);
					return 0;
				case HUI:
					setIdOut(0x02);
					return 0;
				case ALM:
					setIdOut(0x30);
					return 0;
				case EPH:
					setIdOut(0x31);
					return 0;
				}

			case TIM:
				setClassOut(0x0D);
				switch (tim.valueOf(msgtype)) {
				case TM:
					setIdOut(0x02);
					return 0;
				case TM2:
					setIdOut(0x03);
					return 0;
				case TP:
					setIdOut(0x01);
					return 0;
				case SVIN:
					setIdOut(0x04);
					return 0;
				}
			case NMEA:
				setClassOut(0xF0);
				switch (nmea.valueOf(msgtype)) {
				case GGA:
					setIdOut(0x00);
					return 0;
				case GLL:
					setIdOut(0x01);
					return 0;
				case GSA:
					setIdOut(0x02);
					return 0;
				case GSV:
					setIdOut(0x03);
					return 0;
				case RMC:
					setIdOut(0x04);
					return 0;
				case VTG:
					setIdOut(0x05);
					return 0;
				case GRS:
					setIdOut(0x06);
					return 0;
				case GST:
					setIdOut(0x07);
					return 0;
				case ZDA:
					setIdOut(0x08);
					return 0;
				case GBS:
					setIdOut(0x09);
					return 0;
				case DTM:
					setIdOut(0x0A);
					return 0;
				}

			case PUBX:
				setClassOut(0xF1);
				switch (pubx.valueOf(msgtype)) {
				case A:
					setIdOut(0x00);
					return 0;
				case B:
					setIdOut(0x01);
					return 0;
				case C:
					setIdOut(0x03);
					return 0;
				case D:
					setIdOut(0x04);
					return 0;
				}
				// return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;

	}

}
