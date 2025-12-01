package org.danskells.bitstream.write.coder;

import java.nio.ByteBuffer;

/**
 * MSB Coded Integers
 * <p>
 * Uses the high order bits of the first byte to indicate how many bytes are used to store the value.
 * The remaining bits in the first byte and all bits in subsequent bytes are used to store the value.
 * Conceptually similar to using the top bit of a byte, but his is faster and less branchy to encode.
 * <p>
 *   <pre></pre>
 * Encoding:
 * 0xxxxxxx                             : 1 byte
 * 10xxxxxx xxxxxxxx                    : 2 bytes
 * 110xxxxx xxxxxxxx xxxxxxxx           : 3 bytes
 * 1110xxxx xxxxxxxx xxxxxxxx xxxxxxxx  : 4 bytes
 * 11110xxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx : 5 bytes
 * 111110xx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx : 6 bytes
 * 1111110x xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx : 7 bytes
 * 11111110 xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx : 8 bytes
 * 11111111 xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx : 9 bytes
 * </pre
 */
public class MsbWriter implements IWrite {
  public final static MsbWriter INSTANCE = new MsbWriter();

  private MsbWriter() {
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, long value) {
    switch (LEADING_ZERO_TO_SIZE[Long.numberOfLeadingZeros(value)]) {
      //< 128
      case 1:
        buffer.put((byte) (value));
        return;
      case 2:
        buffer.put((byte) (0x80 | (value & 0x3f)));
        buffer.put((byte) (value >>> 6));
        return;
      case 3:
        buffer.put((byte) (0xC0 | (value & 0x1f)));
        buffer.putShort((short) (value >>> 5));
        return;
      case 4:
        buffer.put((byte) (0xE0 | (value & 0x0f)));
        buffer.putShort((short) (value >>> 4));
        buffer.put((byte) (value >>> 20));
        return;
      case 5:
        buffer.put((byte) (0xF0 | (value & 0x07)));
        buffer.putInt((int) (value >>> 3));
        return;
      case 6:
        buffer.put((byte) (0xF8 | (value & 0x03)));
        buffer.putInt((int) (value >>> 2));
        buffer.put((byte) (value >>> 34));
        return;
      case 7:
        buffer.put((byte) (0xFC | (value & 0x01)));
        buffer.putInt((int) (value >>> 1));
        buffer.putShort((short) (value >>> 33));
        return;
      case 8:
        buffer.put((byte) (0xFE));
        buffer.putInt((int) (value));
        buffer.putShort((short) (value >>> 32));
        buffer.put((byte) (value >>> 48));
        return;
      case 9:
        buffer.put((byte) (0xFF));
        //it not worth trying to save a bit here, so break the pattern
        buffer.putLong(value);
        return;
      default:
          throw new IllegalStateException("Unreachable");
    }
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, int value) {
    writeUnsigned(buffer, Integer.toUnsignedLong(value));
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, char value) {
    writeUnsigned(buffer, (long) value);
  }

  private final static byte[] LEADING_ZERO_TO_SIZE = new byte[]{
    9, 9, 9, 9, 9, 9, 9, 9, //0  - 7
    8, 8, 8, 8, 8, 8, 8,    //8  - 14
    7, 7, 7, 7, 7, 7, 7,    //15 - 21
    6, 6, 6, 6, 6, 6, 6,    //22 - 28
    5, 5, 5, 5, 5, 5, 5,    //29 - 35
    4, 4, 4, 4, 4, 4, 4,    //36 - 42
    3, 3, 3, 3, 3, 3, 3,    //43 - 49
    2, 2, 2, 2, 2, 2, 2,    //50 - 56
    1, 1, 1, 1, 1, 1, 1, 1  //57 - 64
  };
  @Override
  public int sizeOf(long value) {
    return LEADING_ZERO_TO_SIZE[Long.numberOfLeadingZeros(value)];
  }

  @Override
  public int sizeOf(int value) {
    return sizeOf(Integer.toUnsignedLong(value));
  }

  @Override
  public int sizeOf(char value) {
    return sizeOf((long)value);
  }
}
