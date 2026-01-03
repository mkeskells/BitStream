package org.danskells.bitstream.read;

import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public interface BitContainer {

    Biterator biterator();
    Biterator setBits();
    Biterator clearBits();

    default LongStream stream() {
        return StreamSupport.longStream(biterator(), false);
    }
}

