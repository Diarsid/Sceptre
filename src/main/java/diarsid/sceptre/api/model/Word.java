package diarsid.sceptre.api.model;

import java.util.Comparator;

public class Word {

    public static Comparator<Word> FIRST_IS_BEST = (word0, word1) -> {
        return -1 * Integer.compare(word0.quality, word1.quality);
    };

    public static Comparator<Word> FIRST_IS_BY_NATURAL_ORDER = (word0, word1) -> {
        return Integer.compare(word0.index, word1.index);
    };

    public final String string;
    public final int index;
    public final int quality;

    public Word(String string, int index, int quality) {
        this.string = string;
        this.index = index;
        this.quality = quality;
    }
}
