```txt
     # Read offset to block: 0 (one based) offset = 1 bits
00   #   (vint 1 byte) 0

41   #                -> control byte
     # 010            -> block type = List Block
     # 00001          -> (block specific) 2 additional values (1 based)
     #    Value #1 is the control position of the previous block (-1) + block offset (1) = 0
     #    Control Point is the first value (0)

     # Read length of block in bytes: 3 (one based) = 4 bytes
     # the length starts from the end of this value
03   #   (vint 1 byte) 3

     # Read value #2 (7) -> bit position 0 + 7 + 1 = 8
07   #   (vint 1 byte) 7

     # Read value #3 (1050852) -> bit position 8 + 1050852 + 1 = 1050861
C4   #   (vint 3 bytes, lower 5 bits 00100 = 4
47
80   #   (vint 0x8047 << 5 + 4 = 1050852

```