package org.danskells.bitstream.read.block;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HeapLongArrayBlockTest {


    public static Stream<Arguments> arrayArguments() {
        return Stream.of(
                Arguments.of(
                        "Single",
                        TestBlock.fromArray(0L)
                ),
                Arguments.of(
                        "Packed",
                        TestBlock.fromArray(0L, 1L, 2L, 3L, 4L, 5L, 6L)
                ),
                Arguments.of(
                        "Sparse",
                        TestBlock.fromArray(0L, 100L, 200L, 300L, 400L)
                ),
                Arguments.of(
                        "Non-zero start",
                        TestBlock.fromArray(1000L, 1010L, 1020L)
                )
        );
    }

    record TestBlock(HeapLongArrayBlock block, List<Long> values) {
        public static TestBlock fromArray(long... array) {
            var data = new long[array.length - 1];
            for (int i = 1; i < array.length; i++) {
                data[i - 1] = array[i] - array[i - 1];
            }
            var list = new ArrayList<Long>();
            for (long v : array) {
                list.add(v);
            }
            return new TestBlock(new HeapLongArrayBlock(array[0], data), list);
        }
    }

    @ParameterizedTest
    @MethodSource("arrayArguments")
    public void createAndIterateLongArrayBlock(String description, TestBlock testData) {
        var arrayBlock = testData.block;
        var bits = arrayBlock.bits(testData.block.initialOffset());

        var actual = new ArrayList<Long>();
        for (var i = 0; i < testData.values.size(); i++) {
            assertTrue(bits.tryIndexedAdvance(((value, index) -> actual.add(value)), i), "end at " + i);
        }
        assertFalse(bits.tryIndexedAdvance((a, b) -> {
        }, -1), "no more values");
        assertEquals(testData.values, actual, "values match for " + description);
    }

}