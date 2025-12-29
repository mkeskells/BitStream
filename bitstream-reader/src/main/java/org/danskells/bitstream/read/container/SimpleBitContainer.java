package org.danskells.bitstream.read.container;

import org.danskells.bitstream.read.BitContainer;
import org.danskells.bitstream.read.biterator.Biterator;
import org.danskells.bitstream.read.biterator.ParentBiterator;

import java.util.List;
import java.util.function.Supplier;

public class SimpleBitContainer implements BitContainer {

  private final List<BitContainerRegion> regions;

  public SimpleBitContainer(List<BitContainerRegion> regions) {
    this.regions = regions;
    assert isValidLayout();
  }
  private boolean isValidLayout() {
    for (int i = 1; i < regions.size(); i++) {
      if (regions.get(i).getStartInclusive() < regions.get(i-1).getStartInclusive()) {
        throw new IllegalArgumentException("Regions are not sorted: " + regions.get(i-1) + " and " + regions.get(i));
      }
      if (regions.get(i).getStartInclusive() < regions.get(i-1).getEndExclusive()) {
        throw new IllegalArgumentException("Regions overlap: " + regions.get(i-1) + " and " + regions.get(i));
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

}
