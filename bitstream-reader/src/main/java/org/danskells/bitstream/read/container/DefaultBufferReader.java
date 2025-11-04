package org.danskells.bitstream.read.container;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.common.block.ControlByte;
import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.block.LongArrayBlock;
import org.danskells.bitstream.read.coder.IRead;

import java.nio.ByteBuffer;
import java.util.BitSet;

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

  public Block readBlock() {
    long offset = intReader.readULong(buffer);
    currentBlockBitAddress += offset;
    var control = buffer.get();
    var blockType = BlockType.ofId(control >>> ControlByte.BLOCK_TYPE_SHIFT);
    var blockSpecific = control & ControlByte.BLOCK_SPECIFIC_MASK;
    return switch (blockType) {
      case BITMAP -> decodeBitmapBlock(control, blockSpecific);
      case RUN_LENGTH -> decodeRleBlock(control, blockSpecific);
      case LIST-> decodeListBlock(control, blockSpecific);
    };
  }

  protected LongArrayBlock decodeListBlock(byte control, int size) {
    var blockLength = readUInt(buffer);
    var pos = buffer.position();
    var longs = new long[size + 1];
    longs[0] = currentBlockBitAddress;
    for (int i = 1; i <= size; i++) {
      var delta = readULong(buffer) + 1;
      currentBlockBitAddress += delta;
      longs[i] = currentBlockBitAddress;
    }
    assert buffer.position() - pos == blockLength : "Read length does not match block length";
    return new LongArrayBlock(longs);
  }

  protected Block decodeRleBlock(byte control, int blockSpecific)
    {
      var blockLength = intReader.readUInt(buffer);
      var startValue = intReader.readUInt(buffer);

      throw new UnsupportedOperationException();
    }


  protected BitmapBlock decodeBitmapBlock( byte control, int blockSpecific) {
    BitSet allBits = new BitSet(blockSpecific << 3 +1); // +1 because 0 is implied

    allBits.set(0); //0 is implied to be present

    // Convert bytes to bits
    for (int i = 0; i < blockSpecific; i++) {
      byte currentByte = buffer.get();
      addBitsToBitmap(currentByte, allBits, i);
    }
    return new BitmapBlock(allBits);

  }

  protected void addBitsToBitmap(byte currentByte, BitSet allBits, int index) {
    for (int bit = 0; bit < 8; bit++) {
      if ((currentByte & (1 << bit)) != 0) {
        allBits.set((index * 8) + bit+1); // +1 because 0 is implied
      }
    }
  }


  protected int readUInt(ByteBuffer buffer) {
    return intReader.readUInt(buffer);
  }
  protected long readULong(ByteBuffer bufferr) {
    return intReader.readULong(buffer);
  }
}
