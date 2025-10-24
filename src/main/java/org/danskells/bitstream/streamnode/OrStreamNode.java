package org.danskells.bitstream.streamnode;

import org.danskells.bitstream.BitContainerStream;
import org.danskells.bitstream.StreamNode;

import java.util.Arrays;

public class OrStreamNode implements StreamNode {

    private final StreamNode[] children;
    private final long[] childNextValues;

    public OrStreamNode(StreamNode[] children) {
        assert(children.length > 1);
        this.children = children;
        this.childNextValues = Arrays
                .stream(children)
                .mapToLong(StreamNode::next)
                .toArray();
    }

    @Override
    public long next() {
        var minimum = childNextValues[0];
        for (int i = 1; i < childNextValues.length; i++) {
            minimum = Math.min(minimum, childNextValues[i]);
        }
        for (int i = 0; i < childNextValues.length; i++) {
            if (childNextValues[i] == minimum) {
                childNextValues[i] = children[i].next();
            }
        }
        return minimum;
    }

    @Override
    public long skipToNext(long start) {
        var minimum = children[0].skipToNext(start);
        for (int i = 0; i < childNextValues.length; i++) {
            childNextValues[i] = children[i].skipToNext(start);
            minimum = Math.min(minimum, childNextValues[i]);
        }
        return minimum;
    }
}
