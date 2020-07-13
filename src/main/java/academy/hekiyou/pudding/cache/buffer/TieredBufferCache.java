package academy.hekiyou.pudding.cache.buffer;

import academy.hekiyou.pudding.cache.CacheEntry;
import academy.hekiyou.pudding.cache.TieredCache;
import academy.hekiyou.pudding.fs.PuddingFile;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class TieredBufferCache extends TieredCache<PuddingFile, ByteBuffer> {
    
    public TieredBufferCache(int capacity){
        super(capacity);
    }
    
    @Override
    public @NotNull CacheEntry<PuddingFile, ByteBuffer> makeEntry(PuddingFile key, ByteBuffer value){
        return new CachedBufferEntry(key, value);
    }
    
    public Pointer getPointerFor(PuddingFile file){
        CacheEntry<PuddingFile, ByteBuffer> entry = getEntry(file);
        if(entry == null){
            return null;
        } else {
            return ((CachedBufferEntry)entry).getPointer();
        }
    }
    
}
