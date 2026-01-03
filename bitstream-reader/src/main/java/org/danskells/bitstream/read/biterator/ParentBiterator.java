package org.danskells.bitstream.read.biterator;

import org.danskells.bitstream.read.Biterator;

import java.util.Iterator;
import java.util.function.Supplier;

public class ParentBiterator extends Biterator {
    private final Iterator<Supplier<Biterator>> children;
    private Biterator currentBiterator;

    public ParentBiterator(Iterator<Supplier<Biterator>> children) {
        this.children = children;
        currentBiterator = children.hasNext() ? children.next().get() : null;
    }

    @Override
    public boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
        while (currentBiterator != null) {
            if (currentBiterator.tryIndexedAdvance(action, actionParameter)) {
                return true;
            }
            currentBiterator = children.hasNext() ? children.next().get() : null;
        }
        return false;
    }

    @Override
    public boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
        while (currentBiterator != null) {
            if (currentBiterator.trySkipTo(position, action, actionParameter)) {
                return true;
            }
            currentBiterator = children.hasNext() ? children.next().get() : null;
        }
        return false;
    }
}
