package com.nlp.netbaredemo.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zdd on 2019/11/6
 */
public class ChunkedInputStream
{
//    private InputStream in;
//    private HttpClient hc;
//    private MessageHeader responses;
//    private int chunkSize;
//    private int chunkRead;
//    private byte[] chunkData;
//    private int chunkPos;
//    private int chunkCount;
//    private byte[] rawData;
//    private int rawPos;
//    private int rawCount;
//    private boolean error;
//    private boolean closed;
//    private static final int MAX_CHUNK_HEADER_SIZE = 2050;
//    static final int STATE_AWAITING_CHUNK_HEADER = 1;
//    static final int STATE_READING_CHUNK = 2;
//    static final int STATE_AWAITING_CHUNK_EOL = 3;
//    static final int STATE_AWAITING_TRAILERS = 4;
//    static final int STATE_DONE = 5;
//    private int state;
//
//    private void ensureOpen() throws IOException {
//        if (this.closed) {
//            throw new IOException("stream is closed");
//        }
//    }
//
//    private void ensureRawAvailable(final int n) {
//        if (this.rawCount + n > this.rawData.length) {
//            final int rawCount = this.rawCount - this.rawPos;
//            if (rawCount + n > this.rawData.length) {
//                final byte[] rawData = new byte[rawCount + n];
//                if (rawCount > 0) {
//                    System.arraycopy(this.rawData, this.rawPos, rawData, 0, rawCount);
//                }
//                this.rawData = rawData;
//            }
//            else if (rawCount > 0) {
//                System.arraycopy(this.rawData, this.rawPos, this.rawData, 0, rawCount);
//            }
//            this.rawCount = rawCount;
//            this.rawPos = 0;
//        }
//    }
//
//    private void closeUnderlying() throws IOException {
//        if (this.in == null) {
//            return;
//        }
//        if (!this.error && this.state == 5) {
//            this.hc.finished();
//        }
//        else if (!this.hurry()) {
//            this.hc.closeServer();
//        }
//        this.in = null;
//    }
//
//    private int fastRead(final byte[] array, final int n, final int n2) throws IOException {
//        final int n3 = this.chunkSize - this.chunkRead;
//        final int n4 = (n3 < n2) ? n3 : n2;
//        if (n4 <= 0) {
//            return 0;
//        }
//        int read;
//        try {
//            read = this.in.read(array, n, n4);
//        }
//        catch (IOException ex) {
//            this.error = true;
//            throw ex;
//        }
//        if (read > 0) {
//            this.chunkRead += read;
//            if (this.chunkRead >= this.chunkSize) {
//                this.state = 3;
//            }
//            return read;
//        }
//        this.error = true;
//        throw new IOException("Premature EOF");
//    }
//
//    private void processRaw() throws IOException {
//        while (this.state != 5) {
//            switch (this.state) {
//                case 1: {
//                    int rawPos = this.rawPos;
//                    while (rawPos < this.rawCount && this.rawData[rawPos] != 10) {
//                        if (++rawPos - this.rawPos >= 2050) {
//                            this.error = true;
//                            throw new IOException("Chunk header too long");
//                        }
//                    }
//                    if (rawPos >= this.rawCount) {
//                        return;
//                    }
//                    String s;
//                    int i;
//                    for (s = new String(this.rawData, this.rawPos, rawPos - this.rawPos + 1, "US-ASCII"), i = 0; i < s.length(); ++i) {
//                        if (Character.digit(s.charAt(i), 16) == -1) {
//                            break;
//                        }
//                    }
//                    try {
//                        this.chunkSize = Integer.parseInt(s.substring(0, i), 16);
//                    }
//                    catch (NumberFormatException ex) {
//                        this.error = true;
//                        throw new IOException("Bogus chunk size");
//                    }
//                    this.rawPos = rawPos + 1;
//                    this.chunkRead = 0;
//                    if (this.chunkSize > 0) {
//                        this.state = 2;
//                        continue;
//                    }
//                    this.state = 4;
//                    continue;
//                }
//                case 2: {
//                    if (this.rawPos >= this.rawCount) {
//                        return;
//                    }
//                    final int min = Math.min(this.chunkSize - this.chunkRead, this.rawCount - this.rawPos);
//                    if (this.chunkData.length < this.chunkCount + min) {
//                        final int chunkCount = this.chunkCount - this.chunkPos;
//                        if (this.chunkData.length < chunkCount + min) {
//                            final byte[] chunkData = new byte[chunkCount + min];
//                            System.arraycopy(this.chunkData, this.chunkPos, chunkData, 0, chunkCount);
//                            this.chunkData = chunkData;
//                        }
//                        else {
//                            System.arraycopy(this.chunkData, this.chunkPos, this.chunkData, 0, chunkCount);
//                        }
//                        this.chunkPos = 0;
//                        this.chunkCount = chunkCount;
//                    }
//                    System.arraycopy(this.rawData, this.rawPos, this.chunkData, this.chunkCount, min);
//                    this.rawPos += min;
//                    this.chunkCount += min;
//                    this.chunkRead += min;
//                    if (this.chunkSize - this.chunkRead <= 0) {
//                        this.state = 3;
//                        continue;
//                    }
//                }
//                case 3: {
//                    if (this.rawPos + 1 >= this.rawCount) {
//                        return;
//                    }
//                    if (this.rawData[this.rawPos] != 13) {
//                        this.error = true;
//                        throw new IOException("missing CR");
//                    }
//                    if (this.rawData[this.rawPos + 1] != 10) {
//                        this.error = true;
//                        throw new IOException("missing LF");
//                    }
//                    this.rawPos += 2;
//                    this.state = 1;
//                    continue;
//                }
//                case 4: {
//                    int rawPos2;
//                    for (rawPos2 = this.rawPos; rawPos2 < this.rawCount && this.rawData[rawPos2] != 10; ++rawPos2) {}
//                    if (rawPos2 >= this.rawCount) {
//                        return;
//                    }
//                    if (rawPos2 == this.rawPos) {
//                        this.error = true;
//                        throw new IOException("LF should be proceeded by CR");
//                    }
//                    if (this.rawData[rawPos2 - 1] != 13) {
//                        this.error = true;
//                        throw new IOException("LF should be proceeded by CR");
//                    }
//                    if (rawPos2 == this.rawPos + 1) {
//                        this.state = 5;
//                        this.closeUnderlying();
//                        return;
//                    }
//                    final String s2 = new String(this.rawData, this.rawPos, rawPos2 - this.rawPos, "US-ASCII");
//                    final int index = s2.indexOf(58);
//                    if (index == -1) {
//                        throw new IOException("Malformed tailer - format should be key:value");
//                    }
//                    this.responses.add(s2.substring(0, index).trim(), s2.substring(index + 1, s2.length()).trim());
//                    this.rawPos = rawPos2 + 1;
//                    continue;
//                }
//            }
//        }
//    }
//
//    private int readAheadNonBlocking() throws IOException {
//        final int available = this.in.available();
//        if (available > 0) {
//            this.ensureRawAvailable(available);
//            int read;
//            try {
//                read = this.in.read(this.rawData, this.rawCount, available);
//            }
//            catch (IOException ex) {
//                this.error = true;
//                throw ex;
//            }
//            if (read < 0) {
//                this.error = true;
//                return -1;
//            }
//            this.rawCount += read;
//            this.processRaw();
//        }
//        return this.chunkCount - this.chunkPos;
//    }
//
//    private int readAheadBlocking() throws IOException {
//        while (this.state != 5) {
//            this.ensureRawAvailable(32);
//            int read;
//            try {
//                read = this.in.read(this.rawData, this.rawCount, this.rawData.length - this.rawCount);
//            }
//            catch (IOException ex) {
//                this.error = true;
//                throw ex;
//            }
//            if (read < 0) {
//                this.error = true;
//                throw new IOException("Premature EOF");
//            }
//            this.rawCount += read;
//            this.processRaw();
//            if (this.chunkCount > 0) {
//                return this.chunkCount - this.chunkPos;
//            }
//        }
//        return -1;
//    }
//
//    private int readAhead(final boolean b) throws IOException {
//        if (this.state == 5) {
//            return -1;
//        }
//        if (this.chunkPos >= this.chunkCount) {
//            this.chunkCount = 0;
//            this.chunkPos = 0;
//        }
//        if (b) {
//            return this.readAheadBlocking();
//        }
//        return this.readAheadNonBlocking();
//    }
//
//    public ChunkedInputStream(final InputStream in, final HttpClient hc, final MessageHeader responses) throws IOException {
//        this.chunkData = new byte[4096];
//        this.rawData = new byte[32];
//        this.in = in;
//        this.responses = responses;
//        this.hc = hc;
//        this.state = 1;
//    }
//
//    @Override
//    public synchronized int read() throws IOException {
//        this.ensureOpen();
//        if (this.chunkPos >= this.chunkCount && this.readAhead(true) <= 0) {
//            return -1;
//        }
//        return this.chunkData[this.chunkPos++] & 0xFF;
//    }
//
//    @Override
//    public synchronized int read(final byte[] array, final int n, final int n2) throws IOException {
//        this.ensureOpen();
//        if (n < 0 || n > array.length || n2 < 0 || n + n2 > array.length || n + n2 < 0) {
//            throw new IndexOutOfBoundsException();
//        }
//        if (n2 == 0) {
//            return 0;
//        }
//        int ahead = this.chunkCount - this.chunkPos;
//        if (ahead <= 0) {
//            if (this.state == 2) {
//                return this.fastRead(array, n, n2);
//            }
//            ahead = this.readAhead(true);
//            if (ahead < 0) {
//                return -1;
//            }
//        }
//        final int n3 = (ahead < n2) ? ahead : n2;
//        System.arraycopy(this.chunkData, this.chunkPos, array, n, n3);
//        this.chunkPos += n3;
//        return n3;
//    }
//
//    @Override
//    public synchronized int available() throws IOException {
//        this.ensureOpen();
//        final int n = this.chunkCount - this.chunkPos;
//        if (n > 0) {
//            return n;
//        }
//        final int ahead = this.readAhead(false);
//        if (ahead < 0) {
//            return 0;
//        }
//        return ahead;
//    }
//
//    @Override
//    public synchronized void close() throws IOException {
//        if (this.closed) {
//            return;
//        }
//        this.closeUnderlying();
//        this.closed = true;
//    }
//
//    @Override
//    public synchronized boolean hurry() {
//        if (this.in == null || this.error) {
//            return false;
//        }
//        try {
//            this.readAhead(false);
//        }
//        catch (Exception ex) {
//            return false;
//        }
//        return !this.error && this.state == 5;
//    }
}
