package org.danskells.bitstream.read.biterator;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public abstract class Biterator implements Spliterator.OfLong {

  /** Skips forward to the position specified. After this, the next call to [#tryAdvance(LongConsumer)] will yield the first
   *  set bit on or after `position`.
   *
   *  @param position The position to skip to.
   *  @throws IllegalArgumentException if the position is less than the current position.
   *  @return true if the skip was successful, false if the end of the stream was reached
   */
  abstract boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter);
  boolean trySkipTo(long position) {
    return trySkipTo(position, (value, index) -> {}, 0);
  }
//
//  /**
//   * apply the next value without consuming it.
//   * @param action
//   * @return true if a value was produced, false if the end of the stream was reached
//   */
//  boolean tryPeek(IndexedLongConsumer action, int index);

  abstract boolean tryIndexedAdvance (IndexedLongConsumer action, int actionParameter);

  public boolean tryAdvance(LongConsumer action) {
    return tryIndexedAdvance((value, index) -> action.accept(value), 0);
  }


  @Override
  public OfLong trySplit() {
    //no splitting supported
    return null;
  }

  @Override
  public long estimateSize() {
    //too expensive to compute
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | DISTINCT | SORTED | NONNULL | IMMUTABLE;
  }

  @Override
  public Comparator<? super Long> getComparator() {
    //null => natural ordering
    return null;
  }

  public LongStream stream() {
    return StreamSupport.longStream(this, false);
  }

  @FunctionalInterface
  interface IndexedLongConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(long value, int index);
  }
}
