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
    private long controlPoint;

    public BitContainerWriter(long containerStart, int size, ByteBufferAllocator allocator, IWrite intWriter) {
        this.allocator = allocator;
        this.intWriter = intWriter;
        this.buffer = allocator.allocate(size);
        controlPoint = containerStart-1; //one based
    }

    /** control point is the last bit set, or the first bit in the last byte written, whichever is largest */
    public void writeBitmap(long blockStart, BitSet bitset, int firstBit, int lastBit) {
       assert blockStart > controlPoint: "backwards";
       assert bitset.get(firstBit): "first bit must be set";
        int byteCount = (lastBit + 7 - firstBit) / 8;
        assert byteCount <= 32: "too large";
        assert byteCount > 0 : "too small";

        var delta = blockStart - controlPoint;
        var sizeNeeded = intWriter.sizeOf(delta) + byteCount + 1;

        var prevPosition = buffer.position();

        ensureCapacity(sizeNeeded);
        intWriter.writeUnsigned(buffer, delta -1); //one based
        putControl(BlockType.BITMAP, byteCount - 1);

        var bytes = new byte[byteCount];
        for (int sourceBit = bitset.nextSetBit(firstBit + 1); sourceBit <= lastBit && sourceBit > 0; sourceBit = bitset.nextSetBit(sourceBit + 1)) {
            var targetBit = sourceBit - firstBit - 1;
            bytes[targetBit >> 3] |= (byte) (1 << (targetBit & 7));
        }
        buffer.put(bytes);
        assert prevPosition + sizeNeeded == buffer.position() : "expected to use allocated space";
        controlPoint = blockStart + lastBit - firstBit + 1;
    }

    public void writeArray(long[] arrayValues, int firstIndexInclusive, int lastIndexExclusive) {
        assert firstIndexInclusive <lastIndexExclusive: "array must not be empty";
        assert lastIndexExclusive - firstIndexInclusive >= 2: "too small array";
        assert lastIndexExclusive - firstIndexInclusive <= 33: "too large array";

        var sizeNeeded = 1; //control byte
        long delta = arrayValues[firstIndexInclusive] - controlPoint -1; //one based
        sizeNeeded += intWriter.sizeOf(delta); //offset
        var sizeBeforeDeltas = sizeNeeded;
        for (int i = firstIndexInclusive + 1; i < lastIndexExclusive; i++ ) {
            assert arrayValues[i] > arrayValues[i-1]: "array must be strictly increasing";
            sizeNeeded += intWriter.sizeOf(arrayValues[i] - arrayValues[i-1] - 1);
        }
        var arrayEncodedSize = sizeNeeded - sizeBeforeDeltas;
        sizeNeeded += intWriter.sizeOf(sizeNeeded - sizeBeforeDeltas); //block vars size
        ensureCapacity(sizeNeeded);

        intWriter.writeUnsigned(buffer, delta);
        putControl(BlockType.LIST, lastIndexExclusive - firstIndexInclusive -2);
        intWriter.writeUnsigned(buffer, arrayEncodedSize -1); //one based
        for (int i = firstIndexInclusive + 1; i < lastIndexExclusive; i++ ) {
            intWriter.writeUnsigned(buffer, arrayValues[i] - arrayValues[i-1] - 1);
        }
        controlPoint = arrayValues[firstIndexInclusive];

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
        assert blockSpecific < 32 && blockSpecific >= 0;
        buffer.put((byte) ((blockType.ordinal() << ControlByte.BLOCK_TYPE_SHIFT) + blockSpecific));
    }

    /* gets a read only view of the written data */
    public ByteBuffer asReadOnlyBuffer() {
        return buffer.asReadOnlyBuffer().flip().order(ByteOrder.LITTLE_ENDIAN);
    }

}
