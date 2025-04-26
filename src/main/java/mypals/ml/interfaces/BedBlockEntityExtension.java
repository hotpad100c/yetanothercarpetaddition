package mypals.ml.interfaces;

import org.spongepowered.asm.mixin.Unique;

public interface BedBlockEntityExtension {
    float getSleeperYaw();


    float getSleeperPitch();

    void setSleeperYaw(float yaw);

    void getSleeperPitch(float pitch);
}
