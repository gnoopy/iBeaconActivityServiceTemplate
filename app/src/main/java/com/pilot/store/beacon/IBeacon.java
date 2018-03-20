/**
 * Radius Networks, Inc.
 * http://www.radiusnetworks.com
 * 
 * @author David G. Young
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.pilot.store.beacon;

import android.util.Log;

import java.io.Serializable;
import java.util.HashSet;


public class IBeacon implements Serializable {

	public static HashSet<IBeacon> db = new HashSet<>();
	public static void insertBeacon(IBeacon d) {
		db.add(d);
	}
	public static void deleteBeacon(String uuid){
		boolean b = db.removeIf(s -> s.getProximityUuid().equalsIgnoreCase(uuid));
	}
	public static HashSet<IBeacon> getFoundBeacons(){
		return IBeacon.db;
	}

	public static final int PROXIMITY_IMMEDIATE = 1;
	public static final int PROXIMITY_NEAR = 2;
	public static final int PROXIMITY_FAR = 3;
	public static final int PROXIMITY_UNKNOWN = 0;

    final private static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private static final String TAG = "IBeacon";
	
	protected String proximityUuid;
	protected int major;
	protected int minor;
	protected Integer proximity;
	protected Double accuracy;
	protected int rssi;
	protected int txPower;
	
	protected Double runningAverageRssi = null;
	
	public double getAccuracy() {
		if (accuracy == null) {
			accuracy = calculateAccuracy(txPower, runningAverageRssi != null ? runningAverageRssi : rssi );		
		}
		return accuracy;
	}
	public int getMajor() {
		return major;
	}
	public int getMinor() {
		return minor;
	}
	public int getProximity() {
		if (proximity == null) {
			proximity = calculateProximity(getAccuracy());		
		}
		return proximity;		
	}
	public int getRssi() {
		return rssi;
	}
	public int getTxPower() {
		return txPower;
	}

	public String getProximityUuid() {
		return proximityUuid;
	}
	
	
	@Override
	public int hashCode() {
		return minor;
	}
	
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof IBeacon)) {
			return false;
		}
		IBeacon thatIBeacon = (IBeacon) that;		
		return (thatIBeacon.getMajor() == this.getMajor() && thatIBeacon.getMinor() == this.getMinor() && thatIBeacon.getProximityUuid() == thatIBeacon.getProximityUuid());
	}

	@Override
	public String toString(){
		return "uuid:+"+proximityUuid+", major:"+major+", minor:"+minor+", rssi:"+rssi;
	}

	public static IBeacon fromScanData(byte[] scanData, int rssi) {

		
		if (((int)scanData[5] & 0xff) == 0x4c &&
			((int)scanData[6] & 0xff) == 0x00 &&
			((int)scanData[7] & 0xff) == 0x02 &&
			((int)scanData[8] & 0xff) == 0x15) {			
			// yes!  This is an iBeacon		
		}

		
		
		else if (((int)scanData[5] & 0xff) == 0x2d &&
				((int)scanData[6] & 0xff) == 0x24 &&
				((int)scanData[7] & 0xff) == 0xbf &&
				((int)scanData[8] & 0xff) == 0x16) {	
			// this is an Estimote beacon
			IBeacon iBeacon = new IBeacon();
			iBeacon.major = 0;
			iBeacon.minor = 0;
			iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
			iBeacon.txPower = -55;
			return iBeacon;
		}		
		else {
			return null;
		}
								
		IBeacon iBeacon = new IBeacon();
		
		iBeacon.major = (scanData[25] & 0xff) * 0x100 + (scanData[26] & 0xff);
		iBeacon.minor = (scanData[27] & 0xff) * 0x100 + (scanData[28] & 0xff);
		iBeacon.txPower = (int)scanData[29]; // this one is signed
		iBeacon.rssi = rssi;
				
		// AirLocate:
		// 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
		// e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
		// 00 00 # major 
		// 00 00 # minor 
		// c5 # The 2's complement of the calibrated Tx Power
		// Estimote:		
		// 02 01 1a 11 07 2d 24 bf 16 
		// 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000
		
		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanData, 9, proximityUuidBytes, 0, 16);
		String hexString = bytesToHex(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0,8));
		sb.append("-");
		sb.append(hexString.substring(8,12));
		sb.append("-");
		sb.append(hexString.substring(12,16));
		sb.append("-");
		sb.append(hexString.substring(16,20));
		sb.append("-");
		sb.append(hexString.substring(20,32));
		iBeacon.proximityUuid = sb.toString();

		return iBeacon;
	}

	protected IBeacon(IBeacon otherIBeacon) {
		this.major = otherIBeacon.major;
		this.minor = otherIBeacon.minor;
		this.accuracy = otherIBeacon.accuracy;
		this.proximity = otherIBeacon.proximity;
		this.rssi = otherIBeacon.rssi;
		this.proximityUuid = otherIBeacon.proximityUuid;
		this.txPower = otherIBeacon.txPower;
	}
	
	protected IBeacon() {
		
	}
	
	protected static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}
		
		Log.d(TAG, "calculating accuracy based on rssi of "+rssi);


		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			double accuracy =  (0.89976)* Math.pow(ratio,7.7095) + 0.111;
			Log.d(TAG, " avg rssi: "+rssi+" accuracy: "+accuracy);
			return accuracy;
		}
	}	
	
	protected static int calculateProximity(double accuracy) {
		if (accuracy < 0) {
			return PROXIMITY_UNKNOWN;	 
		}
		if (accuracy < 0.5 ) {
			return IBeacon.PROXIMITY_IMMEDIATE;
		}
		if (accuracy <= 4.0) {
			return IBeacon.PROXIMITY_NEAR;
		}
		return IBeacon.PROXIMITY_FAR;

	}

	private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

	static public boolean isGateBeacon(String uuidStr) {
		//TODO: 1. define any uuid of your iBeacon to check if it is scanned
		if (uuidStr.equalsIgnoreCase("43cbda6e-28fa-4f5b-af12-416caf3e3737") || uuidStr.equalsIgnoreCase("cddebf7f-b06b-b504-8697-09be31530126"))
			return true;
		return false;
	}

	static public String getIbeaconUuidFromScannedData(byte [] scanRecord) {
		String uuidStr="";
		for (int i = 0; i < scanRecord.length; i++) {
			if ( i>=9 && i<=24) {
				if (i==13 || i==15 || i==17 || i==19)
					uuidStr+="-";
				uuidStr+= String.format("%02X", scanRecord[i]);
			}
		}
		return uuidStr;
	}
}
