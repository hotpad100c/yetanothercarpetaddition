package mypals.ml.mixin.features.betterCommmand;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.betterCommands.GamerulesDefaultValueSorter;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GameRules.class)
public class GamerulesMixin {
    @Shadow
    @Final
    public Map<GameRules.Key<?>, GameRules.Rule<?>> rules;

    @Inject(
            method = "Lnet/minecraft/world/GameRules;<init>()V",
            at = @At(
                    "RETURN"
            )
    )
    public void createGameRules(CallbackInfo ci) {
        GamerulesDefaultValueSorter.gamerulesDefaultValues.clear();
        this.rules.forEach((key, rule) -> {
            GamerulesDefaultValueSorter
                    .gamerulesDefaultValues.put(key, rule.toString());

        });
    }

    @Inject(
            method = "Lnet/minecraft/world/GameRules;<init>(Ljava/util/Map;)V",
            at = @At(
                    "RETURN"
            )
    )
    public void createGameRules2(Map rules, CallbackInfo ci) {
        GamerulesDefaultValueSorter.gamerulesDefaultValues.clear();
        rules.forEach((key, rule) -> {
            GamerulesDefaultValueSorter
                    .gamerulesDefaultValues.put((GameRules.Key<?>) key, rule.toString());

        });
    }
    
}
