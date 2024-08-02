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
  @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel createdBy;

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
      final @NotNull PlayerModel createdBy,
      final @Nullable GuildModel guild,
      final @NotNull InvitationStatus status) {
    this.createdAt = createdAt;
    this.createdBy = createdBy;
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

  public @NotNull PlayerModel getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(final @NotNull PlayerModel createdBy) {
    this.createdBy = createdBy;
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
