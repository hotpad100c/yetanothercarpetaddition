package mypals.ml.mixin.features.noServerCrash;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class PreventServerCrashMixin {

    @Shadow
    public abstract PlayerManager getPlayerManager();

    @WrapMethod(method = "runServer")
    private void preventServerCrashAll(Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call();
            } catch (Exception e) {
                e.printStackTrace();
                if (this != null && this.getPlayerManager() != null) {
                    this.getPlayerManager().broadcast(Text.literal("[CrashPrevented] " + e.getLocalizedMessage()).formatted(Formatting.RED), false);
                }
            }
        } else {
            original.call();
        }
    }

    @WrapMethod(method = "tickWorlds")
    private void preventServerCrashWorld(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.bypassCrashForcibly) {
            try {
                original.call(shouldKeepTicking);
            } catch (Exception e) {
                e.printStackTrace();
                if (this != null && this.getPlayerManager() != null) {
                    this.getPlayerManager().broadcast(Text.literal("[CrashPrevented] " + e.getLocalizedMessage()).formatted(Formatting.RED), false);
                }
            }
        } else {
            original.call(shouldKeepTicking);
        }
    }
}
