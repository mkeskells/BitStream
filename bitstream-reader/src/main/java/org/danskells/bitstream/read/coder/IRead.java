package org.danskells.bitstream.read.coder;

import java.nio.ByteBuffer;

public interface IRead {
    long readULong(ByteBuffer buffer);

    int readUInt(ByteBuffer buffer);

    char readUShort(ByteBuffer buffer);
}
