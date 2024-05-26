package top.weixiansen574.lyreplayer2;

import android.os.RemoteException;
import android.os.SystemClock;
import android.view.MotionEvent;

public class WClicker extends Clicker {
    public WClicker(IShizukuInputService service) {
        super(service);
    }

    @Override
    public void down(int x, int y) throws RemoteException {
        int index = putCoordinate(x, y);
        int size = getCoordinateSize();

        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[size];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[size];

        int i = 0;
        for (int id = 0; id < coordinates.length; id++) {
            if (coordinates[id] != null){
                pointerProperties[i] = new MotionEvent.PointerProperties();
                pointerProperties[i].id = id;
                pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
                pointerCoords[i] = new MotionEvent.PointerCoords();
                pointerCoords[i].x = coordinates[id].x;
                pointerCoords[i].y = coordinates[id].y;
                i++;
            }
        }

        if (size == 1){
            //MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0);
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,1,
                    pointerProperties,pointerCoords,0, 0,1.0f,
                    1.0f,8,0,0x1002,0x200802);
            sendPointerSync(motionEvent);
        } else {
            //     index    action
            //   |--------|--------|
            //    00000001 00000101   261   ACTION_POINTER_DOWN(1)
            int action = (index << 8) | MotionEvent.ACTION_POINTER_DOWN;
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    action,size,
                    pointerProperties,pointerCoords,0, 0,1.0f,
                    1.0f,8,0,0x1002,0x200802);

            sendPointerSync(motionEvent);
        }
        //System.out.println("down "+x+" "+y);
    }

    @Override
    public void up(int x, int y) throws RemoteException {

        int size = getCoordinateSize();
        int index = -1;
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[size];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[size];

        int i = 0;
        for (int id = 0; id < coordinates.length; id++) {
            if (coordinates[id] != null) {
                pointerProperties[i] = new MotionEvent.PointerProperties();
                pointerProperties[i].id = id;
                pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
                pointerCoords[i] = new MotionEvent.PointerCoords();
                pointerCoords[i].x = coordinates[id].x;
                pointerCoords[i].y = coordinates[id].y;
                if (x == coordinates[id].x && y == coordinates[id].y) {
                    index = i;
                }
                i++;
            }
        }
        if (index == -1){
            throw new TouchNotFoundException("up coordinate not found");
        }
        if (size == 1){
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,1,
                    pointerProperties,pointerCoords,0, 0,1.0f,
                    1.0f,8,0,0x1002,0x200802);
            sendPointerSync(motionEvent);
        } else {
            int action = (index << 8) | MotionEvent.ACTION_POINTER_UP;
            MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    action,size,
                    pointerProperties,pointerCoords,0, 0,1.0f,
                    1.0f,8,0,0x1002,0x200802);
            sendPointerSync(motionEvent);
        }
        removeCoordinate(x,y);
    }

    private void sendPointerSync(MotionEvent event) throws RemoteException {
        service.injectInputEvent(event);
        System.out.println(event);
    }

}
