package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.StreamNode;

import java.util.Arrays;

import static org.danskells.bitstream.read.BitContainerStream.END_OF_STREAM;

public abstract class AbstractLongArrayBlock implements Block {

  protected abstract static class AbstractLongArrayBlockStreamNode implements StreamNode {

    private int nextIndex = 0;
    private long[] values;
    private int valuesLength;

    protected void reset(long[] values, int valuesLength) {
      nextIndex = 0;
      this.values = values;
      this.valuesLength = valuesLength;
    }

    @Override
    public long next() {
      if (nextIndex == valuesLength) return END_OF_STREAM;
      return values[nextIndex++];
    }

    @Override
    public long skipToNext(long start) {
      var position = Arrays.binarySearch(values, nextIndex, valuesLength, start);
      if (position < 0) position = -position;
      if (position == valuesLength) return END_OF_STREAM;
      nextIndex = position;
      return values[position];
    }
  }
}
