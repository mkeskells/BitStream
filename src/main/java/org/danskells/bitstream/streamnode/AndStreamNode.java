package org.danskells.bitstream.streamnode;

import org.danskells.bitstream.BitContainerStream;
import org.danskells.bitstream.StreamNode;

import java.util.Arrays;

public class AndStreamNode implements StreamNode {

    private final StreamNode[] children;
    private final long[] childNextValues;

    public AndStreamNode(StreamNode[] children) {
        assert(children.length > 1);
        this.children = children;
        this.childNextValues = Arrays
                .stream(children)
                .mapToLong(StreamNode::next)
                .toArray();
    }

    @Override
    public long next() {
        var next = childNextValues[0];
        for (int i = 1; i < childNextValues.length; i++) {
            next = Math.max(next, childNextValues[i]);
        }
        return skipToNext(next);
    }

    @Override
    public long skipToNext(long start) {
        var nextPosition = start;
        var nextPositionAtStart = nextPosition;
        do {
            nextPositionAtStart = nextPosition;
            for (int i = 0; i < children.length; i++) {
                nextPosition = children[i].skipToNext(nextPosition);
                childNextValues[i] = nextPosition;
            }
        } while (nextPositionAtStart != nextPosition);
        return nextPosition;
    }
}

