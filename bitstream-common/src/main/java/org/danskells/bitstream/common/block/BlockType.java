package org.danskells.bitstream.common.block;

public enum BlockType {
  BITMAP,
  RUN_LENGTH,
  LIST,
  ;
  private final static BlockType[] typesById = values();

  public static BlockType ofId(int i) {
    return typesById[i];
  }
}
