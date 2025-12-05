package org.danskells.bitstream.read.block;

public class HeapBitmapBlock extends BitmapBlock{
  private final byte[] data;

  public HeapBitmapBlock(long initialOffset, long controlOffset, int numberOfBits, byte[] data) {
    this.data = data;
    reset(initialOffset, controlOffset, numberOfBits);
  }

  @Override
  public BlockBits bits(long baseOffset) {
    var result =  new BitmapBlockBits();
    result.reset(baseOffset);
    return result;
  }

  @Override
  protected int findNextRelative(int relativeStart) {
    var byteIndex = relativeStart / 8;
    if (byteIndex >= data.length) {
      //TODO - remove this check?
      //should be able to do this without it, when we rework parent method to make findNextRelative lazier
      return Integer.MAX_VALUE;
    }
    var bitIndex = relativeStart % 8;
    var currentByte = Byte.toUnsignedInt(data[byteIndex]) & (-1 << bitIndex) ;

    if (currentByte != 0)  {
      bitIndex = Integer.numberOfTrailingZeros(currentByte);
      return byteIndex * 8 + bitIndex;
    }
    else {
      while (++byteIndex < data.length) {
        currentByte = Byte.toUnsignedInt(data[byteIndex]);
        if (currentByte != 0) {
          bitIndex = Integer.numberOfTrailingZeros(currentByte);
          return byteIndex * 8 + bitIndex;
        }
      }

      return Integer.MAX_VALUE;
    }
  }
}
