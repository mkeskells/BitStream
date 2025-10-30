package org.danskells.bitstream.test.coder;

import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.danskells.bitstream.write.coder.IWrite;
import org.danskells.bitstream.write.coder.MsbWriter;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

public class MsbIReadWriteTest extends IReadWriteTest{
  @Override
  protected IRead read() {
    return MsbReader.INSTANCE;
  }

  @Override
  protected IWrite write() {
    return MsbWriter.INSTANCE;
  }
  @BeforeAll
  public static void setup() {
    readULongDataExtra = List.of(
        new T2<>(0L, 1),
        new T2<>(127L, 1),
        new T2<>(128L, 2),
        new T2<>(16383L, 2),
        new T2<>(16384L, 3),
        new T2<>(0x1fffffL, 3),
        new T2<>(0x200000L, 4),
        new T2<>(0xfffffffL, 4),
        new T2<>(0x10000000L, 5),
        new T2<>(0x7ffffffffL, 5),
        new T2<>(0x800000000L, 6),
        new T2<>(0x3ffffffffffL, 6),
        new T2<>(0x40000000000L, 7),
        new T2<>(0x1ffffffffffffL, 7),
        new T2<>(0x2000000000000L, 8),
        new T2<>(0xffffffffffffffL, 8),
        new T2<>(0x100000000000000L, 9),
        new T2<>(Long.MAX_VALUE, 9),
        new T2<>(Long.MIN_VALUE, 9),
        new T2<>(-1L, 9)
    );
    readUIntDataExtra = List.of(
        new T2<>(0, 1),
        new T2<>(127, 1),
        new T2<>(128, 2),
        new T2<>(16383, 2),
        new T2<>(16384, 3),
        new T2<>(0x1fffff, 3),
        new T2<>(0x200000, 4),
        new T2<>(0xfffffff, 4),
        new T2<>(0x10000000, 5),
        new T2<>(Integer.MAX_VALUE, 5),
        new T2<>(Integer.MIN_VALUE, 5),
        new T2<>(-1, 5)
    );
    readUShortDataExtra = List.of(
        new T2<>((char)0, 1),
        new T2<>((char)127, 1),
        new T2<>((char)128, 2),
        new T2<>((char)16383, 2),
        new T2<>((char)16384, 3),
        new T2<>((char)65535, 3)
    );
  }
}
