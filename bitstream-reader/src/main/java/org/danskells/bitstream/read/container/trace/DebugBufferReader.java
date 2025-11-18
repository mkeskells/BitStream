package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.block.LongArrayBlock;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;
import org.danskells.bitstream.read.container.DefaultBufferReader;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;

public class DebugBufferReader extends DefaultBufferReader {

  private PrintStream out;
  private final ThreadLocal<Stack<IntTracer>> intReaderTl = ThreadLocal.withInitial(Stack::new);

  public DebugBufferReader(ByteBuffer buffer, IRead intReader, PrintStream out) {
    super(buffer, intReader);
    this.out = out;
  }

  @Override
  protected LongArrayBlock decodeListBlock(byte control, int size) {
    showControl( control, BlockType.LIST, size);
    intReaderTl.get().push(new LongArrayIntReaderTracer(intReader));
    try {
      return super.decodeListBlock(control, size);
    } finally {
      intReaderTl.get().pop();
    }
  }

  @Override
  protected Block decodeRleBlock(byte control, int blockSpecific) {
    return super.decodeRleBlock(control, blockSpecific);
  }

  @Override
  protected BitmapBlock decodeBitmapBlock(byte control, int blockSpecific) {
    showControl( control, BlockType.BITMAP, blockSpecific);
    return super.decodeBitmapBlock(control, blockSpecific);
  }

  @Override
  protected void addBitsToBitmap(byte currentByte, BitSet allBits, int index) {
    showBitset(index, currentByte);
    super.addBitsToBitmap(currentByte, allBits, index);
  }

  protected void showControl(byte control, BlockType blockType, int blockSpecific) {
    var controlBits = String.format("%8s", Integer.toBinaryString(control & 0xFF)).replace(' ', '0');
    var controlBin = controlBits.substring(0,3);
    var blockSpecificBin = controlBits.substring(3,8);
    var explainBlockType = switch (blockType) {
      case BITMAP -> "Bitmap Block";
      case RUN_LENGTH -> "Run-Length Encoded Block";
      case LIST -> "List Block";
    };
    var explainBlockSpecific = switch (blockType) {
      case BITMAP -> String.format("""
      bytes in bitmap: %d (1 based)
                            -> bit 0 is implied""", blockSpecific +1);
      case RUN_LENGTH -> String.format("Run length: %d (1 based)", blockSpecific +1);
      case LIST -> String.format(" %d values (1 based)", blockSpecific + 1);
    };

    out.printf("""
        %02X   #                -> control byte
             # %s            -> block type = %s
             # %s          -> (block specific) %s""",
        control, controlBin, explainBlockType, blockSpecificBin, explainBlockSpecific);
  }
  protected void showBitset(int byteIndex, byte currentByte) {
    var hexValue = String.format("%02X", currentByte & 0xFF);

    // List which bits are set
    StringBuilder bitsSet = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      if ((currentByte & (1 << i)) != 0) {
        if (!bitsSet.isEmpty()) bitsSet.append(", ");
        bitsSet.append(i + (byteIndex * 8) +1); // +1 because bit 0 is implied
      }
    }

    out.printf("""
        
        %s   #  byte[%d]       -> bits set: %s""", hexValue, byteIndex, bitsSet);
  }

  public PrintStream out() {
    return out;
  }
  public void out(PrintStream newOut) {
    this.out = newOut;
  }

  private abstract class IntTracer {
    private final IRead intReader;

    public IntTracer(IRead intReader) {
      this.intReader = intReader;
    }
    protected void traceReadLong(int posBefore, int posAfter, long result) {
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

    abstract void trace(int posBefore, int posAfter, long result);
  }


  @Override
  protected int readUInt(ByteBuffer buffer) {
    var tracer = intReaderTl.get().peek();
    var posBefore = buffer.position();
    var result = super.readUInt(buffer);
    var posAfter = buffer.position();
    tracer.trace(posBefore, posAfter, result);
    return result;
  }

  @Override
  protected long readULong(ByteBuffer buffer) {
    var tracer = intReaderTl.get().peek();
    var posBefore = buffer.position();
    var result = super.readULong(buffer);
    var posAfter = buffer.position();
    tracer.trace(posBefore, posAfter, result);
    return result;
  }

  private class LongArrayIntReaderTracer extends IntTracer {
    LongArrayIntReaderTracer(IRead intReader) {
      super(intReader);
    }
    int index = 0;
    long offset = 0;
    List<Long> values = new ArrayList<>();
    @Override
    public void trace(int posBefore, int posAfter, long result) {
      switch(index++ ) {
        case 0 -> {
          out.printf("%n     # value #1 is implied (0) %n     # Read length of block in bytes%n");
        }
        default -> {
          out.printf("%n     # Read value #%d (%d) -> bit position %d + %d  + 1 = %d%n", index, result, offset, result, offset + result + 1);
          offset = offset + result + 1;
        }
      }
      values.add(offset);

      traceReadLong(posBefore, posAfter, result);
    }
  }
}

