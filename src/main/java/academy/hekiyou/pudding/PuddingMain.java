package academy.hekiyou.pudding;

import academy.hekiyou.pudding.fs.PuddingFS;
import academy.hekiyou.pudding.fs.PuddingFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PuddingMain {
    
    private static PuddingFS fs;
    
    public static void main(String[] args){
        Thread thread = new Thread(new ReportTask());
        thread.setDaemon(true);
        thread.start();
        
        fs = null;
        try {
            fs = new PuddingFS(Paths.get("C:\\Program Files\\ModifiableWindowsApps\\pso2_bin\\data\\win32_real"));
            fs.mount(Paths.get("C:\\Program Files\\ModifiableWindowsApps\\pso2_bin\\data\\win32"), true, false, new String[]{"-o", "FileInfoTimout=0"});
        } catch (IOException exc) {
            exc.printStackTrace();
        } finally {
            if(fs != null)
                fs.umount();
        }
    }
    
    public static class ReportTask implements Runnable {
    
        @Override
        public void run(){
            while(true){
                double percent = (PuddingFile.HIT / ((double)PuddingFile.HIT + PuddingFile.MISS) * 100);
                System.out.printf("STATS: %d hits, %d misses (%.2f%% hit rate)\n",
                        PuddingFile.HIT,
                        PuddingFile.MISS,
                        Double.isNaN(percent) ? 0D : percent);
                PuddingFile.printStats();
                try {
                    Thread.sleep(3 * 1000);
                } catch(InterruptedException e){}
            }
        }
    
    }
    
}
