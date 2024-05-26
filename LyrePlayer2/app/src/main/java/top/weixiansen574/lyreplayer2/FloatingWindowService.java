package top.weixiansen574.lyreplayer2;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import top.weixiansen574.async.TaskManger;
import top.weixiansen574.lyreplayer2.async.LoadMidiTask;
import top.weixiansen574.lyreplayer2.enums.InvalidKeySetting;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;
import top.weixiansen574.lyreplayer2.midi.Note;


/**
 * @noinspection NonAsciiCharacters
 */
public class FloatingWindowService extends Service implements View.OnClickListener {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_INSTRUMENT = "instrument";
    public static final String EXTRA_TRANSPOSITION = "transposition";
    public static final String EXTRA_GROUP_MAPPING = "group_mapping";
    public static final String EXTRA_INVALID_KEY_SETTING = "invalid_key_setting";
    ArrayList<Note> currentNoteSequence;

    private View windowView;
    SharedPreferences keyCoordinates;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private Button stopBtn;
    private Button playOrPauseBtn;
    private Button listBtn;
    private TextView nameTxv;
    private TextView currentTime;
    private ProgressBar progressBar;
    private TextView totalTime;
    private boolean 装逼模式 = false;

    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        //floatListManager = new FloatListManager(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 505;
        layoutParams.height = 280;
        layoutParams.x = 30;
        layoutParams.y = 100;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        String name = intent.getStringExtra("name");
        Uri midiUri = intent.getData();
        if (midiUri == null) {
            Toast.makeText(this, R.string.错误_未指定文件Uri, Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }
        playOrPauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);
        setMusic(midiUri, name,
                MusicInstrumentType.fromValue(intent.getIntExtra(EXTRA_INSTRUMENT, MusicInstrumentType.lyre.getValue())),
                intent.getIntExtra(EXTRA_TRANSPOSITION, 0),
                intent.getIntArrayExtra(EXTRA_GROUP_MAPPING),
                InvalidKeySetting.fromValue(intent.getIntExtra(EXTRA_INVALID_KEY_SETTING, InvalidKeySetting.no.getValue())));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.float_start) {
            if (!isPlaying.get()) {
                playMidi();
                playOrPauseBtn.setText("  ▎▎");
            } else {
                if (!isPaused.get()) {
                    pausePlayback();
                    playOrPauseBtn.setText(" ▶ ");
                } else {
                    resumePlayback();
                    playOrPauseBtn.setText("  ▎▎");
                }
            }
        } else if (id == R.id.float_stop) {
            stopPlayback();
        } else if (id == R.id.float_list) {
            showMusicList();
        } else if (id == R.id.close) {
            if (isPlaying.get()) {
                stopPlayback();
            }
            stopSelf();
        }
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            //从xml布局文件中读取布局
            final View view = View.inflate(this, R.layout.float_layout, null);
            windowManager.addView(view, layoutParams);
            nameTxv = view.findViewById(R.id.midi_name);
            currentTime = view.findViewById(R.id.current_time);
            progressBar = view.findViewById(R.id.progressBar);
            totalTime = view.findViewById(R.id.total_time);
            playOrPauseBtn = view.findViewById(R.id.float_start);
            stopBtn = view.findViewById(R.id.float_stop);
            listBtn = view.findViewById(R.id.float_list);
            this.windowView = view;
            view.findViewById(R.id.close).setOnClickListener(this);
            view.setOnTouchListener(new FloatingOnTouchListener());
            view.findViewById(R.id.float_list).setOnClickListener(this);
            playOrPauseBtn.setOnLongClickListener(v -> {
                if (!isPlaying.get()) {
                    //隐藏播放器，供装逼使用
                    windowManager.removeViewImmediate(view);
                    装逼模式 = true;
                    Toast.makeText(FloatingWindowService.this,
                            R.string.装逼模式启动,
                            Toast.LENGTH_LONG).show();
                    TaskManger.start(() -> {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ignored) {
                        }
                        TaskManger.postOnUiThread(this::playMidi);
                    });
                }
                return false;
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setMusic(Uri uri, String name, MusicInstrumentType instrumentType, int transposition,
                          int[] groupMapping, InvalidKeySetting blackKeySetting) {
        progressBar.setIndeterminate(true);
        playOrPauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        listBtn.setEnabled(false);
        currentTime.setText("--:--");
        totalTime.setText("--:--");
        nameTxv.setText(R.string.loading_midi);
        new LoadMidiTask(new LoadMidiTask.EventHandler() {

            @Override
            public void onLoadMidiSuccess(ArrayList<Note> notes) {
                currentNoteSequence = notes;
                long maxTick = notes.get(notes.size() - 1).getTick();
                totalTime.setText(formatTime(maxTick));
                progressBar.setMax((int) maxTick);
            }

            @Override
            public void onError(Throwable th) {
                nameTxv.setText(R.string.加载失败);
                Toast.makeText(FloatingWindowService.this, th.toString(), Toast.LENGTH_SHORT).show();
                progressBar.setIndeterminate(false);
                listBtn.setEnabled(true);
            }

            @Override
            public void onComplete() {
                playOrPauseBtn.setEnabled(true);
                stopBtn.setEnabled(true);
                listBtn.setEnabled(true);
                nameTxv.setText(name);
                currentTime.setText(formatTime(0));
                progressBar.setIndeterminate(false);
            }
        }, getContentResolver(), uri, instrumentType, transposition, groupMapping, blackKeySetting)
                .execute();
    }

    public void playMidi() {
        System.out.println("playMidi");
        if (isPlaying.get()) {
            System.out.println("已经在播放中！");
            return;
        }
        isPlaying.set(true);
        isPaused.set(false);
        progressBar.setProgress(0);
        ShizukuUtil.getOrBindService(this, this::createPlayThreadAndStart);
    }

    private void createPlayThreadAndStart(IShizukuInputService service) {
        Thread playbackThread = new Thread(() -> {
            System.out.println("演奏开始！");

            keyCoordinates = getSharedPreferences("key_coordinates", Context.MODE_PRIVATE);
            int[] input_x = {
                    keyCoordinates.getInt("x1", 0),
                    keyCoordinates.getInt("x2", 0),
                    keyCoordinates.getInt("x3", 0),
                    keyCoordinates.getInt("x4", 0),
                    keyCoordinates.getInt("x5", 0),
                    keyCoordinates.getInt("x6", 0),
                    keyCoordinates.getInt("x7", 0)};

            int[] input_y = {
                    keyCoordinates.getInt("y3", 0),
                    keyCoordinates.getInt("y2", 0),
                    keyCoordinates.getInt("y1", 0)};

            Clicker clicker = new WClicker(service);

            long firstTime = System.currentTimeMillis();

            for (Note note : currentNoteSequence) {
                int index = (note.getValue() <= 7) ? 0 : ((note.getValue() <= 14) ? 1 : 2);
                int x = input_x[(note.getValue() - 1) % 7];
                int y = input_y[index];

                long sleepTime = firstTime - System.currentTimeMillis() + note.getTick();
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isPaused.get()) {
                    synchronized (FloatingWindowService.this) {
                        try {
                            long startMillis = System.currentTimeMillis();
                            FloatingWindowService.this.wait();
                            // 计算暂停时间
                            firstTime += System.currentTimeMillis() - startMillis;
                            System.out.println("退出暂停状态");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!isPlaying.get()) {
                    break; // 停止播放
                }
                TaskManger.postOnUiThread(() -> {
                    progressBar.setProgress((int) note.getTick());
                    currentTime.setText(formatTime(note.getTick()));
                });
                try {
                    if (note.isNoteOn()) {
                        clicker.down(x, y);
                    } else {
                        clicker.up(x, y);
                    }
                } catch (RemoteException e) {
                    isPlaying.set(false);
                    throw new RuntimeException(e);
                } catch (RuntimeException e){
                    isPlaying.set(false);
                    TaskManger.postOnUiThread(() -> {
                        currentTime.setText(formatTime(0));
                        progressBar.setProgress(0);
                        playOrPauseBtn.setText(" ▶ ");
                        if (e instanceof Clicker.TooManyTouchesException){
                            Toast.makeText(FloatingWindowService.this,
                                    R.string.错误_midi同时按下的键位太多_已超出10个的限制,
                                    Toast.LENGTH_LONG).show();
                        } else if (e instanceof Clicker.TouchNotFoundException) {
                            Toast.makeText(FloatingWindowService.this,
                                    R.string.读取midi数据时发生问题_弹起事件未对应按下事件,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
            }
            System.out.println("演奏结束！");
            isPlaying.set(false);
            TaskManger.postOnUiThread(() -> {
                playOrPauseBtn.setText(" ▶ ");
                if (!装逼模式) {
                    Toast.makeText(FloatingWindowService.this, R.string.演奏结束, Toast.LENGTH_SHORT).show();
                }
            });
        });
        playbackThread.start();
    }

    public void pausePlayback() {
        System.out.println("暂停播放！");
        if (!isPlaying.get()) {
            System.out.println("当前没有播放中的音乐！");
            return;
        }
        if (!isPaused.get()) {
            isPaused.set(true);
            System.out.println("播放已暂停！");
        }
    }

    public void resumePlayback() {
        System.out.println("恢复播放！");
        if (!isPlaying.get()) {
            System.out.println("当前没有播放中的音乐！");
            return;
        }
        if (isPaused.get()) {
            isPaused.set(false);
            synchronized (this) {
                notify(); // 恢复播放
            }
            System.out.println("播放已恢复！");
        }
    }

    public void stopPlayback() {
        System.out.println("停止播放！");
        if (!isPlaying.get()) {
            Toast.makeText(this, R.string.当前没有播放中的音乐, Toast.LENGTH_SHORT).show();
            return;
        }
        isPlaying.set(false);
        if (isPaused.get()) {
            isPaused.set(false);
            synchronized (this) {
                notify(); // 停止暂停状态
            }
        }
        currentTime.setText(formatTime(0));
        progressBar.setProgress(0);
        playOrPauseBtn.setText(" ▶ ");
        System.out.println("播放已停止！");
    }


    private void showMusicList() {
        if (isPlaying.get()) {
            Toast.makeText(FloatingWindowService.this, getString(R.string.qxtzbf),
                    Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.Dialog)
                    .setTitle(R.string.音乐列表)
                    .setPositiveButton(R.string.close, null)
                    .create();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Objects.requireNonNull(dialog.getWindow()).setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
            } else {
                Objects.requireNonNull(dialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }

            View musicListView = View.inflate(FloatingWindowService.this,
                    R.layout.float_music_list, null);
            dialog.setTitle(getString(R.string.选择音乐));
            dialog.setView(musicListView);

            RecyclerView music_list = musicListView.findViewById(R.id.music_list);

            FLoatMusicListAdapter adapter = new FLoatMusicListAdapter(FloatingWindowService.this);

            adapter.setOnItemClickListener(music -> {
                Uri uri = Uri.fromFile(new File(music.path));
                setMusic(uri, music.name, music.getInstrumentType(), music.transposition,
                        music.getGroupMapping(), music.getInvalidKeySetting());
                dialog.dismiss();
            });

            music_list.setAdapter(adapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FloatingWindowService.this);
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            music_list.setLayoutManager(linearLayoutManager);
            dialog.show();

        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying.set(false);
        windowManager.removeViewImmediate(windowView);
        System.out.println("Service Destroyed");
    }

    public static String formatTime(long time) {
        //to mm:ss
        return String.format(Locale.getDefault(), "%02d:%02d",
                (time / 1000) / 60, (time / 1000) % 60);
    }
}