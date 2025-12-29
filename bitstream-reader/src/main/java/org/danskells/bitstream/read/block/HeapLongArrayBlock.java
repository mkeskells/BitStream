package org.danskells.bitstream.read.block;

public class HeapLongArrayBlock extends LongArrayBlock {

    private final long[] values;

    public HeapLongArrayBlock(long initialOffset, long[] values) {
        this.values = values;
        reset(initialOffset, 0, values.length);
    }

    @Override
    public BlockBits bits(long baseOffset) {
        var result = new HeapLongArrayBlockBits();
        result.reset(baseOffset);
        return result;
    }

    class HeapLongArrayBlockBits extends LongArrayBlockBits {
        private int currentIndex = -1;

        @Override
        long getNextValue() {
            currentIndex++;
            return values[currentIndex];
        }
    }
}
