package org.danskells.bitstream.read.region;

import org.danskells.bitstream.common.block.BlockType;
import org.danskells.bitstream.common.block.ControlByte;
import org.danskells.bitstream.read.coder.IRead;

import java.nio.ByteBuffer;

import static org.danskells.bitstream.common.block.BlockType.*;

public class BufferReader {

    protected final ByteBuffer buffer;
    protected final IRead intReader;

    private final FileHeader header;
    protected long currentBlockBitAddress = 0;

    protected BufferReader(ByteBuffer buffer, IRead intReader) {
        this.buffer = buffer;
        this.intReader = intReader;
        this.header = readHeader();
    }

    private FileHeader readHeader() {
        return new FileHeader();
    }

    protected int readUInt(ByteBuffer buffer) {
        return intReader.readUInt(buffer);
    }

    protected long readULong(ByteBuffer buffer) {
        return intReader.readULong(buffer);
    }

    void populateNextBlock(RegionBitsBase regionBitsBase) {
        long offset = intReader.readULong(buffer) + 1;
        regionBitsBase.currentBlockBitAddress += offset;
        var control = buffer.get();
        var blockType = BlockType.ofId(control >>> ControlByte.BLOCK_TYPE_SHIFT);
        var blockSpecific = control & ControlByte.BLOCK_SPECIFIC_MASK;
        switch (blockType) {
            case BITMAP -> populateBitmapBlockBits(regionBitsBase, blockSpecific);
            case RUN_LENGTH -> populateRleBlockBits(regionBitsBase, blockSpecific);
            case LIST -> populateListBlockBits(regionBitsBase, blockSpecific);
        }
    }

    void populateBitmapBlockBits(RegionBitsBase regionBitsBase, int size) {
        regionBitsBase.bitmap_byteSize = size + 1; // one based
        regionBitsBase.bitmap_byteStart = buffer.position();
        regionBitsBase.type(BITMAP);
        //so that whenwe read the next block it works correctly
        buffer.position( regionBitsBase.bitmap_byteStart + regionBitsBase.bitmap_byteSize );
    }

    void populateRleBlockBits(RegionBitsBase regionBitsBase, int size) {
        regionBitsBase.common_remaining = size + 1; //one based
        regionBitsBase.rle_startInc = regionBitsBase.currentBlockBitAddress;
        regionBitsBase.rle_endInc = regionBitsBase.currentBlockBitAddress + readULong(buffer) + 1; // One based. Length of zero is an array
        regionBitsBase.type(RUN_LENGTH);
    }

    void populateNextRlePair(RegionBitsBase regionBitsBase) {
        var previousEnd = regionBitsBase.rle_endInc;

        //it would not make sense to have a gap of one, as that would be the same run
        regionBitsBase.rle_startInc = previousEnd + readULong(buffer) + 2;
        regionBitsBase.rle_endInc = regionBitsBase.currentBlockBitAddress + readULong(buffer) + 1; // One based. Length of zero is an array
    }

    void populateListBlockBits(RegionBitsBase regionBitsBase, int size) {
        regionBitsBase.common_remaining = size + 1; //one based
        regionBitsBase.common_blockByteSize = readUInt(buffer) + 1; //one based
        regionBitsBase.list_nextSet = regionBitsBase.currentBlockBitAddress;
        regionBitsBase.type(LIST);
    }

    void populateNextListEntry(RegionBitsBase regionBitsBase) {
        regionBitsBase.list_nextSet += readULong(buffer) + 1; //one based
    }
}
