package org.danskells.bitstream.test.coder;


import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.write.coder.IWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class DebugIReadWrite implements IRead, IWrite {
  private final static Logger LOGGER = LoggerFactory.getLogger(DebugIReadWrite.class);
  public final static DebugIReadWrite INSTANCE = new DebugIReadWrite();
  private enum Type {
    ULONG_START,
    UINT_START,
    USHORT_START,
    ULONG_CONTINUE,
    UINT_CONTINUE,
    USHORT_CONTINUE;
    public String full() {
      return this.name()+"("+this.ordinal()+")";
    }
    private static Type[] all = values();
    public static String fullFromOrdinal(int ordinal) {
      if (ordinal < 0 || ordinal >= all.length) {
        return "Unknown("+ordinal+")";
      }
      return all[ordinal].full();
    }
  }


  private DebugIReadWrite() {
  }

  @Override
  public long readULong(ByteBuffer buffer) {
    long result = read(buffer, Type.ULONG_START);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);

    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);

    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);

    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    result = (result <<4) + read(buffer, Type.ULONG_CONTINUE);
    return result;
  }

  @Override
  public int readUInt(ByteBuffer buffer) {
    int result = read(buffer, Type.UINT_START);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);

    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    result = (result <<4) + read(buffer, Type.UINT_CONTINUE);
    return result;
  }

  @Override
  public char readUShort(ByteBuffer buffer) {
    int result = read(buffer, Type.USHORT_START);
    result = (result <<4) + read(buffer, Type.USHORT_CONTINUE);
    result = (result <<4) + read(buffer, Type.USHORT_CONTINUE);
    result = (result <<4) + read(buffer, Type.USHORT_CONTINUE);
    return (char) result;
  }

  private byte read(ByteBuffer buffer, Type type) {
    var read = buffer.get();
    var typeRead = read >>> 4;
    if (typeRead != type.ordinal()) {
      throw new IllegalArgumentException("Expected type " + type.full() +" but got " + Type.fullFromOrdinal(typeRead));
    }
    return (byte) (read & 0x0F);
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, long value) {
    write(buffer, Type.ULONG_START, value >> 60);
    write(buffer, Type.ULONG_CONTINUE, value >> 56);
    write(buffer, Type.ULONG_CONTINUE, value >> 52);
    write(buffer, Type.ULONG_CONTINUE, value >> 48);

    write(buffer, Type.ULONG_CONTINUE, value >> 44);
    write(buffer, Type.ULONG_CONTINUE, value >> 40);
    write(buffer, Type.ULONG_CONTINUE, value >> 36);
    write(buffer, Type.ULONG_CONTINUE, value >> 32);

    write(buffer, Type.ULONG_CONTINUE, value >> 28);
    write(buffer, Type.ULONG_CONTINUE, value >> 24);
    write(buffer, Type.ULONG_CONTINUE, value >> 20);
    write(buffer, Type.ULONG_CONTINUE, value >> 16);

    write(buffer, Type.ULONG_CONTINUE, value >> 12);
    write(buffer, Type.ULONG_CONTINUE, value >> 8);
    write(buffer, Type.ULONG_CONTINUE, value >> 4);
    write(buffer, Type.ULONG_CONTINUE, value >> 0);
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, int value) {

    write(buffer, Type.UINT_START, value >> 28);
    write(buffer, Type.UINT_CONTINUE, value >> 24);
    write(buffer, Type.UINT_CONTINUE, value >> 20);
    write(buffer, Type.UINT_CONTINUE, value >> 16);

    write(buffer, Type.UINT_CONTINUE, value >> 12);
    write(buffer, Type.UINT_CONTINUE, value >> 8);
    write(buffer, Type.UINT_CONTINUE, value >> 4);
    write(buffer, Type.UINT_CONTINUE, value >> 0);
  }

  @Override
  public void writeUnsigned(ByteBuffer buffer, char value) {
    write(buffer, Type.USHORT_START, value >> 12);
    write(buffer, Type.USHORT_CONTINUE, value >> 8);
    write(buffer, Type.USHORT_CONTINUE, value >> 4);
    write(buffer, Type.USHORT_CONTINUE, value >> 0);

  }

  private void write(ByteBuffer buffer, Type debugCIntType, long i) {
    buffer.put((byte)((debugCIntType.ordinal() <<4) | (i & 0xF)));
  }

  @Override
  public int sizeOf(long value) {
    return 16;
  }

  @Override
  public int sizeOf(int value) {
    return 8;
  }

  @Override
  public int sizeOf(char value) {
    return 4;
  }
}
