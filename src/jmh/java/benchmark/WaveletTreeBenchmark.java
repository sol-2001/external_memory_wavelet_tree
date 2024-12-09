package benchmark;


import org.example.MappedCharSequence;
import org.example.WaveletTree;
import org.openjdk.jmh.annotations.*;

import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(1)
public class WaveletTreeBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        private static final Path DATA_FILE = Path.of("wavelet_data.txt");
        public WaveletTree waveletTree;
        public Random random = new Random();
        public int dataLength;

        @Setup(Level.Iteration)
        public void setUp() throws Exception {
            MappedCharSequence sequence = new MappedCharSequence(DATA_FILE);
            dataLength = sequence.length();
            random = new Random();
            waveletTree = new WaveletTree(sequence);
        }
    }
    @Benchmark
    public void testBuildTree(BenchmarkState state) throws Exception {
        MappedCharSequence sequence = new MappedCharSequence(Path.of("wavelet_data.txt"));
        new WaveletTree(sequence);

    }

    // операция access
    @Benchmark
    public char testAccess(BenchmarkState state) throws Exception {
        int randomIndex = state.random.nextInt(state.dataLength);
        return state.waveletTree.access(randomIndex);
    }

    // операция rank
    @Benchmark
    public int testRank(BenchmarkState state) throws Exception {
        int randomIndex = state.random.nextInt(state.dataLength);
        char randomChar = (char) ('a' + state.random.nextInt(26));

        return state.waveletTree.rank(randomChar, randomIndex);
    }

}
