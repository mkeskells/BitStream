package org.danskells.bitstream.read.biterator;

import java.util.Arrays;

public class OrBiterator extends Biterator {

  private final Biterator[] children;
  private final long[] childNextValues;
  private int maxIndex;
  private long minValue;
  private long maxValue;

  public OrBiterator(Biterator[] children, long minValue, long maxValue) {
    assert (children.length > 1);
    assert minValue <= maxValue;

    this.minValue = minValue;
    this.maxValue = maxValue;

    this.children = Arrays.copyOf(children, children.length);
    this.childNextValues = new long[children.length];
    this.maxIndex = children.length-1;

    for (int i = maxIndex; i >= 0; i--) {
      if (!children[i].trySkipTo(minValue, this::acceptNext, i)) {
        remove(i);
      }
    }
  }

  private void remove(int i) {
    children[i] = children[maxIndex];
    children[maxIndex] = null;
    childNextValues[i] = childNextValues[maxIndex];
    maxIndex--;
  }

  private void acceptNext(long value, int index) {
    childNextValues[index] = value;
  }

  @Override
  boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
    if (maxIndex < 0)
      return false;

    var next = getMinimum();
    for (int i = maxIndex; i >= 0; i--) {
      if (childNextValues[i] == next) {
        if (!children[i].tryIndexedAdvance(this::acceptNext, i)) {
          remove(i);
        }
      }
    }
    action.accept(next, actionParameter);
    minValue = next;
    return true;
  }

  private long getMinimum() {
    var minimum = childNextValues[maxIndex];
    for (int i = maxIndex - 1; i >= 0; i--) {
      minimum = Math.min(minimum, childNextValues[i]);
    }
    return minimum;
  }

  @Override
  boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
    if (position < minValue) {
      throw new IllegalArgumentException("can't go backwards");
    }
    for (int i = maxIndex; i >= 0; i--) {
      if (!children[i].trySkipTo(position, this::acceptNext, i)) {
        remove(i);
      }
    }
    if (maxIndex < 0) {
      return false;
    } else {
      action.accept(getMinimum(), actionParameter);
      return true;
    }
  }

}
