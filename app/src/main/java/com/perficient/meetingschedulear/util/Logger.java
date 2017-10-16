package com.perficient.meetingschedulear.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;


public class Logger {
    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final char I = 'I', W = 'W', D = 'D', E = 'E', V = 'V', A = 'A', M = 'M';
    private static final String TAG = "Cedars-Sinai";
    /**
     * Print the log or not
     */
    public static boolean isDebug = true;
    private static String LINE_SEPARATOR = System.getProperty("line.separator");
    @SuppressWarnings("FieldCanBeLocal")
    private static int JSON_INDENT = 4;

    private Logger() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void m(Map map) {
        Set set = map.entrySet();
        if (set.size() < 1) {
            printLog(D, "[]");
            return;
        }

        int i = 0;
        String[] s = new String[set.size()];
        for (Object aSet : set) {
            Map.Entry entry = (Map.Entry) aSet;
            s[i] = entry.getKey() + " = " + entry.getValue() + ",\n";
            i++;
        }
        printLog(V, s);
    }

    public static void j(String jsonStr) {
        if (isDebug) {
            String message;
            try {
                if (jsonStr.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    message = jsonObject.toString(JSON_INDENT);
                } else if (jsonStr.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    message = jsonArray.toString(JSON_INDENT);
                } else {
                    message = jsonStr;
                }
            } catch (JSONException e) {
                message = jsonStr;
            }

            message = LINE_SEPARATOR + message;
            String[] lines = message.split(LINE_SEPARATOR);
            StringBuilder sb = new StringBuilder();
            printLog(D, lines);
        }
    }

    public static void i(String... msg) {
        if (isDebug)
            printLog(I, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            printLog(D, msg);
    }

    public static void w(String... msg) {
        if (isDebug)
            printLog(W, msg);
    }

    public static void e(String... msg) {
        if (isDebug)
            printLog(E, msg);
    }

    public static void v(String... msg) {
        if (isDebug)
            printLog(V, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.i(tag, msg, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.d(tag, msg, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.e(tag, msg, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.v(tag, msg, tr);
    }

    private static void printHunk(char type, String str) {
        switch (type) {
            case I:
                Log.i(TAG, str);
                break;
            case D:
                Log.d(TAG, str);
                break;
            case E:
                Log.e(TAG, str);
                break;
            case V:
                Log.v(TAG, str);
                break;
            case A:
                Log.wtf(TAG, str);
                break;
            case W:
                Log.w(TAG, str);
                break;
        }
    }

    private static void printHead(char type) {
        printHunk(type, TOP_BORDER);
        printHunk(type, HORIZONTAL_DOUBLE_LINE + "   Thread:");
        printHunk(type, HORIZONTAL_DOUBLE_LINE + "   " + Thread.currentThread().getName());
        printHunk(type, MIDDLE_BORDER);
    }

    private static void printLocation(char type, String... msg) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int i = 0;
        for (StackTraceElement e : stack) {
            String name = e.getClassName();
            if (!name.equals(Logger.class.getName())) {
                i++;
            } else {
                break;
            }
        }
        i += 3;
        String className = stack[i].getFileName();
        String methodName = stack[i].getMethodName();
        int lineNumber = stack[i].getLineNumber();
        StringBuilder sb = new StringBuilder();
        printHunk(type, HORIZONTAL_DOUBLE_LINE + "   Location:");
        sb.append(HORIZONTAL_DOUBLE_LINE)
                .append("   (").append(className).append(":").append(lineNumber).append(")# ").append(methodName);
        printHunk(type, sb.toString());
        printHunk(type, msg == null || msg.length == 0 ? BOTTOM_BORDER : MIDDLE_BORDER);
    }

    private static void printMsg(char type, String... msg) {
        printHunk(type, HORIZONTAL_DOUBLE_LINE + "   Message:");
        for (String str : msg) {
            printHunk(type, HORIZONTAL_DOUBLE_LINE + "   " + str);
        }
        printHunk(type, BOTTOM_BORDER);
    }

    private static void printLog(char type, String... msg) {
        printHead(type);
        printLocation(type, msg);
        if (msg == null || msg.length == 0) {
            return;
        }
        printMsg(type, msg);
    }

}