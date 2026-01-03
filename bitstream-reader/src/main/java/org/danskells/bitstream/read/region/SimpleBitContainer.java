package org.danskells.bitstream.read.region;

import org.danskells.bitstream.read.BitContainer;
import org.danskells.bitstream.read.Biterator;
import org.danskells.bitstream.read.biterator.ParentBiterator;

import java.util.List;
import java.util.function.Supplier;

public class SimpleBitContainer implements BitContainer {

    private final List<BitRegion> regions;

    public SimpleBitContainer(List<BitRegion> regions) {
        this.regions = regions;
        assert isValidLayout();
    }

    private boolean isValidLayout() {
        for (int i = 1; i < regions.size(); i++) {
            if (regions.get(i).getStartInclusive() < regions.get(i - 1).getStartInclusive()) {
                throw new IllegalArgumentException("Regions are not sorted: " + regions.get(i - 1) + " and " + regions.get(i));
            }
            if (regions.get(i).getStartInclusive() < regions.get(i - 1).getEndExclusive()) {
                throw new IllegalArgumentException("Regions overlap: " + regions.get(i - 1) + " and " + regions.get(i));
            }
        }
        return true;
    }

    @Override
    public Biterator biterator() {
        return new ParentBiterator(
                regions
                        .stream()
                        .map(x -> (Supplier<Biterator>) x::biterator)
                        .iterator());
    }

    @Override
    public Biterator setBits() {
        return new SimpleBitContainerBiterator(true);
    }

    @Override
    public Biterator clearBits() {
        return new SimpleBitContainerBiterator(false);
    }

    public class SimpleBitContainerBiterator extends Biterator {
        private final boolean setBits;
        private int currentRegionIndex;
        private final SimpleBitContainerBiteratorCallback callback;
        private RegionBits current;

        SimpleBitContainerBiterator(boolean setBits) {
            this.setBits = setBits;
            this.currentRegionIndex = 0;
            this.callback = new SimpleBitContainerBiteratorCallback();
            this.current = regions.isEmpty() ? RegionBits.empty() : regions.getFirst().bits(setBits, callback);
        }

        @Override
        public boolean trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
            return current.trySkipTo(position, action, actionParameter);
        }

        @Override
        public boolean tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
            return current.tryIndexedAdvance(action, actionParameter);
        }

        public class SimpleBitContainerBiteratorCallback {
            public boolean nextRegion_trySkipTo(long position, IndexedLongConsumer action, int actionParameter) {
                currentRegionIndex++;
                while (currentRegionIndex < regions.size() &&
                        regions.get(currentRegionIndex).getEndExclusive() <= position) {
                    currentRegionIndex++;
                }
                if (currentRegionIndex >= regions.size()) {
                    current = RegionBits.empty();
                } else {
                    current = regions.get(currentRegionIndex).bits(setBits, callback);
                }
                return current.trySkipTo(position, action, actionParameter);
            }

            public boolean nextRegion_tryIndexedAdvance(IndexedLongConsumer action, int actionParameter) {
                currentRegionIndex++;
                if (currentRegionIndex >= regions.size()) {
                    current = RegionBits.empty();
                } else {
                    current = regions.get(currentRegionIndex).bits(setBits, callback);
                }
                return current.tryIndexedAdvance(action, actionParameter);
            }
        }
    }
}
