package top.weixiansen574.lyreplayer2.midi;

import java.util.List;

public class SequenceOfNotes {
    public List<Note> noteList;
    public float speed;

    public SequenceOfNotes(List<Note> noteList, float speed) {
        this.noteList = noteList;
        this.speed = speed;
    }
}
