package com.ranull.dualwield.nms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class NMS_v1_9_R2 implements NMS {
	@Override
	public void offHandAnimation(Player player) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		PlayerConnection playerConnection = entityPlayer.playerConnection;
		PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(entityPlayer, 3);

		playerConnection.sendPacket(packetPlayOutAnimation);
		playerConnection.a(new PacketPlayInArmAnimation(EnumHand.OFF_HAND));
	}

	@Override
	public void blockBreakAnimation(Player player, org.bukkit.block.Block block, int entityID, int stage) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		PlayerConnection playerConnection = entityPlayer.playerConnection;
		BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketPlayOutBlockBreakAnimation packetPlayOutBlockBreakAnimation = new PacketPlayOutBlockBreakAnimation(entityID, blockPosition, stage);

		playerConnection.sendPacket(packetPlayOutBlockBreakAnimation);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void blockCrackParticle(org.bukkit.block.Block block) {
		MaterialData materialData = new MaterialData(block.getType(), block.getData());

		block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0, 0.5), 10, materialData);
	}

	@Override
	public float getToolStrength(org.bukkit.block.Block block, org.bukkit.inventory.ItemStack itemStack) {
		if (itemStack.getType() != org.bukkit.Material.AIR) {
			ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
			Block nmsBlock = CraftMagicNumbers.getBlock(block);

			return craftItemStack.a(nmsBlock.getBlockData());
		}

		return 1;
	}

	@Override
	public void setItemInMainHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);

		entityPlayer.setSlot(EnumItemSlot.MAINHAND, craftItemStack);
	}

	@Override
	public double getAttackDamage(org.bukkit.inventory.ItemStack itemStack) {
		if (itemStack.getType() != org.bukkit.Material.AIR) {
			ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
			Multimap<String, AttributeModifier> attributeMultimap = craftItemStack.a(EnumItemSlot.MAINHAND);

			AttributeModifier attributeModifier = Iterables
					.getFirst(attributeMultimap.get("generic.attackDamage"), null);

			if (attributeModifier != null) {
				return attributeModifier.d() + 1;
			}
		}

		return 1;
	}

	@Override
	public Sound getBreakSound(org.bukkit.block.Block block) {
		try {
			World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
			Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
			SoundEffectType soundEffectType = nmsBlock.w();

			SoundEffect soundEffect = soundEffectType.g();
			Field keyField = SoundEffect.class.getDeclaredField("b");
			keyField.setAccessible(true);

			MinecraftKey minecraftKey = (MinecraftKey) keyField.get(soundEffect);

			String soundString = minecraftKey.a().toUpperCase()
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

	@SuppressWarnings("deprecation")
	@Override
	public float getBlockHardness(org.bukkit.block.Block block) {
		Block nmsBlock = CraftMagicNumbers.getBlock(block);
		World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

		return nmsBlock.b(nmsBlock.getBlockData(), nmsWorld, blockPosition);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void damageItem(org.bukkit.inventory.ItemStack itemStack, Player player) {
		if (itemStack.getType().getMaxDurability() > 0 && calculateUnbreakingChance(itemStack)) {
			if (itemStack.getDurability() >= itemStack.getType().getMaxDurability()) {
				ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
				EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

				CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, craftItemStack);

				itemStack.setAmount(0);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			}

			itemStack.setDurability((short) (itemStack.getDurability() + 1));
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
	public org.bukkit.inventory.ItemStack setAPIData(org.bukkit.inventory.ItemStack itemStack) {
		ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

		nbtTagCompound.set("dualWieldItem", new NBTTagInt(1));
		craftItemStack.setTag(nbtTagCompound);

		return CraftItemStack.asBukkitCopy(craftItemStack);
	}

	@Override
	public org.bukkit.inventory.ItemStack removeAPIData(org.bukkit.inventory.ItemStack itemStack) {
		ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

		nbtTagCompound.remove("dualWieldItem");
		craftItemStack.setTag(nbtTagCompound);

		return CraftItemStack.asBukkitCopy(craftItemStack);
	}

	@Override
	public boolean hasAPIData(org.bukkit.inventory.ItemStack itemStack) {
		ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound nbtTagCompound = (craftItemStack.hasTag()) ? craftItemStack.getTag() : new NBTTagCompound();

		return nbtTagCompound.hasKey("dualWieldItem");
	}

	@Override
	public void attackEntityOffHand(Player player, org.bukkit.entity.Entity entity) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		Entity nmsEntity = ((CraftEntity) entity).getHandle();

		org.bukkit.inventory.ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
		ItemStack craftItemInOffHand = CraftItemStack.asNMSCopy(itemInOffHand);

		float damage = (float) getAttackDamage(itemInOffHand);
		float enchantmentLevel;
		if (nmsEntity instanceof EntityLiving) {
			enchantmentLevel = EnchantmentManager.a(craftItemInOffHand, ((EntityLiving) nmsEntity).getMonsterType());
		} else {
			enchantmentLevel = EnchantmentManager.a(craftItemInOffHand, EnumMonsterType.UNDEFINED);
		}

		float attackCooldown = entityPlayer.o(0.5F);

		damage *= 0.2F + attackCooldown * attackCooldown * 0.8F;
		enchantmentLevel *= attackCooldown;

		entityPlayer.da();

		if (damage > 0.0F || enchantmentLevel > 0.0F) {
			boolean cooldownOver = attackCooldown > 0.9F;
			boolean hasKnockedback = false;

			byte enchantmentByte = 0;
			int enchantmentCounter = enchantmentByte + EnchantmentManager.a(entityPlayer);

			if (player.isSprinting() && cooldownOver) {
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
				++enchantmentCounter;
				hasKnockedback = true;
			}

			boolean shouldCrit = cooldownOver
					&& entityPlayer.fallDistance > 0.0F
					&& !player.isOnGround()
					&& !entityPlayer.isInWater()
					&& !entityPlayer.hasEffect(MobEffects.BLINDNESS)
					&& !entityPlayer.isPassenger()
					&& entity instanceof LivingEntity;

			shouldCrit = shouldCrit && !entityPlayer.isSprinting();

			if (shouldCrit) {
				damage *= 1.5F;
			}

			damage += enchantmentLevel;

			boolean shouldSweep = false;
			double d0 = (entityPlayer.J - entityPlayer.I);

			if (cooldownOver && !shouldCrit && !hasKnockedback && player.isOnGround()
					&& d0 < (double) entityPlayer.cl()) {
				ItemStack itemStack = entityPlayer.b(EnumHand.OFF_HAND);

				if (itemStack != null && itemStack.getItem() instanceof ItemSword) {
					shouldSweep = true;
				}
			}

			float entityHealth = 0.0F;
			boolean onFire = false;
			int fireAspectEnchantmentLevel = EnchantmentManager.getEnchantmentLevel(Enchantments.FIRE_ASPECT, craftItemInOffHand);
			if (nmsEntity instanceof EntityLiving) {
				entityHealth = ((EntityLiving) nmsEntity).getHealth();
				if (fireAspectEnchantmentLevel > 0 && !nmsEntity.isBurning()) {
					EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(entityPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), 1);
					Bukkit.getPluginManager().callEvent(combustEvent);
					if (!combustEvent.isCancelled()) {
						onFire = true;
						nmsEntity.setOnFire(combustEvent.getDuration());
					}
				}
			}

			double vectorX = nmsEntity.motX;
			double vectorY = nmsEntity.motY;
			double vectorZ = nmsEntity.motZ;
			boolean flag5 = nmsEntity.damageEntity(DamageSource.playerAttack(entityPlayer), damage);
			if (flag5) {
				if (enchantmentCounter > 0) {
					if (nmsEntity instanceof EntityLiving) {
						((EntityLiving) nmsEntity).a(entityPlayer, (float) enchantmentCounter * 0.5F, MathHelper.sin(entityPlayer.yaw * 0.017453292F), (-MathHelper.cos(entityPlayer.yaw * 0.017453292F)));
					} else {
						nmsEntity.f((-MathHelper.sin(entityPlayer.yaw * 0.017453292F) * (float) enchantmentCounter * 0.5F), 0.1D, (MathHelper.cos(entityPlayer.yaw * 0.017453292F) * (float) enchantmentCounter * 0.5F));
					}

					entityPlayer.motX *= 0.6D;
					entityPlayer.motZ *= 0.6D;
					entityPlayer.setSprinting(false);
				}

				if (shouldSweep) {
					List list = entityPlayer.world.a(EntityLiving.class, nmsEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D));
					Iterator iterator = list.iterator();

					while(iterator.hasNext()) {
						EntityLiving entityliving = (EntityLiving)iterator.next();
						if (entityliving != entityPlayer && entityliving != entity && !entityliving.r(entityliving) && entityliving.h(entityliving) < 9.0D && entityliving.damageEntity(DamageSource.playerAttack(entityPlayer), 1.0F)) {
							entityliving.a(entityliving, 0.4F, MathHelper.sin(entityliving.yaw * 0.017453292F), -MathHelper.cos(entityliving.yaw * 0.017453292F));
						}
					}

					entityPlayer.world.a(null, entityPlayer.locX, entityPlayer.locY, entityPlayer.locZ, SoundEffects.eb, entityPlayer.bA(), 1.0F, 1.0F);
					entityPlayer.cI();
				}

				if (nmsEntity instanceof EntityPlayer && nmsEntity.velocityChanged) {
					boolean cancelled = false;

					Player otherPlayer = (Player) nmsEntity.getBukkitEntity();
					Vector velocity = new Vector(vectorX, vectorY, vectorZ);

					PlayerVelocityEvent playerVelocityEvent = new PlayerVelocityEvent(otherPlayer, velocity.clone());
					Bukkit.getServer().getPluginManager().callEvent(playerVelocityEvent);

					if (playerVelocityEvent.isCancelled()) {
						cancelled = true;
					} else if (!velocity.equals(playerVelocityEvent.getVelocity())) {
						otherPlayer.setVelocity(playerVelocityEvent.getVelocity());
					}

					if (!cancelled) {
						((EntityPlayer) nmsEntity).playerConnection.sendPacket(new PacketPlayOutEntityVelocity(nmsEntity));
						nmsEntity.velocityChanged = false;
						nmsEntity.motX = vectorX;
						nmsEntity.motY = vectorY;
						nmsEntity.motZ = vectorZ;
					}
				}

				if (shouldCrit) {
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
					entityPlayer.a(nmsEntity);
				}

				if (!shouldCrit && !shouldSweep) {
					if (cooldownOver) {
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
					} else {
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_WEAK, 1.0F, 1.0F);
					}
				}

				if (enchantmentLevel > 0.0F) {
					entityPlayer.b(nmsEntity);
				}

				if (!entityPlayer.world.isClientSide && nmsEntity instanceof EntityHuman) {
					EntityHuman entityHuman = (EntityHuman) nmsEntity;
					ItemStack humanItemStack = entityHuman.ct() ? entityHuman.cw() : null;
					if (craftItemInOffHand != null && humanItemStack != null && craftItemInOffHand.getItem() instanceof ItemAxe && humanItemStack.getItem() == Items.SHIELD) {
						float f4 = 0.25F + (float)EnchantmentManager.getDigSpeedEnchantmentLevel(entityPlayer) * 0.05F;
						if (hasKnockedback) {
							f4 += 0.75F;
						}

						if (new Random().nextFloat() < f4) {
							entityHuman.db().a(Items.SHIELD, 100);
							entityPlayer.world.broadcastEntityEffect(entityHuman, (byte)30);
						}
					}
				}

				if (damage >= 18.0F) {
					entityPlayer.b(AchievementList.F);
				}

				entityPlayer.z(nmsEntity);
				if (nmsEntity instanceof EntityLiving) {
					EnchantmentManager.a((EntityLiving) nmsEntity, entityPlayer);
				}

				EnchantmentManager.b(entityPlayer, nmsEntity);
				Object object = nmsEntity;

				if (entity instanceof EntityComplexPart) {
					IComplex icomplex = ((EntityComplexPart) entity).owner;
					if (icomplex instanceof EntityLiving) {
						object = icomplex;
					}
				}

				if (craftItemInOffHand != null && object instanceof EntityLiving) {
					craftItemInOffHand.a((EntityLiving) object, entityPlayer);
					if (craftItemInOffHand.count == 0) {
						entityPlayer.a(EnumHand.MAIN_HAND, null);
					}
				}

				if (entity instanceof EntityLiving) {
					float newEntityHealth = entityHealth - ((EntityLiving) entity).getHealth();
					entityPlayer.a(StatisticList.y, Math.round(newEntityHealth * 10.0F));

					if (fireAspectEnchantmentLevel > 0) {
						EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(entityPlayer.getBukkitEntity(), nmsEntity.getBukkitEntity(), fireAspectEnchantmentLevel * 4);
						Bukkit.getPluginManager().callEvent(combustEvent);

						if (!combustEvent.isCancelled()) {
							nmsEntity.setOnFire(combustEvent.getDuration());
						}
					}

					if (entityPlayer.world instanceof WorldServer && newEntityHealth > 2.0F) {
						int k = (int) ((double) newEntityHealth * 0.5D);
						((WorldServer) entityPlayer.world).a(EnumParticle.DAMAGE_INDICATOR, nmsEntity.locX, nmsEntity.locY + (double) (nmsEntity.length * 0.5F), nmsEntity.locZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
					}
				}

				entityPlayer.applyExhaustion(entityPlayer.world.spigotConfig.combatExhaustion);
			} else {
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0F, 1.0F);

				if (onFire) {
					entity.setFireTicks(0);
				}

				player.updateInventory();
			}
		}
	}
}
