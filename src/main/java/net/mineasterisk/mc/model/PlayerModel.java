package net.mineasterisk.mc.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(name = "player")
public final class PlayerModel {
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

  public PlayerModel(@NotNull Instant createdAt, @NotNull UUID uuid, @Nullable GuildModel guild) {
    this.createdAt = createdAt;
    this.uuid = uuid;
    this.guild = guild;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public @NotNull Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@NotNull Instant createdAt) {
    this.createdAt = createdAt;
  }

  public @NotNull UUID getUuid() {
    return uuid;
  }

  public void setUuid(@NotNull UUID uuid) {
    this.uuid = uuid;
  }

  public @Nullable GuildModel getGuild() {
    return guild;
  }

  public void setGuild(@NotNull GuildModel guild) {
    this.guild = guild;
  }
}
