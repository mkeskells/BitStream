package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.StreamNode;

import java.nio.ByteBuffer;

public class ByteBufferStreamNode implements StreamNode {
  public ByteBufferStreamNode(long startInclusive, long endExclusive, ByteBuffer backingBuffer) {

  }

  public void seekToBlockPosition(long position) {
  }

  @Override
  public long next() {
    return 0;
  }

  @Override
  public long skipToNext(long start) {
    return 0;
  }
}
