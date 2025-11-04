package org.danskells.bitstream.write.coder;

import java.nio.ByteBuffer;

public interface IWrite {

  void writeUnsigned(ByteBuffer buffer, long value);
  void writeUnsigned(ByteBuffer buffer, int value);
  void writeUnsigned(ByteBuffer buffer, char value);

  int sizeOf(long value);
  int sizeOf(int value);
  int sizeOf(char value);
}
