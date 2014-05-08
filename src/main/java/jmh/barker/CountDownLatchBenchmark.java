package jmh.barker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Borrowed from https://gist.github.com/sbtourist/51cd5c5081e9399c6356
 * by @sbtourist.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class CountDownLatchBenchmark
{
    private CountDownLatch latch;

    @Setup
    public void setup()
    {
        latch = new CountDownLatch(1);
        latch.countDown();
    }

    @GenerateMicroBenchmark
    public boolean testZeroAwait() throws InterruptedException
    {
        return latch.await(1, TimeUnit.MILLISECONDS);
    }

    @GenerateMicroBenchmark
    public long testGetCount()
    {
        return latch.getCount();
    }

    @GenerateMicroBenchmark
    public Thread testCurrentThread()
    {
        return Thread.currentThread();
    }
}
