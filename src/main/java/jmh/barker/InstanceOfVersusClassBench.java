package jmh.barker;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 4, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1,jvmArgsAppend = "-Xmx512M")
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class InstanceOfVersusClassBench
{
    private static final Object SAMPLE_STRING = "sample string";
    private static final Integer SAMPLE_INTEGER = 3742394;
    private boolean typeSwitch;

    @Benchmark
    public boolean instanceOfCheck()
    {
        return getSample() instanceof String;
    }

    private Object getSample()
    {
        typeSwitch = !typeSwitch;
        return typeSwitch ? SAMPLE_INTEGER : SAMPLE_STRING;
    }

    @Benchmark
    public boolean getClassCheck()
    {
        return getSample().getClass() == SAMPLE_STRING.getClass();
    }
}
