package diarsid.sceptre.impl.collections.impl;

import org.junit.jupiter.api.Test;

import diarsid.sceptre.impl.collections.SetInt;

import static org.assertj.core.api.Assertions.assertThat;

public class SetIntTest {

    private SetInt set = new SetIntImpl();

    @Test
    void add_straight() {
        set.add(10);
        set.add(20);
        set.add(40);
        set.add(50);

        set.add(30);

        int a = 5;
    }

    @Test
    void add_reverse() {
        set.add(50);
        set.add(40);
        set.add(20);
        set.add(10);

        set.add(30);

        int a = 5;
    }

    @Test
    void add_random() {
        set.add(20);
        set.add(50);
        set.add(30);
        set.add(10);
        set.add(40);

        int a = 5;
    }

    @Test
    void lesserThan_returs_NO_VALUE() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThan(5);


        assertThat(x).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void lesserThan_returs_20() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThan(30);


        assertThat(x).isEqualTo(20);
    }

    @Test
    void lesserThan_returs_20_2() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThan(25);


        assertThat(x).isEqualTo(20);
    }

    @Test
    void lesserThan_returs_50() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThan(100);


        assertThat(x).isEqualTo(50);
    }

    @Test
    void lesserThanOrEqual_returs_NO_VALUE() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThanOrEqual(5);

        assertThat(x).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void lesserThanOrEqual_returs_30() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThanOrEqual(30);

        assertThat(x).isEqualTo(30);
    }

    @Test
    void lesserThanOrEqual_returs_10() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThanOrEqual(10);

        assertThat(x).isEqualTo(10);
    }

    @Test
    void lesserThanOrEqual_returs_50() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThanOrEqual(50);

        assertThat(x).isEqualTo(50);
    }

    @Test
    void lesserThanOrEqual_returs_50_2() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.lesserThanOrEqual(55);

        assertThat(x).isEqualTo(50);
    }

    @Test
    void greaterThan_returs_10() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThan(5);


        assertThat(x).isEqualTo(10);
    }

    @Test
    void greaterThan_returs_NO_VALUE() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThan(55);


        assertThat(x).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void greaterThan_returs_30() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThan(20);


        assertThat(x).isEqualTo(30);
    }

    @Test
    void greaterThan_returs_30_2() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThan(25);


        assertThat(x).isEqualTo(30);
    }

    @Test
    void greaterThanOrEquals_returs_30() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThanOrEqual(25);


        assertThat(x).isEqualTo(30);
    }

    @Test
    void greaterThanOrEquals_returs_10() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThanOrEqual(10);


        assertThat(x).isEqualTo(10);
    }

    @Test
    void greaterThanOrEquals_returs_10_2() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThanOrEqual(5);


        assertThat(x).isEqualTo(10);
    }

    @Test
    void greaterThanOrEquals_returs_NO_VALUE() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThanOrEqual(55);


        assertThat(x).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void greaterThanOrEquals_returs_50() {
        set.add(10);
        set.add(20);
        set.add(30);
        set.add(40);
        set.add(50);

        int x = set.greaterThanOrEqual(50);


        assertThat(x).isEqualTo(50);
    }
}
