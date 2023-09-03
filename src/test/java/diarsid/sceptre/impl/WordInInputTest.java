package diarsid.sceptre.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WordInInputTest {

    static WordInInput wordInInput = new WordInInput();

    @Test
    public void test1() {
        wordInInput = new WordInInput();
    }

    @Test
    public void test2() {
        wordInInput.set(8, '1');
        wordInInput.set(9, '2');
        wordInInput.set(10, '3');
        wordInInput.set(11, '4');

        assertThat(wordInInput.chars.length).isEqualTo(WordInInput.INITIAL_LENGTH);
        assertThat(wordInInput.length).isEqualTo(4);
        assertThat(wordInInput.startIndex).isEqualTo(8);
        assertThat(wordInInput.endIndex).isEqualTo(11);
        assertThat(wordInInput.charsInVariant.length).isEqualTo(WordInInput.INITIAL_LENGTH * 2);
        assertThat(new String(wordInInput.charsInVariant)).isEqualTo("________1234________");
        assertThat(new String(wordInInput.chars)).startsWith("1234");

        wordInInput.complete();

        wordInInput.clearForReuse();

        assertThat(wordInInput.length).isEqualTo(0);
        assertThat(wordInInput.charsInVariant.length).isEqualTo(WordInInput.INITIAL_LENGTH * 2);
    }

    @Test
    public void test3() {
        wordInInput.set(2, '1');
        wordInInput.set(3, '2');
        wordInInput.set(4, '3');
        wordInInput.set(5, '4');
        wordInInput.set(6, '5');
        wordInInput.set(7, '6');
        wordInInput.set(8, '7');
        wordInInput.set(9, '8');
        wordInInput.set(10, '9');
        wordInInput.set(11, 'a');
        wordInInput.set(12, 'b');

        assertThat(wordInInput.chars.length).isEqualTo(WordInInput.INITIAL_LENGTH * 2);
        assertThat(wordInInput.length).isEqualTo(11);
        assertThat(wordInInput.startIndex).isEqualTo(2);
        assertThat(wordInInput.endIndex).isEqualTo(12);
        assertThat(wordInInput.charsInVariant.length).isEqualTo(WordInInput.INITIAL_LENGTH * 2);
        assertThat(new String(wordInInput.charsInVariant)).isEqualTo("__123456789ab_______");
        assertThat(new String(wordInInput.chars)).startsWith("123456789ab");

        wordInInput.complete();

        wordInInput.clearForReuse();

        assertThat(wordInInput.length).isEqualTo(0);
        assertThat(wordInInput.charsInVariant.length).isEqualTo(WordInInput.INITIAL_LENGTH * 2);
    }
}
