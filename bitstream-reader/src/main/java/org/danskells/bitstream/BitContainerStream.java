package org.danskells.bitstream;

public interface BitContainerStream {
  long END_OF_STREAM = -1;

  /**
   *
   * @return The address of the next bit in the stream,
   * or {@link BitContainerStream#END_OF_STREAM} if the end of the stream is reached
   */
  long nextLong();
}
