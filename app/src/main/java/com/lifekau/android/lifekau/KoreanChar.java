package com.lifekau.android.lifekau;

public final class KoreanChar {

    private static final int CHOSEONG_COUNT = 19;
    private static final int JUNGSEONG_COUNT = 21;
    private static final int JONGSEONG_COUNT = 28;
    private static final int HANGUL_SYLLABLE_COUNT = CHOSEONG_COUNT * JUNGSEONG_COUNT * JONGSEONG_COUNT;
    private static final int HANGUL_SYLLABLES_BASE = 0xAC00;
    private static final int HANGUL_SYLLABLES_END = HANGUL_SYLLABLES_BASE + HANGUL_SYLLABLE_COUNT;

    private static final int[] COMPAT_CHOSEONG_MAP = new int[] {
            0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
            0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
    };

    private KoreanChar() {
        // Can never be instantiated.
    }

    public static boolean isChoseong(char c) {
        return 0x1100 <= c && c <= 0x1112;
    }

    public static boolean isCompatChoseong(char c) {
        return 0x3131 <= c && c <= 0x314E;
    }

    public static boolean isSyllable(char c) {
        return HANGUL_SYLLABLES_BASE <= c && c < HANGUL_SYLLABLES_END;
    }

    public static char getChoseong(char value) {
        if (!isSyllable(value))
            return '\0';

        final int choseongIndex = getChoseongIndex(value);
        return (char)(0x1100 + choseongIndex);
    }

    public static char getCompatChoseong(char value) {
        if (!isSyllable(value))
            return '\0';

        final int choseongIndex = getChoseongIndex(value);
        return (char)COMPAT_CHOSEONG_MAP[choseongIndex];
    }

    private static int getChoseongIndex(char syllable) {
        final int syllableIndex = syllable - HANGUL_SYLLABLES_BASE;
        final int choseongIndex = syllableIndex / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
        return choseongIndex;
    }
}