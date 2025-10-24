package org.danskells.bitstream;

public interface ReadableBitContainer {

    StreamNode getStreamNode(long startInclusive, long endExclusive);

}

