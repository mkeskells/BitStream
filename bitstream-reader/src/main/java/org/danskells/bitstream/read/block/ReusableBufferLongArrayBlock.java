package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.coder.MsbReader;

import java.nio.ByteBuffer;

public class ReusableBufferLongArrayBlock extends LongArrayBlock {


    private final static MsbReader reader = MsbReader.INSTANCE;
    private ByteBuffer data;
    private int byteDataStart;
    private int byteDataLength;
    private final BufferLongArrayBlockBits bits = new BufferLongArrayBlockBits();

    public void reset(long initialOffset, long controlOffset, int numberOfValues, ByteBuffer data, int byteDataStart) {
        super.reset(initialOffset, controlOffset, numberOfValues);
        this.data = data;
        this.byteDataStart = byteDataStart;
    }


    @Override
    public BlockBits bits(long baseOffset) {
        bits.reset(baseOffset);
        return bits;
    }

    class BufferLongArrayBlockBits extends LongArrayBlockBits {
        @Override
        long getNextValue() {
            return reader.readULong(data);
        }
    }
}
