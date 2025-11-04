package org.danskells.bitstream.read.container.header;

public class ContainerHeader {
  public final long initialOffset;
  public final int headerSize;

  public ContainerHeader(long initialOffset, int headerSize) {
    this.initialOffset = initialOffset;
    this.headerSize = headerSize;
  }
}
