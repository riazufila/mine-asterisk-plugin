package net.mineasterisk.mc.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import net.mineasterisk.mc.constant.GuildStatus;
import org.jetbrains.annotations.NotNull;

@Entity(name = "guild")
public class GuildModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private int id;

  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME")
  private Instant createdAt;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false)
  private PlayerModel createdBy;

  @Column(name = "name", unique = true, nullable = false, length = 10)
  private String name;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false)
  private PlayerModel owner;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private GuildStatus status;

  @OneToMany(mappedBy = "guild")
  private Set<PlayerModel> players = new HashSet<>();

  public GuildModel() {}

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

  public @NotNull Set<PlayerModel> getPlayers() {
    return players;
  }

  public void setPlayers(@NotNull Set<PlayerModel> players) {
    for (PlayerModel player : players) {
      player.setGuild(this);
    }
  }
}
