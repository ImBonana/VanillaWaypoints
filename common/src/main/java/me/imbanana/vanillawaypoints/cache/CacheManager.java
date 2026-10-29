package me.imbanana.vanillawaypoints.cache;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kotlin.jvm.functions.Function2;
import me.imbanana.vanillawaypoints.VanillaWaypoints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class CacheManager {
    public static final Codec<List<CachedWaypointData>> FILE_CODEC = CachedWaypointData.CODEC.listOf().fieldOf("entries").codec();

    private final Path cacheDir;
    private final Map<Long, Map<Long, CachedWaypointData>> CACHE = new ConcurrentHashMap<>();
    private final Set<Long> DIRTY = ConcurrentHashMap.newKeySet();

    public CacheManager(Minecraft minecraft, ClientLevel level) {
         this.cacheDir = minecraft.gameDirectory.toPath()
                 .resolve(VanillaWaypoints.MOD_ID)
                 .resolve(createLevelKey(minecraft))
                 .resolve(level.dimension().identifier().toString().replace(':', '_'));
    }

    private String createLevelKey(Minecraft minecraft) {
        IntegratedServer srv = minecraft.getSingleplayerServer();

        if (srv != null) {
            String folder = srv.getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
            return "sp_" + sanitize(folder);
        }

        ServerData info = minecraft.getCurrentServer();
        if (info == null) return "unknown";
        if (info.isRealm()) return "realm_" + sanitize(info.name);

        return "mp_" + sanitize(info.ip);
    }

    private String sanitize(String s) { return s.replaceAll("[^a-zA-Z0-9._-]", "_"); }

    public void saveWaypointData(BlockPos blockPos, DyeColor dyeColor) {
        BlockPos pos = blockPos.immutable();
        long chunk = ChunkPos.pack(pos);

        CACHE.computeIfAbsent(chunk, key -> new ConcurrentHashMap<>()).put(pos.asLong(), new CachedWaypointData(pos, dyeColor));
        DIRTY.add(chunk);
    }

    public void removeWaypointData(BlockPos blockPos) {
        long chunk = ChunkPos.pack(blockPos);
        Map<Long, CachedWaypointData> data = CACHE.get(chunk);

        if (data != null && data.remove(blockPos.asLong()) != null) {
            DIRTY.add(chunk);

            if (data.isEmpty()) CACHE.remove(chunk);
        }
    }

    public CachedWaypointData getCachedWaypointData(BlockPos blockPos) {
        Map<Long, CachedWaypointData> data = CACHE.get(ChunkPos.pack(blockPos));
        return data == null ? null : data.get(blockPos.asLong());
    }

    public boolean hasWaypointData(BlockPos pos) {
        return getCachedWaypointData(pos) != null;
    }

    public void updateChunkData(LevelChunk chunk, Function2<BlockState, BlockPos, CachedWaypointData> updater) {
        long chunkId = chunk.getPos().pack();
        Map<Long, CachedWaypointData> data = CACHE.get(chunkId);
        if (data == null) return;

        boolean changed = false;
        for (CachedWaypointData cachedWaypointData : new ArrayList<>(data.values())) {
            CachedWaypointData newData = updater.invoke(chunk.getBlockState(cachedWaypointData.blockPos()), cachedWaypointData.blockPos());
            if (newData == null) {
                data.remove(cachedWaypointData.blockPos().asLong());
                changed = true;
            } else if (!cachedWaypointData.compare(newData)) {
                data.put(cachedWaypointData.blockPos().asLong(), newData);
                changed = true;
            }
        }

        if (data.isEmpty()) CACHE.remove(chunkId);
        if (changed) DIRTY.add(chunkId);
    }

    public void flushAll() {
        for (Long chunk : new ArrayList<>(DIRTY)) {
            saveChunk(chunk);
        }
    }

    public void saveChunk(long chunk) {
        if (!DIRTY.remove(chunk)) return;

        Path target = this.cacheDir.resolve(Long.toHexString(chunk) + ".nbt");
        Map<Long, CachedWaypointData> data = CACHE.get(chunk);

        if (data == null || data.isEmpty()) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) { }

            return;
        }

        try {
            DataResult<Tag> result = FILE_CODEC.encodeStart(NbtOps.INSTANCE, new ArrayList<>(data.values()));
            Tag encoded = result.getOrThrow();
            Files.createDirectories(cacheDir);
            Path temp = target.resolveSibling(target.getFileName() + ".tmp");
            NbtIo.writeCompressed((CompoundTag) encoded, temp);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            VanillaWaypoints.LOGGER.error(e.getMessage(), e);
        }
    }

    public void loadAllFromDisk() {
        if (!Files.isDirectory(this.cacheDir)) return;

        try (Stream<Path> files = Files.list(this.cacheDir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".nbt")).forEach(path -> {
                try {
                    String name = path.getFileName().toString();
                    long chunk = Long.parseUnsignedLong(name.substring(0, name.length() - 4), 16);

                    CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                    List<CachedWaypointData> cachedData = FILE_CODEC.parse(NbtOps.INSTANCE, tag)
                            .resultOrPartial(err -> VanillaWaypoints.LOGGER.error("bad file {}: {}", path, err))
                            .orElse(null);

                    if (cachedData == null | cachedData.isEmpty()) return;

                    Map<Long, CachedWaypointData> map = new ConcurrentHashMap<>();
                    for (CachedWaypointData data : cachedData) {
                        map.put(data.blockPos().asLong(), data);
                    }
                    CACHE.put(chunk, map);
                } catch (IOException e) {
                    VanillaWaypoints.LOGGER.error("Failed to load {}", path, e);
                }
            });
        } catch (IOException e) {
            VanillaWaypoints.LOGGER.error(e.getMessage(), e);
        }
    }

    public record CachedWaypointData(BlockPos blockPos, DyeColor color) {
        public static final Codec<CachedWaypointData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("blockPos").forGetter(CachedWaypointData::blockPos),
                DyeColor.CODEC.fieldOf("color").forGetter(CachedWaypointData::color)
        ).apply(instance, CachedWaypointData::new));

        public boolean compare(CachedWaypointData data) {
            return data.color == this.color;
        }
    }
}
