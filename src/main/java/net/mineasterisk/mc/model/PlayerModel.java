package net.mineasterisk.mc.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(name = PlayerModel.entity)
public final class PlayerModel {
  public static final @NotNull String entity = "player";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private int id;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME")
  private @NotNull Instant createdAt;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "uuid", unique = true, nullable = false)
  private @NotNull UUID uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guild_id")
  private @Nullable GuildModel guild;

  /**
   * This no argument constructor is deprecated and should not be used. It exists solely for the
   * purpose of Hibernate requiring a no argument constructor. Use {@link
   * PlayerModel#PlayerModel(Instant, UUID, GuildModel)} instead.
   */
  @Deprecated
  private PlayerModel() {}

  public PlayerModel(
      final @NotNull Instant createdAt,
      final @NotNull UUID uuid,
      final @Nullable GuildModel guild) {
    this.createdAt = createdAt;
    this.uuid = uuid;
    this.guild = guild;
  }

  public int getId() {
    return this.id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public @NotNull Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(final @NotNull Instant createdAt) {
    this.createdAt = createdAt;
  }

  public @NotNull UUID getUuid() {
    return this.uuid;
  }

  public void setUuid(final @NotNull UUID uuid) {
    this.uuid = uuid;
  }

  public @Nullable GuildModel getGuild() {
    return this.guild;
  }

  public void setGuild(final @NotNull GuildModel guild) {
    this.guild = guild;
  }
}
