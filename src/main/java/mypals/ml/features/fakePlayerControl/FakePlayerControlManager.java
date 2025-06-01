/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.features.fakePlayerControl;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.TeamCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FakePlayerControlManager {
    public static Map<ServerPlayerEntity, Map.Entry<Team, EntityPlayerMPFake>> binds = new HashMap<>();
    public static Map<ServerPlayerEntity, NbtCompound> bindTempData = new HashMap<>();

    public static boolean tryBind(ServerPlayerEntity player, EntityPlayerMPFake fakePlayer) {
        if (binds.containsKey(player)) {
            if (fakePlayer.getUuid() != binds.get(player).getValue().getUuid()) {
                unbindPlayer(player, binds.get(player).getValue());
                bindPlayer(player, fakePlayer);
                return true;
            } else {
                unbindPlayer(player, binds.get(player).getValue());
                return false;
            }
        } else {
            bindPlayer(player, fakePlayer);
            return true;
        }
    }

    public static void bindPlayer(ServerPlayerEntity player, EntityPlayerMPFake fakePlayer) {
        NbtCompound playerData = new NbtCompound();
        player.writeNbt(playerData);
        player.writeCustomDataToNbt(playerData);
        playerData.putString("GameMode", player.interactionManager.getGameMode().getName());
        playerData.putString("MainArm", player.getMainArm().toString());
        bindTempData.put(player, playerData);

        binds.put(player,
                Map.entry(addBindTeam(player, fakePlayer, player.getServerWorld()), fakePlayer));
        player.readNbt(fakePlayer.writeNbt(new NbtCompound()));
        player.setPosition(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ());
        player.setYaw(fakePlayer.getYaw());
        player.setPitch(fakePlayer.getPitch());
        player.setMainArm(fakePlayer.getMainArm());
        player.setHealth(fakePlayer.getHealth());
        player.changeGameMode(fakePlayer.interactionManager.getGameMode());
        player.setInvisible(true);
        fakePlayer.noClip = true;
    }

    public static Team addBindTeam(ServerPlayerEntity player, EntityPlayerMPFake fakePlayer, ServerWorld serverWorld) {
        System.out.println("Binding player: " + player.getName().getString() + " to fake player: " + fakePlayer.getName().getString());
        Team team = serverWorld.getServer().getScoreboard().addTeam(player.getName().getString() + "+" + fakePlayer.getName().getString());
        team.setShowFriendlyInvisibles(false);
        team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        team.setFriendlyFireAllowed(false);
        team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM);
        serverWorld.getServer().getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), team);
        serverWorld.getServer().getScoreboard().addScoreHolderToTeam(fakePlayer.getNameForScoreboard(), team);
        return team;
    }

    public static void tickBinds(ServerWorld serverWorld) {
        for (Map.Entry<ServerPlayerEntity, Map.Entry<Team, EntityPlayerMPFake>> entry : binds.entrySet()) {
            ServerPlayerEntity player = entry.getKey();
            EntityPlayerMPFake fakePlayer = entry.getValue().getValue();

            if (!fakePlayer.isAlive() || !player.isAlive() || player.isDisconnected()) {
                unbindPlayer(player, fakePlayer);
                continue;
            }
            if (!player.isInvisible()) {
                player.setInvisible(true);
            }
            fakePlayer.getInventory().clone(player.getInventory());
            player.getInventory().clone(fakePlayer.getInventory());

            player.setHealth(fakePlayer.getHealth());
            fakePlayer.setHealth(player.getHealth());
            fakePlayer.setAbsorptionAmount(player.getAbsorptionAmount());

            if (player.isFallFlying() && !fakePlayer.isFallFlying())
                fakePlayer.startFallFlying();
            else if (!player.isFallFlying() && fakePlayer.isFallFlying())
                fakePlayer.stopFallFlying();
            if (player.isSleeping() && !fakePlayer.isSleeping() && player.getSleepingPosition().isPresent())
                fakePlayer.setSleepingPosition(player.getSleepingPosition().get());
            else if (!player.isSleeping() && fakePlayer.isFallFlying())
                fakePlayer.wakeUp();

            fakePlayer.setOnGround(player.isOnGround());
            fakePlayer.setVelocity(player.getVelocity());
            fakePlayer.setPosition(player.getX(), player.getY(), player.getZ());
            fakePlayer.setYaw(player.getYaw());
            fakePlayer.setPose(player.getPose());
            fakePlayer.setPitch(player.getPitch());
            fakePlayer.setSneaking(player.isSneaking());
            fakePlayer.setSprinting(player.isSprinting());
            fakePlayer.setSwimming(player.isSwimming());

            fakePlayer.setInvulnerable(player.isInvulnerable());
            if (fakePlayer.isGlowing() != player.isGlowing())
                fakePlayer.setGlowing(player.isGlowing());
            if (fakePlayer.isOnFire() != player.isOnFire())
                fakePlayer.setOnFire(player.isOnFire());
            if (fakePlayer.getMainArm() != player.getMainArm())
                fakePlayer.setMainArm(player.getMainArm());
            if (fakePlayer.interactionManager.getGameMode() != player.interactionManager.getGameMode())
                fakePlayer.changeGameMode(player.interactionManager.getGameMode());

            fakePlayer.setFireTicks(player.getFireTicks());
            fakePlayer.setExperienceLevel(player.experienceLevel);
            fakePlayer.fallDistance = player.fallDistance;
            fakePlayer.setAir(player.getAir());
        }
    }

    public static void unbindPlayer(ServerPlayerEntity player, EntityPlayerMPFake fakePlayer) {
        Team team = binds.get(player).getKey();
        team.getScoreboard().removeTeam(team);
        System.out.println("Unbinding player: " + player.getName().getString() + " from fake player: " + fakePlayer.getName().getString());
        fakePlayer.readNbt(player.writeNbt(new NbtCompound()));

        NbtCompound playerData = bindTempData.get(player);
        player.readNbt(playerData);
        player.readCustomDataFromNbt(playerData);

        //#if MC>=12105
        //$$String gameModeName = playerData.getString("GameMode").get();
        //$$Arm arm = Arm.valueOf(playerData.getString("MainArm").get());
        //#else
        String gameModeName = playerData.getString("GameMode");
        Arm arm = Arm.valueOf(playerData.getString("MainArm"));
        //#endif
        GameMode gameMode = GameMode.byName(gameModeName, GameMode.SURVIVAL);
        player.changeGameMode(gameMode);
        player.setMainArm(arm);
        binds.remove(player);
        bindTempData.remove(player);
        player.setInvisible(false);
        fakePlayer.noClip = false;
    }
}