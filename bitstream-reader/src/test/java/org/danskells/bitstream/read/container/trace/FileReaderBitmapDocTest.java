package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileReaderBitmapDocTest extends AbstractFileReaderDocTest{
  record BitmapTestData(String name, byte[] data, String mdDocFileName, String expectedBits, IRead intReader) implements TestData{
    @Override
    public String mdDir() {
      return "bitmap";
    }
  }
  public static Stream<Arguments> bitmapTestsData() {
    return Stream.of(
        Arguments.of(new BitmapTestData("Simple example", new byte[]{
                0x00,                      // offset = 0 (vint)
                0x04,                      // control byte: bitmap block, 4 bytes
                0x01,                      // bitmap data: bits 0, 1
                0x04,                      // bitmap data: bit 11
                0x05,                      // bitmap data: bits 17, 19
                (byte) 0xff                // bitmap data: bits 25-32))
            }, "ex-01.md", "0,1,11,17,19,25,26,27,28,29,30,31,32", MsbReader.INSTANCE)

        ));
  }

  @ParameterizedTest
  @MethodSource("bitmapTestsData")
  void testBitmapBlockReadAndOutput(BitmapTestData testData) throws IOException {

    var blockAndText = prepareBlockAndText(testData);

    // Verify the bitmap contains the expected bits
    switch (blockAndText.block()) {
      case BitmapBlock bitmapBlock -> {
        var stream = bitmapBlock.getStreamNode();
        assertEquals(testData.expectedBits, allBits(stream));
      }
      default -> fail("Expected a BitmapBlock");
    }

    testOrGenerate(testData, blockAndText.expectedText());
  }

}
