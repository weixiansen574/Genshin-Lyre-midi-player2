package top.weixiansen574.lyreplayer2;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.sound.midi.InvalidMidiDataException;

import rikka.shizuku.Shizuku;
import top.weixiansen574.lyreplayer2.midi.MidiProcessor;
import top.weixiansen574.lyreplayer2.midi.Note;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, ServiceConnection {
    public static final int REQUEST_CODE_OPEN_FILE = 1;

    Button openMidiFile;
    Button openFloatList;
    Button selectFromServer;
    SharedPreferences server;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.openMidiFile = this.findViewById(R.id.open_midi_file);
        this.openMidiFile.setOnClickListener(this);
        test();
        server = getSharedPreferences("server", Context.MODE_PRIVATE);
        openFloatList = findViewById(R.id.open_float_list);
        openFloatList.setOnClickListener(this);
        selectFromServer = findViewById(R.id.select_from_server);
        selectFromServer.setOnLongClickListener(this);
        selectFromServer.setEnabled(false);

        selectFromServer.setOnClickListener(this);
        Intent intent0 = getIntent();
        //当使用文件管理器、QQ等第三方应用打开文件时，直接将URI传给下一activity
        if (intent0.getAction() != null && intent0.getAction().equals("android.intent.action.VIEW")) {
            Uri uri = intent0.getData();
            Intent intent = new Intent(this, AdjustAndStartActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }

        //获取当前语言
        String language = Locale.getDefault().getLanguage();
        //判断是否是日语
        if (language.equals("ja")) {
            Toast.makeText(this, "既然你们对文字狱不喊不骂，那就河蟹你全家！", Toast.LENGTH_LONG).show();
        }

        /*if (!checkPermission(1)) {
            Toast.makeText(this, "没有shizuku权限", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在绑定shizuku", Toast.LENGTH_SHORT).show();
        System.out.println(Shizuku.getUid());
        System.out.println(Shizuku.getVersion());*/



       // bindShizuku();



    }

    private void bindShizuku(){
        Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(this, ShizukuInputService.class))
                .daemon(false)
                .processNameSuffix(":ShizukuInputService")
                .debuggable(false)
                .version(1);
        Shizuku.bindUserService(userServiceArgs, this);
    }


    private void test() {


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.about){
            startActivity(new Intent(this, AboutActivity2.class));
        }
        return true;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == 0) return;
                Toast.makeText(this, getString(R.string.qxsqsb), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.open_midi_file){//打开midi文件按钮
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/midi");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
        } else if (id == R.id.open_float_list){
            startActivity(new Intent(this, FloatListActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                this.uri = uri;
                Intent intent = new Intent(this, AdjustAndStartActivity.class);
                intent.setData(uri);
                startActivity(intent);
                //Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //手动设置服务器地址
    @Override
    public boolean onLongClick(View v) {
        View view = View.inflate(MainActivity.this, R.layout.edit_text, null);
        final EditText editText = view.findViewById(R.id.edit_text);
        editText.setText(server.getString("address", "lyre-player.weixiansen574.top:1180"));
        AlertDialog setServerDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("服务器地址")
                .setMessage("仅开发者调试用，请不要乱改！")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        server.edit().putString("address", editText.getText().toString()).apply();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("默认",null).show();
        setServerDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("lyre-player.weixiansen574.top:1180");
            }
        });
        return true;
    }

    private boolean checkPermission(int code) {
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

    //这是测试远程shizuku控制的，与本程序功能无关，只是不想开新项目来做，索性直接放这里了，感兴趣的自己琢磨
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        IShizukuInputService iShizukuInputService = IShizukuInputService.Stub.asInterface(service);
        BlockingDeque<MotionEvent> events = new LinkedBlockingDeque<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    ServerSocket serverSocket = new ServerSocket(8888);
                    Socket socket = serverSocket.accept();

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, socket.getInetAddress()+" 已连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                    while (true) {

                        int action = dataInputStream.readInt();
                        int pointerCount = dataInputStream.readInt();
                        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[pointerCount];
                        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];

                        for (int i = 0; i < pointerCount; i++) {
                            pointerProperties[i] = new MotionEvent.PointerProperties();
                            pointerProperties[i].id = dataInputStream.readByte();
                            pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
                            pointerCoords[i] = new MotionEvent.PointerCoords();
                            pointerCoords[i].x = dataInputStream.readFloat();
                            pointerCoords[i].y = dataInputStream.readFloat();
                        }
                        MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                action,pointerCount,
                                pointerProperties,pointerCoords,0,
                                0,1.0f,1.0f,0,0,0x1002,0x200802);
                        events.add(motionEvent);
                    }


                    /*int x1 = 1350;
                    int y1 = 1020;

                    int x2 = 1600;
                    int y2 = 1020;*/

                   // String device = "/dev/input/event7";
                    /*iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_SLOT,0);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_TRACKING_ID,0);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_POSITION_X,6710);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_POSITION_Y,20920);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_KEY,InputEvent.CODE_BTN_TOUCH,InputEvent.VALUE_DOWN);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_KEY,InputEvent.CODE_BTN_TOOL_FINGER,InputEvent.VALUE_DOWN);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_SYN,InputEvent.CODE_SYN_REPORT,0);
                    Thread.sleep(1000);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_SLOT,1);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_TRACKING_ID,1);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_POSITION_X,8000);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_POSITION_Y,20920);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_SYN,InputEvent.CODE_SYN_REPORT,0);
                    Thread.sleep(1000);

                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_SLOT,0);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_TRACKING_ID,-1);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_SYN,InputEvent.CODE_SYN_REPORT,0);

                    Thread.sleep(1000);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_SLOT,0);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_ABS,InputEvent.CODE_ABS_MT_TRACKING_ID,-1);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_KEY,InputEvent.CODE_BTN_TOUCH,InputEvent.VALUE_UP);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_KEY,InputEvent.CODE_BTN_TOOL_FINGER,InputEvent.VALUE_UP);
                    iShizukuInputService.input(device,InputEvent.TYPE_EV_SYN,InputEvent.CODE_SYN_REPORT,0);*/

                    /*int centerOfCircle_X = 610;
                    int centerOfCircle_Y = 1356;
                    int radius = 200;

                    String command =
                            "/dev/input/event7 3 57 62442\n" +
                            "/dev/input/event7 3 53 5500\n" +
                            "/dev/input/event7 3 54 6000\n" +
                            "/dev/input/event7 1 330 1\n" +
                            "/dev/input/event7 1 325 1\n" +
                            "/dev/input/event7 0 0 0\n";
                    String command2 =
                            "/dev/input/event7 3 57 -1\n" +
                            "/dev/input/event7 0 0 0\n";

                    String[] line = command.split("\n");
                    String[] line2 = command2.split("\n");





                    for (String l : line) {
                        String[] event = l.split(" ");
                        if (event.length == 4) {
                            iShizukuInputService.input(event[0], Integer.parseInt(event[1]), Integer.parseInt(event[2]),Integer.parseInt(event[3]));
                        }
                    }

                    //画圆
                    for (int i = 0; i < 1000; i++) {
                        iShizukuInputService.input("/dev/input/event7",3,53, (int) (10 * (radius * Math.sin(i/10.0) + centerOfCircle_X)));
                        iShizukuInputService.input("/dev/input/event7",3,54,(int) (10 * (radius * Math.cos(i/10.0) + centerOfCircle_Y)));
                        iShizukuInputService.input("/dev/input/event7",0,0,0);
                        //Thread.sleep(10);
                    }

                    for (String l : line2) {
                        String[] event = l.split(" ");
                        if (event.length == 4) {
                            iShizukuInputService.input(event[0], Integer.parseInt(event[1]), Integer.parseInt(event[2]),Integer.parseInt(event[3]));
                        }
                    }*/



                    /*iShizukuInputService.input("/dev/input/event7", 3, 57, 62442);
                    iShizukuInputService.input("/dev/input/event7", 3, 53, 1437);
                    iShizukuInputService.input("/dev/input/event7", 3, 54, 21374);
                    iShizukuInputService.input("/dev/input/event7", 1, 330, 1);
                    iShizukuInputService.input("/dev/input/event7", 1, 325, 1);
                    iShizukuInputService.input("/dev/input/event7", 0, 0, 0);
                    iShizukuInputService.input("/dev/input/event7", 3, 53, 2007);
                    iShizukuInputService.input("/dev/input/event7", 3, 54, 21374);
                    iShizukuInputService.input("/dev/input/event7", 0, 0, 0);
                    iShizukuInputService.input("/dev/input/event7", 3, 57, -1);
                    iShizukuInputService.input("/dev/input/event7", 0, 0, 0);*/
                    /*int x1 = 400;
                    int y1 = 800;

                    int x2 = 500;
                    int y2 = 800;

                    int x3 = 600;
                    int y3 = 800;

                    Thread.sleep(3000);
                    Clicker clicker = new CClicker(iShizukuInputService,device);
                    //Clicker clicker = new IClicker(iShizukuInputService);
                    clicker.down(x1, y1);
                    Thread.sleep(1000);
                    clicker.down(x2, y2);
                    Thread.sleep(1000);
                    clicker.down(x3, y3);

                    Thread.sleep(1000);
                    clicker.up(x2, y2);

                    Thread.sleep(1000);
                    clicker.up(x3, y3);

                    Thread.sleep(1000);
                    clicker.up(x1, y1);
*/

                    /*int centerOfCircle_X = 610;
                    int centerOfCircle_Y = 1356;
                    int radius = 200;
                    MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
                    MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
                    pointerProperties[0] = new MotionEvent.PointerProperties();
                    pointerProperties[0].id = 0;
                    pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
                    pointerCoords[0] = new MotionEvent.PointerCoords();
                    pointerCoords[0].x = centerOfCircle_X;
                    pointerCoords[0].y = centerOfCircle_Y;
                    MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN,1,
                            pointerProperties,pointerCoords,0, 0,1.0f,
                            1.0f,8,0,0x1002,0x200802);
                    iShizukuInputService.sendPointerSync(motionEvent);

                    for (int i = 0; i < 10000; i++) {
                        pointerProperties[0].id = 0;
                        pointerCoords[0].x = (float) (radius * Math.sin(i/10.0) + centerOfCircle_X);
                        pointerCoords[0].y = (float) (radius * Math.cos(i/10.0) + centerOfCircle_Y);
                        MotionEvent motionEvent1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_MOVE,1,
                                pointerProperties,pointerCoords,0, 0,1.0f,
                                1.0f,8,0,0x1002,0x200802);
                        iShizukuInputService.sendPointerSync(motionEvent1);

                    }
*/


