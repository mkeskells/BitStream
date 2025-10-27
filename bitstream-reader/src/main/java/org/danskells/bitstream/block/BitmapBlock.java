package org.danskells.bitstream.block;

import org.danskells.bitstream.Block;
import org.danskells.bitstream.StreamNode;

import java.util.BitSet;

public class BitmapBlock implements Block {

  private final BitSet bitset;

  public BitmapBlock(BitSet bitset) {
    this.bitset = bitset;
  }

  public StreamNode getStreamNode() {
    return new BitmapBlockStreamNode();
  }

  private class BitmapBlockStreamNode implements StreamNode {

    int nextIndex = bitset.nextSetBit(0);

    @Override
    public long next() {
      var currentIndex = nextIndex;
      nextIndex = bitset.nextSetBit(nextIndex + 1);
      return currentIndex;
    }

    @Override
    public long skipToNext(long start) {
      nextIndex = bitset.nextSetBit((int) start);
      return nextIndex;
    }
  }
}
