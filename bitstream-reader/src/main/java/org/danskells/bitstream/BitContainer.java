package org.danskells.bitstream;

public interface BitContainer {

  StreamNode getStreamNode(long startInclusive, long endExclusive);

}

