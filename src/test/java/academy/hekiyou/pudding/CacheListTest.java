package academy.hekiyou.pudding;

import academy.hekiyou.pudding.cache.CacheEntry;
import academy.hekiyou.pudding.cache.StructuredList;
import academy.hekiyou.pudding.cache.SimpleStructuredList;

public class CacheListTest {
    
    public static void main(String[] args){
        StructuredList<CacheEntry<Integer, Object>> list = new SimpleStructuredList<>(3);
        
        CacheEntry<Integer, Object> one = new SimpleCacheEntry(1, new Object());
        CacheEntry<Integer, Object> two = new SimpleCacheEntry(2, new Object());
        CacheEntry<Integer, Object> three = new SimpleCacheEntry(3, new Object());
        CacheEntry<Integer, Object> four = new SimpleCacheEntry(4, new Object());
    
        list.add(one);
        list.add(two);
        list.add(three);
    
        StructuredList.printList(list);
        
        list.remove(two);
        
        one.incrementFreq();
//        list.restructure(one);
        
        list.add(four);
        
        StructuredList.printList(list);
    }
    
}
