package org.danskells.bitstream.read.region;

import org.danskells.bitstream.read.Biterator;

public interface ContainerBiteratorCallback {
    boolean nextRegion_trySkipTo(long position, Biterator.IndexedLongConsumer action, int actionParameter) ;

    boolean nextRegion_tryIndexedAdvance(Biterator.IndexedLongConsumer action, int actionParameter) ;
    }
