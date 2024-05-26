package top.weixiansen574.lyreplayer2.enums;
public enum InvalidKeySetting {
    leftAndRight(1),
    left(2),
    right(3),
    no(4);

    private final int value;

    InvalidKeySetting(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static InvalidKeySetting fromValue(int value) {
        for (InvalidKeySetting invalidKeySetting : values()) {
            if (invalidKeySetting.value == value) {
                return invalidKeySetting;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
