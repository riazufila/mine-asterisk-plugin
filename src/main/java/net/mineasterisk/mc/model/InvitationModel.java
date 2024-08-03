package net.mineasterisk.mc.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;
import net.mineasterisk.mc.constant.status.InvitationStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(name = "invitation")
public final class InvitationModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private int id;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME")
  private @NotNull Instant createdAt;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "inviter", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel inviter;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "invitee", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel invitee;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guild_id")
  private @NotNull GuildModel guild;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private @NotNull InvitationStatus status;

  /**
   * This no argument constructor is deprecated and should not be used. It exists solely for the
   * purpose of Hibernate requiring a no argument constructor. Use {@link
   * PlayerModel#PlayerModel(Instant, UUID, GuildModel)} instead.
   */
  @Deprecated
  private InvitationModel() {}

  public InvitationModel(
      final @NotNull Instant createdAt,
      final @NotNull PlayerModel inviter,
      final @NotNull PlayerModel invitee,
      final @Nullable GuildModel guild,
      final @NotNull InvitationStatus status) {
    this.createdAt = createdAt;
    this.inviter = inviter;
    this.invitee = invitee;
    this.guild = guild;
    this.status = status;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public @NotNull Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final @NotNull Instant createdAt) {
    this.createdAt = createdAt;
  }

  public @NotNull PlayerModel getInviter() {
    return inviter;
  }

  public void setInviter(final @NotNull PlayerModel inviter) {
    this.inviter = inviter;
  }

  public @NotNull PlayerModel getInvitee() {
    return invitee;
  }

  public void setInvitee(final @NotNull PlayerModel invitee) {
    this.invitee = invitee;
  }

  public @NotNull GuildModel getGuild() {
    return guild;
  }

  public void setGuild(final @NotNull GuildModel guild) {
    this.guild = guild;
  }

  public @NotNull InvitationStatus getStatus() {
    return status;
  }

  public void setStatus(final @NotNull InvitationStatus status) {
    this.status = status;
  }
}
