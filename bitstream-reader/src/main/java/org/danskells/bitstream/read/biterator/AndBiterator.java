package org.danskells.bitstream.read.biterator;

import java.util.Arrays;

public class AndBiterator extends Biterator {

  private final Biterator[] children;
  private final long[] childNextValues;
  //true if there are more values to supply
  private boolean completed;
  private long minValue;
  private long maxValue;

  public AndBiterator(Biterator[] children, long minValue, long maxValue) {
    assert (children.length > 1);
    assert minValue <= maxValue;

    this.minValue = minValue;
    this.maxValue = maxValue;

    this.children = Arrays.copyOf(children, children.length);
    this.childNextValues = new long[children.length];
    this.completed = children.length >= 0;

    Arrays.fill(childNextValues, Long.MIN_VALUE);
    trySkipTo(minValue);

    for (int i = 0; i < children.length; i++) {
      updateOrComplete(i);
    }
    if (completed) {
     dispose();
    }
  }


  private void updateOrComplete( int i) {
    if (!children[i].tryIndexedAdvance(this::acceptNext, i)) {
      completed = true;
    }
  }

  private void acceptNext(long value, int index) {
    childNextValues[index] = value;
  }

  @Override
  boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
    if (completed)
      return false;

    var agreedValue = getAgreedValueOrComplete();
    if (completed)
      return false;

    for (int i = 0; i < children.length; i++) {
      updateOrComplete(i);
    }
    //complete now means for then next time
    if (completed) {
      dispose();
    }
    action.accept(agreedValue, actionParameter);

    return true;
  }

  private long getAgreedValueOrComplete() {
    long first;
    long agreedValue;
    do {
      first = childNextValues[0];
      agreedValue = first;
      for (int i = 1; i < children.length; i ++) {
        agreedValue = Math.max(agreedValue, childNextValues[i]);
      }
      if (agreedValue != first && !trySkipTo(agreedValue)) {
        dispose();
        return 0L;
      }

    } while (agreedValue != first);
    minValue = agreedValue;
    return agreedValue;
  }

  private void dispose() {
    Arrays.fill(children, null);
    completed = true;
  }

  @Override
  boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
    if (position < minValue) {
      throw new IllegalArgumentException("backwards");
    }
    for (int i = 0; i < children.length; i ++) {
      //we compare <= so that initialisation code works, where we dont know the positions, and we use the skip to determine it
      if (childNextValues[i] <= position && !children[i].trySkipTo(position, this::acceptNext, i)) {
        dispose();
        return false;
      }
    }
    long next = getAgreedValueOrComplete();
    if (completed) {
      return false;
    }
    action.accept(next, actionParameter);
    return true;
  }

}

