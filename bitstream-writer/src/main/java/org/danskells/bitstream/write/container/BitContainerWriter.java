package org.danskells.bitstream.write.container;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.common.block.ControlByte;
import org.danskells.bitstream.write.coder.IWrite;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class BitContainerWriter {
    private ByteBuffer buffer;
    private final ByteBufferAllocator allocator;
    private final IWrite intWriter;
    private long currentOffset;

    public BitContainerWriter(int size, ByteBufferAllocator allocator, IWrite intWriter) {
        this.allocator = allocator;
        this.intWriter = intWriter;
        this.buffer = allocator.allocate(size);
    }
    public void writeBitmap(long blockOffset, BitSet bitset, int firstBit, int lastBit) {
        if (blockOffset < currentOffset) {
            throw new IllegalArgumentException("backwards");
        }
        if (!bitset.get(firstBit)) {
            throw new IllegalArgumentException("first bit must be set");
        }
        int byteCount = (lastBit - firstBit) / 8;
        if (byteCount * 8 + firstBit != lastBit) {
            throw new IllegalArgumentException("last bit is not byte aligned");
        }
        if (byteCount > 32) {
            throw new IllegalArgumentException("too large");
        }
        if (byteCount == 0) {
            throw new IllegalArgumentException("too small");
        }
        var delta = blockOffset - currentOffset;
        var sizeNeeded = intWriter.sizeOf(delta) + byteCount + 1;

        var prevPosition = buffer.position();

        ensureCapacity(sizeNeeded);
        intWriter.writeUnsigned(buffer, delta);
        putControl(BlockType.BITMAP, byteCount -1);

        var bytes = new byte[byteCount];
        for (int sourceBit = bitset.nextSetBit(firstBit + 1); sourceBit <= lastBit && sourceBit > 0; sourceBit = bitset.nextSetBit(sourceBit+1)) {
            var targetBit = sourceBit - firstBit -1;
            bytes[targetBit >>3] |= (byte) (1 << (targetBit & 7));
        }
        buffer.put(bytes);
        assert prevPosition + sizeNeeded == buffer.position(): "expected to use allocated space";

    }

    private void ensureCapacity(int extraNeeded) {
        if (buffer.remaining() < extraNeeded) {
            int newCapacity = Math.max(buffer.capacity() * 2, buffer.position() + extraNeeded);
            var newBuffer = allocator.allocate(newCapacity);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    private void putControl(BlockType blockType, int blockSpecific) {
        assert blockSpecific < 32 && blockSpecific >=0;
        buffer.put((byte) ((blockType.ordinal() << ControlByte.BLOCK_TYPE_SHIFT) + blockSpecific));
    }

    /* gets a read only view of the written data */
    public ByteBuffer asReadOnlyBuffer() {
        return buffer.asReadOnlyBuffer().flip().order(ByteOrder.LITTLE_ENDIAN);
    }
}
