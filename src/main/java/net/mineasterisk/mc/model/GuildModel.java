package net.mineasterisk.mc.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;
import net.mineasterisk.mc.constant.GuildStatus;
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
  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel createdBy;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Column(name = "name", unique = true, nullable = false, length = 10)
  private @NotNull String name;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false)
  private @NotNull PlayerModel owner;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private @NotNull GuildStatus status;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @OneToMany(mappedBy = "guild")
  private @NotNull Set<@NotNull PlayerModel> players;

  /**
   * This no argument constructor is deprecated and should not be used. It exists solely for the
   * purpose of Hibernate requiring a no argument constructor. Use {@link
   * GuildModel#GuildModel(Instant, PlayerModel, String, PlayerModel, GuildStatus, Set)} instead.
   */
  @Deprecated
  private GuildModel() {}

  public GuildModel(
      @NotNull Instant createdAt,
      @NotNull PlayerModel createdBy,
      @NotNull String name,
      @NotNull PlayerModel owner,
      @NotNull GuildStatus status,
      @NotNull Set<@NotNull PlayerModel> players) {
    this.createdAt = createdAt;
    this.createdBy = createdBy;
    this.name = name;
    this.owner = owner;
    this.status = status;
    this.players = players;
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

  public @NotNull PlayerModel getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(@NotNull PlayerModel createdBy) {
    this.createdBy = createdBy;
  }

  public @NotNull String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  public @NotNull PlayerModel getOwner() {
    return owner;
  }

  public void setOwner(@NotNull PlayerModel owner) {
    this.owner = owner;
  }

  public @NotNull GuildStatus getStatus() {
    return status;
  }

  public void setStatus(@NotNull GuildStatus status) {
    this.status = status;
  }

  public @NotNull Set<@NotNull PlayerModel> getPlayers() {
    return players;
  }

  public void setPlayers(@NotNull Set<@NotNull PlayerModel> players) {
    for (PlayerModel player : players) {
      player.setGuild(this);
    }
  }
}
