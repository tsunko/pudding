package academy.hekiyou.pudding.cache.buffer;

import academy.hekiyou.pudding.cache.CacheEntry;
import academy.hekiyou.pudding.fs.PuddingFile;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

public class CachedBufferEntry extends CacheEntry<PuddingFile, ByteBuffer> {
    
    private final Pointer pointer;
    
    public CachedBufferEntry(PuddingFile key, ByteBuffer value){
        super(key, value);
        this.pointer = Pointer.wrap(Runtime.getSystemRuntime(), value);
    }
    
    public Pointer getPointer(){
        return pointer;
    }
    
    @Override
    public void clean(){
        if(getValue() instanceof DirectBuffer)
            ((DirectBuffer)getValue()).cleaner().clean();
    }
    
}
