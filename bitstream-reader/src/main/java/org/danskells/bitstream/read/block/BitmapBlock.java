package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.biterator.Biterator;

public abstract class BitmapBlock extends Block {

  private int numberOfBits;
  protected int numberOfBits() {
    return numberOfBits;
  }

  protected void reset(long initialOffset, long controlOffset, int numberOfBits) {
    super.reset(initialOffset, controlOffset);
    this.numberOfBits = numberOfBits;
  }

  protected  class BitmapBlockBits extends BlockBits {

    @Override
    public boolean trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter) {
      var relativePositionL = position - baseOffset;
      if (relativePositionL >= numberOfBits) {
        return false;
      }
      var relativePosition = (int) relativePositionL;
      assert relativePosition == relativePositionL;

      if (relativePosition < nextRelativePosition) {
        throw new IllegalArgumentException();
      }
      nextRelativePosition = findNextRelative(relativePosition + 1);
      if (nextRelativePosition == -1) {
        return false;
      }
      action.accept(baseOffset + nextRelativePosition, actionParameter);
      return true;

    }

    @Override
    public boolean tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter) {
      if (nextRelativePosition > numberOfBits) {
        return false;
      }
      //TODO make this cleaner
      //on the first call, nextRelativePosition is -1
      //make findNextRelative lazy, called before accept. that way we dont do extra work unnecessarily
      //on subsequent calls, we just use nextRelativePosition
      if (nextRelativePosition >= 0) {
        //TODO offset by 1, as we dont need the first bit
        action.accept(baseOffset + nextRelativePosition, actionParameter);
        nextRelativePosition = findNextRelative(nextRelativePosition+1);
      } else {
        action.accept(baseOffset, actionParameter);
        //its the initial call, so the bit specified by baseOffset
        nextRelativePosition =  findNextRelative(0);
      }
      return true;
    }
  }

  /**
   *
   * @param relativeStart, the start location in the bitmap to search from
   * @return the next bit set >= relativeStart, or Integer.MAX_VALUE if none found
   */
  protected abstract int findNextRelative(int relativeStart);
}
