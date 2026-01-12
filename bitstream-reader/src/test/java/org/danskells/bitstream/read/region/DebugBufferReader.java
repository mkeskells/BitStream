package org.danskells.bitstream.read.region;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DebugBufferReader extends BufferReader {

  private enum ReaderState {
        COMMON_START,
        LIST_START,
        LIST_VALUE,
    UNEXPECTED
    }
    private RegionBitsBase regionBitsBase;
    private ReaderState state = ReaderState.COMMON_START;
    private Long blockEndAbs = null;
    private long blockEndStart = 0;

    private final PrintStream out;
    private final Stack<IntTracer> intTracer = new Stack<>();
    private final Supplier<String> captureSoFar;

    int arrayIndex = 0;
    long arrayOffset = 0;
    int bitmapSize = 0;

    long controlPointPrior = 0;
    long blockOffset = 0;

    public static DebugBufferReader create(ByteBuffer buffer, IRead intReader, PrintStream out, Supplier<String> captureSoFar) {
        var debugIntReader = new DebugIntReader(intReader);
        var result = new DebugBufferReader(buffer, debugIntReader, out, captureSoFar);
        debugIntReader.owner = result;
        return result;
    }
    DebugBufferReader(ByteBuffer buffer, IRead intReader, PrintStream out, Supplier<String> captureSoFar) {
        super(buffer, intReader);
        this.captureSoFar = captureSoFar;

        this.out = out;
    }
    static class DebugIntReader implements IRead {
        private final IRead intReader;
        DebugBufferReader owner;

        public DebugIntReader(IRead intReader) {
            this.intReader = intReader;
        }

        @Override
        public int readUInt(ByteBuffer buffer) {
            var posBefore = buffer.position();
            var result = intReader.readUInt(buffer);
            var posAfter = buffer.position();
            owner.traceRead(intReader, posBefore, posAfter, result);
            return result;
        }

        @Override
        public char readUShort(ByteBuffer buffer) {
            var posBefore = buffer.position();
            var result = intReader.readUShort(buffer);
            var posAfter = buffer.position();
            owner.traceRead(intReader, posBefore, posAfter, result);
            return result;
        }

        @Override
        public long readULong(ByteBuffer buffer) {
            var posBefore = buffer.position();
            var result = intReader.readULong(buffer);
            var posAfter = buffer.position();
            owner.traceRead(intReader, posBefore, posAfter, result);
            return result;
        }
    }
    public ByteBuffer buffer() {
        return buffer;
    }

    private void traceRead(IRead intReader, int posBefore, int posAfter, long result) {
        switch (state) {
            case COMMON_START -> {
                checkEndOfBlock(posBefore);
                out.printf("     # Read offset to block: %d (one based) offset = %d bits%n", result, result + 1);
                this.controlPointPrior = regionBitsBase.currentBlockBitAddress;
                this.blockOffset = result + 1;
                out.printf("         Prior control point was %d, current block starts at %d%n",controlPointPrior, controlPointPrior+blockOffset);
                traceReadLong(intReader, posBefore, posAfter, result);
                state = ReaderState.UNEXPECTED;
            }
            case LIST_START -> {
                out.printf("""
                     
                             #    Control Point is the first value (%d)
                        
                             # Read length of block in bytes: %d (one based) = %d bytes
                             # the length starts from the end of this value
                        """ ,
                    regionBitsBase.currentBlockBitAddress, result, result + 1);
                traceReadLong(intReader, posBefore, posAfter, result);
                blockEndAbs = posAfter + result + 1;
                blockEndStart = posAfter;
                state = ReaderState.LIST_VALUE;
            }
            case LIST_VALUE -> {
                out.printf("%n%n     # Read value #%d (%d) -> bit position %d + %d + 1 = %d%n", arrayIndex, result, arrayOffset, result, arrayOffset + result + 1);
                traceReadLong(intReader, posBefore, posAfter, result);
                arrayOffset = arrayOffset + result + 1;
                arrayIndex++;
            }
        }
    }
    public void checkEndOfBlock(int posBeforeNextBlock) {
      if (blockEndAbs != null) {
        assertEquals(blockEndAbs, posBeforeNextBlock, "expected end of block %d but was %d indicator was before %d%n%s".formatted(blockEndAbs, posBeforeNextBlock, blockEndStart, captureSoFar.get()));
        blockEndAbs = null;
      }
    }


    @Override
    void populateNextBlock(RegionBitsBase regionBitsBase) {
        state = ReaderState.COMMON_START;
        this.regionBitsBase = regionBitsBase;
        super.populateNextBlock(regionBitsBase);
    }


        @Override
    void populateListBlockBits(RegionBitsBase regionBitsBase, int size) {
        showControl(BlockType.LIST, size);
        state = ReaderState.LIST_START;
        super.populateListBlockBits(regionBitsBase, size);
    }

  @Override
  void populateBitmapBlockBits(RegionBitsBase regionBitsBase, int size) {
    showControl(BlockType.BITMAP, size);
    super.populateBitmapBlockBits(regionBitsBase, size);
    for (int index = 0; index < regionBitsBase.bitmap_byteSize; index++) {
      var currentByte = buffer.get(regionBitsBase.bitmap_byteStart + index);
      showBitset(index, currentByte, regionBitsBase.currentBlockBitAddress + 1);
    }

  }

  @Override
  void populateRleBlockBits(RegionBitsBase regionBitsBase, int size) {
    showControl(BlockType.RUN_LENGTH, size);
    super.populateRleBlockBits(regionBitsBase, size);
  }

  private void showControl(BlockType blockType, int blockSpecific) {
        var control = (byte) ((blockType.ordinal() << 5) | (blockSpecific & 0x1F));
        showControl(control, blockType, blockSpecific);
    }
    protected void showControl(byte control, BlockType blockType, int blockSpecific) {
        var controlBits = String.format("%8s", Integer.toBinaryString(control & 0xFF)).replace(' ', '0');
        var controlBin = controlBits.substring(0, 3);
        var blockSpecificBin = controlBits.substring(3, 8);
        var explainBlockType = switch (blockType) {
            case BITMAP -> "Bitmap Block";
            case RUN_LENGTH -> "Run-Length Encoded Block";
            case LIST -> "List Block";
        };
        assertEquals(regionBitsBase.currentBlockBitAddress, controlPointPrior + blockOffset);
        var explainBlockSpecific = switch (blockType) {
            case BITMAP -> {
              bitmapSize = blockSpecific + 1;

              yield String.format("""
                    bytes in bitmap: %d (1 based)
                                          -> bit %d is implied - the control position of the previous block (%d) + block offset (%d) = %d""",
                  blockSpecific + 1, regionBitsBase.currentBlockBitAddress,
                  controlPointPrior,blockOffset, controlPointPrior + blockOffset);
            }
            case RUN_LENGTH -> String.format("Run length: %d (1 based)", blockSpecific + 1);
            case LIST -> {
                arrayOffset = currentBlockBitAddress;
                arrayIndex = 2;
                yield String.format("""
                 %d additional values (1 based)
                      #    Value #1 is the control position of the previous block (%d) + block offset (%d) = %d""",
                blockSpecific + 1, controlPointPrior,blockOffset, controlPointPrior + blockOffset);
            }
        };

        out.printf("""
                        
                        
                        %02X   #                -> control byte
                             # %s            -> block type = %s
                             # %s          -> (block specific) %s""",
                control, controlBin, explainBlockType, blockSpecificBin, explainBlockSpecific);
    }

    protected void showBitset(int byteIndex, byte currentByte, long baseBitPosition) {
        var hexValue = String.format("%02X", currentByte & 0xFF);

        // List which bits are set
        StringBuilder bitsSet = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if ((currentByte & (1 << i)) != 0) {
                if (!bitsSet.isEmpty()) bitsSet.append(", ");
                bitsSet.append(i + (byteIndex * 8L) + baseBitPosition);
            }
        }

        out.printf("""
                
                %s   #  byte[%d]       -> bits set: %s""", hexValue, byteIndex, bitsSet);
    }

    public PrintStream out() {
        return out;
    }

    abstract class IntTracer {
        private final IRead intReader;
        private final ByteBuffer buffer;
        protected final PrintStream out;

        public IntTracer(ByteBuffer buffer, PrintStream out, IRead intReader) {
            this.buffer = buffer;
            this.out = out;
            this.intReader = intReader;
        }

        abstract void trace(int posBefore, int posAfter, long result);
    }
    class LongArrayIntReaderTracer extends IntTracer {

        LongArrayIntReaderTracer(ByteBuffer buffer, PrintStream out, IRead intReader) {
            super(buffer, out, intReader);
        }

        int index = 0;
        long offset = 0;
        List<Long> values = new ArrayList<>();

        @Override
        public void trace(int posBefore, int posAfter, long result) {
            switch (index++) {
                case 0 -> {
                    out.printf("%n     # value #1 is implied (0) %n     # Read length of block in bytes%n");
                }
                default -> {
                    out.printf("%n     # Read value #%d (%d) -> bit position %d + %d + 1 = %d%n", index, result, offset, result, offset + result + 1);
                    offset = offset + result + 1;
                }
            }
            values.add(offset);

            traceReadLong(intReader, posBefore, posAfter, result);
        }
    }

    protected void traceReadLong(IRead intReader, int posBefore, int posAfter, long result) {
        if (intReader == MsbReader.INSTANCE) {
            traceMsbReadLong(posBefore, posAfter, result);
        } else {
            throw new IllegalStateException("Unexpected intReader" + intReader.getClass().getName());
        }
    }

    private void traceMsbReadLong(int posBefore, int posAfter, long result) {
        var length = posAfter - posBefore;
        if (length == 1) {
            out.printf("%02X   #   (vint 1 byte) %d", result, result);
        } else {
            var bits = String.format("%8s", Integer.toBinaryString(buffer.get(posBefore) & 0xFF)).replace(' ', '0');
            var lowerBits = bits.substring(Math.min(length, 8), 8);
            out.printf("%02X   #   (vint %d bytes, lower %d bits %s = %d%n", buffer.get(posBefore), length, lowerBits.length(), lowerBits, Integer.parseInt(lowerBits, 2));
            var hex = "";
            for (int i = posBefore + 1; i < posAfter - 1; i++) {
                out.printf("%02X%n", buffer.get(i));
                hex = String.format("%02X", buffer.get(i)) + hex;
            }
            hex = String.format("%02X", buffer.get(posAfter - 1)) + hex;
            out.printf("%02X   #   (vint 0x%s << %d + %d = %d%n", buffer.get(posAfter - 1), hex, lowerBits.length(), Integer.parseInt(lowerBits, 2), result);
            var check = (Integer.parseInt(hex, 16) << lowerBits.length()) + (Integer.parseInt(lowerBits, 2));
            if (check != result) {
                throw new IllegalStateException(String.format("ERROR: Computed value %d does not match read value %d%n", check, result));
            }
        }
    }


}

