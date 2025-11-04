package org.danskells.bitstream.test.container;

import org.danskells.bitstream.read.BitContainer;
import org.danskells.bitstream.read.BitContainerStream;
import org.danskells.bitstream.read.container.ByteBufferContainer;
import org.danskells.bitstream.write.coder.MsbWriter;
import org.danskells.bitstream.write.container.BitContainerWriter;
import org.danskells.bitstream.write.container.ByteBufferAllocator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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
        var bitset = of(0,2,4,5,6,7,9,22,80);
        var writer = new BitContainerWriter(1024, ByteBufferAllocator.HEAP, MsbWriter.INSTANCE);
        writer.writeBitmap(0, bitset, 0, 80);

        ByteBuffer written = writer.asReadOnlyBuffer();

        var reader = new ByteBufferContainer(0, Long.MAX_VALUE, written);

        var allBits = readFully(reader);
        assertEquals(List.of(0L,2L,4L,5L,6L,7L,9L,22L,80L), allBits);

    }
    List<Long> readFully(BitContainer container) {
        var result = new ArrayList<Long>();
        var stream  = container.getStreamNode(0, Long.MAX_VALUE);
        var next = stream.next();
        while (next != BitContainerStream.END_OF_STREAM) {
            result.add(next);
            next = stream.next();
        }
        return result;
    }
}
