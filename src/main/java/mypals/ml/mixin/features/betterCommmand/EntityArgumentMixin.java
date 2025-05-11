package mypals.ml.mixin.features.betterCommmand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityArgumentType.class)
public class EntityArgumentMixin {
    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
    private void onListSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;

        CommandSource source = (CommandSource) context.getSource();

        if (!(source instanceof ServerCommandSource serverCommandSource)) return;

        MinecraftServer server = serverCommandSource.getServer();
        List<String> names = new ArrayList<>();

        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof PlayerEntity)) {
                    String name = entity.getDisplayName().getString();
                    names.add(entity.getUuidAsString() + "(%s)".formatted(name) + "(%s)".formatted(world.getRegistryKey().getValue()));
                }
            }
        }

        CommandSource.suggestMatching(names, builder);
        cir.setReturnValue(CompletableFuture.completedFuture(builder.build()));
    }
}
