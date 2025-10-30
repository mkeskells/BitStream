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
    switch (Long.numberOfLeadingZeros(value)) {
      //< 128
      case 57, 58, 59, 60, 61, 62, 63, 64:
        buffer.put((byte) (value));
        return;
      case 50, 51, 52, 53, 54, 55, 56:
        buffer.put((byte) (0x80 | (value & 0x3f)));
        buffer.put((byte) (value >>> 6));
        return;
      case 43, 44, 45, 46, 47, 48, 49:
        buffer.put((byte) (0xC0 | (value & 0x1f)));
        buffer.putShort((short) (value >>> 5));
        return;
      case 36, 37, 38, 39, 40, 41, 42:
        buffer.put((byte) (0xE0 | (value & 0x0f)));
        buffer.putShort((short) (value >>> 4));
        buffer.put((byte) (value >>> 20));
        return;
      case 29, 30, 31, 32, 33, 34, 35:
        buffer.put((byte) (0xF0 | (value & 0x07)));
        buffer.putInt((int) (value >>> 3));
        return;
      case 22, 23, 24, 25, 26, 27, 28:
        buffer.put((byte) (0xF8 | (value & 0x03)));
        buffer.putInt((int) (value >>> 2));
        buffer.put((byte) (value >>> 34));
        return;
      case 15, 16, 17, 18, 19, 20, 21:
        buffer.put((byte) (0xFC | (value & 0x01)));
        buffer.putInt((int) (value >>> 1));
        buffer.putShort((short) (value >>> 33));
        return;
      case 8, 9, 10, 11, 12, 13, 14:
        buffer.put((byte) (0xFE));
        buffer.putInt((int) (value));
        buffer.putShort((short) (value >>> 32));
        buffer.put((byte) (value >>> 48));
        return;
      default:
        buffer.put((byte) (0xFF));
        //it not worth trying to save a bit here, so break the pattern
        buffer.putLong(value);
        return;
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
}
