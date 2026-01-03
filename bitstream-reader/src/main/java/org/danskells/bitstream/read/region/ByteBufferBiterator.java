package org.danskells.bitstream.read.region;

import org.danskells.bitstream.read.Biterator;
import org.danskells.bitstream.read.block.Block;
import org.danskells.bitstream.read.coder.MsbReader;

import java.nio.ByteBuffer;

public class ByteBufferBiterator extends Biterator {
    private final BufferReader reader;
    private Block.BlockBits bits;

    public ByteBufferBiterator(long startInclusive, long endExclusive, ByteBuffer backingBuffer) {
        reader = new BufferReader(backingBuffer, MsbReader.INSTANCE);
        bits = reader.readBlockBits();
    }

    @Override
    public boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
        return bits.trySkipTo(position, action, actionParameter);
    }

    @Override
    public boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
        return bits.tryIndexedAdvance(action, actionParameter);
    }
}
