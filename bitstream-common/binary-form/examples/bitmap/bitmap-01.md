```txt
     # Read offset to block: 0 (one based) offset = 1 bits
00   #   (vint 1 byte) 0

03   #                -> control byte
     # 000            -> block type = Bitmap Block
     # 00011          -> (block specific) bytes in bitmap: 4 (1 based)
                      -> bit 0 is implied
01   #  byte[0]       -> bits set: 1
04   #  byte[1]       -> bits set: 11
05   #  byte[2]       -> bits set: 17, 19
FF   #  byte[3]       -> bits set: 25, 26, 27, 28, 29, 30, 31, 32
```