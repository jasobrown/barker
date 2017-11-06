package jmh.barker;

import java.util.HashMap;
import java.util.Map;
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
@Measurement(iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.SampleTime)
public class MapLookup
{
    enum LETTER { a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z }

    private static final Map<LETTER, String> map = new HashMap();
    static
    {
        for (LETTER l : LETTER.values())
            map.put(l, l.toString());
    }

    @Benchmark
    public String fromMap()
    {
        return map.get(LETTER.g);
    }

    @Benchmark
    public String fromSwitchFirstCase()
    {
        LETTER g = LETTER.g;
        switch (g)
        {
            case g:
                return "g";
            default:
                return "5";
        }
    }

    @Benchmark
    public String fromSwitchDefault()
    {
        LETTER g = LETTER.g;
        switch (g)
        {
            case d:
                return "e";
            case a:
                return "q";
            case j:
                return "a";
            default:
                return "h";
        }
    }

    @Benchmark
    public String fromOr()
    {
        LETTER g = LETTER.g;
        if (g == LETTER.d || g == LETTER.g || g == LETTER.i)
            return "g";
        return "f";
    }
}
