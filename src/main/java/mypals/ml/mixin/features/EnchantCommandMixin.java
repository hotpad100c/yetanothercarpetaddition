package mypals.ml.mixin.features;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {

    @Shadow @Final private static Dynamic2CommandExceptionType FAILED_LEVEL_EXCEPTION;

    @Shadow @Final private static DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION;

    @Shadow @Final private static DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION;

    @Shadow @Final private static DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION;

    @Shadow @Final private static SimpleCommandExceptionType FAILED_EXCEPTION;

    @WrapMethod(
            method = "execute"
    )
    private static int getMaxLevel(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment, int level, Operation<Integer> original) throws CommandSyntaxException {
        Enchantment enchantment2 = enchantment.value();
        if (level > enchantment2.getMaxLevel()) {
            throw FAILED_LEVEL_EXCEPTION.create(level, enchantment2.getMaxLevel());
        } else {
            int i = 0;

            for (Entity entity : targets) {
                if (entity instanceof LivingEntity livingEntity) {
                    ItemStack itemStack = livingEntity.getMainHandStack();
                    if (!itemStack.isEmpty()) {
                        if ((enchantment2.isAcceptableItem(itemStack)
                                && EnchantmentHelper.isCompatible(EnchantmentHelper.getEnchantments(itemStack).getEnchantments(), enchantment)) ||
                                YetAnotherCarpetAdditionRules.enchantCommandBypassItemType) {
                            itemStack.addEnchantment(enchantment, level);
                            i++;
                        } else if (targets.size() == 1) {
                            throw FAILED_INCOMPATIBLE_EXCEPTION.create(itemStack.getItem().getName(itemStack).getString());
                        }
                    } else if (targets.size() == 1) {
                        throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                    }
                } else if (targets.size() == 1) {
                    throw FAILED_ENTITY_EXCEPTION.create(entity.getName().getString());
                }
            }

            if (i == 0) {
                throw FAILED_EXCEPTION.create();
            } else {
                if (targets.size() == 1) {
                    source.sendFeedback(
                            () -> Text.translatable("commands.enchant.success.single", Enchantment.getName(enchantment, level), ((Entity)targets.iterator().next()).getDisplayName()),
                            true
                    );
                } else {
                    source.sendFeedback(() -> Text.translatable("commands.enchant.success.multiple", Enchantment.getName(enchantment, level), targets.size()), true);
                }

                return i;
            }
        }
    }
}
