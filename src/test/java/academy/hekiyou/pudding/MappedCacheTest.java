package academy.hekiyou.pudding;

import academy.hekiyou.pudding.cache.CacheEntry;
import academy.hekiyou.pudding.cache.TieredCache;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.util.Random;

public class MappedCacheTest {
    
    private static final int CACHE_SIZE = 1_000;
    
    @State(Scope.Benchmark)
    public static class BenchState {
        
        private final TieredCache<Integer, Object> tieredCache = new TieredCache<Integer, Object>(CACHE_SIZE) {
            
            @Override
            public @NotNull CacheEntry<Integer, Object> makeEntry(Integer key, Object value){
                return new SimpleCacheEntry(key, value);
            }
            
        };
        
        private final Random random = new Random();
        
        @Setup(Level.Iteration)
        public void doSetup(){
            for(int i=0; i < CACHE_SIZE; i++){
                tieredCache.put(i, new Object());
            }
        }
    
        @TearDown(Level.Iteration)
        public void doTeardown(){
            tieredCache.clear();
        }
        
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 1)
    public void init(BenchState state){
        state.tieredCache.get(state.random.nextInt(CACHE_SIZE));
    }
    
    public static void main(String[] args){
        BenchState state = new BenchState();
        state.doSetup();
        
        MappedCacheTest test = new MappedCacheTest();
        for(int i=0; i < 1_000_000; i++){
            state.tieredCache.get(state.random.nextInt(CACHE_SIZE / 100));
            state.tieredCache.get(state.random.nextInt(CACHE_SIZE / 10));
            state.tieredCache.get(state.random.nextInt(CACHE_SIZE));
            
            if(state.random.nextDouble() > 0.75){
                state.tieredCache.put(CACHE_SIZE + state.random.nextInt(10000000), new Object());
            }
        }
        
        state.tieredCache.printStats();
    }
    
}
