package top.weixiansen574.lyreplayer2.async;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import top.weixiansen574.async.BackstageTask;
import top.weixiansen574.lyreplayer2.enums.InvalidKeySetting;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;
import top.weixiansen574.lyreplayer2.midi.MidiProcessor;
import top.weixiansen574.lyreplayer2.midi.Note;

public class LoadMidiTask extends BackstageTask<LoadMidiTask.EventHandler> {
    ContentResolver contentResolver;
    Uri uri;
    MusicInstrumentType instrumentType;
    int transposition;
    int[] groupMapping;
    InvalidKeySetting blackKeySetting;

    public LoadMidiTask(EventHandler uiHandler, ContentResolver contentResolver, Uri uri,
                        MusicInstrumentType instrumentType, int transposition, int[] groupMapping,
                        InvalidKeySetting blackKeySetting)
    {
        super(uiHandler);
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.instrumentType = instrumentType;
        this.transposition = transposition;
        this.groupMapping = groupMapping;
        this.blackKeySetting = blackKeySetting;
    }

    @Override
    protected void onStart(EventHandler handler) throws Throwable {
        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null){
            throw new IOException("无法获取输入流");
        }
        ArrayList<Note> notes = MidiProcessor.toNoteList(inputStream);
        switch (instrumentType){
            case lyre:
                notes = MidiProcessor.toLyreNoteList(notes, transposition, groupMapping,
                        blackKeySetting, true);
                break;
            case oldLyre:
                notes = MidiProcessor.toOldLyreNoteList(notes, transposition, groupMapping,
                        blackKeySetting, true);
                break;
        }
        inputStream.close();
        handler.onLoadMidiSuccess(notes);
    }

    public interface EventHandler extends BaseEventHandler{
        void onLoadMidiSuccess(ArrayList<Note> notes);
    }
}
