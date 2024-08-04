package net.mineasterisk.mc.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;
import net.mineasterisk.mc.constant.status.GuildStatus;
import org.jetbrains.annotations.NotNull;

@Entity(name = "guild")
public final class GuildModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private int id;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME")
  private @NotNull Instant createdAt;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel createdBy;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "name", unique = true, nullable = false, length = 10)
  private @NotNull String name;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel owner;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private @NotNull GuildStatus status;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @OneToMany(mappedBy = "guild", cascade = CascadeType.MERGE)
  private @NotNull Set<@NotNull PlayerModel> players;

  /**
   * This no argument constructor is deprecated and should not be used. It exists solely for the
   * purpose of Hibernate requiring a no argument constructor. Use {@link
   * GuildModel#GuildModel(Instant, PlayerModel, String, PlayerModel, GuildStatus, Set)} instead.
   */
  @Deprecated
  private GuildModel() {}

  public GuildModel(
      final @NotNull Instant createdAt,
      final @NotNull PlayerModel createdBy,
      final @NotNull String name,
      final @NotNull PlayerModel owner,
      final @NotNull GuildStatus status,
      final @NotNull Set<@NotNull PlayerModel> players) {
    this.createdAt = createdAt;
    this.createdBy = createdBy;
    this.name = name;
    this.owner = owner;
    this.status = status;
    this.players = players;
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

  public @NotNull PlayerModel getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final @NotNull PlayerModel createdBy) {
    this.createdBy = createdBy;
  }

  public @NotNull String getName() {
    return this.name;
  }

  public void setName(final @NotNull String name) {
    this.name = name;
  }

  public @NotNull PlayerModel getOwner() {
    return this.owner;
  }

  public void setOwner(final @NotNull PlayerModel owner) {
    this.owner = owner;
  }

  public @NotNull GuildStatus getStatus() {
    return this.status;
  }

  public void setStatus(final @NotNull GuildStatus status) {
    this.status = status;
  }

  public @NotNull Set<@NotNull PlayerModel> getPlayers() {
    return this.players;
  }

  public void setPlayers(final @NotNull Set<@NotNull PlayerModel> players) {
    for (PlayerModel player : players) {
      player.setGuild(this);
    }
  }
}