/*
                    Thread.sleep(3000);
                    MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
                    pointerProperties[0] = new MotionEvent.PointerProperties();
                    pointerProperties[0].id = 0;
                    pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
                    MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
                    pointerCoords[0] = new MotionEvent.PointerCoords();
                    pointerCoords[0].x = x1;
                    pointerCoords[0].y = y1;
                    MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN,1,
                            pointerProperties,pointerCoords,0,
                            0,1.0f,1.0f,0,0,0x1002,0x200802);
                    iShizukuInputService.sendPointerSync(motionEvent);

                    Thread.sleep(1000);

                    MotionEvent.PointerProperties[] pointerProperties1 = new MotionEvent.PointerProperties[2];
                    pointerProperties1[0] = new MotionEvent.PointerProperties();
                    pointerProperties1[0].id = 0;
                    pointerProperties1[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
                    pointerProperties1[1] = new MotionEvent.PointerProperties();
                    pointerProperties1[1].id = 1;
                    pointerProperties1[1].toolType = MotionEvent.TOOL_TYPE_FINGER;
                    MotionEvent.PointerCoords[] pointerCoords1 = new MotionEvent.PointerCoords[2];
                    pointerCoords1[0] = new MotionEvent.PointerCoords();
                    pointerCoords1[0].x = x1;
                    pointerCoords1[0].y = y1;
                    pointerCoords1[1] = new MotionEvent.PointerCoords();
                    pointerCoords1[1].x = x2;
                    pointerCoords1[1].y = y2;

                    MotionEvent motionEvent1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            261,2,
                            pointerProperties1,pointerCoords1,0,
                            0,1.0f,1.0f,0,0,0x1002,0x200802);
                    iShizukuInputService.sendPointerSync(motionEvent1);
                    //262
                    Thread.sleep(1000);
                    //抬起一根手指
                    MotionEvent motionEvent2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            262,2,
                            pointerProperties1,pointerCoords1,0,
                            0,1.0f,1.0f,0,0,0x1002,0x200802);
                    iShizukuInputService.sendPointerSync(motionEvent2);

                    Thread.sleep(1000);

                    MotionEvent motionEvent3 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP,1,
                            pointerProperties,pointerCoords,0,
                            0,1.0f,1.0f,0,0,0x1002,0x200802);
                    iShizukuInputService.sendPointerSync(motionEvent3);
*/
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(new Runnable(){

            @Override
            public void run() {
                while (true){
                    try {
                        MotionEvent event = events.take();
                        System.out.println(event);
                        /*if (event.getAction() == MotionEvent.ACTION_MOVE){
                            while (events.size() >= 4 && event.getAction() == MotionEvent.ACTION_MOVE){
                                for (int i = 0; i < 3; i++) {
                                    event = events.take();
                                    System.out.println("丢弃："+event);
                                    if (event.getAction() != MotionEvent.ACTION_MOVE){
                                        break;
                                    }
                                }

                            }
                        }*/
                        iShizukuInputService.injectInputEvent(event);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isAccessibilitySettingsOn(this, "top.weixiansen574.LyrePlayer.ClickService")) {
            new AlertDialog.Builder(this).setTitle("温馨提示：").setMessage("请先关闭无障碍再退出程序，不然下次打开会导致无障碍功能出故障，导致启用了但是等于没有的问题（安卓系统背锅，万年不修此bug！）")
                    .setPositiveButton("去关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            } catch (Exception e) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消", null).setNeutralButton("使用root快速关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ShellUtils.execCommand("settings put secure enabled_accessibility_services \"\"", true).result == 0) {
                                Toast.makeText(MainActivity.this, "无障碍已关闭，正在安全退出程序", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "你的手机没有root权限或拒绝了授权", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).show();
        } else {
            finish();
        }
    }
*/
  /*  public static boolean isAccessibilitySettingsOn(Context context,String className){
        if (context == null){
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(100);// 获取正在运行的服务列表
        if (runningServices.size()<0){
            return false;
        }
        for (int i=0;i<runningServices.size();i++){
            ComponentName service = runningServices.get(i).service;
            System.out.println(service.getClassName());
            if (service.getClassName().equals(className)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
*/
   /* public void exit(){
        finish();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }).start();
    }
*/
}
