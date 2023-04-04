package com.bytehamster.mmphfexperiments.benchmark;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class LongStream implements Closeable {
        private final InputStream stream;
        private final byte[] readBuffer = new byte[Long.BYTES];

        public LongStream (InputStream stream) {
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

        public long readLong() throws IOException {
            readFully(this.readBuffer, 0, Long.BYTES);
            return    ((long)(this.readBuffer[7] & 255) << 56)
                    + ((long)(this.readBuffer[6] & 255) << 48)
                    + ((long)(this.readBuffer[5] & 255) << 40)
                    + ((long)(this.readBuffer[4] & 255) << 32)
                    + ((long)(this.readBuffer[3] & 255) << 24)
                    + (long)((this.readBuffer[2] & 255) << 16)
                    + (long)((this.readBuffer[1] & 255) << 8)
                    + (long)((this.readBuffer[0] & 255) << 0);
        }
    }
