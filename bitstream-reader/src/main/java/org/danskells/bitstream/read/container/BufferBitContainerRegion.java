package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.biterator.Biterator;
import org.danskells.bitstream.read.block.Block;
import org.danskells.bitstream.read.coder.MsbReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferBitContainerRegion extends BitContainerRegion {

  private final ByteBuffer buffer;

  public BufferBitContainerRegion(long startInclusive, long endExclusive, ByteBuffer buffer) {
    super(startInclusive, endExclusive);
    this.buffer = buffer;
  }

  @Override
  public Biterator biterator() {
    var readOnly = buffer.asReadOnlyBuffer();
    readOnly.order(ByteOrder.LITTLE_ENDIAN);
    return new BufferBlockContainerBiterator(readOnly);
  }

  class BufferBlockContainerBiterator extends BlockContainerBiterator {

    private final ByteBuffer buffer;
    private final static MsbReader reader = MsbReader.INSTANCE;

    public BufferBlockContainerBiterator(ByteBuffer buffer) {
      super();
      this.buffer = buffer;
    }

    @Override
    Block.BlockBits nextBlockOrNull(long currentOffset) {
      if (!buffer.hasRemaining()) {
        return null;
      }
      //TODO: reuse a single block instance instead of creating new ones
      //and merge the red mechnaism from other branches
      return null;
    }
  }
}
