A BitStream can contain a series of block, and each block is at least conseptually a file.
The format of a block should allow to be parsed into a java data structure, or be accessed in a native memory


# File Structure
the file contains a header, with metadata and index, and then blocks.

```
+------------------+
| File Header      |
+------------------+
| Block Stream 1   |
+------------------+
| Block Stream 2   |
+------------------+
| ...              |
+------------------+
| Block Stream N   |
+------------------+
```
The file header will contain some index, probably a skip list, but not defined yet


Each block contains a packed type and type data, followed by the binary data
For each block the first bit is implied. I.e. each block is positioned at a set bit, and that set bit is not in the data stored

in ebnf
```
block-header        :== bit-offset block-specific-data
bit-offset          :== vint # the relative offset from the preceeding block control point

block type          :== 3 bits
block-specific      :== 5 bits of block-specific-data # currently the length of the block for the 3 encodings
block-length        :== vint # size of the block in bytes. The end of this is the control point.

block-specific-data :== bitmap-block-data | rle-block-data | list-block-data # maybe others
#block specific data starts with a contol byte, that udentifies the type of block (3 bits) and the length of the block (5 bits)
#the length of the block means different things for each block type

bitmap-block-data   :== byte* # raw bitmap data, length is determined by block-specific
#for bitmap block, the block-specific indicates the length of the bitmap -1 in and the control point is the end of that block
#its length -1 because we know that we will have one byte, so 0 would be invalid
#the control point is the last bit in the array + 1

rle-block-data      :== block-length block-rle-pair-count pair-length rle-pair*
rle-pair            :== pair-offset pair-length
pair-offset         :== vint # the offset since the last pair end -1 (as we know 0 is invalid)
#for a rle block, we dont need a first offset, just the length
#the control point is the start of the block (so as to avoid having to parse all of the values)

list-block-data     :== block-length list-item-count list-item*
list-item-count     :== vint
list-item           :== vint
#the control point is the start of the block (so as to avoid having to parse all of the values)
```

ids - bitmap-block  -> 0
      rle-block     -> 1
      list-block    -> 2
others are reserved for future use.
probably small versions of the above

examples of block specific data (hex), assuming compressed 7 bit vint

a bitmap
```txt
04   #         -> control byte 
     #   000   -> block type = bitmap-block
     #   00100 -> block-length = 4 bytes
01   #         -> bit-offsets = 0, 1 (0 is implied)
04   #         -> bit offsets = 11
05   #         -> bit offsets = 17, 19
ff   #         -> bit offsets = 25 .. 32
```
a rle-block
```txt
22   #         -> control byte
     #   001   -> block type = rle-block
     #   00010 -> block-length = 2 pairs
04   #   (vint 4) length of block in bytes
07   #   (vint 7) length            -> bit offsets 0 ..7 
80   #   (vint 2 bytes) 
01   #   (vint 129) offest 129
30   #   (vint 48) length 48        -> bit offsets 136 .. 184
```
an array-block
```txt
42   #         -> control byte
     #   010   -> block type = array-block
     #   00010 -> block-length = 2 values
                                    -> bit position 0 implied
07   #   (vint 7)                   -> bit position 8 
c4   #   (vint 3 bytes, lower 5 bits = 00100)
47
80   #   (vint 0x8047 << 5 + 3 = 0x1008E0 (or 1051872 in decimal) + 3 = 1051875
                                    -> bit position 1051883
```

