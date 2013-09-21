package uk.org.baverstock.cgiadbremote;

import com.android.ddmlib.IDevice;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;

public class MiscUtils {
    public static IDevice deviceFromSerial(String serial, AndroidDebugBridgeWrapper bridge) {
        for (IDevice iDevice : bridge.getDevices()) {
            if(iDevice.getSerialNumber().equals(serial)) {
                return iDevice;
            }
        }
        throw new RuntimeException("No device connected with serial number '" + serial + "'");
    }

    public static IDevice getDevice(NanoHTTPD.IHTTPSession session, AndroidDebugBridgeWrapper bridge) {
        String serial = session.getParms().get(CgiAdbRemote.PARAM_SERIAL);
        return deviceFromSerial(serial, bridge);
    }

    public static NanoHTTPD.Response getResponseForThrowable(Throwable t) {
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", out.toString());
    }
}
