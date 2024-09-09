package net.mineasterisk.mc.enchantment.blinkstrike;

import net.mineasterisk.mc.util.EnchantmentUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BlinkStrikeEnchantmentListener implements Listener {
  @EventHandler
  public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
    final Enchantment enchantment = EnchantmentUtil.get(BlinkStrikeEnchantment.getKey());
    final Entity attacker = event.getDamager();
    final Entity attacked = event.getEntity();
    final boolean isAttackerLiving = attacker instanceof LivingEntity;
    final boolean isAttackedLiving = attacked instanceof LivingEntity;

    if (!(isAttackerLiving && isAttackedLiving)
        || ((LivingEntity) attacker).getEquipment() == null) {
      return;
    }

    final int totalEnchantmentLevel =
        EnchantmentUtil.getTotalEnchantmentLevel(
            enchantment, ((LivingEntity) attacker).getEquipment());

    if (totalEnchantmentLevel == 0) {
      return;
    }

    new BlinkStrikeEnchantment((LivingEntity) attacker, (LivingEntity) attacked)
        .onAttack(totalEnchantmentLevel);
  }
}
