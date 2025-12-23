package ru.mytheria.main.module.combat.aura.util;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.api.client.friend.FriendService;
import ru.mytheria.api.clientannotation.QuickImport;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Getter
public class TargetEntitySelector implements QuickImport {
    private LivingEntity currentTarget;
    private Stream<LivingEntity> potentialTargets;

    public enum TargetType {
        UNARMORED_PLAYERS("Голые игроки"),
        ARMORED_PLAYERS("В броне"),
        INVISIBLE_PLAYERS("Невидимые игроки"),
        HOSTILE_MOBS("Мобы"),
        ANIMALS("Животные"),
        FRIENDS("Друзья"),
        ARMOR_STANDS("Стойка для брони"),
        VILLAGERS("Жители"),
        GOLEM("Голем");

        @Getter
        private final String displayName;

        TargetType(String displayName) {
            this.displayName = displayName;
        }
    }

    public TargetEntitySelector() {
        this.currentTarget = null;
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance) {
        if (isTargetOutOfRange(maxDistance)) {
            releaseTarget();
        }

        potentialTargets = StreamSupport.stream(entities.spliterator(), false)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> getDistanceTo(entity) <= maxDistance)
                .sorted(Comparator.comparingDouble(this::getDistanceTo));
    }

    public Optional<LivingEntity> findTarget(Collection<TargetType> targetTypes) {
        return potentialTargets
                .filter(entity -> isValidTarget(entity, targetTypes))
                .findFirst();
    }

    public void lockTarget(LivingEntity target) {
        if (this.currentTarget == null) {
            this.currentTarget = target;
        }
    }

    public void releaseTarget() {
        this.currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        if (this.currentTarget != null && !predicate.test(this.currentTarget)) {
            releaseTarget();
        }
    }

    public void validateCurrentTarget(Collection<TargetType> targetTypes) {
        validateTarget(entity -> isValidTarget(entity, targetTypes));
    }

    public LivingEntity updateTarget(Collection<TargetType> targetTypes, Iterable<Entity> entities, float maxDistance) {
        searchTargets(entities, maxDistance);
        validateCurrentTarget(targetTypes);

        if (currentTarget == null) {
            findTarget(targetTypes).ifPresent(this::lockTarget);
        }

        return currentTarget;
    }

    public boolean isValidTarget(LivingEntity entity, Collection<TargetType> targetTypes) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.getHealth() <= 0) return false;

        if (entity instanceof PlayerEntity player) {
            if (FriendService.isFriend(player.getName().getString())) {
                return targetTypes.contains(TargetType.FRIENDS);
            }
        }

        if (entity instanceof PlayerEntity player) {
            boolean hasArmor = hasAnyArmor(player);

            if (hasArmor && targetTypes.contains(TargetType.ARMORED_PLAYERS)) {
                return true;
            }

            if (!hasArmor && targetTypes.contains(TargetType.UNARMORED_PLAYERS)) {
                return true;
            }

            if (player.isInvisible() && !hasArmor && targetTypes.contains(TargetType.INVISIBLE_PLAYERS)) {
                return true;
            }
        }

        if (entity instanceof HostileEntity && targetTypes.contains(TargetType.HOSTILE_MOBS)) {
            return true;
        }
        if (entity instanceof AnimalEntity && targetTypes.contains(TargetType.ANIMALS)) {
            return true;
        }
        if (entity instanceof ArmorStandEntity && targetTypes.contains(TargetType.ARMOR_STANDS)) {
            return true;
        }
        if (entity instanceof VillagerEntity && targetTypes.contains(TargetType.VILLAGERS)) {
            return true;
        }
        if (entity instanceof GolemEntity && targetTypes.contains(TargetType.GOLEM)) {
            return true;
        }
        return false;
    }

    private boolean hasAnyArmor(PlayerEntity player) {
        for (var item : player.getArmorItems()) {
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetOutOfRange(float maxDistance) {
        return currentTarget != null && getDistanceTo(currentTarget) > maxDistance;
    }

    public double getDistanceTo(LivingEntity entity) {
        return mc.player.getEyePos().distanceTo(entity.getEyePos());
    }

    public double getDistanceToCurrentTarget() {
        return currentTarget != null ? getDistanceTo(currentTarget) : -1;
    }

    public Vec3d getInterpolatedPos(LivingEntity entity, float tickDelta) {
        return new Vec3d(
                entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
                entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
                entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }
}