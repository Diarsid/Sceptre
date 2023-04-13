package diarsid.sceptre.impl.collections.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import diarsid.sceptre.impl.collections.ListInt;

public class ListIntTest {

    ListInt list = new ListIntImpl();

    @AfterEach
    void tearDownCase() {
        list.clear();
    }

    @Test
    void testAdd() {
        list.add(10);
        list.add(11);
        list.add(12);

        int a = 5;
    }

    @Test
    void testAdd_at0() {
        list.add(10);
        list.add(11);
        list.add(12);

        list.add(0, -10);

        int a = 5;
    }

    @Test
    void testAdd_atMiddle() {
        list.add(10);
        list.add(11);
        list.add(12);

        list.add(1, 1111);

        int a = 5;
    }

    @Test
    void testAdd_atEnd() {
        list.add(10);
        list.add(11);
        list.add(12);

        list.add(2, 1222222);

        int a = 5;
    }

    @Test
    void testRemove_fromMiddle() {
        list.add(10);
        list.add(11);
        list.add(12);
        list.add(13);

        list.remove(1);

        int a = 5;
    }

    @Test
    void testRemove_fromStart() {
        list.add(10);
        list.add(11);
        list.add(12);
        list.add(13);

        list.remove(0);

        int a = 5;
    }

    @Test
    void testRemove_fromEnd() {
        list.add(10);
        list.add(11);
        list.add(12);
        list.add(13);

        list.remove(3);

        int a = 5;
    }
}
