package com.bytehamster.mmphfexperiments.benchmark;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IntStream implements Closeable {
        private final InputStream stream;
        private final byte[] readBuffer = new byte[Integer.BYTES];

        public IntStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        private void readFully(byte[] b, int off, int len) throws IOException {
            int count;
            for(int n = 0; n < len; n += count) {
                count = stream.read(b, off + n, len - n);
                if (count < 0) {
                    throw new EOFException();
                }
            }

        }

        public int readInt() throws IOException {
            readFully(this.readBuffer, 0, Integer.BYTES);
            return    ((int)(this.readBuffer[3] & 255) << 24)
                    + (int)((this.readBuffer[2] & 255) << 16)
                    + (int)((this.readBuffer[1] & 255) << 8)
                    + (int)((this.readBuffer[0] & 255) << 0);
        }
    }
