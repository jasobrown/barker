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
public class InstanceOfBench
{
    private enum AnimalType { DOG, CAT }

    private interface Animal
    {
        AnimalType getAnimalType();
    }

    private class Dog implements Animal
    {
        public AnimalType getAnimalType()
        {
            return AnimalType.DOG;
        }
    }

    private class Cat implements Animal
    {
        public AnimalType getAnimalType()
        {
            return AnimalType.CAT;
        }
    }

    private final Animal dog = new Dog();

    @Benchmark
    public boolean enumCheck()
    {
        return dog.getAnimalType() == AnimalType.DOG;
    }

    @Benchmark
    public boolean instanceCheck()
    {
        return dog instanceof Animal;
    }

}
