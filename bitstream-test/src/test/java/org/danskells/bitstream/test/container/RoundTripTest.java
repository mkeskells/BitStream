package org.danskells.bitstream.test.container;

import org.danskells.bitstream.read.region.BitRegion;
import org.danskells.bitstream.read.region.SimpleBitContainer;
import org.danskells.bitstream.write.coder.MsbWriter;
import org.danskells.bitstream.write.container.BitContainerWriter;
import org.danskells.bitstream.write.container.ByteBufferAllocator;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoundTripTest {
    static BitSet of(int... indices) {
        var bitset = new BitSet();
        for (var index : indices) {
            bitset.set(index);
        }
        return bitset;
    }

    @Test
    void writeBitset() {
        var bitset = of(0, 2, 4, 5, 6, 7, 9, 22, 80);
        var writer = new BitContainerWriter(-99, 1024, ByteBufferAllocator.HEAP, MsbWriter.INSTANCE);
        writer.writeBitmap(0, bitset, 0, 80);

        ByteBuffer written = writer.asReadOnlyBuffer();

        var reader = new BitRegion(-99, Long.MAX_VALUE, written);

        var allBits = readFully(reader);
        assertEquals(List.of(0L, 2L, 4L, 5L, 6L, 7L, 9L, 22L, 80L), allBits);

    }

    List<Long> readFully(BitRegion region) {
        var stream = new SimpleBitContainer(List.of(region)).setBits();
        return stream.stream().boxed().toList();
    }
}
