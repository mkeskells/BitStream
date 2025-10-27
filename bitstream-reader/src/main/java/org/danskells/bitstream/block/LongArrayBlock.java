package org.danskells.bitstream.block;

import org.danskells.bitstream.Block;
import org.danskells.bitstream.StreamNode;

import java.util.Arrays;

import static org.danskells.bitstream.BitContainerStream.END_OF_STREAM;

public class LongArrayBlock implements Block {

  private final long[] values;


  public LongArrayBlock(long[] values) {
    this.values = values;
  }

  public StreamNode getStreamNode() {
    return new LongArrayBlockStreamNode();
  }

  private class LongArrayBlockStreamNode implements StreamNode {

    int nextIndex = 0;

    @Override
    public long next() {
      if (nextIndex == values.length) return END_OF_STREAM;
      return values[nextIndex++];
    }

    @Override
    public long skipToNext(long start) {
      var position = Arrays.binarySearch(values, start);
      if (position < 0) position = -position;
      if (position == values.length) return END_OF_STREAM;
      nextIndex = position;
      return values[position];
    }
  }
}
