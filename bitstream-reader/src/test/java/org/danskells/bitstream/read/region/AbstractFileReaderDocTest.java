package org.danskells.bitstream.read.region;

import org.danskells.bitstream.read.Biterator;
import org.danskells.bitstream.read.block.Block.BlockBits;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.danskells.bitstream.read.region.SimpleBitContainer.SimpleBitContainerBiterator.SimpleBitContainerBiteratorCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    BitsAndText<?> prepareBlockAndText(TestData testData) {
        var buffer = ByteBuffer.wrap(testData.data());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        var outputCapture = new ByteArrayOutputStream();
        var captureStream = new PrintStream(outputCapture);
        var reader = new DebugBufferReader(buffer, testData.intReader(), captureStream, outputCapture::toString);
        reader.out().println("```txt");
        var blockBits = reader.readBlockBits();
        reader.out().println();
        reader.out().print("```");

        var output = outputCapture.toString().replace("\r\n", "\n");
        return new BitsAndText<>(blockBits, output);
    }

    @Deprecated
    record BitsAndText<B extends BlockBits>(B bits, String expectedText) {
    }
    record ValuesAndTextCapture(RegionBits bits, ByteArrayOutputStream expectedText) {
        String output() {
            return expectedText.toString().replace("\r\n", "\n");
        }
    }
    ValuesAndTextCapture prepareValuesAndText(TestData testData, boolean setBits) {
        var buffer = ByteBuffer.wrap(testData.data());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        var outputCapture = new ByteArrayOutputStream();
        var captureStream = new PrintStream(outputCapture);
        AtomicReference<DebugBufferReader> readerRef = new AtomicReference<>();
        var region = new BitRegion(0, Long.MAX_VALUE, buffer) {
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
        var bits =  region.bits(setBits, callback);

        return new ValuesAndTextCapture(bits, outputCapture);
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

    String allBits(BlockBits stream) {
        StringBuilder sb = new StringBuilder();
        while (stream.tryIndexedAdvance((val, idx) -> {
            if (!sb.isEmpty()) {
                sb.append(",");
            }
            sb.append(val);
        }, 0)) {
        }
        return sb.toString();
    }


String allBits(RegionBits stream) {
    StringBuilder sb = new StringBuilder();
    while (stream.tryIndexedAdvance((val, idx) -> {
        if (!sb.isEmpty()) {
            sb.append(",");
        }
        sb.append(val);
    }, 0)) {
    }
    return sb.toString();
}

}