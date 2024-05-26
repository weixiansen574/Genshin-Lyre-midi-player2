// IShizukuInputService.aidl
package top.weixiansen574.lyreplayer2;

// Declare any non-default types here with import statements

interface IShizukuInputService {
    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    void injectInputEvent(in MotionEvent event) = 10;

    void sendPointerSync(in MotionEvent event) = 20;

    void input(String device,int type,int code,int value) = 30;
}