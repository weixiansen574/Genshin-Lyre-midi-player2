package top.weixiansen574.lyreplayer2;

import android.os.RemoteException;
import android.os.SystemClock;

public class CClicker extends Clicker {
    int trackingId = (int) (SystemClock.uptimeMillis() / 1000);
    String device;

    public CClicker(IShizukuInputService service, String device) {
        super(service);
        this.device = device;
    }

    @Override
    public void down(int x, int y) throws RemoteException {
        int index = putCoordinate(x, y);

        if (getCoordinateSize() == 1) {
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_SLOT, index);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_TRACKING_ID, trackingId++);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_POSITION_Y, y * 10);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_POSITION_X, x * 10);

            service.input(device, InputEvent.TYPE_EV_KEY, InputEvent.CODE_BTN_TOUCH, InputEvent.VALUE_DOWN);
            service.input(device, InputEvent.TYPE_EV_KEY, InputEvent.CODE_BTN_TOOL_FINGER, InputEvent.VALUE_DOWN);
            service.input(device, InputEvent.TYPE_EV_SYN, InputEvent.CODE_SYN_REPORT, 0);
        } else {
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_SLOT, index);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_TRACKING_ID, trackingId++);

            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_POSITION_Y, y * 10);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_POSITION_X, x * 10);
            service.input(device, InputEvent.TYPE_EV_SYN, InputEvent.CODE_SYN_REPORT, 0);
        }
        System.out.println("down x:" + x+ " y:" + y + " index:" + index);
    }

    @Override
    public void up(int x, int y) throws RemoteException {

        int index = removeCoordinate(x, y);

        if (getCoordinateSize() == 0) {
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_SLOT, index);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_TRACKING_ID, -1);
            service.input(device, InputEvent.TYPE_EV_KEY, InputEvent.CODE_BTN_TOUCH, InputEvent.VALUE_UP);
            service.input(device, InputEvent.TYPE_EV_KEY, InputEvent.CODE_BTN_TOOL_FINGER, InputEvent.VALUE_UP);
            service.input(device, InputEvent.TYPE_EV_SYN, InputEvent.CODE_SYN_REPORT, 0);
        } else {
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_SLOT, index);
            service.input(device, InputEvent.TYPE_EV_ABS, InputEvent.CODE_ABS_MT_TRACKING_ID, -1);
            service.input(device, InputEvent.TYPE_EV_SYN, InputEvent.CODE_SYN_REPORT, 0);
        }
        System.out.println("up x:" + x+ " y:" + y + " index:" + index);
    }

    public int putCoordinate(int x, int y) {
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            if (coordinate == null) {
                coordinates[i] = new Coordinate(x, y);
                return i;
            }
        }
        throw new RuntimeException("Too many touches");
    }

}
