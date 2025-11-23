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
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FakePlayerControlManager {
    public static Map<ServerPlayer, Map.Entry<PlayerTeam, EntityPlayerMPFake>> binds = new HashMap<>();
    public static Map<ServerPlayer, CompoundTag> bindTempData = new HashMap<>();

    public static boolean tryBind(ServerPlayer player, EntityPlayerMPFake fakePlayer) {
        if (binds.containsKey(player)) {
            if (fakePlayer.getUUID() != binds.get(player).getValue().getUUID()) {
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

    public static void bindPlayer(ServerPlayer player, EntityPlayerMPFake fakePlayer) {
        CompoundTag playerData = new CompoundTag();
        NBTDataManager.readFromEntity(player, playerData);
        playerData.putString("GameMode", player.gameMode.getGameModeForPlayer().getName());
        playerData.putString("MainArm", player.getMainArm().toString());
        bindTempData.put(player, playerData);

        binds.put(player,
                Map.entry(addBindTeam(player, fakePlayer, player.level()), fakePlayer));


        CompoundTag fakePlayerData = new CompoundTag();

        NBTDataManager.writeToEntity(player, NBTDataManager.readFromEntity(fakePlayer, fakePlayerData));

        player.setPos(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ());
        player.setYRot(fakePlayer.getYRot());
        player.setXRot(fakePlayer.getXRot());
        player.setMainArm(fakePlayer.getMainArm());
        player.setHealth(fakePlayer.getHealth());
        player.setGameMode(fakePlayer.gameMode.getGameModeForPlayer());
        player.setInvisible(true);
        fakePlayer.noPhysics = true;
    }

    public static PlayerTeam addBindTeam(ServerPlayer player, EntityPlayerMPFake fakePlayer, ServerLevel serverWorld) {
        System.out.println("Binding player: " + player.getName().getString() + " to fake player: " + fakePlayer.getName().getString());
        PlayerTeam team = serverWorld.getServer().getScoreboard().addPlayerTeam(player.getName().getString() + "+" + fakePlayer.getName().getString());
        team.setSeeFriendlyInvisibles(false);
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.setAllowFriendlyFire(false);
        team.setNameTagVisibility(Team.Visibility.HIDE_FOR_OWN_TEAM);
        serverWorld.getServer().getScoreboard().addPlayerToTeam(player.getScoreboardName(), team);
        serverWorld.getServer().getScoreboard().addPlayerToTeam(fakePlayer.getScoreboardName(), team);
        return team;
    }

    public static void tickBinds(ServerLevel serverWorld) {
        for (Map.Entry<ServerPlayer, Map.Entry<PlayerTeam, EntityPlayerMPFake>> entry : binds.entrySet()) {
            ServerPlayer player = entry.getKey();
            EntityPlayerMPFake fakePlayer = entry.getValue().getValue();

            if (!fakePlayer.isAlive() || !player.isAlive() || player.hasDisconnected()) {
                unbindPlayer(player, fakePlayer);
                continue;
            }
            if (!player.isInvisible()) {
                player.setInvisible(true);
            }
            fakePlayer.getInventory().replaceWith(player.getInventory());
            player.getInventory().replaceWith(fakePlayer.getInventory());

            player.setHealth(fakePlayer.getHealth());
            fakePlayer.setHealth(player.getHealth());
            fakePlayer.setAbsorptionAmount(player.getAbsorptionAmount());

            if (player.isFallFlying() && !fakePlayer.isFallFlying())
                fakePlayer.startFallFlying();
            else if (!player.isFallFlying() && fakePlayer.isFallFlying())
                fakePlayer.stopFallFlying();
            if (player.isSleeping() && !fakePlayer.isSleeping() && player.getSleepingPos().isPresent())
                fakePlayer.setSleepingPos(player.getSleepingPos().get());
            else if (!player.isSleeping() && fakePlayer.isFallFlying())
                fakePlayer.stopSleeping();

            fakePlayer.setOnGround(player.onGround());
            fakePlayer.setDeltaMovement(player.getDeltaMovement());
            fakePlayer.setPos(player.getX(), player.getY(), player.getZ());
            fakePlayer.setYRot(player.getYRot());
            fakePlayer.setPose(player.getPose());
            fakePlayer.setXRot(player.getXRot());
            fakePlayer.setShiftKeyDown(player.isShiftKeyDown());
            fakePlayer.setSprinting(player.isSprinting());
            fakePlayer.setSwimming(player.isSwimming());

            fakePlayer.setInvulnerable(player.isInvulnerable());
            if (fakePlayer.isCurrentlyGlowing() != player.isCurrentlyGlowing())
                fakePlayer.setGlowingTag(player.isCurrentlyGlowing());
            if (fakePlayer.isOnFire() != player.isOnFire())
                fakePlayer.setSharedFlagOnFire(player.isOnFire());
            if (fakePlayer.getMainArm() != player.getMainArm())
                fakePlayer.setMainArm(player.getMainArm());
            if (fakePlayer.gameMode.getGameModeForPlayer() != player.gameMode.getGameModeForPlayer())
                fakePlayer.setGameMode(player.gameMode.getGameModeForPlayer());

            fakePlayer.setRemainingFireTicks(player.getRemainingFireTicks());
            fakePlayer.setExperienceLevels(player.experienceLevel);
            fakePlayer.fallDistance = player.fallDistance;
            fakePlayer.setAirSupply(player.getAirSupply());
        }
    }

    public static void unbindPlayer(ServerPlayer player, EntityPlayerMPFake fakePlayer) {
        PlayerTeam team = binds.get(player).getKey();
        team.getScoreboard().removePlayerTeam(team);
        System.out.println("Unbinding player: " + player.getName().getString() + " from fake player: " + fakePlayer.getName().getString());
        NBTDataManager.writeToEntity(fakePlayer, NBTDataManager.readFromEntity(player, new CompoundTag()));

        CompoundTag playerData = bindTempData.get(player);
        NBTDataManager.writeToEntity(player, playerData);

        //#if MC>=12105
        String gameModeName = playerData.getString("GameMode").get();
        HumanoidArm arm = HumanoidArm.valueOf(playerData.getString("MainArm").get());
        //#else
        //$$ String gameModeName = playerData.getString("GameMode");
        //$$ HumanoidArm arm = HumanoidArm.valueOf(playerData.getString("MainArm"));
        //#endif
        GameType gameMode = GameType.byName(gameModeName, GameType.SURVIVAL);
        player.setGameMode(gameMode);
        player.setMainArm(arm);
        binds.remove(player);
        bindTempData.remove(player);
        player.setInvisible(false);
        fakePlayer.noPhysics = false;
    }
}