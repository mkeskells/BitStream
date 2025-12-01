package org.danskells.bitstream.write.container;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public enum ByteBufferAllocator {
    DIRECT {
        @Override
        ByteBuffer allocate(int capacity) {
            var result = ByteBuffer.allocateDirect(capacity);
            result.order(ByteOrder.LITTLE_ENDIAN);
            return result;
        }
    },
    HEAP {
        @Override
        ByteBuffer allocate(int capacity) {
            var result = ByteBuffer.allocate(capacity);
            result.order(ByteOrder.LITTLE_ENDIAN);
            return result;
        }

    };
    abstract ByteBuffer allocate(int capacity);
}
