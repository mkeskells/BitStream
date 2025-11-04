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
each block contains a packed type and type data, followed by the binary data

in ebnf
```
block-header        :== bit-offset block-specific-data
bit-offset          :== vint # the relative offset from the preceeding block control point

block type          :== 3 bits
block-size          :== 5 bits of block-specific-data # length of the block
block-length        :== vint # size of the block in bytes. The end of this is the control point.

block-specific-data :== bitmap-block-data | rle-block-data | list-block-data # maybe others
#block specific data starts with a contol byte, that udentifies the type of block (3 bits) and the length of the block (5 bits)
#the length of the block means different things for each block type

bitmap-block-data   :== byte* # raw bitmap data, length is determined by block-length
#for bitmap block, the block-length indicates the length of the bitmap in and the control point is the enad of that block

rle-block-data      :== block-rle-pair-count rle-pair*
rle-pair            :== pair-offset pair-length

list-block-data     :== list-item-count list-item*
list-item-count     :== vint
list-item           :== vint
```

ids - bitmap-block  -> 0
      rle-block     -> 1
      list-block    -> 2
others are reserved for future use.

examples of block (hex)
04   #         -> control byte 
     #   000   -> block type = bitmap-block
     #   00100 -> block-length = 4 bytes
01   #         -> bit-offsets = 0
04   #         -> bit offsets = 10
05   #         -> bit offsets = 16, 18
ff   #         -> bit offsets = 24 .. 31

22   #         -> control byte
     #   001   -> block type = rle-block
     #   00010 -> block-length = 2 pairs

```
bit-offset: the offset in bits from the start of the previous element, or the start of the block if its the first element.
block-length: the length of the block in bytes
block-specific-data: probably onl a few bits to indicate the type of block, and then the rest is the data for that block type.

pair-offset & pair length - both relative
so and rle-block-data would be like
5, 2, 7, 9, 14, 3, 2, 5, 96, 7, 15

means
5 pairs
offset 2 length 7
offset 9 length 14
offset 3 length 2
offset 5 length 96
offset 7 length 15
so relative to the start of the block 
bits 2- (2 + 7) are set
gap of 9 then next 14 bits are set
etc 



```


offset (compressed int)
type (a few bits)
length of block in bytes (rest of bits) and folowing bytes) (compressed int)
block specific data (variable length)


parts, top bits and bottom
top is the type

block type 0 :== bitmap block. Bottom bits is the start of the length



