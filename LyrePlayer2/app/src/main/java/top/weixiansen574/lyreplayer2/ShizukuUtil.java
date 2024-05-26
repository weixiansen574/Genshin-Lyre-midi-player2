package top.weixiansen574.lyreplayer2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import rikka.shizuku.Shizuku;

public class ShizukuUtil {
    private static IShizukuInputService service;
    private static Connection connection;
    private static Shizuku.UserServiceArgs userServiceArgs;
    //private static

    public synchronized static void getOrBindService(Context context,OnBindListener listener){
        if (service == null){
            userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(context, ShizukuInputService.class))
                    .daemon(false)
                    .processNameSuffix(":ShizukuInputService")
                    .debuggable(false)
                    .version(1);
            connection = new Connection(listener);
            Shizuku.bindUserService(userServiceArgs, connection);
        } else {
            listener.onBind(service);
        }
    }

    public static synchronized void unbindService(){
        if (service == null) {
            Shizuku.unbindUserService(userServiceArgs, connection, true);
        }
    }


    interface OnBindListener{
        void onBind(IShizukuInputService service);
    }
    public static class Connection implements ServiceConnection {

        public OnBindListener listener;

        public Connection(OnBindListener listener) {
            this.listener = listener;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IShizukuInputService iShizukuInputService = IShizukuInputService.Stub.asInterface(service);
            ShizukuUtil.service = iShizukuInputService;
            listener.onBind(iShizukuInputService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ShizukuUtil.service = null;
            ShizukuUtil.connection = null;
        }
    }

    public static void bindShizukuAndStartService(Activity activity, Intent intent,boolean stopStated){

        if (stopStated){
            activity.stopService(intent);
        }

        if (!Shizuku.pingBinder()){
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.启动失败)
                    .setMessage(R.string.Shizuku服务未启动或未安装)
                    .setNeutralButton(R.string.安装shizuku, (dialog, which) -> {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setData(Uri.parse("https://github.com/RikkaApps/Shizuku/releases/latest"));
                        activity.startActivity(intent1);
                    })
                    .setNegativeButton(R.string.打开shizuku,(dialog, which) -> {
                        Intent intent1 = new Intent();
                        intent1.setComponent(new ComponentName("moe.shizuku.privileged.api","moe.shizuku.manager.MainActivity"));
                        activity.startActivity(intent1);
                    })
                    .setPositiveButton(R.string.close,null)
                    .show();
            return;
        }

        if (!checkPermission(1)) {
            Toast.makeText(activity, R.string.未获得shizuku权限_请到管理器启用本应用, Toast.LENGTH_SHORT).show();
        } else {
            //如果服务未启动才显示等待对话框，不然会造成性能浪费
            ProgressDialog dialog;
            if (service == null) {
                dialog = new ProgressDialog(activity);
                dialog.setMessage(activity.getString(R.string.正在连接shizuku服务));
                dialog.setCancelable(false);
                dialog.show();
            } else {
                dialog = null;
            }
            getOrBindService(activity, service -> {
                //Toast.makeText(activity, "服务已连接", Toast.LENGTH_SHORT).show();
                if (dialog != null){
                    dialog.dismiss();
                }
                activity.startService(intent);
            });
        }
    }

    public static boolean checkPermission(int code) {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            return true;
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            return false;
        } else {
            // Request the permission
            Shizuku.requestPermission(code);
            return false;
        }
    }
}
