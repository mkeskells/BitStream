package org.danskells.bitstream.read.block;

import java.nio.ByteBuffer;

/**
 * A BitmapBlock that reads its data from a ByteBuffer.
 * Implementation Note: the ByteBuffer is never modified.
 * ReusableBufferBitmapBlock is designed to be cached, reused, and single threaded. It internally caches the
 */
public class ReusableBufferBitmapBlock extends BitmapBlock {

    private ByteBuffer data;
    private int byteDataStart;
    private int byteDataLength;
    private final BlockBits bits = new BitmapBlockBits();

    public void reset(long initialOffset, long controlOffset, int numberOfBits, ByteBuffer data, int byteDataStart, int byteDataLength) {
        this.data = data;
        this.byteDataStart = byteDataStart;
        this.byteDataLength = byteDataLength;
        reset(initialOffset, controlOffset, numberOfBits);
    }

    @Override
    public BlockBits bits(long baseOffset) {
        bits.reset(baseOffset);
        return bits;
    }


    @Override
    protected int findNextRelative(int relativeStart) {
        var byteIndex = relativeStart / 8;
        var bitIndex = relativeStart % 8;
        var currentByte = Byte.toUnsignedInt(data.get(byteIndex + byteDataStart)) & (-1 << bitIndex);

        if (currentByte != 0) {
            bitIndex = Integer.numberOfTrailingZeros(currentByte);
            return byteIndex * 8 + bitIndex;
        } else {
            for (; byteIndex < byteDataLength; byteIndex++) {
                currentByte = Byte.toUnsignedInt(data.get(byteIndex + byteDataStart));
                if (currentByte != 0) {
                    bitIndex = Integer.numberOfTrailingZeros(currentByte);
                    return byteIndex * 8 + bitIndex;
                }
            }

            return Integer.MAX_VALUE;
        }
    }
}
