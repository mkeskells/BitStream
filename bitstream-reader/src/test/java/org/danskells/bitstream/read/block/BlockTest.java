package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.Biterator;
import org.danskells.bitstream.read.coder.MsbReader;
import org.danskells.bitstream.read.region.*;
import org.danskells.bitstream.write.coder.MsbWriter;
import org.danskells.bitstream.write.container.BitContainerWriter;
import org.danskells.bitstream.write.container.ByteBufferAllocator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.danskells.bitstream.read.region.AbstractFileReaderDocTest.allBits;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class BlockTest {

  public static TestBlock buildBitSetBits(long regionStart, long firstBit, long secondBit, long... bits) {
    var bitset = new BitSet();
    bitset.set(0);
    bitset.set(Math.toIntExact(secondBit - firstBit));
    for (var index = 0; index < bits.length; index++) {
      bitset.set(Math.toIntExact(bits[index] - firstBit));
    }

    var writer = new BitContainerWriter(regionStart, 1024, ByteBufferAllocator.HEAP, MsbWriter.INSTANCE);
    long lastBit = bits.length == 0 ? secondBit : bits[bits.length - 1];
    writer.writeBitmap(firstBit - regionStart -1, bitset, 0, Math.toIntExact(lastBit - firstBit));
    var written = writer.asReadOnlyBuffer();

    long controlPoint = (lastBit +7) & 0xFFFFFFFFFFFFFFF8L;

    var list = new ArrayList<Long>();
    list.add(firstBit);
    list.add(secondBit);
    for (long v : bits) {
      list.add(v);
    }

    return new TestBlock(regionStart, controlPoint, written, list);
  }
  public static TestBlock buildArrayBits(long regionStart, long firstBit, long secondBit, long... bits) {

    var writer = new BitContainerWriter(regionStart, 1024, ByteBufferAllocator.HEAP, MsbWriter.INSTANCE);
    var values = new long[bits.length+2];
    values[0] = firstBit;
    values[1] = secondBit;
    System.arraycopy(bits, 0, values, 2, bits.length);

    writer.writeArray(values, 0, values.length);
    var written = writer.asReadOnlyBuffer();

    int controlOffset = Math.toIntExact((bits.length == 0? secondBit: bits[bits.length - 1]) - firstBit + 1);

    var list = new ArrayList<Long>();
    list.add(firstBit);
    list.add(secondBit);
    for (long v : bits) {
      list.add(v);
    }

    return new TestBlock(regionStart, controlOffset, written, list);
  }
  record TestBlock(long regionStart, long controlOffset, ByteBuffer bytes, ArrayList<Long> values) {
  }
  public static Stream<Arguments> bitsetArguments() {
    return Stream.of(
        Arguments.of(
            "Single",
            buildBitSetBits(-1, 0, 1)
        ),
        Arguments.of(
            "Packed",
            buildBitSetBits(-1, 0, 1, 2, 3, 4, 5)
        ),
        Arguments.of(
            "Sparse",
            buildBitSetBits(-1, 0, 2, 4, 6, 8)
        ),
        Arguments.of(
            "Non-zero start",
            buildBitSetBits(-1, 2, 3, 4)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("bitsetArguments")
  void iterateBitsetBlock(String description, TestBlock data) {
    var debugText = getDebugInfo(data);
    var bitmapBlock = data.bytes;
    var region = new BitRegion(data.regionStart, Long.MAX_VALUE, bitmapBlock);

    var stream = new SimpleBitContainer(List.of(region)).setBits();
    var actual =  stream.stream().boxed().toList();

    assertEquals(data.values, actual, "values match for " + description+"\n\nDebug info:\n"+debugText);
  }

  private static String getDebugInfo(TestBlock data) {
    var buffer = data.bytes.duplicate();
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    var outputCapture = new ByteArrayOutputStream();
    var captureStream = new PrintStream(outputCapture);
    AtomicReference<DebugBufferReader> readerRef = new AtomicReference<>();
    var region2 = new BitRegion(data.regionStart, Long.MAX_VALUE, buffer) {
      @Override
      protected BufferReader newBufferReader(ByteBuffer readOnly, MsbReader instance) {
        var result = DebugBufferReader.create(readOnly, instance, captureStream, () -> outputCapture.toString());
        result.out().println("```txt");

        readerRef.set(result);
        return result;
      }
    };
    var callback = new ContainerBiteratorCallback() {

      @Override
      public boolean nextRegion_trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter) {
        var debugBufferReader = readerRef.get();
        debugBufferReader.checkEndOfBlock(debugBufferReader.buffer().position());
        debugBufferReader.out().println();
        debugBufferReader.out().print("```");
        return false;
      }

      @Override
      public boolean nextRegion_tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter) {
        var debugBufferReader = readerRef.get();
        debugBufferReader.checkEndOfBlock(debugBufferReader.buffer().position());
        debugBufferReader.out().println();
        debugBufferReader.out().print("```");
        return false;
      }
    };
    var bits = region2.bits(true, callback);
    var content = allBits(bits);
    var debugText = outputCapture.toString() +"All bits: " +content;
    return debugText;
  }


  @ParameterizedTest
  @MethodSource("arrayArguments")
  void iterateArrayBlock(String description, TestBlock data) {
    var debugText = getDebugInfo(data);
    var bitmapBlock = data.bytes;
    var region = new BitRegion(data.regionStart, Long.MAX_VALUE, bitmapBlock);


    var stream = new SimpleBitContainer(List.of(region)).setBits();
    var actual =  stream.stream().boxed().toList();
    assertEquals(data.values, actual, "values match for " + description+"\n\nDebug info:\n"+debugText);
  }

  public static Stream<Arguments> arrayArguments() {
    return Stream.of(
        Arguments.of(
            "Single",
            buildArrayBits(-1000, 0L, 99L)
        ),
        Arguments.of(
            "Packed",
            buildArrayBits(-100, 0L, 1L, 2L, 3L, 4L, 5L, 6L)
        ),
        Arguments.of(
            "Sparse",
            buildArrayBits(-100, 0L, 1000L, 20000L, 30000L, 40000L)
        ),
        Arguments.of(
            "Non-zero start",
            buildArrayBits(-100, 1000L, 1010L, 1020L)
        )
    );
  }
}
