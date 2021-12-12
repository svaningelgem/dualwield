package com.ranull.dualwield.nms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.MathHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftVector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class NMS_v1_18_R1 implements NMS {
    @Override
    public void offHandAnimation(Player player) {
        player.swingOffHand();
    }

    @Override
    public void blockBreakAnimation(Player player, org.bukkit.block.Block block, int entityID, int stage) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl serverGamePacketListener = serverPlayer.connection;
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        serverGamePacketListener.send(new PacketPlayOutBlockBreakAnimation(entityID, blockPosition, stage));
    }

    @Override
    public void blockCrackParticle(org.bukkit.block.Block block) {
        block.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0, 0.5),
                10, block.getBlockData());
    }

    @Override
    public float getToolStrength(org.bukkit.block.Block block, org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

            return craftItemStack.getDestroySpeed(nmsBlock.defaultBlockState());
        }

        return 1;
    }

    @Override
    public void setItemInMainHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        ((CraftPlayer) player).getHandle().setItemSlot(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public void setItemInOffHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        ((CraftPlayer) player).getHandle().setItemSlot(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public double getAttackDamage(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            Multimap<Attribute, AttributeModifier> attributeMultimap = craftItemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);

            AttributeModifier attributeModifier = Iterables
                    .getFirst(attributeMultimap.get(Attributes.ATTACK_DAMAGE), null);

            if (attributeModifier != null) {
                return attributeModifier.getAmount() + 1;
            }
        }

        return 1;
    }

    /*

    public double getAttackSpeed(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            Multimap<AttributeBase, AttributeModifier> attributeMultimap = craftItemStack.a(EnumItemSlot.a);

            AttributeModifier attributeModifier = Iterables
                    .getFirst(attributeMultimap.get(GenericAttributes.h), null);

            if (attributeModifier != null) {
                return Math.abs(attributeModifier.getAmount());
            }
        }

        return 4;
    }

    public void setAttackCooldown(Player player, int cooldown) {
        try {
            EntityLiving entityLivingPlayer = ((CraftPlayer) player).getHandle();
            Field keyField = EntityLiving.class.getDeclaredField("at");

            keyField.setAccessible(true);
            keyField.setInt(entityLivingPlayer, cooldown);
            keyField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    public int getAttackCooldown(Player player) {
        try {
            EntityLiving entityLivingPlayer = ((CraftPlayer) player).getHandle();
            Field keyField = EntityLiving.class.getDeclaredField("at");

            keyField.setAccessible(true);

            int cooldown = (int) keyField.get(entityLivingPlayer);

            keyField.setAccessible(false);

            return cooldown;
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }

        return 1;
    }
     */

    @Override
    public Sound getHitSound(org.bukkit.block.Block block) {
        try {
            ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundType soundType = nmsBlock.getSoundType(nmsBlock.defaultBlockState());

            return Sound.valueOf(soundType.getHitSound().getLocation().getPath().toUpperCase()
                    .replace(".", "_"));
        } catch (IllegalArgumentException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public float getBlockHardness(org.bukkit.block.Block block) {
        ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
        Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

        return nmsBlock.defaultBlockState().destroySpeed;
    }

    @Override
    public void damageItem(org.bukkit.inventory.ItemStack itemStack, Player player) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        if (itemStack.getItemMeta() instanceof Damageable
                && craftItemStack.getItem().getMaxDamage() > 0
                && calculateUnbreakingChance(itemStack)) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();

            damageable.setDamage(damageable.getDamage() + 1);
            itemStack.setItemMeta(damageable);

            if (damageable.getDamage() >= craftItemStack.getItem().getMaxDamage()) {
                ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

                CraftEventFactory.callPlayerItemBreakEvent(serverPlayer, craftItemStack);
                itemStack.setAmount(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
        }
    }

    public boolean calculateUnbreakingChance(org.bukkit.inventory.ItemStack itemStack) {
        int enchantmentLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
        Random random = new Random();

        if (enchantmentLevel == 1) {
            return random.nextFloat() <= 0.20f;
        } else if (enchantmentLevel == 2) {
            return random.nextFloat() <= 0.27f;
        } else if (enchantmentLevel == 3) {
            return random.nextFloat() <= 0.30f;
        }

        return true;
    }

    @Override
    public org.bukkit.inventory.ItemStack addNBTKey(org.bukkit.inventory.ItemStack itemStack, String key) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag compoundTag = craftItemStack.getOrCreateTag();

        compoundTag.put(key, (Tag) NBTTagByte.a((byte) 1));
        craftItemStack.setTag(compoundTag);

        return CraftItemStack.asBukkitCopy(craftItemStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack removeNBTKey(org.bukkit.inventory.ItemStack itemStack, String key) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag compoundTag = craftItemStack.getOrCreateTag();

        compoundTag.remove(key);
        craftItemStack.setTag(compoundTag);

        return CraftItemStack.asBukkitCopy(craftItemStack);
    }

    @Override
    public boolean hasNBTKey(org.bukkit.inventory.ItemStack itemStack, String key) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag compoundTag = craftItemStack.getOrCreateTag();

        return compoundTag.contains(key);
    }

    @Override
    public String getItemName(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        return craftItemStack.getDescriptionId().replace("item.minecraft.", "").toUpperCase();
    }

    @Override
    public boolean breakBlock(Player player, org.bukkit.block.Block block) {
        return player.breakBlock(block);
    }

    @Override
    public void attackEntityOffHand(Player player, org.bukkit.entity.Entity entity) {
        ServerPlayer serverPlayer = (((CraftPlayer) player).getHandle());
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        org.bukkit.inventory.ItemStack itemInMainHand = player.getInventory().getItemInOffHand();
        org.bukkit.inventory.ItemStack itemInOffHand = player.getInventory().getItemInMainHand();
        ItemStack craftItemInOffHand = CraftItemStack.asNMSCopy(itemInOffHand);

        if (nmsEntity.isAttackable() && !nmsEntity.skipAttackInteraction(serverPlayer)) {
            float f = (float) getAttackDamage(itemInOffHand) + ((float) serverPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE) - (float) getAttackDamage(itemInMainHand));
            float f1;
            if (nmsEntity instanceof LivingEntity) {
                f1 = EnchantmentHelper.getDamageBonus(craftItemInOffHand, ((LivingEntity) nmsEntity).getMobType());
            } else {
                f1 = EnchantmentHelper.getDamageBonus(craftItemInOffHand, MobType.UNDEFINED);
            }

            float f2 = serverPlayer.getAttackStrengthScale(0.5F);
            f *= 0.2F + f2 * f2 * 0.8F;
            f1 *= f2;
            if (f > 0.0F || f1 > 0.0F) {
                boolean flag = f2 > 0.9F;
                boolean flag1 = false;
                byte b0 = 0;
                int i = b0 + EnchantmentHelper.getKnockbackBonus(serverPlayer);
                if (serverPlayer.isSprinting() && flag) {
                    serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                    ++i;
                    flag1 = true;
                }


                boolean flag2 = flag && serverPlayer.fallDistance > 0.0F && !serverPlayer.isOnGround() && !serverPlayer.onClimbable() && !serverPlayer.isInWater() && !serverPlayer.hasEffect(MobEffects.BLINDNESS) && !serverPlayer.isPassenger() && nmsEntity instanceof EntityLiving;
                flag2 = flag2 && !serverPlayer.isSprinting();
                if (flag2) {
                    f *= 1.5F;
                }

                f += f1;
                boolean flag3 = false;
                double d0 = serverPlayer.walkDist - serverPlayer.walkDistO;
                if (flag && !flag2 && !flag1 && serverPlayer.isOnGround() && d0 < serverPlayer.getAttributeValue(Attributes.MOVEMENT_SPEED)) {
                    ItemStack itemstack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                    if (itemstack.getItem() instanceof ItemSword) {
                        flag3 = true;
                    }
                }

                float f3 = 0.0F;
                boolean flag4 = false;
                int j = EnchantmentHelper.getFireAspect(serverPlayer);
                if (nmsEntity instanceof EntityLiving) {
                    f3 = ((EntityLiving) nmsEntity).getHealth();
                    if (j > 0 && !nmsEntity.isOnFire()) {
                        EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(serverPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), 1);
                        Bukkit.getPluginManager().callEvent(combustEvent);
                        if (!combustEvent.isCancelled()) {
                            flag4 = true;
                            nmsEntity.setSecondsOnFire(combustEvent.getDuration(), false);
                        }
                    }
                }

                Vec3 vec3 = nmsEntity.getDeltaMovement();
                boolean flag5 = nmsEntity.hurt(DamageSource.playerAttack(serverPlayer), f);
                if (flag5) {
                    if (i > 0) {
                        if (nmsEntity instanceof EntityLiving) {
                            ((EntityLiving) nmsEntity).p((float) i * 0.5F, MathHelper.sin(serverPlayer.getYRot() * 0.017453292F), -MathHelper.cos(serverPlayer.getYRot() * 0.017453292F));
                        } else {
                            nmsEntity.push(-MathHelper.sin(serverPlayer.getYRot() * 0.017453292F) * (float) i * 0.5F, 0.1D, MathHelper.cos(serverPlayer.getYRot() * 0.017453292F) * (float) i * 0.5F);
                        }

                        serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                        serverPlayer.setSprinting(false);
                    }

                    if (flag3) {
                        float f4 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(serverPlayer) * f;
                        List<LivingEntity> list = serverPlayer.level.getEntitiesOfClass(LivingEntity.class, nmsEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D));
                        Iterator<LivingEntity> iterator = list.iterator();

                        label179:
                        while (true) {
                            LivingEntity entityliving;
                            do {
                                do {
                                    do {
                                        do {
                                            if (!iterator.hasNext()) {
                                                serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                                                serverPlayer.sweepAttack();
                                                break label179;
                                            }

                                            entityliving = iterator.next();
                                        } while (entityliving == serverPlayer);
                                    } while (entityliving == nmsEntity);
                                } while (serverPlayer.isAlliedTo(entityliving));
                            } while (entityliving instanceof ArmorStand && ((ArmorStand) entityliving).isMarker());

                            if (serverPlayer.distanceToSqr(entityliving) < 9.0D && entityliving.hurt(DamageSource.playerAttack(serverPlayer).sweep(), f4)) {
                                entityliving.knockback(0.4000000059604645D, MathHelper.sin(serverPlayer.getYRot() * 0.017453292F), -MathHelper.cos(serverPlayer.getYRot() * 0.017453292F));
                            }
                        }
                    }

                    if (nmsEntity instanceof ServerPlayer && nmsEntity.hurtMarked) {
                        boolean cancelled = false;
                        Player player2 = (Player) nmsEntity.getBukkitEntity();
                        Vector velocity = CraftVector.toBukkit(vec3);
                        PlayerVelocityEvent event = new PlayerVelocityEvent(player2, velocity.clone());
                        serverPlayer.level.getCraftServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            cancelled = true;
                        } else if (!velocity.equals(event.getVelocity())) {
                            player2.setVelocity(event.getVelocity());
                        }

                        if (!cancelled) {
                            ((ServerPlayer) nmsEntity).connection.send(new PacketPlayOutEntityVelocity(nmsEntity));
                            nmsEntity.hurtMarked = false;
                            nmsEntity.setDeltaMovement(vec3);
                        }
                    }

                    if (flag2) {
                        serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                        serverPlayer.crit(nmsEntity);
                    }

                    if (!flag2 && !flag3) {
                        if (flag) {
                            serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                        } else {
                            serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                        }
                    }

                    if (f1 > 0.0F) {
                        serverPlayer.magicCrit(nmsEntity);
                    }

                    serverPlayer.setLastHurtMob(nmsEntity);
                    if (nmsEntity instanceof LivingEntity) {
                        EnchantmentHelper.doPostHurtEffects((LivingEntity) nmsEntity, serverPlayer);
                    }

                    EnchantmentHelper.doPostDamageEffects(serverPlayer, nmsEntity);
                    Object object = nmsEntity;

                    if (nmsEntity instanceof EnderDragonPart) {
                        object = ((EnderDragonPart) nmsEntity).parentMob;
                    }

                    if (!serverPlayer.level.isClientSide && !craftItemInOffHand.isEmpty() && object instanceof LivingEntity) {
                        craftItemInOffHand.hurtEnemy((LivingEntity) object, serverPlayer);
                        if (craftItemInOffHand.isEmpty()) {
                            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }

                    if (nmsEntity instanceof EntityLiving) {
                        float f5 = f3 - ((EntityLiving) nmsEntity).getHealth();
                        serverPlayer.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                        if (j > 0) {
                            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(serverPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), j * 4);
                            Bukkit.getPluginManager().callEvent(combustEvent);

                            if (!combustEvent.isCancelled()) {
                                nmsEntity.setSecondsOnFire(combustEvent.getDuration(), false);
                            }
                        }

                        if (serverPlayer.level instanceof ServerLevel && f5 > 2.0F) {
                            int k = (int) ((double) f5 * 0.5D);
                            ((ServerLevel) serverPlayer.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, nmsEntity.getX(), nmsEntity.getY(0.5D), nmsEntity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                        }
                    }

                    try {
                        Class.forName("org.spigotmc.SpigotConfig");
                        serverPlayer.causeFoodExhaustion(serverPlayer.level.spigotConfig.combatExhaustion,
                                EntityExhaustionEvent.ExhaustionReason.ATTACK);
                    } catch (ClassNotFoundException ignored) {
                        serverPlayer.causeFoodExhaustion(0.1F, EntityExhaustionEvent.ExhaustionReason.ATTACK);
                    }
                } else {
                    serverPlayer.level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, serverPlayer.getSoundSource(), 1.0F, 1.0F);
                    if (flag4) {
                        nmsEntity.clearFire();
                    }

                    (serverPlayer).getBukkitEntity().updateInventory();
                }
            }
        }
    }
}
