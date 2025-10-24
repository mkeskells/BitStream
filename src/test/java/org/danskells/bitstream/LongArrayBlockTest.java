package org.danskells.bitstream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LongArrayBlockTest {


    public static Stream<Arguments> arrayArguments() {
        return Stream.of(
                Arguments.of(
                        "Single",
                        new long[]{0L}
                ),
                Arguments.of(
                        "Packed",
                        new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L}
                ),
                Arguments.of(
                        "Sparse",
                        new long[]{0L, 100L, 200L, 300L, 400L}
                ),
                Arguments.of(
                        "Non-zero start",
                        new long[]{1000L, 1010L, 1020L}
                )
        );
    }

    @ParameterizedTest @MethodSource("arrayArguments")
    public void createAndIterateLongArrayBlock(String description, long[] simpleLongArray){
        var arrayBlock = new LongArrayBlock(simpleLongArray);
        var node = arrayBlock.getStreamNode();
        for (long l : simpleLongArray) {
            assertEquals(l, node.next());
        }
        assertEquals(-1, node.next());
    }

}