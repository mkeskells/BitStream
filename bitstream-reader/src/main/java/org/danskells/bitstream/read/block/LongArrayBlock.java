package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.StreamNode;

public class LongArrayBlock extends AbstractLongArrayBlock {

  private final long[] values;

  public LongArrayBlock(long[] values) {
    this.values = values;
  }

  public StreamNode getStreamNode() {
    return new LongArrayBlockStreamNode();
  }


  private class LongArrayBlockStreamNode extends AbstractLongArrayBlockStreamNode {

    public LongArrayBlockStreamNode() {
      super();
      reset(values, values.length);
    }
  }
}
