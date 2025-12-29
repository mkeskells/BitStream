package org.danskells.bitstream.read;

import org.danskells.bitstream.read.biterator.Biterator;

import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public interface BitContainer {

    Biterator biterator();

    default LongStream stream() {
        return StreamSupport.longStream(biterator(), false);
    }
}

