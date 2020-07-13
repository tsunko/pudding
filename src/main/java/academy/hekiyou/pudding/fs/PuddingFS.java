package academy.hekiyou.pudding.fs;

import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.Statvfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple FSUE file system that denies writing and most reading operations
 */
public class PuddingFS extends FuseStubFS {

    private final Map<String, PuddingFile> files;
    
    public PuddingFS(Path real) throws IOException {
        files = generateFS(real);
    }
    
    @Override
    public int statfs(String path, Statvfs stbuf){
        // windows requires these to be filled
        stbuf.f_blocks.set(1024L);
        stbuf.f_frsize.set(1024L);
        stbuf.f_bfree.set(1L);
        return super.statfs(path, stbuf);
    }
    
    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi){
        if(path.equals("/")){
            for(PuddingFile file : files.values())
                filter.apply(buf, file.getName(), null, 0);
            return 0;
        } else {
            return -ErrorCodes.ENOENT();
        }
    }
    
    @Override
    public int read(String path, Pointer buf, long size, long offset, FuseFileInfo fi){
        PuddingFile file = getFile(path);
        if(file == null)
            return -ErrorCodes.ENOENT();
        return file.read(buf, size, offset);
    }
    
    @Override
    public int open(String path, FuseFileInfo fi){
        PuddingFile file = getFile(path);
        if(file == null)
            return -ErrorCodes.ENOENT();
        file.incrementFreq();
        return 0;
    }
    
    @Override
    public int getattr(String path, FileStat stat){
        if(path.equals("/")){
            stat.st_mode.set(FileStat.S_IFDIR | 0744);
        } else {
            PuddingFile file = getFile(path);
            if(file != null){
                stat.st_mode.set(FileStat.S_IFREG | 0744);
                stat.st_size.set(file.getSize());
            } else {
                System.out.println("Failed to find file " + path);
                return -ErrorCodes.ENOENT();
            }
        }
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().gid.get());
        return 0;
    }
    
    @Override
    public int mkdir(String path, @mode_t long mode){
        return fail("rmdir", path, "mode", mode);
    }
    
    @Override
    public int rmdir(String path){
        return fail("rmdir", path);
    }
    
    @Override
    public int mknod(String path, long mode, long rdev){
        return fail("rmdir", path, "mode", mode, "rdev", rdev);
    }
    
    @Override
    public int unlink(String path){
        return fail("unlink", path);
    }
    
    @Override
    public int symlink(String oldpath, String newpath){
        return fail("symlink", oldpath, "newpath", newpath);
    }
    
    @Override
    public int rename(String oldpath, String newpath){
        return fail("rename", oldpath, "newpath", newpath);
    }
    
    @Override
    public int link(String oldpath, String newpath){
        return fail("link", oldpath, "newpath", newpath);
    }
    
    @Override
    public int write(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi){
        return fail("write", path, "buf", buf.address(), "size", size, "offset", offset, "fi", fi.toString());
    }
    
    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi){
        return fail("create", path, "mode", mode, "fi", fi.toString());
    }
    
    private PuddingFile getFile(String path){
        if(path.startsWith("/"))
            path = path.substring(path.indexOf('/') + 1);
        return files.get(path);
    }
    
    private int fail(String functionName, String path, Object... args){
        if(args.length > 0){
            StringBuilder argFormat = new StringBuilder("(");
            for(int i = 0; i < args.length; i += 2)
                argFormat.append(args[i]).append("=").append(args[i + 1]).append(",");
            argFormat.setLength(argFormat.length() - 1);
            argFormat.append(")");
            System.out.println(functionName + ": \"" + path + "\" "  + argFormat + " returning EACCES...");
        } else {
            System.out.println(functionName + ": \"" + path + "\" returning EACCES...");
        }
        return -ErrorCodes.EACCES();
    }
    
    private Map<String, PuddingFile> generateFS(Path path) throws IOException {
        return Files.list(path)
                .parallel()
                .filter(Files::isRegularFile)
                .map(PuddingFile::new)
                .collect(Collectors.toConcurrentMap(
                        PuddingFile::getName,
                        Function.identity()
                ));
    }
    
}
