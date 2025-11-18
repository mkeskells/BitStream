package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.read.block.LongArrayBlock;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileReaderArrayDocTest extends AbstractFileReaderDocTest {

  record ArrayTestData(String name, byte[] data, String mdDocFileName, String expectedBits, IRead intReader) implements TestData{
    @Override
    public String mdDir() {
      return "array";
    }
  }

  public static Stream<Arguments> arrayTestsData() {
    return Stream.of(
        Arguments.of(new ArrayTestData("Simple example", new byte[]{
                0x00,                      // offset = 0 (vint)
                0x42,                      // control byte: array block, 2 values
                0x04,                      //   (vint 4) length of block in bytes
                0x07,                      //   (vint 7)                   -> bit position 8
                (byte) 0xc4,                      //   (vint 3 bytes, lower 5 bits = 00100)
                0x47,                      //
                (byte) 0x80,                      //   (vint 0x8047 << 5 + 4 = 0x1008E0 (or 1051872 in decimal) + 3 = 1051875
                //          -> bit position 1051883
            }, "ex-01.md", "0,8,1050861", MsbReader.INSTANCE)

        ));
  }
  @ParameterizedTest
  @MethodSource("arrayTestsData")
  void testArrayBlockReadAndOutput(ArrayTestData testData) throws IOException {

    var blockAndText = prepareBlockAndText(testData);

    // Verify the bitmap contains the expected bits
    switch (blockAndText.block()) {
      case LongArrayBlock bitmapBlock -> {
        var stream = bitmapBlock.getStreamNode();
        assertEquals(testData.expectedBits, allBits(stream));
      }
      default -> fail("Expected a LongArrayBlock");
    }

    testOrGenerate(testData, blockAndText.expectedText());
  }

}
