package org.danskells.bitstream.read.block;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BitSetBlockTest {

    public static TestBits buildTestBits(int... bits) {
        int start = bits[0];
        int controlOffset = bits[bits.length - 1] - start + 1;
        var bitset = new BitSet();
        for (var index = 1; index < bits.length; index++) {
            bitset.set(bits[index] - start - 1);
        }
        var bytes = bitset.toByteArray();
        var list = new ArrayList<Long>();
        for (long v : bits) {
            list.add(v);
        }
        return new TestBits(new HeapBitmapBlock(start, controlOffset, bytes), list);
    }


    public static Stream<Arguments> arrayArguments() {
        return Stream.of(
                Arguments.of(
                        "Single",
                        buildTestBits(0)
                ),
                Arguments.of(
                        "Packed",
                        buildTestBits(0, 1, 2, 3, 4, 5)
                ),
                Arguments.of(
                        "Sparse",
                        buildTestBits(0, 2, 4, 6, 8)
                ),
                Arguments.of(
                        "Non-zero start",
                        buildTestBits(2, 3, 4)
                )
        );
    }

    record TestBits(BitmapBlock block, List<Long> values) {
    }

    @ParameterizedTest
    @MethodSource("arrayArguments")
    public void iterateBitsetBlock(String description, TestBits data) {
        var bitmapBlock = data.block;

        var bits = bitmapBlock.bits(bitmapBlock.initialOffset());

        var actual = new ArrayList<Long>();
        for (var i = 0; i < data.values.size(); i++) {
            assertTrue(bits.tryIndexedAdvance((val, index) ->
                    actual.add(val), i), "end at " + i);
        }
        assertEquals(data.values, actual, "values match for " + description);
        assertFalse(bits.tryIndexedAdvance((a, b) -> fail(a + ", " + b), -1), "no more values");
    }

}