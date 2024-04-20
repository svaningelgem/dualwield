package org.avarion.dualwield.nms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Field;
import java.util.UUID;

public final class NMS_v1_15_R1 implements NMS {
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

    @Override
    public void blockCrackParticle(org.bukkit.block.Block block) {
        block.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0, 0.5),
                10, block.getBlockData());
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

            return attributeModifier != null ? attributeModifier.getAmount() : 0;
        }

        return 0;
    }

    @Override
    public Sound getHitSound(org.bukkit.block.Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

            SoundEffect soundEffect = soundEffectType.g();
            Field keyField = SoundEffect.class.getDeclaredField("a");
            keyField.setAccessible(true);

            MinecraftKey minecraftKey = (MinecraftKey) keyField.get(soundEffect);

            return Sound.valueOf(minecraftKey.getKey().toUpperCase()
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
        Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();

        return nmsBlock.g(null, null, null);
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
        AttributeInstance speedAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_SPEED);

        if (damageAttributeInstance != null) {
            damageAttributeInstance.addModifier(new AttributeModifier(uuid, "Weapon modifier", damage,
                    AttributeModifier.Operation.ADDITION));
        }

        if (speedAttributeInstance != null) {
            speedAttributeInstance.addModifier(new AttributeModifier(uuid, "Weapon modifier", speed,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    public void removeModifier(Player player, UUID uuid) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance damageAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
        AttributeInstance speedAttributeInstance = entityPlayer.getAttributeInstance(GenericAttributes.ATTACK_SPEED);

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
