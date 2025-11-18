package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.read.BitContainerStream;
import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.StreamNode;
import org.danskells.bitstream.read.coder.IRead;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractFileReaderDocTest {

  private enum Mode {
    GENERATE_MD,
    TEST_AGAINST_MD
  }

  //change this to ENERATE_MD to regenerate the md files
  private final static Mode mode = Mode.TEST_AGAINST_MD;
  private final static String PREFIX = "../bitstream-common/binary-form/examples/";


  interface TestData {
    String name();

    byte[] data();

    String mdDocFileName();

    String mdDir();

    IRead intReader();

    default Path fullPath() {
      return Path.of(PREFIX, mdDir(), mdDocFileName());
    }

    default String expectedContent() throws IOException {
      return switch (mode) {
        case TEST_AGAINST_MD -> String.join("\n",
            Files.readAllLines(fullPath()));
        case GENERATE_MD -> "XXXXX"; //dummy
      };
    }
  }

  BlockAndText prepareBlockAndText(TestData testData) {
    var buffer = ByteBuffer.wrap(testData.data());
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    var outputCapture = new ByteArrayOutputStream();
    var captureStream = new PrintStream(outputCapture);
    var reader = new DebugBufferReader(buffer, testData.intReader(), captureStream);
    reader.out().println("```txt");
    var block = reader.readBlock();
    reader.out().println();
    reader.out().print("```");

    var output = outputCapture.toString().replace("\r\n", "\n");
    return new BlockAndText(block, output);
  }

  record BlockAndText(Block block, String expectedText) {
  }

  static void testOrGenerate(TestData testData, String output) throws IOException {
    switch (mode) {
      case GENERATE_MD ->
        // Overwrite the expected content file
          Files.writeString(testData.fullPath(),
              output.replace("\n", System.lineSeparator()));
      case TEST_AGAINST_MD ->
        // Compare output to expected content
          assertEquals(testData.expectedContent(), output, "Output did not match expected content for " + testData.name());
    }
  }

  String allBits(StreamNode stream) {
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