package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.biterator.Biterator;
import org.danskells.bitstream.read.block.Block;
import org.danskells.bitstream.read.coder.MsbReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A region within a BitContainer.
 * Conceptually maps to a file or a region of files
 * 
 * A region contains blocks of bits that can be read, and some indexing.
 * A region has a start and end address within the BitContainer.
 * 
 */
public class BitContainerRegion {
  private final long startInclusive;
  private final long endExclusive;
  private final ByteBuffer buffer;

  public BitContainerRegion(long startInclusive, long endExclusive, ByteBuffer buffer) {
    this.startInclusive = startInclusive;
    this.endExclusive = endExclusive;
    this.buffer = buffer;
  }

  public Biterator biterator() {
    var readOnly = buffer.asReadOnlyBuffer();
    //asReadOnly does not preserve byte order
    readOnly.order(ByteOrder.LITTLE_ENDIAN);
    return new ByteBufferBiterator(startInclusive, endExclusive, readOnly);
  }

  long getStartInclusive() {
    return startInclusive;
  }

  long getEndExclusive() {
    return endExclusive;
  }

}
