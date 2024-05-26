
package top.weixiansen574.lyreplayer2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.weixiansen574.lyreplayer2.database.MusicBean;
import top.weixiansen574.lyreplayer2.database.MusicDao;
import top.weixiansen574.lyreplayer2.enums.InvalidKeySetting;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;
import top.weixiansen574.lyreplayer2.midi.MidiProcessor;
import top.weixiansen574.lyreplayer2.midi.Note;


public class AdjustAndStartActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_INSTRUMENT = "instrument";
    public static final String EXTRA_TRANSPOSITION = "transposition";
    public static final String EXTRA_INVALID_KEY_SETTING = "invalid_key_setting";
    public static final String EXTRA_MAPPING_1 = "mapping1";
    public static final String EXTRA_MAPPING_2 = "mapping2";
    public static final String EXTRA_MAPPING_3 = "mapping3";
    public static final String EXTRA_MAPPING_4 = "mapping4";
    public static final String EXTRA_MAPPING_5 = "mapping5";
    public static final String EXTRA_MAPPING_6 = "mapping6";
    public static final String EXTRA_MAPPING_7 = "mapping7";

    Spinner spinner1;
    Spinner spinner2;
    Spinner spinner3;
    Spinner spinner4;
    Spinner spinner5;
    Spinner spinner6;
    Spinner spinner7;
    RadioGroup blackKeySetting;
    RadioGroup rg_musicInstrumentType;
    int transpositionIndex;
    int currentTransposition = -1;
    SharedPreferences keyCoordinates;
    static Uri midiUri;
    static String midiName;

    TextView transpositionText;
    public MusicDao musicDao;

    AdjustAndStartActivity context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_adjust_and_start_new);

        context = this;
        musicDao = new MusicDao(context);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent == null || (midiUri = intent.getData()) == null) {
            showLunchError(getString(R.string.空意图_需要一个文件URI));
            return;
        }

        String path = midiUri.getEncodedPath();
        if (path == null) {
            showLunchError(getString(R.string.无法获取文件路径));
            return;
        }

        midiName = intent.getStringExtra(EXTRA_NAME);

        if (midiName == null) {
            midiName = Uri.decode(path);
            midiName = midiName.substring(midiName.lastIndexOf("/") + 1);
            View dialogView = getLayoutInflater().inflate(R.layout.edit_text, null);
            EditText editText = dialogView.findViewById(R.id.edit_text);
            editText.setText(midiName);
            new AlertDialog.Builder(AdjustAndStartActivity.this)
                    .setTitle(R.string.设置音乐名称)
                    .setView(dialogView)
                    .setNeutralButton(R.string.show_help, (dialogInterface, i)
                            -> startActivity(new Intent(AdjustAndStartActivity.this, FileHelp.class)))
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        midiName = editText.getText().toString();
                    })
                    .show();
        }

        //判断是否是合法的midi文件
        if (!checkMidiFile()) {
            return;
        }

        //提示当前midi信息
        Toast.makeText(this, getString(R.string.filename) + midiName, Toast.LENGTH_SHORT).show();


        //初始化这些按钮
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.launch_setting).setOnClickListener(this);
        findViewById(R.id.analyze).setOnClickListener(this);
        findViewById(R.id.save_to_list).setOnClickListener(this);

        keyCoordinates = getSharedPreferences("key_coordinates", Context.MODE_PRIVATE);

        TextView textView = (TextView) findViewById(R.id.textView);
        transpositionText = findViewById(R.id.transposition_text);
        transpositionIndex = 11 + (-intent.getIntExtra(EXTRA_TRANSPOSITION, 0));
        setTranspositionText();
        spinner1 = (Spinner) this.findViewById(R.id.spinner1);
        spinner2 = (Spinner) this.findViewById(R.id.spinner2);
        spinner3 = (Spinner) this.findViewById(R.id.spinner3);
        spinner4 = (Spinner) this.findViewById(R.id.spinner4);
        spinner5 = (Spinner) this.findViewById(R.id.spinner5);
        spinner6 = (Spinner) this.findViewById(R.id.spinner6);
        spinner7 = (Spinner) this.findViewById(R.id.spinner7);
        this.setSpinner(spinner1, intent.getIntExtra(EXTRA_MAPPING_1, 1));
        this.setSpinner(spinner2, intent.getIntExtra(EXTRA_MAPPING_2, 1));
        this.setSpinner(spinner3, intent.getIntExtra(EXTRA_MAPPING_3, 1));
        this.setSpinner(spinner4, intent.getIntExtra(EXTRA_MAPPING_4, 2));
        this.setSpinner(spinner5, intent.getIntExtra(EXTRA_MAPPING_5, 3));
        this.setSpinner(spinner6, intent.getIntExtra(EXTRA_MAPPING_6, 3));
        this.setSpinner(spinner7, intent.getIntExtra(EXTRA_MAPPING_7, 3));
        blackKeySetting = findViewById(R.id.black_key_settings);
        rg_musicInstrumentType = findViewById(R.id.music_instrument_type);
        setMusicInstrumentType(intent.getIntExtra(EXTRA_INSTRUMENT, -1));
        setInvalidKeySettings(intent.getIntExtra(EXTRA_INVALID_KEY_SETTING, -1));
        final EditText x1 = findViewById(R.id.x1);
        final EditText x2 = findViewById(R.id.x2);
        final EditText x3 = findViewById(R.id.x3);
        final EditText x4 = findViewById(R.id.x4);
        final EditText x5 = findViewById(R.id.x5);
        final EditText x6 = findViewById(R.id.x6);
        final EditText x7 = findViewById(R.id.x7);
        final EditText y1 = findViewById(R.id.y1);
        final EditText y2 = findViewById(R.id.y2);
        final EditText y3 = findViewById(R.id.y3);

        if (keyCoordinates.contains("x1")) {
            x1.setText(String.valueOf(keyCoordinates.getInt("x1", 0)));
            x2.setText(String.valueOf(keyCoordinates.getInt("x2", 0)));
            x3.setText(String.valueOf(keyCoordinates.getInt("x3", 0)));
            x4.setText(String.valueOf(keyCoordinates.getInt("x4", 0)));
            x5.setText(String.valueOf(keyCoordinates.getInt("x5", 0)));
            x6.setText(String.valueOf(keyCoordinates.getInt("x6", 0)));
            x7.setText(String.valueOf(keyCoordinates.getInt("x7", 0)));
            y1.setText(String.valueOf(keyCoordinates.getInt("y1", 0)));
            y2.setText(String.valueOf(keyCoordinates.getInt("y2", 0)));
            y3.setText(String.valueOf(keyCoordinates.getInt("y3", 0)));
        } else {
            //自动填写坐标，根据屏幕分辨率
            try {
                DataInputStream inputStream = new DataInputStream(getResources().getAssets().open("ResolutionCoordinateMapping.json"));
                byte[] buffer = new byte[inputStream.available()];
                inputStream.readFully(buffer, 0, inputStream.available());
                inputStream.close();
                String JSONString = new String(buffer);
                JSONObject coordinateMappingJSON = JSON.parseObject(JSONString);
                String resolution = getResolution();
                if (coordinateMappingJSON.containsKey(resolution)) {
                    Toast.makeText(this, String.format(getString(R.string.已根据您的屏幕分辨率_s_自动填写坐标), resolution), Toast.LENGTH_LONG).show();
                    JSONObject coordinatesJSON = coordinateMappingJSON.getJSONObject(resolution);
                    x1.setText(coordinatesJSON.getString("x1"));
                    x2.setText(coordinatesJSON.getString("x2"));
                    x3.setText(coordinatesJSON.getString("x3"));
                    x4.setText(coordinatesJSON.getString("x4"));
                    x5.setText(coordinatesJSON.getString("x5"));
                    x6.setText(coordinatesJSON.getString("x6"));
                    x7.setText(coordinatesJSON.getString("x7"));
                    y1.setText(coordinatesJSON.getString("y1"));
                    y2.setText(coordinatesJSON.getString("y2"));
                    y3.setText(coordinatesJSON.getString("y3"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //保存坐标
        findViewById(R.id.save_coordinates).setOnClickListener(view -> {
            try {
                keyCoordinates.edit().putInt("x1", Integer.parseInt(x1.getText().toString())).
                        putInt("x2", Integer.parseInt(x2.getText().toString())).
                        putInt("x3", Integer.parseInt(x3.getText().toString())).
                        putInt("x4", Integer.parseInt(x4.getText().toString())).
                        putInt("x5", Integer.parseInt(x5.getText().toString())).
                        putInt("x6", Integer.parseInt(x6.getText().toString())).
                        putInt("x7", Integer.parseInt(x7.getText().toString())).
                        putInt("y1", Integer.parseInt(y1.getText().toString())).
                        putInt("y2", Integer.parseInt(y2.getText().toString())).
                        putInt("y3", Integer.parseInt(y3.getText().toString())).apply();
                Toast.makeText(AdjustAndStartActivity.this, R.string.saved_successfully, Toast.LENGTH_LONG).show();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                new AlertDialog.Builder(AdjustAndStartActivity.this)
                        .setTitle(R.string.save_failed)
                        .setMessage(R.string.save_failed_msg)
                        .setPositiveButton(R.string.got_it, null)
                        .show();
            }
        });
    }

    private void setTranspositionText() {
        int transposition = (11 - transpositionIndex);
        transpositionText.setText(String.format(getString(R.string.note_transposition),
                (transposition > 0 ? "+" : "") + transposition));
    }

    public void showLunchError(String message) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .show();
    }

    public void setSpinner(Spinner spinner, int mapping) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_mapping_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(mapping == -1 ? 0 : mapping);
    }

    //处理midi&打开悬浮窗
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.start) {
            start();
        } else if (id == R.id.launch_setting) {
            startActivity(new Intent(getPackageManager().getLaunchIntentForPackage("com.android.settings")));
        } else if (id == R.id.analyze) {
            transpositionAnalyze();
        } else if (id == R.id.save_to_list) {
            saveToList();
        }
    }

    private void saveToList() {
        View view = View.inflate(AdjustAndStartActivity.this, R.layout.edit_text, null);
        final EditText editText = view.findViewById(R.id.edit_text);
        editText.setText(midiName.replace(".mid", ""));
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.请输入你要保存的名称)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = editText.getText().toString();

                if (!isFileNameValid(inputText)) {
                    editText.setError(getString(R.string.文件名不能包含字符____));
                    return;
                }

                System.out.println("checkMusicExists " + musicDao.checkMusicExists(inputText));
                if (musicDao.checkMusicExists(inputText)) {
                    alertDialog.dismiss();
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.名称已存在)
                            .setMessage(R.string.是否覆盖同名音乐的文件及配置)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                saveMusic(inputText, true);
                            }).show();
                    return;
                }
                saveMusic(inputText, false);
                alertDialog.dismiss();
            }

            private void saveMusic(String name, boolean isUpdate) {
                File midiFile = new File(AdjustAndStartActivity.this.getExternalFilesDir("midis"), name + ".mid");
                try {
                    //如果文件源路径和本地库路径一致，则跳过存储到本地
                    boolean isIdentical = Uri.fromFile(midiFile).equals(midiUri);
                    if (!isIdentical) {
                        FileOutputStream fileOutputStream = new FileOutputStream(midiFile);
                        InputStream inputStream = AdjustAndStartActivity.this.openCurrentUriInputStream();
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                        fileOutputStream.close();
                        inputStream.close();
                    }
                    MusicBean musicBean = new MusicBean(name,
                            AdjustAndStartActivity.this.getMusicInstrumentType().getValue(),
                            getTransposition(),
                            AdjustAndStartActivity.this.getInvalidKeySettings().getValue(),
                            AdjustAndStartActivity.this.getMappingSettings(),
                            midiFile.getPath());
                    if (isUpdate) {
                        musicDao.updateMusic(musicBean);
                    } else {
                        musicDao.insertMusic(musicBean);
                    }
                    Toast.makeText(AdjustAndStartActivity.this, String.format(getString(R.string._s_已保存到播放列表),
                            editText.getText().toString()), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(AdjustAndStartActivity.this, String.format(getString(R.string._s_保存失败),
                            editText.getText().toString()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void start() {
        if (!keyCoordinates.contains("x1")) {
            new AlertDialog.Builder(AdjustAndStartActivity.this)
                    .setTitle(R.string.Error_coordinates_are_empty)
                    .setMessage(R.string.Error_coordinates_are_empty_msg)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        } else {
            //判断是否有悬浮窗权限
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.floating_window_permission_is_required)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 100);
                        }).show();
                return;
            }

            Intent intent = new Intent(context, FloatingWindowService.class);
            intent.putExtra(FloatingWindowService.EXTRA_NAME, midiName);
            intent.putExtra(FloatingWindowService.EXTRA_INSTRUMENT, getMusicInstrumentType().getValue());
            intent.putExtra(FloatingWindowService.EXTRA_GROUP_MAPPING, getMappingSettings());
            intent.putExtra(FloatingWindowService.EXTRA_TRANSPOSITION, getTransposition());
            intent.putExtra(FloatingWindowService.EXTRA_INVALID_KEY_SETTING, getInvalidKeySettings().getValue());
            intent.setData(midiUri);
            ShizukuUtil.bindShizukuAndStartService(context, intent, true);
        }
    }

    private void transpositionAnalyze() {
        final ProgressDialog analyzing = ProgressDialog.show(context, getString(R.string.analyzing), getString(R.string.analyzing_msg), false, true);
        analyzing.show();
        String[] tanspositionString = {"+11", "+10", "+9", "+8", "+7", "+6", "+5", "+4", "+3", "+2", "+1", "0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10", "-11"};

        Thread analyzeBlackKeyThread = new Thread(() -> {
            ArrayList<Note> noteArrayList = MidiProcessor.processorToNoteListAndHandleExceptions(AdjustAndStartActivity.this, getContentResolver(), midiUri);
            if (noteArrayList != null) {
                for (int i = 0; i < tanspositionString.length; i++) {
                    int blackKeyQuantity = 0;
                    if (getMusicInstrumentType() == MusicInstrumentType.lyre) {
                        blackKeyQuantity = MidiProcessor.analyzeBlackKeyQuantity(noteArrayList, (11 - i));
                    } else if (getMusicInstrumentType() == MusicInstrumentType.oldLyre) {
                        blackKeyQuantity = MidiProcessor.analyzeInvalidKeyQuantityForOldLyre(noteArrayList, (11 - i), getMappingSettings());
                    }
                    tanspositionString[i] += ("  " + getString(R.string.black_quantity) + (blackKeyQuantity / 2));
                }
                runOnUiThread(() -> {
                    analyzing.dismiss();
                    new AlertDialog.Builder(AdjustAndStartActivity.this)
                            .setTitle(R.string.please_select_an_offset)
                            .setSingleChoiceItems(tanspositionString, transpositionIndex,
                                    (dialogInterface, i) -> currentTransposition = i)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                if (currentTransposition == -1) {
                                    transpositionIndex = 11;
                                } else {
                                    transpositionIndex = currentTransposition;
                                }
                                setTranspositionText();
                            }).show();
                });
            }
        });
        analyzeBlackKeyThread.setName("analyzeBlackKey");
        analyzeBlackKeyThread.start();
    }

    private InputStream openCurrentUriInputStream() throws FileNotFoundException {
        return getContentResolver().openInputStream(midiUri);
    }

    public boolean checkMidiFile() {
        try {
            DataInputStream inputStream = new DataInputStream(openCurrentUriInputStream());
            byte b1 = inputStream.readByte();
            byte b2 = inputStream.readByte();
            byte b3 = inputStream.readByte();
            byte b4 = inputStream.readByte();
            inputStream.close();
            if (b1 == 0x4D && b2 == 0x54 && b3 == 0x68 && b4 == 0x64) {
                return true;
            } else {
                showLunchError(
                        String.format(
                                getString(R.string.根据文件头_这并不是一个正确的midi文件___您所选择的_s_),
                                (Integer.toHexString(b1 & 0xFF) + " " +
                                 Integer.toHexString(b2 & 0xFF) + " " +
                                 Integer.toHexString(b3 & 0xFF) + " " +
                                 Integer.toHexString(b4 & 0xFF)
                                )));
                return false;
            }
        } catch (IOException e) {
            showLunchError(String.format(getString(R.string.打开文件出错_s_),e));
            return false;
        }
    }


    public int[] getMappingSettings() {
        int[] mapping = new int[7];
        mapping[0] = getMapping(spinner1);
        mapping[1] = getMapping(spinner2);
        mapping[2] = getMapping(spinner3);
        mapping[3] = getMapping(spinner4);
        mapping[4] = getMapping(spinner5);
        mapping[5] = getMapping(spinner6);
        mapping[6] = getMapping(spinner7);
        return mapping;
    }

    private int getMapping(Spinner spinner) {
        return spinner.getSelectedItemPosition() == 0 ? -1 : spinner.getSelectedItemPosition();
    }

