package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.BitContainer;
import org.danskells.bitstream.read.StreamNode;

import java.nio.ByteBuffer;

/**
 * A BitContainer backed by a ByteBuffer.
 */
public class ByteBufferContainer implements BitContainer {
  private final long start;
  private final long end;
  private final ByteBuffer backingBuffer;

  public ByteBufferContainer(long start, long end, ByteBuffer backingBuffer) {
    this.start = start;
    this.end = end;
    this.backingBuffer = backingBuffer;
  }

  @Override
  public StreamNode getStreamNode(long startInclusive, long endExclusive) {
    if (startInclusive < start || endExclusive > end) {
      throw new IllegalArgumentException();
    }
    startInclusive -= start;
    endExclusive -= start;

    long position = findBlock(startInclusive);
    var result = new ByteBufferStreamNode(startInclusive, endExclusive, backingBuffer);
    result.seekToBlockPosition(position);
    return result;
  }

  private long findBlock(long startInclusive) {
return 0L;
  }
}
