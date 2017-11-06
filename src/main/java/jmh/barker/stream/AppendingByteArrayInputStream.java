package jmh.barker.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;

public class AppendingByteArrayInputStream extends InputStream
{
    private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
    private byte[] currentBuf;
    public int currentPosition;
    private long bytesRead;

    /**
     * Total count of bytes in all {@link ByteBuf}s held by this instance. This is retained so that we know when to enable/disable
     * the netty channel's auto-read behavior. This value only indicates that total number of bytes in all buffers, and does
     * not distinguish between read and unread bytes - just anything taking up memory.
     */
    private final AtomicInteger liveByteCount;

    public AppendingByteArrayInputStream(byte[] buf)
    {
        liveByteCount = new AtomicInteger(0);
        updateBufferedByteCount(buf.length);
        currentBuf = buf;
    }

    public void append(byte[] buf) throws IllegalStateException
    {
        updateBufferedByteCount(buf.length);
        queue.add(buf);
    }

    void updateBufferedByteCount(int diff)
    {
        int liveBytes = liveByteCount.addAndGet(diff);
    }

    @Override
    public int read() throws IOException
    {
        if (currentBuf != null)
        {
            if (currentPosition < currentBuf.length)
            {
                byte b = currentBuf[currentPosition];
                currentPosition++;
                bytesRead++;
                return b & 0xFF;
            }

            updateBufferedByteCount(-currentBuf.length);
            currentBuf = null;
        }

        try
        {
            currentBuf = queue.take();
        }
        catch (InterruptedException ie)
        {
            throw new EOFException();
        }

        byte b = currentBuf[0];
        currentPosition = 1;
        bytesRead++;
        return b & 0xFF;
    }

    public int read(byte[] out, int off, final int len) throws IOException
    {
        int remaining = len;
        while (true)
        {
            if (currentBuf != null)
            {
                int currentBufRemaining = currentBuf.length - currentPosition;
                if (currentBufRemaining > 0)
                {
                    int toReadCount = Math.min(remaining, currentBufRemaining);
                    System.arraycopy(currentBuf, currentPosition, out, off, toReadCount);
                    remaining -= toReadCount;
                    currentPosition += toReadCount;

                    if (remaining == 0)
                    {
                        // TODO:JEB refactor this code - to avoid duplication
                        if (currentBufRemaining - toReadCount == 0)
                        {
                            updateBufferedByteCount(-currentBuf.length);
                            currentBuf = null;
                        }
                        bytesRead += len;
                        return len;
                    }
                    off += toReadCount;
                }

                updateBufferedByteCount(-currentBuf.length);
                currentBuf = null;
            }

            try
            {
                currentBuf = queue.take();
                currentPosition = 0;
            }
            catch (InterruptedException ie)
            {
                throw new EOFException();
            }
        }
    }
}
