package org.avarion.dualwield.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class OffHandAttackEvent extends EntityDamageByEntityEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public OffHandAttackEvent(Entity damager, Entity damagee, DamageCause cause, double damage) {
        super(damager, damagee, cause, damage);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
