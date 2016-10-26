package jmh.barker;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class ListVersusMap
{
    private List<Integer> list;
    private Map<Integer, String> map;

    @Param({ "16", "128", "1024", "4096"})
    private int len;

    private Integer lowIndex;
    private Integer midIndex;
    private Integer highIndex;

    @Setup
    public void setup() throws NoSuchAlgorithmException
    {
        list = new ArrayList<>(len);
        map = new HashMap<>(len);
        for (int i = 0; i < len; i++)
        {
            Integer integer = new Integer(i);
            list.add(integer);
            map.put(integer, integer.toString());
        }
        lowIndex = new Integer(2);
        midIndex = new Integer(len / 2);
        highIndex = new Integer(len - (len / 10));
    }

    @Benchmark
    public boolean lowIndex_List()
    {
        return list.contains(lowIndex);
    }

    @Benchmark
    public boolean lowIndex_Map()
    {
        return map.containsKey(lowIndex);
    }

    @Benchmark
    public boolean midIndex_List()
    {
        return list.contains(midIndex);
    }

    @Benchmark
    public boolean midIndex_Map()
    {
        return map.containsKey(midIndex);
    }

    @Benchmark
    public boolean highIndex_List()
    {
        return list.contains(highIndex);
    }

    @Benchmark
    public boolean highIndex_Map()
    {
        return map.containsKey(highIndex);
    }
}
