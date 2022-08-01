package diarsid.sceptre.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WordInVariantTest {

    static WordInVariant wordInVariant = new WordInVariant();

    @Test
    public void test1() {
        wordInVariant = new WordInVariant();
    }

    @Test
    public void test2() {
        wordInVariant.set(8, '1');
        wordInVariant.set(9, '2');
        wordInVariant.set(10, '3');
        wordInVariant.set(11, '4');

        assertThat(wordInVariant.chars.length).isEqualTo(WordInVariant.INITIAL_LENGTH);
        assertThat(wordInVariant.length).isEqualTo(4);
        assertThat(wordInVariant.startIndex).isEqualTo(8);
        assertThat(wordInVariant.endIndex).isEqualTo(11);
        assertThat(wordInVariant.charsInVariant.length).isEqualTo(WordInVariant.INITIAL_LENGTH * 2);
        assertThat(new String(wordInVariant.charsInVariant)).isEqualTo("________1234________");
        assertThat(new String(wordInVariant.chars)).startsWith("1234");

        wordInVariant.complete();

        wordInVariant.clearForReuse();

        assertThat(wordInVariant.length).isEqualTo(0);
        assertThat(wordInVariant.charsInVariant.length).isEqualTo(WordInVariant.INITIAL_LENGTH * 2);
    }

    @Test
    public void test3() {
        wordInVariant.set(2, '1');
        wordInVariant.set(3, '2');
        wordInVariant.set(4, '3');
        wordInVariant.set(5, '4');
        wordInVariant.set(6, '5');
        wordInVariant.set(7, '6');
        wordInVariant.set(8, '7');
        wordInVariant.set(9, '8');
        wordInVariant.set(10, '9');
        wordInVariant.set(11, 'a');
        wordInVariant.set(12, 'b');

        assertThat(wordInVariant.chars.length).isEqualTo(WordInVariant.INITIAL_LENGTH * 2);
        assertThat(wordInVariant.length).isEqualTo(11);
        assertThat(wordInVariant.startIndex).isEqualTo(2);
        assertThat(wordInVariant.endIndex).isEqualTo(12);
        assertThat(wordInVariant.charsInVariant.length).isEqualTo(WordInVariant.INITIAL_LENGTH * 2);
        assertThat(new String(wordInVariant.charsInVariant)).isEqualTo("__123456789ab_______");
        assertThat(new String(wordInVariant.chars)).startsWith("123456789ab");

        wordInVariant.complete();

        wordInVariant.clearForReuse();

        assertThat(wordInVariant.length).isEqualTo(0);
        assertThat(wordInVariant.charsInVariant.length).isEqualTo(WordInVariant.INITIAL_LENGTH * 2);
    }
}
