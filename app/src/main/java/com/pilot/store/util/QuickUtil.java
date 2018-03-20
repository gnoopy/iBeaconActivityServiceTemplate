package com.pilot.store.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class QuickUtil {
	final public static boolean DEBUG=true;
	@SuppressLint("SimpleDateFormat")
	static public String getMMddHHmmssSSS(long timemilis) {
		String ret;
	    SimpleDateFormat df = new SimpleDateFormat("MMdd-HH:mm:ss.SSS");
	    ret = df.format(timemilis);

	    return ret;
	}

    public static String getMMddHHmmss() {
        long timemilis= System.currentTimeMillis();
        String ret;
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
        ret = df.format(timemilis);

        return ret;
    }
    public static String getMMddHHmm() {
        long timemilis= System.currentTimeMillis();
        String ret;
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmm");
        ret = df.format(timemilis);

        return ret;
    }
	
	public static long trimTs(long ts, int unit) {
		long nTs=0;
		nTs=ts/unit*unit/10000000;
		return nTs;
	}

	public static long trimTs2Msec(long ts, int unit) {
		long nTs=0;
		nTs=ts/unit*unit;
		return nTs;
	}
	
	protected static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}


	
	static public void giveMeAsec(long asec) {
		try {
			Thread.sleep(asec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    static public Date parseDate(String dateStr) {
        SimpleDateFormat df = new SimpleDateFormat("MMddHHmm", Locale.ENGLISH);
        Date result = null;
        try {
            result = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String padIntToString(int number, int digits) {
        return String.format("%0"+digits+"d", number);
    }


}
