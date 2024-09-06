package net.mineasterisk.mc.cache.access;

import be.seeseemelk.mockbukkit.MockBukkit;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.util.LoaderUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AccessCacheTest {
  private final @NotNull MockedStatic<@NotNull LoaderUtil> loaderUtil =
      Mockito.mockStatic(LoaderUtil.class);

  private @NotNull ConcurrentHashMap<@NotNull UUID, @NotNull Access> cache;

  @BeforeEach
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    final Field field = AccessCache.class.getDeclaredField("CACHE");

    field.setAccessible(true);

    MockBukkit.mock();

    //noinspection unchecked
    this.cache = (ConcurrentHashMap<UUID, Access>) field.get(null);

    this.cache.clear();
    MockBukkit.load(MineAsterisk.class);
  }

  @AfterEach
  public void tearDown() {
    this.cache.clear();
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenKey_whenKeyDoNotExist_thenAddAndGet() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();

    Assertions.assertNotNull(accessCache.get(key));
    Assertions.assertEquals(0, accessCache.get(key).getAccesses().size());
  }

  @Test
  void givenKey_whenKeyExist_thenAddAndGet() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();
    final Set<String> accesses =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Access value = new Access(accesses);

    accessCache.put(key, value);

    Assertions.assertNotNull(accessCache.get(key));
    Assertions.assertEquals(accesses.size(), accessCache.get(key).getAccesses().size());
  }

  @Test
  void givenCacheIsNotPopulated_whenGetAll_thenReturnEmptyMap() {
    final AccessCache accessCache = new AccessCache();

    Assertions.assertTrue(accessCache.getAll().isEmpty());
  }

  @Test
  void givenCacheIsPopulated_whenGetAll_thenReturnPopulatedMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();
    final Set<String> accesses =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Access value = new Access(accesses);

    accessCache.put(key, value);

    Assertions.assertFalse(accessCache.getAll().isEmpty());
  }

  @Test
  void givenCacheIsNotPopulated_whenGetAllDirty_thenReturnEmptyMap() {
    final AccessCache accessCache = new AccessCache();

    Assertions.assertTrue(accessCache.getAllDirty().isEmpty());
  }

  @Test
  void givenCacheIsPopulatedAndNoDirtyEntry_whenGetAllDirty_thenReturnEmptyMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();
    final Set<String> accesses =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Access value = new Access(accesses);

    accessCache.put(key, value);

    Assertions.assertTrue(accessCache.getAllDirty().isEmpty());
  }

  @Test
  void givenCacheIsPopulatedAndHasDirtyEntry_whenGetAllDirty_thenReturnPopulatedMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();
    final Set<String> accesses =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Access value = new Access(accesses);

    value.setDirty(true);
    accessCache.put(key, value);

    Assertions.assertEquals(1, accessCache.getAllDirty().size());
  }

  @Test
  void givenCacheIsPopulatedAndHasDirtyEntries_whenGetAllDirty_thenReturnPopulatedMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);

    value0.setDirty(true);
    value1.setDirty(true);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);

    Assertions.assertEquals(2, accessCache.getAllDirty().size());
  }

  @Test
  void givenCacheIsPopulatedAndHasSomeDirtyEntries_whenGetAllDirty_thenReturnPopulatedMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);

    value0.setDirty(true);
    value1.setDirty(true);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);
    accessCache.put(key2, value2);

    Assertions.assertEquals(2, accessCache.getAllDirty().size());
  }

  @Test
  void givenIsDirtyAndListIsNotPopulated_whenSetDirty_thenSetNothing() {
    final AccessCache accessCache = new AccessCache();

    Assertions.assertDoesNotThrow(() -> accessCache.setDirty(true, List.of()));
  }

  @Test
  void givenIsNotDirtyAndListIsNotPopulated_whenSetDirty_thenSetNothing() {
    final AccessCache accessCache = new AccessCache();

    Assertions.assertDoesNotThrow(() -> accessCache.setDirty(false, List.of()));
  }

  @Test
  void givenIsDirtyAndListIsPopulated_whenSetDirty_thenSetDirtyForMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);
    accessCache.put(key2, value2);

    accessCache.setDirty(true, List.of(key0, key2));

    Assertions.assertEquals(2, accessCache.getAllDirty().size());
    Assertions.assertEquals(3, accessCache.getAll().size());
  }

  @Test
  void givenIsNotDirtyAndListIsPopulated_whenSetDirty_thenSetDirtyForMap() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);

    value0.setDirty(true);
    value1.setDirty(true);
    value2.setDirty(true);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);
    accessCache.put(key2, value2);

    accessCache.setDirty(false, List.of(key0, key2));

    Assertions.assertEquals(1, accessCache.getAllDirty().size());
  }

  @Test
  void givenKeyAndValue_whenPut_thenPut() {
    final AccessCache accessCache = new AccessCache();
    final UUID key = UUID.randomUUID();
    final Set<String> accesses = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value = new Access(accesses);

    accessCache.put(key, value);

    Assertions.assertEquals(1, this.cache.size());
    Assertions.assertTrue(this.cache.get(key).getAccesses().containsAll(value.getAccesses()));
  }

  @Test
  void givenMultipleKeyAndValue_whenPutAll_thenPutAllAndReturnCurrentCacheCount() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);
    final HashMap<UUID, Access> entries = new HashMap<>();

    accessCache.put(key0, value0);
    entries.put(key1, value1);
    entries.put(key2, value2);

    final int cacheCount = accessCache.putAll(entries);

    Assertions.assertEquals(3, cacheCount);
    Assertions.assertNotNull(this.cache.get(key0));
    Assertions.assertNotNull(this.cache.get(key1));
  }

  @Test
  void givenKey_whenRemove_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);
    accessCache.put(key2, value2);

    Assertions.assertEquals(3, this.cache.size());

    accessCache.remove(key0);

    Assertions.assertEquals(2, this.cache.size());
    Assertions.assertNull(this.cache.get(key0));
  }

  @Test
  void givenCacheIsNotPopulated_whenRemoveAll_thenRemoveAll() {
    final AccessCache accessCache = new AccessCache();

    accessCache.removeAll();

    Assertions.assertEquals(0, this.cache.size());
  }

  @Test
  void givenCacheIsPopulated_whenRemoveAll_thenRemoveAll() {
    final AccessCache accessCache = new AccessCache();
    final UUID key0 = UUID.randomUUID();
    final UUID key1 = UUID.randomUUID();
    final UUID key2 = UUID.randomUUID();

    final Set<String> accesses0 =
        Set.of(
            PermissionConstant.TEAM_LEADER.toString(), PermissionConstant.TEAM_MEMBER.toString());

    final Set<String> accesses1 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Set<String> accesses2 = Set.of(PermissionConstant.TEAM_MEMBER.toString());
    final Access value0 = new Access(accesses0);
    final Access value1 = new Access(accesses1);
    final Access value2 = new Access(accesses2);

    accessCache.put(key0, value0);
    accessCache.put(key1, value1);
    accessCache.put(key2, value2);

    Assertions.assertEquals(3, this.cache.size());

    accessCache.removeAll();

    Assertions.assertEquals(0, this.cache.size());
  }
}
