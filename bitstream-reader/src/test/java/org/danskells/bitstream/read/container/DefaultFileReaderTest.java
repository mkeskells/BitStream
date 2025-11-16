package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.BitContainer;
import org.danskells.bitstream.read.BitContainerStream;
import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.StreamNode;
import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.block.LongArrayBlock;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFileReaderDocTest {

  private enum Mode {
    GENERATE_MD,
    TEST_AGAINST_MD
  }

  //chnage this to ENERATE_MD to regenerate the md files
  private final static Mode mode = Mode.TEST_AGAINST_MD;
  private final static String PREFIX = "../bitstream-common/binary-form/examples/";

  public static Stream<Arguments> bitmapTestsData() {
    return Stream.of(
        Arguments.of(new BitmapTestData("Simple example", new byte[]{
                0x00,                      // offset = 0 (vint)
                0x04,                      // control byte: bitmap block, 4 bytes
                0x01,                      // bitmap data: bits 0, 1
                0x04,                      // bitmap data: bit 11
                0x05,                      // bitmap data: bits 17, 19
                (byte) 0xff                // bitmap data: bits 25-32))
            }, "ex-01.md", "0,1,11,17,19,25,26,27,28,29,30,31,32")
        ));
  }

  record BitmapTestData(String name, byte[] data, String mdDocFileName, String expectedBits) {
  }

  @ParameterizedTest
  @MethodSource("bitmapTestsData")
  void testBitmapBlockReadAndOutput(BitmapTestData testData) throws IOException {
    // Create a byte array with the example from container-structure.md
    byte[] data = testData.data();
    Path path = Path.of(PREFIX, "bitmap", testData.mdDocFileName());
    String expectedContent = switch (mode) {
      case TEST_AGAINST_MD -> String.join("\n",
          Files.readAllLines(path));
      case GENERATE_MD -> "XXXXX"; //dummy
    };

    ByteBuffer buffer = ByteBuffer.wrap(data);
    IRead intReader = MsbReader.INSTANCE;

    // Capture console output
    ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
    PrintStream captureStream = new PrintStream(outputCapture);
    DebugBufferReader reader = new DebugBufferReader(buffer, intReader, captureStream);
    reader.out().println("```txt");
    Block block = reader.readBlock();
    reader.out().println();
    reader.out().print("```");

    String output = outputCapture.toString().replace("\r\n", "\n");


    // Verify the bitmap contains the expected bits
    switch (block) {
      case BitmapBlock bitmapBlock -> {
        var stream = bitmapBlock.getStreamNode();
        assertEquals(testData.expectedBits, allBits(stream));
      }
      default -> fail("Expected a BitmapBlock");
    }

    switch (mode) {
      case GENERATE_MD -> {
        // Overwrite the expected content file
        Files.writeString(path,
            output.replace("\n", System.lineSeparator()));
      }
      case TEST_AGAINST_MD -> {
        // Compare output to expected content
        assertEquals(expectedContent, output, "Output did not match expected content for " + testData.name());
      }
    }
  }

  private String allBits(StreamNode stream) {
    StringBuilder sb = new StringBuilder();
    long nextBit;
    while ((nextBit = stream.next()) != BitContainerStream.END_OF_STREAM) {
      if (!sb.isEmpty()) {
        sb.append(",");
      }
      sb.append(nextBit);
    }
    return sb.toString();
  }


}