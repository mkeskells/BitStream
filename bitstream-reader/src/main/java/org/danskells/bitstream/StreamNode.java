package org.danskells.bitstream;

public interface StreamNode {
  /**
   *
   * @return The address of the next bit in the stream,
   * or {@link BitContainerStream#END_OF_STREAM} if the end of the stream is reached
   */
  long next();

  /**
   * @param start Advances the current position to the next bit set at or after start.
   *              Must be greater than or equal to the current position
   * @return The address of the next bit in the stream after `start`,
   * or {@link BitContainerStream#END_OF_STREAM} if the end of the stream is reached
   */
  long skipToNext(long start);
}
