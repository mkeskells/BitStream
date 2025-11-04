package org.danskells.bitstream.common.block;

public class ControlByte {
  public final static int BLOCK_TYPE_SHIFT = 5;
  public final static int BLOCK_SPECIFIC_MASK = (1 << BLOCK_TYPE_SHIFT) - 1;
}
