package org.danskells.bitstream.test.coder;

import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.write.coder.IWrite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class IReadWriteTest {

  protected ByteBuffer underTest;
  protected IRead read;
  protected IWrite write;

  protected abstract IRead read();
  protected abstract IWrite write();
  @BeforeEach
  void setUp() {
    underTest = ByteBuffer.allocateDirect(100);
    read = read();
    write = write();
  }
  @AfterEach
  void teardown() {
    underTest = null;
  }
  @BeforeAll
  public static void setup() {
    readULongDataExtra = List.of();
    readUIntDataExtra = List.of();
    readUShortDataExtra = List.of();
  }

  protected static Stream<Arguments> readULongData() {
    return Stream.of(
        Arguments.of(0L, -1),
        Arguments.of(1L, -1),
        Arguments.of(255L, -1),
        Arguments.of(256L, -1),
        Arguments.of(65535L, -1),
        Arguments.of(65536L, -1),
        Arguments.of(16777215L, -1),
        Arguments.of(16777216L, -1),
        Arguments.of(4294967295L, -1),
        Arguments.of(4294967296L, -1),
        Arguments.of(1099511627775L, -1),
        Arguments.of(1099511627776L, -1),
        Arguments.of(281474976710655L, -1),
        Arguments.of(281474976710656L, -1),
        Arguments.of(72057594037927935L, -1),
        Arguments.of(72057594037927936L, -1),
        Arguments.of(-1L, -1)
    );
  }
  protected static List<T2<Long,Integer>> readULongDataExtra;

  protected static Stream<Arguments> readULongDataExtra() {
    return readULongDataExtra.stream().map(t -> Arguments.of(t._1, t._2));
  }

  @ParameterizedTest
  @MethodSource("readULongData")
  @MethodSource("readULongDataExtra")
  void readULong(long value, int size) {
    write.writeUnsigned(underTest, value);
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after write");
    }
    underTest.flip();
    var readValue = read.readULong(underTest);
    assertEquals(value, readValue, "Value mismatch \n"+Long.toBinaryString(value)+" vs \n"+Long.toBinaryString(readValue));
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after read");
    }

  }

  protected static Stream<Arguments> readUIntData() {
    return Stream.of(
        Arguments.of(0, -1),
        Arguments.of(1, -1),
        Arguments.of(255, -1),
        Arguments.of(256, -1),
        Arguments.of(65535, -1),
        Arguments.of(65536, -1),
        Arguments.of(16777215, -1),
        Arguments.of(16777216, -1),
        Arguments.of(-1, -1)
    );
  }
  protected static List<T2<Integer,Integer>> readUIntDataExtra;

  protected static Stream<Arguments> readUIntDataExtra() {
    return readUIntDataExtra.stream().map(t -> Arguments.of(t._1, t._2));
  }

  @ParameterizedTest
  @MethodSource("readUIntData")
  @MethodSource("readUIntDataExtra")
  void readUInt(int value, int size) {
    write.writeUnsigned(underTest, value);
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after write");
    }
    underTest.flip();
    var readValue = read.readUInt(underTest);
    assertEquals(value, readValue, "Value mismatch \n"+Integer.toBinaryString(value)+" vs \n"+Integer.toBinaryString(readValue));
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after read");
    }
  }

  protected static Stream<Arguments> readUShortData() {
    return Stream.of(
        Arguments.of((short)(char)0, -1),
        Arguments.of((short)(char)1, -1),
        Arguments.of((short)(char)127, -1),
        Arguments.of((short)(char)128, -1),
        Arguments.of((short)(char)255, -1),
        Arguments.of((short)(char)256, -1),
        Arguments.of((short)(char)32767, -1),
        Arguments.of((short)(char)65535, -1)
    );
  }
  protected static List<T2<Character,Integer>> readUShortDataExtra;

  protected static Stream<Arguments> readUShortDataExtra() {
    return readUShortDataExtra.stream().map(t -> Arguments.of((short)(char)t._1, t._2));
  }

  @ParameterizedTest
  @MethodSource("readUShortData")
  @MethodSource("readUShortDataExtra")
  void readUShort(short value, int size) {
    write.writeUnsigned(underTest, (char)value);
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after write");
    }
    underTest.flip();
    var readValue = read.readUShort(underTest);
    assertEquals(value, (short)readValue, "Value mismatch \n"+
        Integer.toBinaryString(Short.toUnsignedInt(value))+" vs \n"+
        Integer.toBinaryString(Short.toUnsignedInt((short)readValue)));
    if (size >= 0) {
      assertEquals(size, underTest.position(), "Size mismatch after read");
    }
  }
}