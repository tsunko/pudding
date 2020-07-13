package academy.hekiyou.pudding.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TieredCache<K, V> {
    
    private final Map<K, CacheEntry<K, V>> entries;
    private final Map<CacheTier, StructuredList<CacheEntry<K, V>>> frequency;
    
    private final int capacity;
    private int size = 0;
    
    public TieredCache(int capacity){
        this.capacity = capacity;
        
        this.entries = new HashMap<>();
        this.frequency = new EnumMap<>(CacheTier.class);
        
        initialize();
    }
    
    @NotNull
    public abstract CacheEntry<K, V> makeEntry(K key, V value);
    
    public V put(@NotNull K key, @NotNull V value){
        if(contains(key))
            throw new IllegalStateException("Duplicate entry insertion");
        
        StructuredList<CacheEntry<K, V>> rarely = frequency.get(CacheTier.RARELY);
        
        if(size >= capacity){
            // find lowest entry and clean+remove it to make room for newest entry
            CacheEntry<K, V> lowest = rarely.popLowest();
            entries.remove(lowest.getKey());
            lowest.clean();
        }
        
        CacheEntry<K, V> newEntry = makeEntry(key, value);
        entries.put(key, newEntry);
        rarely.add(newEntry);
        size++;
        return null;
    }
    
    @Nullable
    public V get(@NotNull K key){
        CacheEntry<K, V> entry = entries.get(key);
        if(entry == null)
            return null;
        
        // increment frequency and restructure
        entry.incrementFreq();
        frequency.get(entry.getTier()).restructure(entry);
    
        // then try to promote
        updateTiers(entry.getTier());
        
        return entry.getValue();
    }
    
    public boolean contains(@Nullable K key){
        if(key == null) return false;
        return entries.containsKey(key);
    }
    
    public void clear(){
        initialize();
    }
    
    protected CacheEntry<K, V> getEntry(K key){
        return entries.get(key);
    }
    
    private void initialize(){
        if(!entries.isEmpty()){
            for(CacheEntry<K, V> entry : entries.values())
                entry.clean();
        }
    
        this.entries.clear();
        this.frequency.clear();
        
        for(CacheTier tier : CacheTier.values())
            this.frequency.put(tier, new SimpleStructuredList<>(capacity));
    
        this.size = 0;
    }
    
    private void updateTiers(@NotNull CacheTier tier){
        if(isTierFull(tier) && tier.next() != null && !isTierFull(tier.next())){
            // unconditionally promote highest to next tier
            CacheEntry<K, V> highest = frequency.get(tier).popHighest();
            frequency.get(tier.next()).add(highest);
            highest.setTier(tier.next());
        } else {
            // try to promote elements by default
            if(!trySwapEnds(tier, tier.next())){
                // otherwise, try to demote them
                trySwapEnds(tier.prev(), tier);
            }
        }
    }
    
    private boolean trySwapEnds(CacheTier fromTier, CacheTier toTier){
        StructuredList<CacheEntry<K, V>> from = frequency.get(fromTier);
        StructuredList<CacheEntry<K, V>> to = frequency.get(toTier);
        
        // nothing to try swapping from/to
        if(from == null || to == null || from.isEmpty() || to.isEmpty())
            return false;
        
        // from is higher than to
        if(from.getHighest().compareTo(to.getLowest()) > 0){
            // pop them from their respective lists
            CacheEntry<K, V> highest = from.popHighest();
            CacheEntry<K, V> lowest = to.popLowest();
    
            // insert into new list
            from.add(lowest);
            to.add(highest);
            
            // swap tiers
            swapTiers(lowest, highest);
            return true;
        }
        
        return false;
    }
    
    private void swapTiers(CacheEntry<K, V> a, CacheEntry<K, V> b){
        CacheTier bTmp = b.getTier();
        b.setTier(a.getTier());
        a.setTier(bTmp);
    }
    
    private int getThresholdFor(CacheTier tier){
        return (int)(capacity * tier.getScale());
    }
    
    private boolean isTierFull(CacheTier tier){
        return frequency.get(tier).size() > getThresholdFor(tier);
    }
    
    public void printStats(){
        for(CacheTier tier : CacheTier.values()){
            StructuredList.printList(frequency.get(tier));
        }
    }
    
}
