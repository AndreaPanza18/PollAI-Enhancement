package com.example.pollai.benchmark;

import com.example.pollai.magazzino.Cibo;
import com.example.pollai.magazzino.Magazzino;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2)
public class MagazzinoBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {

        @Param({"10", "100", "1000"})
        private int foodItems;

        private Magazzino magazzino;

        @Setup(Level.Trial)
        public void setUp() {
            magazzino = new Magazzino();
            for (int i = 0; i < foodItems; i++) {
                magazzino.addCibo(new Cibo("mangime-" + i, 100, magazzino));
            }
        }
    }

    @Benchmark
    public void calculateStockNotification(BenchmarkState state) {
        state.magazzino.setNotifica();
    }
}
