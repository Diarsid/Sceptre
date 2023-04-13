package diarsid.sceptre.impl.collections.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import diarsid.sceptre.impl.collections.MapIntInt;

import static org.assertj.core.api.Assertions.assertThat;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;

public class MapIntIntTest {

    MapIntInt map = new MapIntIntImpl();

    @AfterEach
    void tearDownCase() {
        map.clear();
    }

    @Test
    void testPut() {
        assertThat(map.isEmpty()).isTrue();
        assertThat(map.isNotEmpty()).isFalse();

        assertThat(map.keys().isEmpty()).isTrue();
        assertThat(map.keys().isNotEmpty()).isFalse();

        assertThat(map.values().isEmpty()).isTrue();
        assertThat(map.values().isNotEmpty()).isFalse();

        assertThat(map.size()).isEqualTo(0);

        map.put(0, 10);

        assertThat(map.isEmpty()).isFalse();
        assertThat(map.isNotEmpty()).isTrue();

        assertThat(map.keys().isEmpty()).isFalse();
        assertThat(map.keys().isNotEmpty()).isTrue();

        assertThat(map.values().isEmpty()).isFalse();
        assertThat(map.values().isNotEmpty()).isTrue();

        assertThat(map.size()).isEqualTo(1);

        map.put(1, 11);
        map.put(3, 13);
        map.put(4, 14);

        map.put(2, 12);

        map.put(-1, -10);

        assertThat(map.size()).isEqualTo(6);

        assertThat(map.keys().contains(-1));
        assertThat(map.keys().contains(0));
        assertThat(map.keys().contains(1));
        assertThat(map.keys().contains(2));
        assertThat(map.keys().contains(3));
        assertThat(map.keys().contains(4));

        assertThat(map.values().contains(-10));
        assertThat(map.values().contains(10));
        assertThat(map.values().contains(11));
        assertThat(map.values().contains(12));
        assertThat(map.values().contains(13));
        assertThat(map.values().contains(14));

        assertThat(map.get(-1)).isEqualTo(-10);
        assertThat(map.get(0)).isEqualTo(10);
        assertThat(map.get(1)).isEqualTo(11);
        assertThat(map.get(2)).isEqualTo(12);
        assertThat(map.get(3)).isEqualTo(13);
        assertThat(map.get(4)).isEqualTo(14);

        assertThat(map.get(99)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void testExtend() {
        int key = 0;
        int value = 10;

        int limit = DEFAULT_ARRAY_SIZE*2 - 2;
        while ( key < limit ) {
            map.put(key, value);
            key++;
            value = value+10;
        }


    }

    @Test
    void testRemoveLast() {
        map.put(0, 10);
        map.put(1, 11);
        map.put(2, 12);
        map.put(3, 13);
        map.put(4, 14);

        map.remove(4);

        int a = 5;
    }

    @Test
    void testRemoveNotLast_middle() {
        map.put(0, 10);
        map.put(1, 11);
        map.put(2, 12);
        map.put(3, 13);
        map.put(4, 14);

        map.remove(2);

        int a = 5;
    }

    @Test
    void testRemoveNotLast_first() {
        map.put(0, 10);
        map.put(1, 11);
        map.put(2, 12);
        map.put(3, 13);
        map.put(4, 14);

        map.remove(0);

        assertThat(map.size()).isEqualTo(4);

        assertThat(map.keys().notContains(0)).isTrue();
        assertThat(map.keys().contains(0)).isEqualTo(false);

        assertThat(map.values().notContains(10)).isTrue();
        assertThat(map.values().contains(10)).isFalse();

        assertThat(map.get(0)).isEqualTo(Integer.MIN_VALUE);

        assertThat(map.keys().contains(1));
        assertThat(map.keys().contains(2));
        assertThat(map.keys().contains(3));
        assertThat(map.keys().contains(4));

        assertThat(map.values().contains(11));
        assertThat(map.values().contains(12));
        assertThat(map.values().contains(13));
        assertThat(map.values().contains(14));

        assertThat(map.get(1)).isEqualTo(11);
        assertThat(map.get(2)).isEqualTo(12);
        assertThat(map.get(3)).isEqualTo(13);
        assertThat(map.get(4)).isEqualTo(14);

        assertThat(map.get(99)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void testEntriesIterator() {
        map.put(0, 10);
        map.put(1, 11);
        map.put(2, 12);
        map.put(3, 13);
        map.put(4, 14);

        Map<Integer, Integer> ints = new HashMap<>();

        MapIntInt.Entries entries = map.entries();
        while ( entries.hasNext() ) {
            entries.next();
            ints.put(entries.currentKey(), entries.currentValue());
        }

        assertThat(ints.size()).isEqualTo(5);

        assertThat(ints.containsKey(0));
        assertThat(ints.containsKey(1));
        assertThat(ints.containsKey(2));
        assertThat(ints.containsKey(3));
        assertThat(ints.containsKey(4));

        assertThat(ints.containsValue(10));
        assertThat(ints.containsValue(11));
        assertThat(ints.containsValue(12));
        assertThat(ints.containsValue(13));
        assertThat(ints.containsValue(14));
    }


}
