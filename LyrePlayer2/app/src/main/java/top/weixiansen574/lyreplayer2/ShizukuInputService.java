package top.weixiansen574.lyreplayer2;

import android.app.Instrumentation;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShizukuInputService extends IShizukuInputService.Stub {

    InputManager inputManager;
    Method injectInputEvent = InputManager.class.getDeclaredMethod("injectInputEvent", InputEvent.class, int.class);
    //不建议使用这个类，因为会导致输入事件被延迟
    Instrumentation instrumentation = new Instrumentation();

    static {
        System.loadLibrary("sendevent-jni");
    }

    public ShizukuInputService() throws NoSuchMethodException {
        try {
            Method method = InputManager.class.getDeclaredMethod("getInstance");
            inputManager = (InputManager) method.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void destroy() throws RemoteException {
        exit();
    }

    @Override
    public void exit() throws RemoteException {
        System.exit(0);
    }

    @Override
    public void injectInputEvent(MotionEvent event) throws RemoteException {
      //  Log.i("ShizukuInputService", event.toString());
        //long currentTimeMillis = System.currentTimeMillis();
        try {
            injectInputEvent.invoke(inputManager, event, 0);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
      //  Log.i("sendPointerSync",(System.currentTimeMillis() - currentTimeMillis) + " ms");
    }

    //sendPointerSync
    @Override
    public void sendPointerSync(MotionEvent event) throws RemoteException {
        instrumentation.sendPointerSync(event);
    }

    public void input(String device,short type,short code,int value){

        try (FileOutputStream outputStream = new FileOutputStream(device)) {
            Log.i("sendevent",Integer.toHexString(type) + " " + Integer.toHexString(code) + " " + Integer.toHexString(value));
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(0);
            buffer.putShort(type);
            buffer.putShort(code);
            buffer.putInt(value);

            byte[] event = buffer.array();

            outputStream.write(event);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("sendevent",e.toString());
            System.exit(1);
        }
    }


    //需要root权限，并且屏幕方向固定为竖屏，自行处理屏幕方向问题
    @Override
    public void input(String device, int type, int code, int value) throws RemoteException {
        Log.i("sendevent", String.valueOf(sendEventJNI(device,type,code,value)));
    }

    public native int sendEventJNI(String device, int type, int code, int value);

}