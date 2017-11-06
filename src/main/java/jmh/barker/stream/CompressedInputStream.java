package jmh.barker.stream;

import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CompressedInputStream extends InputStream
{
    private static final byte[] POISON_PILL = new byte[0];

    // uncompressed bytes
    private final byte[] buffer;

    // offset from the beginning of the buffer
    public long bufferOffset = 0;
    // current position in stream
    public long current = 0;
    // number of bytes in the buffer that are actually valid
    protected int validBufferBytes = -1;

    public final BlockingQueue<byte[]> dataBuffer;

    public CompressedInputStream(byte[] buf)
    {
        this.dataBuffer = new ArrayBlockingQueue<>(16);
        buffer = buf;
        validBufferBytes = buf.length;

        // buffer offset is always aligned
        bufferOffset = current & ~(buffer.length - 1);
    }

    @Override
    public int read() throws IOException
    {
        if (current >= bufferOffset + buffer.length || validBufferBytes == -1)
            decompressNextChunk();

        assert current >= bufferOffset && current < bufferOffset + validBufferBytes;

        return ((int) buffer[(int) (current++ - bufferOffset)]) & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        long nextCurrent = current + len;

        if (current >= bufferOffset + buffer.length || validBufferBytes == -1)
            decompressNextChunk();

        assert nextCurrent >= bufferOffset;

        int read = 0;
        while (read < len)
        {
            int nextLen = Math.min((len - read), (int)((bufferOffset + validBufferBytes) - current));

            System.arraycopy(buffer, (int)(current - bufferOffset), b, off + read, nextLen);
            read += nextLen;

            current += nextLen;
            if (read != len)
                decompressNextChunk();
        }

        return len;
    }

    private void decompressNextChunk() throws IOException
    {
        try
        {
            byte[] compressedWithCRC = dataBuffer.take();
            if (compressedWithCRC == POISON_PILL)
            {
                throw new IOError(null);
            }
            System.arraycopy(compressedWithCRC, 0, buffer, 0, compressedWithCRC.length);
            validBufferBytes = buffer.length;

            // buffer offset is always aligned
            bufferOffset = current & ~(buffer.length - 1);
        }
        catch (InterruptedException e)
        {
            throw new EOFException("No chunk available");
        }
    }

}
