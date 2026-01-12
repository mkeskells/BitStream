package org.danskells.bitstream.read.region;

import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileReaderBitmapDocTest extends AbstractFileReaderDocTest {
    public record BitmapTestData(String name, byte[] data, String mdDocFileName, String expectedBits,
                          IRead intReader) implements TestData {
        @Override
        public String mdDir() {
            return "bitmap";
        }
    }

    public static Stream<Arguments> bitmapTestsData() {
        return Stream.of(
                Arguments.of(new BitmapTestData("Simple example", new byte[]{
                                0x00,                      // offset = 0 (vint)
                                0x03,                      // control byte: bitmap bits, 4 bytes
                                0x01,                      // bitmap data: bits 0, 1
                                0x04,                      // bitmap data: bit 11
                                0x05,                      // bitmap data: bits 17, 19
                                (byte) 0xff                // bitmap data: bits 25-32))
                        }, "bitmap-01.md", "100,101,111,117,119,125,126,127,128,129,130,131,132", MsbReader.INSTANCE)

                ));
    }

    @ParameterizedTest
    @MethodSource("bitmapTestsData")
    void testBitmapBlockReadAndOutput2(BitmapTestData testData) throws IOException {

        var blockAndText = prepareValuesAndText(testData, true);
        var bits = blockAndText.bits();

        //TODO check we read one block, and it was a bitmap

        // Verify the bitmap contains the expected bits
        assertEquals(testData.expectedBits, allBits(bits));

        testOrGenerate(testData, blockAndText.output());
    }

}
