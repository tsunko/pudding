package academy.hekiyou.pudding;


import academy.hekiyou.pudding.cache.CacheEntry;

public class SimpleCacheEntry extends CacheEntry<Integer, Object> {
    
    public SimpleCacheEntry(Integer key, Object value){
        super(key, value);
    }
    
    @Override
    public void clean(){}
    
}
