package org.danskells.bitstream.read.container;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.common.block.ControlByte;
import org.danskells.bitstream.read.block.*;
import org.danskells.bitstream.read.block.Block.BlockBits;
import org.danskells.bitstream.read.coder.IRead;

import java.nio.ByteBuffer;

public class DefaultBufferReader {

  protected final ByteBuffer buffer;
  protected final IRead intReader;

  private final FileHeader header;
  protected long currentBlockBitAddress = 0;
  protected DefaultBufferReader(ByteBuffer buffer, IRead intReader) {
    this.buffer = buffer;
    this.intReader = intReader;
    this.header = readHeader();
  }
  private FileHeader readHeader() {
    return new FileHeader();
  }

  public BlockBits readBlockBits() {
    long offset = intReader.readULong(buffer);
    currentBlockBitAddress += offset;
    var control = buffer.get();
    var blockType = BlockType.ofId(control >>> ControlByte.BLOCK_TYPE_SHIFT);
    var blockSpecific = control & ControlByte.BLOCK_SPECIFIC_MASK;
    return switch (blockType) {
      case BITMAP -> decodeBitmapBlockBits(control, blockSpecific);
      case RUN_LENGTH -> decodeRleBlockBits(control, blockSpecific);
      case LIST-> decodeListBlockBits(control, blockSpecific);
    };
  }

  protected BlockBits decodeListBlockBits(byte control, int size) {
    var blockLength = readUInt(buffer);
    var pos = buffer.position();
    var longs = new long[size];
    for (int i = 1; i <= size; i++) {
      var delta = readULong(buffer) + 1;
      longs[i-1] = delta;
    }
    assert buffer.position() - pos == blockLength : "Read length does not match block length";
    return new HeapLongArrayBlock(0L, longs).bits(0L);
  }

  protected BlockBits decodeRleBlockBits(byte control, int blockSpecific)
    {
      var blockLength = intReader.readUInt(buffer);
      var startValue = intReader.readUInt(buffer);

      throw new UnsupportedOperationException();
    }


  protected BlockBits decodeBitmapBlockBits( byte control, int blockSpecific) {
    var arraySize = blockSpecific + 1; // +1 because 0 is implied
    addBitsToBitmap(arraySize);

    return new HeapBitmapBlock(currentBlockBitAddress, currentBlockBitAddress + arraySize << 3 + 1, arraySize, buffer, buffer.position()).bits(currentBlockBitAddress);

  }

  protected void addBitsToBitmap(int arraySize) {
  }


  protected int readUInt(ByteBuffer buffer) {
    return intReader.readUInt(buffer);
  }
  protected long readULong(ByteBuffer bufferr) {
    return intReader.readULong(buffer);
  }
}
