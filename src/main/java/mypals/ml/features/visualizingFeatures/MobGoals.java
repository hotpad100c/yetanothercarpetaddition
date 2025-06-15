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

package mypals.ml.features.visualizingFeatures;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;

import java.util.Arrays;

public class MobGoals {
    public static String getGoalNameByMap(Class<?> goalClass) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        String actualClassName = goalClass.getSimpleName();
        String yarnClassName = resolver.mapClassName("intermediary", "net.minecraft." + actualClassName);

        String[] parts = yarnClassName.replace("$", ".").split("\\.");
        String simpleClassName = parts[parts.length - 1];

        return simpleClassName;
    }

    public static String getGoalName(Class<?> goalClass) {
        if (goalClass == null) {
            return "UnknownGoal";
        }

        if (goalClass.equals(AnimalMateGoal.class)) return "AnimalMateGoal";
        if (goalClass.equals(AttackGoal.class)) return "AttackGoal";
        if (goalClass.equals(AttackWithOwnerGoal.class)) return "AttackWithOwnerGoal";
        if (goalClass.equals(AvoidSunlightGoal.class)) return "AvoidSunlightGoal";
        if (goalClass.equals(BowAttackGoal.class)) return "BowAttackGoal";
        if (goalClass.equals(BreakDoorGoal.class)) return "BreakDoorGoal";
        if (goalClass.equals(BreatheAirGoal.class)) return "BreatheAirGoal";
        if (goalClass.equals(CatSitOnBlockGoal.class)) return "CatSitOnBlockGoal";
        if (goalClass.equals(ChaseBoatGoal.class)) return "ChaseBoatGoal";
        if (goalClass.equals(CreeperIgniteGoal.class)) return "CreeperIgniteGoal";
        if (goalClass.equals(CrossbowAttackGoal.class)) return "CrossbowAttackGoal";
        if (goalClass.equals(DisableableFollowTargetGoal.class)) return "DisableableFollowTargetGoal";
        if (goalClass.equals(DiveJumpingGoal.class)) return "DiveJumpingGoal";
        if (goalClass.equals(DolphinJumpGoal.class)) return "DolphinJumpGoal";
        if (goalClass.equals(DoorInteractGoal.class)) return "DoorInteractGoal";
        if (goalClass.equals(EatGrassGoal.class)) return "EatGrassGoal";
        if (goalClass.equals(EscapeDangerGoal.class)) return "EscapeDangerGoal";
        if (goalClass.equals(EscapeSunlightGoal.class)) return "EscapeSunlightGoal";
        if (goalClass.equals(FleeEntityGoal.class)) return "FleeEntityGoal";
        if (goalClass.equals(FlyGoal.class)) return "FlyGoal";
        if (goalClass.equals(FollowGroupLeaderGoal.class)) return "FollowGroupLeaderGoal";
        if (goalClass.equals(FollowMobGoal.class)) return "FollowMobGoal";
        if (goalClass.equals(FollowOwnerGoal.class)) return "FollowOwnerGoal";
        if (goalClass.equals(FollowParentGoal.class)) return "FollowParentGoal";
        if (goalClass.equals(FormCaravanGoal.class)) return "FormCaravanGoal";
        if (goalClass.equals(GoToBedAndSleepGoal.class)) return "GoToBedAndSleepGoal";
        if (goalClass.equals(GoToVillageGoal.class)) return "GoToVillageGoal";
        if (goalClass.equals(GoToWalkTargetGoal.class)) return "GoToWalkTargetGoal";
        if (goalClass.equals(HoldInHandsGoal.class)) return "HoldInHandsGoal";
        if (goalClass.equals(HorseBondWithPlayerGoal.class)) return "HorseBondWithPlayerGoal";
        if (goalClass.equals(IronGolemLookGoal.class)) return "IronGolemLookGoal";
        if (goalClass.equals(IronGolemWanderAroundGoal.class)) return "IronGolemWanderAroundGoal";
        if (goalClass.equals(LongDoorInteractGoal.class)) return "LongDoorInteractGoal";
        if (goalClass.equals(LookAroundGoal.class)) return "LookAroundGoal";
        if (goalClass.equals(LookAtCustomerGoal.class)) return "LookAtCustomerGoal";
        if (goalClass.equals(LookAtEntityGoal.class)) return "LookAtEntityGoal";
        if (goalClass.equals(MeleeAttackGoal.class)) return "MeleeAttackGoal";
        if (goalClass.equals(MoveIntoWaterGoal.class)) return "MoveIntoWaterGoal";
        if (goalClass.equals(MoveThroughVillageGoal.class)) return "MoveThroughVillageGoal";
        if (goalClass.equals(MoveToRaidCenterGoal.class)) return "MoveToRaidCenterGoal";
        if (goalClass.equals(MoveToTargetPosGoal.class)) return "MoveToTargetPosGoal";
        if (goalClass.equals(PounceAtTargetGoal.class)) return "PounceAtTargetGoal";
        if (goalClass.equals(ProjectileAttackGoal.class)) return "ProjectileAttackGoal";
        if (goalClass.equals(RaidGoal.class)) return "RaidGoal";
        if (goalClass.equals(RevengeGoal.class)) return "RevengeGoal";
        if (goalClass.equals(SitGoal.class)) return "SitGoal";
        if (goalClass.equals(SitOnOwnerShoulderGoal.class)) return "SitOnOwnerShoulderGoal";
        if (goalClass.equals(SkeletonHorseTrapTriggerGoal.class)) return "SkeletonHorseTrapTriggerGoal";
        if (goalClass.equals(StepAndDestroyBlockGoal.class)) return "StepAndDestroyBlockGoal";
        if (goalClass.equals(StopAndLookAtEntityGoal.class)) return "StopAndLookAtEntityGoal";
        if (goalClass.equals(StopFollowingCustomerGoal.class)) return "StopFollowingCustomerGoal";
        if (goalClass.equals(SwimAroundGoal.class)) return "SwimAroundGoal";
        if (goalClass.equals(SwimGoal.class)) return "SwimGoal";
        if (goalClass.equals(TemptGoal.class)) return "TemptGoal";
        if (goalClass.equals(TrackIronGolemTargetGoal.class)) return "TrackIronGolemTargetGoal";
        if (goalClass.equals(TrackOwnerAttackerGoal.class)) return "TrackOwnerAttackerGoal";
        if (goalClass.equals(TrackTargetGoal.class)) return "TrackTargetGoal";
        if (goalClass.equals(UniversalAngerGoal.class)) return "UniversalAngerGoal";
        if (goalClass.equals(UntamedActiveTargetGoal.class)) return "UntamedActiveTargetGoal";
        if (goalClass.equals(WanderAroundFarGoal.class)) return "WanderAroundFarGoal";
        if (goalClass.equals(WanderAroundGoal.class)) return "WanderAroundGoal";
        if (goalClass.equals(WanderAroundPointOfInterestGoal.class)) return "WanderAroundPointOfInterestGoal";
        if (goalClass.equals(WanderNearTargetGoal.class)) return "WanderNearTargetGoal";
        if (goalClass.equals(WolfBegGoal.class)) return "WolfBegGoal";
        if (goalClass.equals(ZombieAttackGoal.class)) return "ZombieAttackGoal";


        return getGoalNameByMap(goalClass);
    }
}
