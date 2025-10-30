package org.danskells.bitstream.read.coder;

import java.nio.ByteBuffer;

/**
 * MSB Coded Integers
 * <p>
 * Uses the high order bits of the first byte to indicate how many bytes are used to store the value.
 * The remaining bits in the first byte and all bits in subsequent bytes are used to store the value.
 * Conceptually similar to using the top bit of a byte, but his is faster and less branchy to decode.
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
public class MsbReader implements IRead {
  public final static MsbReader INSTANCE = new MsbReader();
  private MsbReader() {
  }

  @Override
  public long readULong(ByteBuffer buffer) {
    var b1 = buffer.get();
    if ((b1 & 0x80) == 0) {
      return Byte.toUnsignedLong(b1);
    }
    //when we widen the byte it will be -ve so the sign extends
    //negate it and the count leading zeros to find number of bytes
    switch (Integer.numberOfLeadingZeros(~b1) - 24) {
      case 1: {
        var b2 = buffer.get();
        return (b1 & 0x3F)
            | (Byte.toUnsignedLong(b2) << 6);
      }
      case 2: {
        var b2 = buffer.getShort();
        return (b1 & 0x1F)
            | (Short.toUnsignedLong(b2) << 5);
      }
      case 3: {
        var b2 = buffer.getShort();
        var b3 = buffer.get();
        return (b1 & 0x0F)
            | (Short.toUnsignedLong(b2) << 4)
            | (Byte.toUnsignedLong(b3) << 20);
      }
      case 4: {
        var b2 = buffer.getInt();
        return (b1 & 0x07)
            | (Integer.toUnsignedLong(b2) << 3);
      }
      case 5: {
        var b2 = buffer.getInt();
        var b3 = buffer.get();
        return (b1 & 0x03)
            | (Integer.toUnsignedLong(b2) << 2)
            | (Byte.toUnsignedLong(b3) << 34);
      }
      case 6: {
        var b2 = buffer.getInt();
        var b3 = buffer.getShort();
        return (b1 & 0x01)
            | (Integer.toUnsignedLong(b2) << 1)
            | (Short.toUnsignedLong(b3) << 33);
      }
      case 7: {
        var b2 = buffer.getInt();
        var b3 = buffer.getShort();
        var b4 = buffer.get();
        return
            (Integer.toUnsignedLong(b2) )
            | (Short.toUnsignedLong(b3) << 32)
            | (Byte.toUnsignedLong(b4) << 48);
      }
      case 8:
        return buffer.getLong();
      default:
        throw new IllegalStateException("Unreachable");
    }
  }

  @Override
  public int readUInt(ByteBuffer buffer) {
    var result = readULong(buffer);
    if ((result & 0xFFFF_FFFF_0000_0000L) != 0) {
      throw new IllegalArgumentException("Value too large for uint: " + result);
    }
    return (int)result;
  }

  @Override
  public char readUShort(ByteBuffer buffer) {
    var result = readULong(buffer);
    if ((result & 0xFFFF_FFFF_FFFF_0000L) != 0) {
      throw new IllegalArgumentException("Value too large for ushort: " + result+ "   0x"+Long.toHexString(result));
    }
    return (char)result;
  }
}
