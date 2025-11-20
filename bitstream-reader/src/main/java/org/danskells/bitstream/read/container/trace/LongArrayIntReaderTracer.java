package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.read.coder.IRead;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class LongArrayIntReaderTracer extends IntTracer {

  LongArrayIntReaderTracer(ByteBuffer buffer, PrintStream out, IRead intReader) {
    super(buffer, out, intReader);
  }

  int index = 0;
  long offset = 0;
  List<Long> values = new ArrayList<>();

  @Override
  public void trace(int posBefore, int posAfter, long result) {
    switch (index++) {
      case 0 -> {
        out.printf("%n     # value #1 is implied (0) %n     # Read length of block in bytes%n");
      }
      default -> {
        out.printf("%n     # Read value #%d (%d) -> bit position %d + %d  + 1 = %d%n", index, result, offset, result, offset + result + 1);
        offset = offset + result + 1;
      }
    }
    values.add(offset);

    traceReadLong(posBefore, posAfter, result);
  }
}
