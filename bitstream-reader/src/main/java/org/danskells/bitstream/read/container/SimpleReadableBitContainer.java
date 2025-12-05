package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.*;
import org.danskells.bitstream.read.biterator.Biterator;

import java.util.Arrays;
import java.util.function.LongConsumer;

public class SimpleReadableBitContainer implements BitContainer {

  private final Block[] blocks;

  private final long[] offsets;

  public SimpleReadableBitContainer(Block[] blocks, long[] offsets) {
    assert (blocks.length == offsets.length);
    this.blocks = blocks;
    this.offsets = offsets;
  }

  @Override
  public Biterator biterator() {
    return new BlockContainerBiterator();
  }

  private class BlockContainerBiterator implements Biterator {

    int currentBlock = 0;
    StreamNode currentStreamNode = blocks[currentBlock].getStreamNode();
    long currentOffset = offsets[currentBlock];


    @Override
    public boolean tryAdvance(LongConsumer action) {
      var positionInStreamNode = currentStreamNode.next();
      if (positionInStreamNode == BitContainerStream.END_OF_STREAM) {
        if (currentBlock + 1 == blocks.length) return false;
        currentStreamNode = blocks[++currentBlock].getStreamNode();
        positionInStreamNode = currentStreamNode.next();
        currentOffset = offsets[currentBlock];
      }
      action.accept(currentOffset + positionInStreamNode);
      return true;

    }

    @Override
    public long advanceTo(long start) {

      // Case 1: in the same block
      long position = skipToRelativePosition(start);
      if (position != BitContainerStream.END_OF_STREAM) return currentOffset + position;

      // Case 2: in a future block

      var blockPosition = Arrays.binarySearch(offsets, start);
      if (blockPosition > 0) {
        moveToNode(blockPosition);
        return currentOffset;
      }
      blockPosition = -blockPosition - 1;
      moveToNode(blockPosition);
      position = skipToRelativePosition(start);
      if (position != BitContainerStream.END_OF_STREAM) {
        return currentOffset + position;
      }

      // Case 4: Beyond all blocks

      if (blockPosition == blocks.length) return BitContainerStream.END_OF_STREAM;

      // Case 3: Between two blocks

      moveToNode(blockPosition + 1);
      return currentOffset;

    }

    private long skipToRelativePosition(long start) {
      return currentStreamNode.skipToNext(start - currentOffset);
    }

    private void moveToNode(int blockPosition) {
      currentBlock = blockPosition;
      currentStreamNode = blocks[blockPosition].getStreamNode();
      currentOffset = offsets[blockPosition];
    }
  }
}
