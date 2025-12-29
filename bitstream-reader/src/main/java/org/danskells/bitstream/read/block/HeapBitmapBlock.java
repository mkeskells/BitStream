package org.danskells.bitstream.read.block;

import java.nio.ByteBuffer;

public class HeapBitmapBlock extends BitmapBlock {

    private final ByteBuffer buffer;
    private final int position;
    private final int numberOfBytes;

    public HeapBitmapBlock(long initialOffset, long controlOffset, byte[] data) {
        this(initialOffset, controlOffset, data.length, ByteBuffer.wrap(data), 0);
    }

    public HeapBitmapBlock(long initialOffset, long controlOffset, int numberOfBytes, ByteBuffer buffer, int position) {
        super();
        this.buffer = buffer;
        this.position = position;
        this.numberOfBytes = numberOfBytes;
        reset(initialOffset, controlOffset, numberOfBytes << 3);
    }

    @Override
    public BlockBits bits(long baseOffset) {
        var result = new BitmapBlockBits();
        result.reset(baseOffset);
        return result;
    }

    @Override
    protected int findNextRelative(int relativeStart) {
        var byteIndex = relativeStart / 8;
        if (relativeStart >= numberOfBits()) {
            //TODO - remove this check?
            //should be able to do this without it, when we rework parent method to make findNextRelative lazier
            return Integer.MAX_VALUE;
        }
        var bitIndex = relativeStart % 8;
        var currentByte = Byte.toUnsignedInt(buffer.get(byteIndex + position)) & (-1 << bitIndex);

        if (currentByte != 0) {
            bitIndex = Integer.numberOfTrailingZeros(currentByte);
            return byteIndex * 8 + bitIndex;
        } else {
            while (++byteIndex < numberOfBytes) {
                currentByte = Byte.toUnsignedInt(buffer.get(byteIndex + position));
                if (currentByte != 0) {
                    bitIndex = Integer.numberOfTrailingZeros(currentByte);
                    return byteIndex * 8 + bitIndex;
                }
            }

            return Integer.MAX_VALUE;
        }
    }
}
