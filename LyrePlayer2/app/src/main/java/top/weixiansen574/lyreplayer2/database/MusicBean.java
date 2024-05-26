package top.weixiansen574.lyreplayer2.database;

import top.weixiansen574.lyreplayer2.enums.InvalidKeySetting;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;

public class MusicBean {
    public String name;
    public int instrument;
    public int transposition;
    public int invalid_key_setting;
    public int mapping1;
    public int mapping2;
    public int mapping3;
    public int mapping4;
    public int mapping5;
    public int mapping6;
    public int mapping7;
    public long create_date;
    public String path;

    public MusicBean(String name, int instrument, int transposition, int invalid_key_setting,int[] groupMapping, String path){
        this(name, instrument, transposition, invalid_key_setting,
                groupMapping[0],groupMapping[1],groupMapping[2],groupMapping[3],
                groupMapping[4],groupMapping[5],groupMapping[6],System.currentTimeMillis(), path);
    }
    public MusicBean(String name, int instrument, int transposition, int invalid_key_setting,
                 int mapping1, int mapping2, int mapping3, int mapping4, int mapping5,
                 int mapping6, int mapping7, long create_date, String path) {
        this.name = name;
        this.instrument = instrument;
        this.transposition = transposition;
        this.invalid_key_setting = invalid_key_setting;
        this.mapping1 = mapping1;
        this.mapping2 = mapping2;
        this.mapping3 = mapping3;
        this.mapping4 = mapping4;
        this.mapping5 = mapping5;
        this.mapping6 = mapping6;
        this.mapping7 = mapping7;
        this.create_date = create_date;
        this.path = path;
    }

    public MusicInstrumentType getInstrumentType(){
        return MusicInstrumentType.fromValue(instrument);
    }

    public InvalidKeySetting getInvalidKeySetting(){
        return InvalidKeySetting.fromValue(invalid_key_setting);
    }

    public int[] getGroupMapping(){
        return new int[]{mapping1, mapping2, mapping3, mapping4, mapping5, mapping6, mapping7};
    }


}
