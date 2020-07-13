package academy.hekiyou.pudding.fs;

import academy.hekiyou.pudding.cache.TieredCache;
import academy.hekiyou.pudding.cache.buffer.TieredBufferCache;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PuddingFile {
    
    public static long MISS = 0, HIT = 0;
    private static final TieredBufferCache TIERED_CACHE = new TieredBufferCache(500);
    private static final Pointer INVALID = Pointer.newIntPointer(Runtime.getSystemRuntime(), 0);
    
    private final Path path;
    private final long size;
    
    public PuddingFile(Path path){
        try {
            this.path = path;
            this.size = Files.size(path);
        } catch (IOException exc){
            throw new IllegalStateException("Failed to get file size for " + path, exc);
        }
    }
    
    public String getName(){
        return path.getFileName().toString();
    }
    
    @Override
    public String toString(){
        return getName();
    }
    
    public long getSize(){
        return size;
    }
    
    public int read(Pointer buffer, long reqSize, long offset){
        try {
            Pointer content = getCachedCopy();
            if(content == INVALID)
                return -1;
    
            int toRead = (int) Math.min(content.size() - offset, reqSize);
            content.transferTo(offset, buffer, 0, toRead);
            return toRead;
        } catch (Throwable t){
            t.printStackTrace(System.out);
            return -1;
        }
    }
    
    public void incrementFreq(){
        // dummy call...
        TIERED_CACHE.get(this);
    }
    
    @NotNull
    private Pointer getCachedCopy(){
        Pointer cached = TIERED_CACHE.getPointerFor(this);
        if(cached == null){
            try {
                FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                TIERED_CACHE.put(this, buffer);
                cached = TIERED_CACHE.getPointerFor(this);
            } catch(IOException exc) {
                exc.printStackTrace();
                return INVALID;
            }
            MISS++;
        } else {
            HIT++;
        }
        return cached;
    }
    
    public static void printStats(){
        TIERED_CACHE.printStats();
    }
    
}
