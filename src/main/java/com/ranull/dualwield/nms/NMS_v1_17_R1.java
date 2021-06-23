package com.ranull.dualwield.nms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayInArmAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMonsterType;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.boss.EntityComplexPart;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftVector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class NMS_v1_17_R1 implements NMS {
    @Override
    public void offHandAnimation(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.b;
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 3);

        playerConnection.sendPacket(packetPlayOutAnimation);
        playerConnection.a(new PacketPlayInArmAnimation(EnumHand.b));
    }

    @Override
    public void blockBreakAnimation(Player player, org.bukkit.block.Block block, int entityID, int stage) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.b;
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        PacketPlayOutBlockBreakAnimation packetPlayOutBlockBreakAnimation = new PacketPlayOutBlockBreakAnimation(entityID, blockPosition, stage);

        playerConnection.sendPacket(packetPlayOutBlockBreakAnimation);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void blockCrackParticle(org.bukkit.block.Block block) {
        MaterialData materialData = new MaterialData(block.getType(), block.getData());

        block.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0, 0.5), 10, materialData);
    }

    @Override
    public float getToolStrength(org.bukkit.block.Block block, org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();

            return craftItemStack.a(nmsBlock.getBlockData());
        }

        return 1;
    }

    @Override
    public void setItemInMainHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        entityPlayer.setSlot(EnumItemSlot.a, craftItemStack);
    }

    @Override
    public void setItemInOffHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        entityPlayer.setSlot(EnumItemSlot.b, craftItemStack);
    }

    @Override
    public double getAttackDamage(org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            Multimap<AttributeBase, AttributeModifier> attributeMultimap = craftItemStack.a(EnumItemSlot.a);

            AttributeModifier attributeModifier = Iterables
                    .getFirst(attributeMultimap.get(GenericAttributes.f), null);

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
    public Sound getBreakSound(org.bukkit.block.Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

            SoundEffect soundEffect = soundEffectType.getFallSound();
            Field keyField = SoundEffect.class.getDeclaredField("b");
            keyField.setAccessible(true);

            MinecraftKey minecraftKey = (MinecraftKey) keyField.get(soundEffect);

            String soundString = minecraftKey.getKey().toUpperCase()
                    .replace(".", "_")
                    .replace("_FALL", "_HIT");
            Sound sound = Sound.valueOf(soundString);

            if (sound != null) {
                return sound;
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public float getBlockHardness(org.bukkit.block.Block block) {
        World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();

        return nmsBlock.getBlockData().k;
    }

    @Override
    public void damageItem(org.bukkit.inventory.ItemStack itemStack, Player player) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        if (itemStack.getItemMeta() instanceof Damageable
                && craftItemStack.getItem().getMaxDurability() > 0
                && calculateUnbreakingChance(itemStack)) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();
            damageable.setDamage(damageable.getDamage() + 1);

            itemStack.setItemMeta((ItemMeta) damageable);

            if (damageable.getDamage() >= craftItemStack.getItem().getMaxDurability()) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, craftItemStack);

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
        NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

        nbtTagCompound.set(key, NBTTagByte.a((byte) 1));
        craftItemStack.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(craftItemStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack removeNBTKey(org.bukkit.inventory.ItemStack itemStack, String key) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

        nbtTagCompound.remove(key);
        craftItemStack.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(craftItemStack);
    }

    @Override
    public boolean hasNBTKey(org.bukkit.inventory.ItemStack itemStack, String key) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

        return nbtTagCompound.hasKey(key);
    }

    @Override
    public String getItemName(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

        return craftItemStack.n().replace("item.minecraft.", "").toUpperCase();
    }

    @Override
    public void attackEntityOffHand(Player player, org.bukkit.entity.Entity entity) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        org.bukkit.inventory.ItemStack itemInMainHand = player.getInventory().getItemInOffHand();
        org.bukkit.inventory.ItemStack itemInOffHand = player.getInventory().getItemInMainHand();
        ItemStack craftItemInOffHand = CraftItemStack.asNMSCopy(itemInOffHand);

        if (nmsEntity.ca() && !nmsEntity.r(entityPlayer)) {
            float f = (float) getAttackDamage(itemInOffHand) + ((float) entityPlayer.b(GenericAttributes.f) - (float) getAttackDamage(itemInMainHand));
            float f1;
            if (nmsEntity instanceof EntityLiving) {
                f1 = EnchantmentManager.a(craftItemInOffHand, ((EntityLiving) nmsEntity).getMonsterType());
            } else {
                f1 = EnchantmentManager.a(craftItemInOffHand, EnumMonsterType.a);
            }

            float f2 = entityPlayer.getAttackCooldown(0.5F);
            f *= 0.2F + f2 * f2 * 0.8F;
            f1 *= f2;
            if (f > 0.0F || f1 > 0.0F) {
                boolean flag = f2 > 0.9F;
                boolean flag1 = false;
                byte b0 = 0;
                int i = b0 + EnchantmentManager.b(entityPlayer);
                if (entityPlayer.isSprinting() && flag) {
                    entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.ok, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                    ++i;
                    flag1 = true;
                }


                boolean flag2 = flag && entityPlayer.K > 0.0F && !entityPlayer.isOnGround() && !entityPlayer.isClimbing() && !entityPlayer.isInWater() && !entityPlayer.hasEffect(MobEffects.o) && !entityPlayer.isPassenger() && nmsEntity instanceof EntityLiving;
                flag2 = flag2 && !entityPlayer.isSprinting();
                if (flag2) {
                    f *= 1.5F;
                }

                f += f1;
                boolean flag3 = false;
                double d0 = entityPlayer.H - entityPlayer.G;
                if (flag && !flag2 && !flag1 && entityPlayer.isOnGround() && d0 < (double) entityPlayer.ev()) {
                    ItemStack itemstack = entityPlayer.b(EnumHand.a);
                    if (itemstack.getItem() instanceof ItemSword) {
                        flag3 = true;
                    }
                }

                float f3 = 0.0F;
                boolean flag4 = false;
                int j = EnchantmentManager.getFireAspectEnchantmentLevel(entityPlayer);
                if (nmsEntity instanceof EntityLiving) {
                    f3 = ((EntityLiving) nmsEntity).getHealth();
                    if (j > 0 && !nmsEntity.isBurning()) {
                        EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(entityPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), 1);
                        Bukkit.getPluginManager().callEvent(combustEvent);
                        if (!combustEvent.isCancelled()) {
                            flag4 = true;
                            nmsEntity.setOnFire(combustEvent.getDuration(), false);
                        }
                    }
                }

                Vec3D vec3d = nmsEntity.getMot();
                boolean flag5 = nmsEntity.damageEntity(DamageSource.playerAttack(entityPlayer), f);
                if (flag5) {
                    if (i > 0) {
                        if (nmsEntity instanceof EntityLiving) {
                            ((EntityLiving) nmsEntity).p((float) i * 0.5F, MathHelper.sin(entityPlayer.getYRot() * 0.017453292F), -MathHelper.cos(entityPlayer.getYRot() * 0.017453292F));
                        } else {
                            nmsEntity.i(-MathHelper.sin(entityPlayer.getYRot() * 0.017453292F) * (float) i * 0.5F, 0.1D, MathHelper.cos(entityPlayer.getYRot() * 0.017453292F) * (float) i * 0.5F);
                        }

                        entityPlayer.setMot(entityPlayer.getMot().d(0.6D, 1.0D, 0.6D));
                        entityPlayer.setSprinting(false);
                    }

                    if (flag3) {
                        float f4 = 1.0F + EnchantmentManager.a(entityPlayer) * f;
                        List<EntityLiving> list = entityPlayer.t.a(EntityLiving.class, nmsEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
                        Iterator<EntityLiving> iterator = list.iterator();

                        label179:
                        while (true) {
                            EntityLiving entityliving;
                            do {
                                do {
                                    do {
                                        do {
                                            if (!iterator.hasNext()) {
                                                entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.on, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                                                entityPlayer.ff();
                                                break label179;
                                            }

                                            entityliving = (EntityLiving) iterator.next();
                                        } while (entityliving == entityPlayer);
                                    } while (entityliving == nmsEntity);
                                } while (entityPlayer.p(entityliving));
                            } while (entityliving instanceof EntityArmorStand && ((EntityArmorStand) entityliving).isMarker());

                            if (entityPlayer.f(entityliving) < 9.0D && entityliving.damageEntity(DamageSource.playerAttack(entityPlayer).sweep(), f4)) {
                                entityliving.p(0.4000000059604645D, MathHelper.sin(entityPlayer.getYRot() * 0.017453292F), -MathHelper.cos(entityPlayer.getYRot() * 0.017453292F));
                            }
                        }
                    }

                    if (nmsEntity instanceof EntityPlayer && nmsEntity.C) {
                        boolean cancelled = false;
                        Player player2 = (Player) nmsEntity.getBukkitEntity();
                        Vector velocity = CraftVector.toBukkit(vec3d);
                        PlayerVelocityEvent event = new PlayerVelocityEvent(player2, velocity.clone());
                        entityPlayer.t.getCraftServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            cancelled = true;
                        } else if (!velocity.equals(event.getVelocity())) {
                            player2.setVelocity(event.getVelocity());
                        }

                        if (!cancelled) {
                            ((EntityPlayer) nmsEntity).b.sendPacket(new PacketPlayOutEntityVelocity(nmsEntity));
                            nmsEntity.C = false;
                            nmsEntity.setMot(vec3d);
                        }
                    }

                    if (flag2) {
                        entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.oj, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                        entityPlayer.a(nmsEntity);
                    }

                    if (!flag2 && !flag3) {
                        if (flag) {
                            entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.om, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                        } else {
                            entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.oo, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                        }
                    }

                    if (f1 > 0.0F) {
                        entityPlayer.b(nmsEntity);
                    }

                    entityPlayer.x(nmsEntity);
                    if (nmsEntity instanceof EntityLiving) {
                        EnchantmentManager.a((EntityLiving) nmsEntity, entityPlayer);
                    }

                    EnchantmentManager.b(entityPlayer, nmsEntity);
                    ItemStack itemstack1 = craftItemInOffHand;
                    Object object = nmsEntity;
                    if (nmsEntity instanceof EntityComplexPart) {
                        object = ((EntityComplexPart) nmsEntity).b;
                    }

                    if (!entityPlayer.t.y && !itemstack1.isEmpty() && object instanceof EntityLiving) {
                        itemstack1.a((EntityLiving) object, entityPlayer);
                        if (itemstack1.isEmpty()) {
                            entityPlayer.a(EnumHand.a, ItemStack.b);
                        }
                    }

                    if (nmsEntity instanceof EntityLiving) {
                        float f5 = f3 - ((EntityLiving) nmsEntity).getHealth();
                        entityPlayer.a(StatisticList.G, Math.round(f5 * 10.0F));

                        if (j > 0) {
                            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(entityPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), j * 4);
                            Bukkit.getPluginManager().callEvent(combustEvent);

                            if (!combustEvent.isCancelled()) {
                                nmsEntity.setOnFire(combustEvent.getDuration(), false);
                            }
                        }

                        if (entityPlayer.t instanceof WorldServer && f5 > 2.0F) {
                            int k = (int) ((double) f5 * 0.5D);
                            ((WorldServer) entityPlayer.t).a(Particles.i, nmsEntity.locX(), nmsEntity.e(0.5D), nmsEntity.locZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                        }
                    }

                    entityPlayer.applyExhaustion(entityPlayer.t.spigotConfig.combatExhaustion, EntityExhaustionEvent.ExhaustionReason.ATTACK);
                } else {
                    entityPlayer.t.playSound(null, entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ(), SoundEffects.ol, entityPlayer.getSoundCategory(), 1.0F, 1.0F);
                    if (flag4) {
                        nmsEntity.extinguish();
                    }

                    if (entityPlayer instanceof EntityPlayer) {
                        (entityPlayer).getBukkitEntity().updateInventory();
                    }
                }
            }
        }
    }
}
