package org.danskells.bitstream.block;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.BitSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.danskells.bitstream.BitContainerStream.END_OF_STREAM;


public class BitSetBlockTest {

  public static BitSet buildBitset(int... bits) {
    var bitset = new BitSet();
    for (int bit : bits) {
      bitset.set(bit);
    }
    return bitset;
  }


  public static Stream<Arguments> arrayArguments() {
    return Stream.of(
        Arguments.of(
            "Single",
            buildBitset(0)
        ),
        Arguments.of(
            "Packed",
            buildBitset(0, 1, 2, 3, 4, 5)
        ),
        Arguments.of(
            "Sparse",
            buildBitset(0, 2, 4, 6, 8)
        ),
        Arguments.of(
            "Non-zero start",
            buildBitset(2, 3, 4)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("arrayArguments")
  public void createAndIterateLongArrayBlock(String description, BitSet bitset) {
    var bitmapBlock = new BitmapBlock(bitset);
    var node = bitmapBlock.getStreamNode();
    for (int bit : bitset.stream().toArray()) {
      assertEquals(bit, node.next());
    }
    assertEquals(END_OF_STREAM, node.next());
  }

}