package org.danskells.bitstream.read.storage;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.common.block.ControlByte;
import org.danskells.bitstream.read.Block;
import org.danskells.bitstream.read.block.BitmapBlock;
import org.danskells.bitstream.read.block.LongArrayBlock;
import org.danskells.bitstream.read.coder.IRead;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class DefaultFileReader extends FileReader {

  private final ByteBuffer buffer;
  private final IRead intReader;

  private final FileHeader header;
  private long currentBlockBitAddress = 0;
  DefaultFileReader(ByteBuffer buffer, IRead intReader) {
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
    switch(blockType) {
      case BITMAP:{
        BitSet allBits = new BitSet(blockSpecific << 3 +1); // +1 because 0 is implied

        allBits.set(0); //0 is implied to be present

        // Convert bytes to bits
        for (int i = 0; i < blockSpecific; i++) {
            byte currentByte = buffer.get();
            for (int bit = 0; bit < 8; bit++) {
                if ((currentByte & (1 << bit)) != 0) {
                    allBits.set((i * 8) + bit+1); // +1 because 0 is implied
                }
            }
        }
        return new BitmapBlock(allBits);
      }
      case RUN_LENGTH: {
        var blockLength = intReader.readUInt(buffer);
        var startValue = intReader.readUInt(buffer);

        throw new UnsupportedOperationException();
      }
      case LIST: {
        var blockLength = intReader.readUInt(buffer);
        var length = intReader.readUInt(buffer);
        var longs = new long[length + 1];
        longs[0] = currentBlockBitAddress;
        for (int i = 1; i <= length; i++) {
          var delta = intReader.readULong(buffer);
          currentBlockBitAddress += delta;
          longs[i] = currentBlockBitAddress;
        }
        return new LongArrayBlock(longs);

      }
    }

    return null;
  }
}
