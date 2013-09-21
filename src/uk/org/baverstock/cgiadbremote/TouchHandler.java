package uk.org.baverstock.cgiadbremote;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import fi.iki.elonen.NanoHTTPD;

import java.net.SocketException;

/**
 * Sends taps to the phone, via ChimpChat
 */

public class TouchHandler implements PathHandler {

    private DeviceConnectionMap deviceConnectionMap;

    public TouchHandler(DeviceConnectionMap deviceConnectionMap) {
        this.deviceConnectionMap = deviceConnectionMap;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String[] param = {"up", "move", "down"};
        TouchPressType[] type = {TouchPressType.UP, TouchPressType.MOVE, TouchPressType.DOWN};
        Point coord = null;
        TouchPressType touch = TouchPressType.DOWN_AND_UP;
        for (int i = 0; i < param.length && coord == null; ++i) {
            coord = Point.fromString(session.getParms().get(param[i]));
            touch = type[i];
        }
        String serial = session.getParms().get(CgiAdbRemote.PARAM_SERIAL);
        Throwable lastChance = null;
        for (int retry = 3; retry > 0; --retry) {
            IChimpDevice iChimpDevice = serial == null ? null : deviceConnectionMap.getDeviceBySerial(serial);
            if (coord == null || iChimpDevice == null) {
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.CONFLICT, null, "");
            }
            try {
                synchronized (iChimpDevice) {
                    iChimpDevice.touch(coord.x, coord.y, touch);
                }
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NO_CONTENT, null, "");
            }
            catch (Exception e) {
                System.err.println(String.format("Retry %d for %s", retry, e.toString()));
                lastChance = e;
            }
            finally {
                deviceConnectionMap.resetDeviceOfSerial(serial);
            }
        }
        return MiscUtils.getResponseForThrowable(lastChance);
    }
}
