package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.biterator.Biterator;
import org.danskells.bitstream.read.block.Block;

public class HeapBitContainerRegion extends BitContainerRegion {

  private final Block[] blocks;

  public HeapBitContainerRegion(long startInclusive, long endExclusive, Block[] blocks) {
    super(startInclusive,endExclusive);
    this.blocks = blocks;
  }

  @Override
  public Biterator biterator() {
    return new HeapBlockContainerBiterator();
  }

   class HeapBlockContainerBiterator extends BlockContainerBiterator {

    int nextBlock = 0;
    @Override
    Block.BlockBits nextBlockOrNull(long currentOffset) {
      if (nextBlock == blocks.length) {
        return null;
      }
      return blocks[nextBlock++].bits(currentOffset);
    }
  }
}
