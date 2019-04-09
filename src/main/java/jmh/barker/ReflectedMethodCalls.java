package jmh.barker;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
public class ReflectedMethodCalls
{

    private final AtomicInteger seed = new AtomicInteger();

    // these are used for the
    private final A a = new A();
    private final B b = new B();
    private final C c = new C();
    private final D d = new D();

    private static final ConcurrentHashMap<Class, Object> memoized = new ConcurrentHashMap<>();

    @Benchmark
    public Object fromMap()
    {
        final Object o;
        int next = seed.incrementAndGet();
        switch (next & 0x03)
        {
            case 0: o = a; break;
            case 1: o = b; break;
            case 2: o = c; break;
            case 3: o = d; break;
            default:
                throw new IllegalArgumentException("you messed up: " + (next & 0x03));
        }

        return memoized.computeIfAbsent(o.getClass(), k -> {
            try
            {
                Method getBucketNameMethod = o.getClass().getMethod("get");
                int val = (Integer) getBucketNameMethod.invoke(o);
                Method setBucketNameMethod = o.getClass().getMethod("set", Integer.class);
                if(val != next)
                    setBucketNameMethod.invoke(o, next);
            }
            catch (Exception e)
            {
                //ignore
            }

            return "value"; // the value can be anything, but cannot be null
        });
    }

    @Benchmark
    public Object viaReflection()
    {
        Object o;
        int next = seed.incrementAndGet();
        switch (next & 0x03)
        {
            case 0: o = a; break;
            case 1: o = b; break;
            case 2: o = c; break;
            case 3: o = d; break;
            default:
                throw new IllegalArgumentException("you messed up: " + (next & 0x03));
        }

        try
        {
            Method getBucketNameMethod = o.getClass().getMethod("get");
            int val = (Integer) getBucketNameMethod.invoke(o);
            Method setBucketNameMethod = o.getClass().getMethod("set", Integer.class);
            if (val == next)
            {
                setBucketNameMethod.invoke(o, next);
            }
        }
        catch (Exception e)
        {
            //ignore
        }
        return o;
    }

    static class A
    {
        private int x;

        int get()
        {
            return x;
        }

        void set(int i)
        {
            x += i;
        }
    }

    static class B
    {
        private int x;

        int get()
        {
            return x;
        }

        void set(int i)
        {
            x *= i;
        }
    }

    /// DOES NOT IMPL set()
    static class C
    {
        int get()
        {
            return 42;
        }
    }

    static class D
    {
        private int x;

        int get()
        {
            return x;
        }

        void set(int i)
        {
            x -= i;
        }
    }

}
