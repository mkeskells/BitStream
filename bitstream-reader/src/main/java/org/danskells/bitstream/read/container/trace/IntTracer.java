package org.danskells.bitstream.read.container.trace;

import org.danskells.bitstream.read.coder.IRead;
import org.danskells.bitstream.read.coder.MsbReader;

import java.io.PrintStream;
import java.nio.ByteBuffer;

abstract class IntTracer {
  private final IRead intReader;
  private final ByteBuffer buffer;
  protected final PrintStream out;

  public IntTracer(ByteBuffer buffer, PrintStream out, IRead intReader) {
    this.buffer = buffer;
    this.out = out;
    this.intReader = intReader;
  }

  protected void traceReadLong(int posBefore, int posAfter, long result) {
    if (intReader == MsbReader.INSTANCE) {
      traceMsbReadLong(posBefore, posAfter, result);
    } else {
      throw new IllegalStateException("Unexpected intReader" + intReader.getClass().getName());
    }
  }

  private void traceMsbReadLong(int posBefore, int posAfter, long result) {
    var length = posAfter - posBefore;
    if (length == 1) {
      out.printf("%02X   #   (vint 1 byte) %d", result, result);
    } else {
      var bits = String.format("%8s", Integer.toBinaryString(buffer.get(posBefore) & 0xFF)).replace(' ', '0');
      var lowerBits = bits.substring(Math.min(length, 8), 8);
      out.printf("%02X   #   (vint %d bytes, lower %d bits %s = %d%n", buffer.get(posBefore), length, lowerBits.length(), lowerBits, Integer.parseInt(lowerBits, 2));
      var hex = "";
      for (int i = posBefore + 1; i < posAfter - 1; i++) {
        out.printf("%02X%n", buffer.get(i));
        hex = String.format("%02X", buffer.get(i)) + hex;
      }
      hex = String.format("%02X", buffer.get(posAfter - 1)) + hex;
      out.printf("%02X   #   (vint 0x%s << %d + %d = %d%n", buffer.get(posAfter - 1), hex, lowerBits.length(), Integer.parseInt(lowerBits, 2), result);
      var check = (Integer.parseInt(hex, 16) << lowerBits.length()) + (Integer.parseInt(lowerBits, 2));
      if (check != result) {
        throw new IllegalStateException(String.format("ERROR: Computed value %d does not match read value %d%n", check, result));
      }
    }
  }

  abstract void trace(int posBefore, int posAfter, long result);
}
