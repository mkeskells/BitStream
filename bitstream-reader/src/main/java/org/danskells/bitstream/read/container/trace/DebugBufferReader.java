package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.read.block.Block;
import org.danskells.bitstream.read.block.Block.BlockBits;
import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.container.DefaultBufferReader;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Stack;

public class DebugBufferReader extends DefaultBufferReader {

  private PrintStream out;
  private final ThreadLocal<Stack<IntTracer>> intReaderTl = ThreadLocal.withInitial(Stack::new);

  public DebugBufferReader(ByteBuffer buffer, IRead intReader, PrintStream out) {
    super(buffer, intReader);
    this.out = out;
  }

  @Override
  protected BlockBits decodeListBlockBits(byte control, int size) {
    showControl( control, BlockType.LIST, size);
    intReaderTl.get().push(new LongArrayIntReaderTracer(buffer, out, intReader));
    try {
      return super.decodeListBlockBits(control, size);
    } finally {
      intReaderTl.get().pop();
    }
  }

  @Override
  protected BlockBits decodeRleBlockBits(byte control, int blockSpecific) {
    return super.decodeRleBlockBits(control, blockSpecific);
  }

  @Override
  protected BlockBits decodeBitmapBlockBits(byte control, int blockSpecific) {
    showControl( control, BlockType.BITMAP, blockSpecific);
    return super.decodeBitmapBlockBits(control, blockSpecific);
  }

  @Override
  protected void addBitsToBitmap(int arraySize) {
    var position  = buffer.position();
    for (int index = 0; index <= arraySize; index++) {
      buffer.position(position + index);
      var currentByte = buffer.get();
      showBitset(index, currentByte);
    }
    buffer.position(position);
    super.addBitsToBitmap(arraySize);

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

}

