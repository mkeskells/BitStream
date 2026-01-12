```txt
                 Lets assume that the region starts at 100
     # Read offset to block: 0 (one based) offset = 1 bits
         Prior control point was 99, current block starts at 100
00   #   (vint 1 byte) 0

03   #                -> control byte
     # 000            -> block type = Bitmap Block
     # 00011          -> (block specific) bytes in bitmap: 4 (1 based)
                      -> bit 100 is implied - the control position of the previous block (99) + block offset (1) = 100
01   #  byte[0]       -> bits set: 101
04   #  byte[1]       -> bits set: 111
05   #  byte[2]       -> bits set: 117, 119
FF   #  byte[3]       -> bits set: 125, 126, 127, 128, 129, 130, 131, 132
```