package mypals.ml.mixin.features.bedRecordHeadRotation;

import com.mojang.authlib.GameProfile;
import mypals.ml.interfaces.BedBlockEntityExtension;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing;
import static net.minecraft.block.BedBlock.PART;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityWakeUpMixin extends PlayerEntity {

    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public PlayerEntityWakeUpMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    @Inject(
            method = "Lnet/minecraft/server/network/ServerPlayerEntity;wakeUp(ZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void wakeUp(CallbackInfo ci) {
        BedBlockEntityExtension bed = findNearbyBeds(this.getBlockPos(), this.getWorld());
        if (bed != null && bedsRecordSleeperFacing) {
            this.networkHandler.requestTeleport(this.getX(), this.getY(), this.getZ(),
                    bed.getSleeperYaw(), bed.getSleeperPitch());
        }


    }
//
//    @Override
//    public Iterable<ItemStack> getArmorItems() {
//        return this.getInventory().armor;
//    }

    @Override
    public boolean isSpectator() {
        return this.interactionManager.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return this.interactionManager.getGameMode() == GameMode.CREATIVE;
    }

//    @Override
//    public ItemStack getEquippedStack(EquipmentSlot slot) {
//        if (slot == EquipmentSlot.MAINHAND) {
//            return this.getEquippedStack(EquipmentSlot.MAINHAND);
//        } else if (slot == EquipmentSlot.OFFHAND) {
//            return this.getEquippedStack(EquipmentSlot.OFFHAND);
//        } else {
//            return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? this.getInventory().getStack(slot.getEntitySlotId()) : ItemStack.EMPTY;
//        }
//    }

//    @Override
//    public void equipStack(EquipmentSlot slot, ItemStack stack) {
//        this.processEquippedStack(stack);
//        if (slot == EquipmentSlot.MAINHAND) {
//            this.onEquipStack(slot, this.getInventory().main.set(this.getInventory().selectedSlot, stack), stack);
//        } else if (slot == EquipmentSlot.OFFHAND) {
//            this.onEquipStack(slot, this.getInventory().offHand.set(0, stack), stack);
//        } else if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
//            this.onEquipStack(slot, this.getInventory().armor.set(slot.getEntitySlotId(), stack), stack);
//        }
//    }

    @Override
    public Arm getMainArm() {
        return this.dataTracker.get(MAIN_ARM) == 0 ? Arm.LEFT : Arm.RIGHT;
    }

    @Unique
    private static BedBlockEntityExtension findNearbyBeds(BlockPos playerPos, World world) {
        for (BlockPos pos : BlockPos.iterate(playerPos.add(-1, -1, -1), playerPos.add(1, 1, 1))) {

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof BedBlockEntityExtension bedBlockEntity) {
                BedPart bedPart = world.getBlockState(pos).get(PART);
                if (bedPart == BedPart.HEAD) {
                    return bedBlockEntity;
                }
            }
        }
        return null;
    }
}
