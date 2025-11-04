format of a binary block. The idea is that is highly compessed and easy to parse to a stream, and to skip


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
each block contains a packed type and type data, fi\ollowed by the binary data

```
+------------------+
| Block Header     |
+------------------+
| Block Data       |
+------------------+
```

in ebnf
```
block-header        :== bit-offset block-length block-specific-data
bit-offset          :== vint
block-length        :== vint

block-specific-data :== bitmap-block-data | rle-block-data | list-block-data # maybe others

bitmap-block-data   :== bitmap-length byte*

rle-block-data      :== rle-pair-count rle-pair*
rle-pair            :== pair-offset pair-length

list-block-data     :== list-item-count list-item*
list-item-count     :== vint
list-item           :== vint
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



