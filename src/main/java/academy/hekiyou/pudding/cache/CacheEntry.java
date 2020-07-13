package academy.hekiyou.pudding.cache;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic cache entry which tracks its own frequency and cache tier
 * @param <K> Key type
 * @param <V> Value type
 */
public abstract class CacheEntry<K, V> implements Comparable<CacheEntry<K, V>> {
    
    @NotNull
    private final K key;
    @NotNull
    private final V value;
    
    private final int keyHash;
    
    @NotNull
    private CacheTier tier;
    private int freq;
    
    public CacheEntry(@NotNull K key, @NotNull V value){
        this.key = key;
        this.value = value;
        
        // precompute the key's hash for performance
        this.keyHash = Objects.hash(key);
        
        // statistics about the current "priority".
        this.freq = 1;
        this.tier = CacheTier.RARELY;
    }
    
    @NotNull
    public K getKey(){
        return key;
    }
    
    @NotNull
    public V getValue(){
        return value;
    }
    
    @NotNull
    public CacheTier getTier(){
        return tier;
    }
    
    public void setTier(@NotNull CacheTier tier){
        this.tier = tier;
    }
    
    public void incrementFreq(){
        freq++;
    }
    
    @Override
    public int compareTo(@NotNull CacheEntry<K, V> o){
        return Integer.compare(freq, o.freq);
    }
    
    @Override
    public String toString(){
        return "CacheEntry{" +
                "key=" + key +
                ", freq=" + freq +
                '}';
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CacheEntry<?, ?> that = (CacheEntry<?, ?>) o;
        // acknowledging the fact that it's better to use key.equals(that.key),
        // the idea behind this is that it's going to be exceedingly rare for a key to be recreated
        // with the same exact values
        return key == that.key;
    }
    
    @Override
    public int hashCode(){
        return keyHash;
    }
    
    public abstract void clean();
    
    
}