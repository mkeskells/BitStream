package org.danskells.bitstream;
/**
 * A Block represents a segment of a BitContainer.
 * It's a structure that holds a collection of bits within a specific range.
 * It provides a StreamNode to iterate over the bits set within that block.
 */
public interface Block {

    StreamNode getStreamNode();
}
