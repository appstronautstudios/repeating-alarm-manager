package com.appstronautstudios.repeatingalarmmanagerdemo.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String getNumberString(double number, int sigFigs, boolean signed) {
        DecimalFormatSymbols DFS = new DecimalFormatSymbols();
        DFS.setDecimalSeparator('.');
        DecimalFormat myFormatter;

        switch (sigFigs) {
            default:
            case 0: {
                myFormatter = new DecimalFormat("#");
                break;
            }
            case 1: {
                myFormatter = new DecimalFormat("#.#");
                break;
            }
            case 2: {
                myFormatter = new DecimalFormat("#.##");
                break;
            }
            case 3: {
                myFormatter = new DecimalFormat("#.###");
                break;
            }
        }
        myFormatter.setDecimalFormatSymbols(DFS);
        if (signed) {
            String sign = "-";
            if (number > 0) {
                sign = "+";
            } else {
                sign = "";
            }
            return sign + myFormatter.format(number);
        } else {
            return myFormatter.format(number);
        }
    }

    public static String timestampToReadableDateString(long timeStamp) {
        Date date = new Date(timeStamp);
        String outDate = null;
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM dd, yyyy");
            outDate = fmtOut.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outDate;
    }

    public static String timestampToReadableTimeString(long timeStamp) {
        Date date = new Date(timeStamp);
        String outDate = null;
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat("h:mm aa");
            outDate = fmtOut.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outDate;
    }

    public static String timestampToCsvDate(long timestamp) {
        Date date = new Date(timestamp);
        String outDate = null;
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            outDate = fmtOut.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outDate;
    }

    public static Date csvDateToDateObject(String dateString) {
        Date outDate = null;
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            outDate = fmtOut.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outDate;
    }
}