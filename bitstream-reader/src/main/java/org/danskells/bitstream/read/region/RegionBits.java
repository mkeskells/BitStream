package org.danskells.bitstream.read.region;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.read.Biterator;
import  org.danskells.bitstream.read.region.SimpleBitContainer.SimpleBitContainerBiterator.SimpleBitContainerBiteratorCallback;

abstract class RegionBits {
    private final static RegionBits EMPTY = new RegionBits() {
        @Override
        boolean trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter) {
            return false;
        }

        @Override
        boolean tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter) {
            return false;
        }
    };

    public static RegionBits empty() {
        return EMPTY;
    }
    abstract boolean trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter);

    abstract boolean tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter);
}
abstract class RegionBitsBase extends RegionBits {
    final ContainerBiteratorCallback callback;
    final BufferReader reader;
    //the current position - the minimum next position
    // the min value acceptable for a trySkipTo call
    // the last value applied to the IndexedLongConsumer of a trySkipTo
    // the last value applied to the IndexedLongConsumer of a tryIndexedAdvance (+1)
    long minPosition = Long.MIN_VALUE;

    protected RegionBitsBase(ContainerBiteratorCallback callback, BufferReader reader) {
        this.callback = callback;
        this.reader = reader;
    }

    boolean basicChecks(long position, int delta) {
        assert position >= minPosition;
        assert (delta == 0 || delta == 1);
        return true;
    }

    @FunctionalInterface
    interface TrySkipOrAdvance {
        /**
         *
         * @param position the minumum position acceptable
         * @param action the action to perform
         * @param actionParameter the parameter to the action to perform
         * @param delta 0 to observ the value, 1 to consume it
         * @return trye if the cation was performed, false otherwise
         */
        boolean apply(long position, Biterator.IndexedLongConsumer action, int actionParameter, int delta);
    }

    /** the end position (inclusive) of the region */
    long endInclusive;

    /** the base address of the current block being read */
    long currentBlockBitAddress;

    //common fields
    /** the number of elements remaining. Used in array and rle formats */
    int common_remaining;
    /** the size in bytes of the current block being read (after control, and size)*/
    int common_blockByteSize;


    // bitmap fields
    /** the number of bytes in the bitmap */
    int bitmap_byteSize;
    /** the start position of the bitmap in the buffer */
    int bitmap_byteStart;

    //rle fields
    /** the current/next RLE start (inclusive) */
    long rle_startInc;
    /** the current/next RLE end (inclusive) */
    long rle_endInc;

    //list fields
    /** the current/next element in the list */
    long list_nextSet;

    //initialise following the population of a new block
    abstract void type(BlockType blockType);
}

