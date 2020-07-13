package academy.hekiyou.pudding.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CacheTier {
    
    CONSTANTLY(0.2),
//    OCCASIONALLY(0.3),
    RARELY(0.8);
    
    private final double scale;
    
    CacheTier(double scale){
        this.scale = scale;
    }
    
    public double getScale(){
        return scale;
    }
    
    @Nullable
    public CacheTier next(){
        switch(this){
            case RARELY:      /*return OCCASIONALLY;
            case OCCASIONALLY:*/return CONSTANTLY;
            default:            return null;
        }
    }
    
    @Nullable
    public CacheTier prev(){
        switch(this){
            case CONSTANTLY:  /*return OCCASIONALLY;
            case OCCASIONALLY:*/return RARELY;
            default:            return null;
        }
    }
    
}