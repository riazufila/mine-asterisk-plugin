package net.mineasterisk.mc.enchantment.blinkaura;

import net.mineasterisk.mc.util.EnchantmentUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BlinkAuraEnchantmentListener implements Listener {
  @EventHandler
  public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
    final Enchantment enchantment = EnchantmentUtil.get(BlinkAuraEnchantment.getNamespacedKey());
    final Entity attacker = event.getDamager();
    final Entity attacked = event.getEntity();
    final boolean isAttackerLiving = attacker instanceof LivingEntity;
    final boolean isAttackedLiving = attacked instanceof LivingEntity;

    if (!(isAttackerLiving && isAttackedLiving)
        || ((LivingEntity) attacked).getEquipment() == null) {
      return;
    }

    final int totalEnchantmentLevel =
        EnchantmentUtil.getTotalEnchantmentLevel(
            enchantment, ((LivingEntity) attacked).getEquipment());

    if (totalEnchantmentLevel == 0) {
      return;
    }

    new BlinkAuraEnchantment((LivingEntity) attacker, (LivingEntity) attacked)
        .onAttack(totalEnchantmentLevel);
  }
}
