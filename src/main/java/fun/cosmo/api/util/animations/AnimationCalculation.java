package fun.cosmo.api.util.animations;

public interface AnimationCalculation {
    default double calculation(double value){
        return 0;
    }
}
