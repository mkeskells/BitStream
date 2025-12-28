package org.danskells.bitstream.read.container;


import org.danskells.bitstream.read.biterator.Biterator;

import java.nio.ByteBuffer;

/**
 * A BitContainer backed by a ByteBuffer.
 */
public class ByteBufferRegion extends BitContainerRegion {
  private final ByteBuffer backingBuffer;

  public ByteBufferRegion(long start, long end, ByteBuffer backingBuffer) {
    super(start, end);
    this.backingBuffer = backingBuffer;
  }

  @Override
  public Biterator biterator() {
      return new ByteBufferBiterator(startInclusive, endExclusive, backingBuffer);
  }
}
