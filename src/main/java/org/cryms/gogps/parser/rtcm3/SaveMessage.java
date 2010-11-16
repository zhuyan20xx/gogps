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

public class SaveMessage implements Runnable {
	boolean started = true;
	RTCMClient device;
	Test server;
	private int sleepTime = 20;
	private Thread readThread;

	public SaveMessage(RTCMClient _device, Test _server) {
		device = _device;
		server = _server;
	}

	@Override
	public void run() {
		started = true;
		try {
			device.start();
		} catch (Exception e) {
		}
		while (started) {
			// going is used to detect if all devices are stopped
			boolean going = false;
			// for(int i = 0; i < devices.length; i++)
			// {
			// try {
			// All messages at available at current device are read!
			// if(device !=null) System.out.println("Not null");
			while (device != null && device.ready() > 0) {
				int[] msg = null;

				// try {
				// msg = device.readMessage();
				// } catch (BufferUnderrunException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				//server.newData(device.getDeviceId(), msg);
			}

			// } catch (BufferUnderrunException e) {
			// }
			// If just 1 device are going going will be true
			going = going || device == null || !device.stopped();

			if (!going) {
				started = false;
				server.reciverStopped();
			}

			// After each iteration the thread sleeps so it doesn't use all
			// available system resources
			else
				try {
					if (sleepTime > 0) {
						Thread.sleep(sleepTime);
					}
				} catch (Exception e) {
					// tester.exceptionPrint(e);
				}
			// loop is stopped if all devices has stopped recving data
			// Note: to ensure that the reciver loop can be stopped by mother
			// program, started is only set to true if it allready was true
			started = started && going;

		}

		device.stop();

	}

	public void setSleepTime(int time) {
		sleepTime = time;
	}

	public void start() {
		readThread = new Thread(this);
		readThread.start();
	}

	public void stop() {
		// When started is set to false, the data gathering loop finish the
		// current itteration and stops.
		started = false;
	}

	public boolean stopped() {
		boolean stopped = started;

		return stopped;
	}

}
