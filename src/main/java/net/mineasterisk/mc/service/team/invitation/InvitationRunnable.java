package net.mineasterisk.mc.service.team.invitation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.service.team.TeamService;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class InvitationRunnable extends BukkitRunnable {
  private final int loop;
  private final @NotNull Invitation invitation;
  private int count = 0;
  private boolean initialized = false;

  public InvitationRunnable(final int loop, final @NotNull Invitation invitation) {
    this.loop = loop;
    this.invitation = invitation;
  }

  @Override
  public void run() {
    if (!initialized) {
      this.onRunInitialize(this.invitation);
    }

    if (count == loop) {
      final Component message = Component.text("Team invitation expired").color(NamedTextColor.RED);

      this.invitation.invitee().sendMessage(message);
      this.invitation.inviter().sendMessage(message);
      this.cancel();
    }

    count++;
  }

  private void onRunInitialize(final @NotNull Invitation invitation) {
    final TeamService teamService = new TeamService();

    teamService.addInvitationTask(this.getTaskId(), invitation);
    this.initialized = true;
  }

  @Override
  public void cancel() {
    super.cancel();
    this.onCancel();
  }

  private void onCancel() {
    final TeamService teamService = new TeamService();

    teamService.removeInvitationTask(this.getTaskId());
  }
}
