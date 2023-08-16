package diarsid.sceptre.impl;

import org.junit.jupiter.api.Test;

import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class SmartMeanTest {

    private final SmartMean smartMean = new SmartMean();
    private ListInt ints;
    private int result;

    @Test
    public void testSimple() {
        ints = new ListIntImpl(1, 1, 3, 3, 3, 5, 6);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void test1() {
        ints = new ListIntImpl(1, 1, 1, 3, 3, 3, 5, 6);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void test2() {
        ints = new ListIntImpl(1, 1, 1, 3, 3, 3, 0, -1);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void test3() {
        ints = new ListIntImpl(8, -4, -4, -4);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(-4);
    }

    @Test
    public void test4() {
        ints = new ListIntImpl(1, 2, 3);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void test5() {
        ints = new ListIntImpl(1, 1, 1);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void test6() {
        ints = new ListIntImpl(6, 6, 6, 7, 7, 7);
        result = smartMean.calculate(ints);
        assertThat(result).isEqualTo(7);
    }
}
