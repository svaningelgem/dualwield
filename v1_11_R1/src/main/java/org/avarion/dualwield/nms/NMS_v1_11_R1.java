package org.avarion.dualwield.nms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_11_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.UUID;

public final class NMS_v1_11_R1 implements NMS {
    @Override
    public void handAnimation(Player player, EquipmentSlot equipmentSlot) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer,
                equipmentSlot == EquipmentSlot.HAND ? 0 : 3);

        playerConnection.sendPacket(packetPlayOutAnimation);
        playerConnection.a(new PacketPlayInArmAnimation(equipmentSlot == EquipmentSlot.HAND ? EnumHand.MAIN_HAND
                : EnumHand.OFF_HAND));
    }

    @Override
    public void blockBreakAnimation(Player player, org.bukkit.block.Block block, int animationID, int stage) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(animationID, blockPosition, stage));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void blockCrackParticle(org.bukkit.block.Block block) {
        block.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0, 0.5),
                10, new MaterialData(block.getType(), block.getData()));
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
    public double getAttackDamage(org.bukkit.inventory.ItemStack itemStack) {
        return getItemStackAttribute(itemStack, "generic.attackDamage");
    }

    @Override
    public double getAttackSpeed(org.bukkit.inventory.ItemStack itemStack) {
        return getItemStackAttribute(itemStack, "generic.attackSpeed");
    }

    private double getItemStackAttribute(org.bukkit.inventory.ItemStack itemStack, String attribute) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            Multimap<String, AttributeModifier> attributeMultimap = craftItemStack.a(EnumItemSlot.MAINHAND);
            AttributeModifier attributeModifier = Iterables.getFirst(attributeMultimap.get(attribute), null);

            return attributeModifier != null ? attributeModifier.d() : 0;
        }

        return 0;
    }

    @Override
    public Sound getHitSound(org.bukkit.block.Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound();

            SoundEffect soundEffect = soundEffectType.g();
            Field keyField = SoundEffect.class.getDeclaredField("b");
            keyField.setAccessible(true);

            MinecraftKey minecraftKey = (MinecraftKey) keyField.get(soundEffect);

            return Sound.valueOf(minecraftKey.a().toUpperCase()
                    .replace(".", "_")
                    .replace("_FALL", "_HIT"));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(org.bukkit.block.Block block) {
        World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        Block nmsBlock = CraftMagicNumbers.getBlock(block);

        return nmsBlock.a(nmsBlock.getBlockData(), nmsWorld, new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public boolean breakBlock(Player player, org.bukkit.block.Block block) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        return entityPlayer.playerInteractManager.breakBlock(blockPosition);
    }

    @Override
    public void setModifier(Player player, double damage, double speed, UUID uuid) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance damageAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
        AttributeInstance speedAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.f);

        if (damageAttributeInstance != null) {
            damageAttributeInstance.b(new AttributeModifier(uuid, "Weapon modifier", damage, 0));
        }

        if (speedAttributeInstance != null) {
            speedAttributeInstance.b(new AttributeModifier(uuid, "Weapon modifier", speed, 0));
        }
    }

    @Override
    public void removeModifier(Player player, UUID uuid) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance damageAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
        AttributeInstance speedAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.f);

        if (damageAttributeInstance != null) {
            damageAttributeInstance.b(uuid);
        }

        if (speedAttributeInstance != null) {
            speedAttributeInstance.b(uuid);
        }
    }

    @Override
    public void attack(Player player, org.bukkit.entity.Entity entity) {
        ((CraftPlayer) player).getHandle().attack(((CraftEntity) entity).getHandle());
    }
}
