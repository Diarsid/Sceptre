package diarsid.sceptre.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import diarsid.sceptre.impl.logs.Logging;

import static org.assertj.core.api.Assertions.assertThat;

public class OrdersEstimatorTest {

    private static final OrdersEstimator estimator = new OrdersEstimator(new Logging());

    @AfterEach
    void cleanUp() {
        estimator.clear();
    }

    void mustBe(boolean expectOk) {
        int quality = estimator.qualityTotal();
        int threshold = estimator.qualityThreshold();

        if ( expectOk ) {
            assertThat(quality).isGreaterThanOrEqualTo(threshold);
        }
        else {
            assertThat(quality).isLessThan(threshold);
        }
    }

    @Test
    public void test() {
        estimator.set(4, 29);

        estimator.add(5, 33);
        estimator.add(7, 34);
        estimator.add(8, 30);

        mustBe(false);
    }

    @Test
    public void test2() {
        estimator.set(0, 0);

        estimator.add(1, 1);
        estimator.add(2, 5);
        estimator.add(3, 2);
        estimator.add(4, 3);

        mustBe(true);
    }

    @Test
    public void test3() {
        estimator.set(0, 19);

        estimator.add(1, 22);
        estimator.add(2, 23);

        mustBe(true);
    }

    @Test
    public void test4() {
        estimator.set(0, 19);

        estimator.add(2, 22);
        estimator.add(3, 23);

        mustBe(true);
    }

    @Test
    public void test5() {
        estimator.set(4, 9);

        estimator.add(5, 12);

        mustBe(true);
    }
}
