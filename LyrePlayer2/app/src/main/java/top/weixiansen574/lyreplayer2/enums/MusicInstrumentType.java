package top.weixiansen574.lyreplayer2.enums;

public enum MusicInstrumentType {
    lyre(0),
    oldLyre(1);

    private final int value;
    MusicInstrumentType(int i) {
        this.value = i;
    }


    public int getValue() {
        return this.value;
    }

    public static MusicInstrumentType fromValue(int value) {
        for (MusicInstrumentType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