/*    public int[] getSpinnerSettings() {
        int[] settings = new int[7];//{1,1,1,2,3,3,3};
        if (spinner1.getSelectedItemPosition() == 0) {
            settings[0] = 1;
        } else if (spinner1.getSelectedItemPosition() == 1) {
            settings[0] = -1;
        }
        if (spinner2.getSelectedItemPosition() == 0) {
            settings[1] = 1;
        } else if (spinner2.getSelectedItemPosition() == 1) {
            settings[1] = -1;
        }
        if (spinner3.getSelectedItemPosition() == 0) {
            settings[2] = 1;
        } else if (spinner3.getSelectedItemPosition() == 1) {
            settings[2] = 2;
        }

        if (spinner4.getSelectedItemPosition() == 0) {
            settings[3] = 1;
        } else if (spinner4.getSelectedItemPosition() == 1) {
            settings[3] = 2;
        } else if (spinner4.getSelectedItemPosition() == 2) {
            settings[3] = 3;
        }

        if (spinner5.getSelectedItemPosition() == 0) {
            settings[4] = 1;
        } else if (spinner5.getSelectedItemPosition() == 1) {
            settings[4] = 2;
        } else if (spinner5.getSelectedItemPosition() == 2) {
            settings[4] = 3;
        }

        if (spinner6.getSelectedItemPosition() == 0) {
            settings[5] = 2;
        } else if (spinner6.getSelectedItemPosition() == 1) {
            settings[5] = 3;
        } else if (spinner6.getSelectedItemPosition() == 2) {
            settings[5] = -1;
        }
        if (spinner7.getSelectedItemPosition() == 0) {
            settings[6] = 3;
        } else if (spinner7.getSelectedItemPosition() == 1) {
            settings[6] = -1;
        }
        return settings;
    }*/

    private int getTransposition() {
        return 11 - transpositionIndex;
    }

    private InvalidKeySetting getInvalidKeySettings() {
        int id = blackKeySetting.getCheckedRadioButtonId();
        if (id == R.id.cb1) {
            return InvalidKeySetting.leftAndRight;
        } else if (id == R.id.cb2) {
            return InvalidKeySetting.left;
        } else if (id == R.id.cb3) {
            return InvalidKeySetting.right;
        } else {
            return InvalidKeySetting.no;
        }
    }


    private void setMusicInstrumentType(int type) {
        if (type == -1) {
            return;
        }
        setMusicInstrumentType(MusicInstrumentType.fromValue(type));
    }

    private void setMusicInstrumentType(MusicInstrumentType type) {
        switch (type) {
            case lyre:
                rg_musicInstrumentType.check(R.id.cb_lyre);
                break;
            case oldLyre:
                rg_musicInstrumentType.check(R.id.cb_old_lrye);
                break;
        }
    }

    private void setInvalidKeySettings(int setting) {
        if (setting == -1) {
            return;
        }
        setInvalidKeySettings(InvalidKeySetting.fromValue(setting));
    }

    private void setInvalidKeySettings(InvalidKeySetting setting) {
        switch (setting) {
            case leftAndRight:
                blackKeySetting.check(R.id.cb1);
                break;
            case left:
                blackKeySetting.check(R.id.cb2);
                break;
            case right:
                blackKeySetting.check(R.id.cb3);
                break;
            case no:
                blackKeySetting.check(R.id.cb4);
                break;
        }
    }


    private MusicInstrumentType getMusicInstrumentType() {
        int id = rg_musicInstrumentType.getCheckedRadioButtonId();
        if (id == R.id.cb_lyre) {
            return MusicInstrumentType.lyre;
        } else if (id == R.id.cb_old_lrye) {
            return MusicInstrumentType.oldLyre;
        }
        return MusicInstrumentType.lyre;
    }

    public String getResolution() {
        WindowManager windowManager = getWindow().getWindowManager();
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        int width = point.x;
        int height = point.y;
        return height + "*" + width;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isFileNameValid(String fileName) {
        // 定义合法文件名的正则表达式
        String regex = "^[^\\\\/:*?<>|]*$"; // 匹配任意非斜杠、冒号、星号、问号、小于号、大于号、竖线的字符串

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 匹配字符串
        Matcher matcher = pattern.matcher(fileName);

        // 返回是否匹配
        return matcher.matches();
    }


}

