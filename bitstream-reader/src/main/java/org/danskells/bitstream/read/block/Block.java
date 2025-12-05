package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.biterator.Biterator;

/**
 * A Block represents a segment of a BitContainer.
 * It's a structure that holds a collection of bits within a specific range.
 * It provides a StreamNode to iterate over the bits set within that block.
 */
public abstract class Block {
  private long initialOffset;
  private long controlOffset;
  protected void reset(long initialOffset, long controlOffset) {
    this.initialOffset = initialOffset;
    this.controlOffset = controlOffset;
  }
  /**
   * the offset from the previous block controlOffset where this block starts
   */
  public long initialOffset() {
    return initialOffset;
  }
  /**
   * the number of bits between the start of this blocka nd the offset to the next block
   * Note - this may not be the same as the range of bits in the block, due to compression or encoding.
   */
  public long controlOffset() {
    return controlOffset;
  }

  /**
   * Provides a BlockBits instance for reading bits from this block.
   * @param baseOffset the offset of the start of this block within the BitContainer
   */
  public abstract BlockBits bits(long baseOffset);


  public abstract class BlockBits {
    protected long baseOffset;
    //-1 meant use the offset
    protected int nextRelativePosition = -1;

    protected void reset(long baseOffset) {
      this.baseOffset = baseOffset;
    }
    public abstract boolean trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter);

    public abstract boolean tryIndexedAdvance (Biterator.IndexedLongConsumer action, int actionParameter);

    public long controlOffset() {
      return controlOffset;
    }
    public long initialOffset() {
      return initialOffset;
    }
  }

}
