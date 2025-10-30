package org.danskells.bitstream.test.coder;

import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.write.coder.IWrite;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

public class DebugIReadWriteTest extends IReadWriteTest{
  @Override
  protected IRead read() {
    return DebugIReadWrite.INSTANCE;
  }

  @Override
  protected IWrite write() {
    return DebugIReadWrite.INSTANCE;
  }
  @BeforeAll
  public static void setup() {
    readULongDataExtra = List.of(
        new T2<>(0L, 16),
        new T2<>(98765432109876L, 16),
        new T2<>(55555555555555L, 16),
        new T2<>(99999999999999L, 16)
    );
    readUIntDataExtra = List.of(
        new T2<>(0, 8),
        new T2<>(12345, 8),
        new T2<>(555555, 8),
        new T2<>(2000120303, 8)
    );
    readUShortDataExtra = List.of(
        new T2<>((char)0, 4),
        new T2<>((char)1,4),
        new T2<>((char)99, 4),
        new T2<>((char)65535, 4)
    );
  }
}
