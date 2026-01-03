package org.danskells.bitstream.read.region;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.read.Biterator;
import org.danskells.bitstream.read.Biterator.IndexedLongConsumer;
import org.danskells.bitstream.read.coder.MsbReader;
import  org.danskells.bitstream.read.region.SimpleBitContainer.SimpleBitContainerBiterator.SimpleBitContainerBiteratorCallback;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A region within a BitContainer.
 * Conceptually maps to a file or a region of files
 * <p>
 * A region contains blocks of bits that can be read, and some indexing.
 * A region has a start and end address within the BitContainer.
 */
public class BitRegion {
    private final long startInclusive;
    private final long endExclusive;
    private final ByteBuffer buffer;

    public BitRegion(long startInclusive, long endExclusive, ByteBuffer buffer) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.buffer = buffer;
    }

    public Biterator biterator() {
        var readOnly = buffer.asReadOnlyBuffer();
        //asReadOnly does not preserve byte order
        readOnly.order(ByteOrder.LITTLE_ENDIAN);
        return new ByteBufferBiterator(startInclusive, endExclusive, readOnly);
    }

    long getStartInclusive() {
        return startInclusive;
    }

    long getEndExclusive() {
        return endExclusive;
    }

    public RegionBits bits(boolean setBits, SimpleBitContainerBiteratorCallback callback) {
        var readOnly = buffer.asReadOnlyBuffer();
        //asReadOnly does not preserve byte order
        readOnly.order(ByteOrder.LITTLE_ENDIAN);
        if (setBits) {
            return new SetBitsRegionBits(callback, newBufferReader(readOnly, MsbReader.INSTANCE));
        } else {
            throw new IllegalArgumentException("TODO: implement clear bits region bits");
        }
    }

    BufferReader newBufferReader(ByteBuffer readOnly, MsbReader instance) {
        return new BufferReader(readOnly, instance);
    }
}

class SetBitsRegionBits extends RegionBitsBase {
    private final TrySkipOrAdvance try_BITMAP1 = this::try_BITMAP1;
    private final TrySkipOrAdvance try_BITMAP2 = this::try_BITMAP2;
    private final TrySkipOrAdvance try_RUN_LENGTH = this::try_RUN_LENGTH;
    private final TrySkipOrAdvance try_LIST = this::try_LIST;


    private TrySkipOrAdvance trySkipOrAdvance;

    SetBitsRegionBits(SimpleBitContainerBiteratorCallback callback, BufferReader reader) {
        super(callback, reader);
        reader.populateNextBlock(this);
    }

    @Override
    void type(BlockType blockType) {
        //TODO it it faster to use the FI or a regular switch?
        trySkipOrAdvance = switch (blockType) {
            case BITMAP -> try_BITMAP1;
            case RUN_LENGTH -> try_RUN_LENGTH;
            case LIST -> try_LIST;
        };
    }

    //TODO - promote this to the callback to avoid another indirection
    @Override
    boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
        assert position >= minPosition;
        return trySkipOrAdvance.apply(position, action, actionParameter, 0);
    }

    @Override
    boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
        return trySkipOrAdvance.apply(minPosition, action, actionParameter, 1);
    }


    //handle BITMAP type, for th initial offset within the block
    private boolean try_BITMAP1(long position, IndexedLongConsumer action, int actionParameter, int delta) {
        assert basicChecks(position, delta);

        if (position <= currentBlockBitAddress) {
            action.accept(currentBlockBitAddress, actionParameter);
            minPosition = currentBlockBitAddress + delta;
            return true;
        } else {
            trySkipOrAdvance = try_BITMAP2;
            return try_BITMAP2.apply(position, action, actionParameter, delta);
        }
    }

    //handle BITMAP type, after the initial offset within the block
    private boolean try_BITMAP2(long position, IndexedLongConsumer action, int actionParameter, int delta) {
        assert basicChecks(position, delta);

        var bitOffset = position - currentBlockBitAddress - 1;
        var byteIndex = bitOffset / 8;
        if (byteIndex < bitmap_byteSize) {
            var byteValue = Byte.toUnsignedInt(reader.buffer.get(bitmap_byteStart + (int) byteIndex)) & (0xFF << (bitOffset % 8));
            int nextBit = Integer.numberOfTrailingZeros(byteValue);
            if (nextBit != 32) {
                var resultPosition = currentBlockBitAddress + (byteIndex * 8) + nextBit + 1;
                minPosition = resultPosition + delta;
                action.accept(resultPosition, actionParameter);
                return true;
            }
            while (++byteIndex < bitmap_byteSize) {
                byteValue = Byte.toUnsignedInt(reader.buffer.get(bitmap_byteStart + (int) byteIndex));
                if (byteValue != 0) {
                    nextBit = Integer.numberOfTrailingZeros(byteValue);
                    var resultPosition = currentBlockBitAddress + (byteIndex * 8) + nextBit + 1;
                    minPosition = resultPosition + delta;
                    action.accept(resultPosition, actionParameter);
                    return true;
                }
            }
        }
        return trySkipNextBlock(position, action, actionParameter, delta);
    }

    //handle RUN_LENGTH type
    private boolean try_RUN_LENGTH(long position, IndexedLongConsumer action, int actionParameter, int delta) {
        assert basicChecks(position, delta);

        if (position <= rle_endInc) {
            var resultPosition = Math.max(rle_startInc, position);
            minPosition = resultPosition + delta;
            action.accept(resultPosition, actionParameter);
            return true;
        }
        //TODO consider skipping to the end of the RLE block, rather han scanning through all blocks
        while (common_remaining-- > 0) {
            reader.populateNextRlePair(this);
            if (position <= rle_endInc) {
                var resultPosition = Math.max(rle_startInc, position);
                minPosition = resultPosition + delta;
                action.accept(resultPosition, actionParameter);
                return true;
            }
        }
        return trySkipNextBlock(position, action, actionParameter, delta);
    }
    //handle RUN_LENGTH type
    private boolean try_LIST(long position, IndexedLongConsumer action, int actionParameter, int delta) {
        assert basicChecks(position, delta);

        if (position <= list_nextSet) {
            minPosition = list_nextSet + delta;
            action.accept(list_nextSet, actionParameter);
            return true;
        }
        //TODO consider skipping to the end of the RLE block, rather han scanning through all blocks
        while (common_remaining-- > 0) {
            reader.populateNextListEntry(this);
            if (position <= list_nextSet) {
                minPosition = list_nextSet + delta;
                action.accept(list_nextSet, actionParameter);
                return true;
            }
        }
        return trySkipNextBlock(position, action, actionParameter, delta);
    }

    boolean trySkipNextBlock(long position, IndexedLongConsumer action, int actionParameter, int delta) {
        // simple for the moment - just load the next block and try again
        //but would b a stack overflow in extreme, and we need indexing anyway
        if (reader.buffer.hasRemaining()) {
            reader.populateNextBlock(this);
            return trySkipOrAdvance.apply(position, action, actionParameter, delta);
        } else {
            //TODO
//            return callback.trySkipNextBlock(...)
            return false;
        }
    }
}

