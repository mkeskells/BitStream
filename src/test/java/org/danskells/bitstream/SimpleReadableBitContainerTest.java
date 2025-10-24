package org.danskells.bitstream;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleReadableBitContainerTest {

    public static Stream<Arguments> abc() {
        return Stream.of(
                Arguments.of(
                      "Single bitmap block", new SimpleReadableBitContainer(
                              new Block[]{new BitmapBlock(BitSetBlockTest.buildBitset(0, 1, 2, 3))},
                              new long[]{0L}
                        ),
                        new long[]{0, 1, 2, 3},
                        0L,
                        4L
                )
        );
    }

    @ParameterizedTest @MethodSource("abc")
    public void IterateThroughAllValues(String description, SimpleReadableBitContainer container, long[] allPositions, long start, long end) {
        var node = container.getStreamNode(start, end);
        for (long position : allPositions) {
            assertEquals(position, node.next());
        }
        assertEquals(-1, node.next());
    }
}