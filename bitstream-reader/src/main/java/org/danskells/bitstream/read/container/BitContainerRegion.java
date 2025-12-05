package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.biterator.Biterator;
import org.danskells.bitstream.read.block.Block;

/**
 * A region within a BitContainer.
 * Conceptually maps to a file or a region of files
 * 
 * A region contains blocks of bits that can be read, and typically some indexing.
 * A region has a start and end address within the BitContainer.
 * 
 */
public abstract class  BitContainerRegion {
  protected final long startInclusive;
  protected final long endExclusive;

  public BitContainerRegion(long startInclusive, long endExclusive) {
    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;
  }
  long getStartInclusive() {
    return startInclusive;
  }

  long getEndExclusive() {
    return endExclusive;
  }

  abstract Biterator biterator();

  abstract class BlockContainerBiterator extends Biterator {

    private long currentOffset = startInclusive;
    private Block.BlockBits current;

    abstract Block.BlockBits nextBlockOrNull(long currentOffset);

    @Override
    public boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
      while (current != null && !current.trySkipTo(position, action, actionParameter)) {
        currentOffset += current.controlOffset();
        current = nextBlockOrNull(currentOffset);
        if (current == null) {
          return false;
        }
        currentOffset +=current.initialOffset();
      }
      return current != null;
    }

    @Override
    public boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
      if (current != null && !current.tryIndexedAdvance(action, actionParameter)) {
        currentOffset += current.controlOffset();
        current = nextBlockOrNull(currentOffset);
        if (current == null) {
          return false;
        }
        currentOffset +=current.initialOffset();
      }
      return current != null;
    }
  }
}
