package org.danskells.bitstream.read.block;

import org.danskells.bitstream.read.biterator.Biterator;

public abstract class LongArrayBlock extends Block {

  private int numberOfValues;

  protected void reset(long initialOffset, long controlOffset, int numberOfValues) {
    super.reset(initialOffset, controlOffset);
    this.numberOfValues = numberOfValues;
  }


  abstract class LongArrayBlockBits extends BlockBits {
    /**
     * the number of results remaining to be returned
     */
    private int remainingValues;
    private long currentOffset;
    public void reset(long baseOffset) {
      super.reset(baseOffset);
      remainingValues = numberOfValues + 1;
      currentOffset = baseOffset;
    }

    @Override
    public boolean trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter) {
      if (position >= currentOffset) {
        while (remainingValues > 0 && currentOffset < position) {
          var value = getNextValue();
          currentOffset += value;
          remainingValues --;
        }
        if (currentOffset < position) {
          return false;
        }
        action.accept(currentOffset, actionParameter);
        return true;
      } else {
        throw new IllegalArgumentException();
      }
    }
    abstract long getNextValue();

    @Override
    public boolean tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter) {
      if (remainingValues > numberOfValues) {
        action.accept(baseOffset, actionParameter);
        remainingValues --;
        return true;
      }
      if (remainingValues <= 0) {
        return false;
      }
      var value = getNextValue();
      currentOffset += value;
      action.accept(currentOffset, actionParameter);
      remainingValues --;
      return true;
    }
  }
}
