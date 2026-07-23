package com.parzival000.forgottenhearths.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.block.RestorationTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class HearthSiteSavedData extends SavedData {
    private static final Codec<HearthSiteSavedData> CODEC = Site.CODEC.listOf().xmap(
            HearthSiteSavedData::new,
            data -> List.copyOf(data.sites.values())
    );
    private static final SavedDataType<HearthSiteSavedData> TYPE = new SavedDataType<>(
            ForgottenHearths.id("hearth_sites"), HearthSiteSavedData::new, CODEC
    );

    private final Map<Long, Site> sites = new HashMap<>();

    public HearthSiteSavedData() {
    }

    private HearthSiteSavedData(List<Site> sites) {
        for (Site site : sites) {
            this.sites.put(site.pos().asLong(), site);
        }
    }

    public static HearthSiteSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Site getOrCreate(BlockPos pos, int variant) {
        long key = pos.asLong();
        Site existing = this.sites.get(key);
        if (existing != null) {
            return existing;
        }

        Site created = new Site(pos.immutable(), UUID.randomUUID(), variant, HearthStage.FORGOTTEN.id(), 0, false);
        this.sites.put(key, created);
        this.setDirty();
        return created;
    }

    public Site setStage(BlockPos pos, int variant, HearthStage expected, HearthStage next) {
        Site current = this.getOrCreate(pos, variant);
        if (current.stage() != expected.id()) {
            return current;
        }

        Site updated = current.withStage(next);
        this.sites.put(pos.asLong(), updated);
        this.setDirty();
        return updated;
    }

    public Site completeTask(BlockPos pos, int variant, RestorationTask task) {
        Site current = this.getOrCreate(pos, variant);
        if (current.stage() != HearthStage.REKINDLED.id() || (current.tasks() & task.mask()) != 0) {
            return current;
        }

        int tasks = current.tasks() | task.mask();
        HearthStage nextStage = tasks == RestorationTask.ALL_MASK ? HearthStage.RESTORED : HearthStage.REKINDLED;
        Site updated = current.withTasksAndStage(tasks, nextStage);
        this.sites.put(pos.asLong(), updated);
        this.setDirty();
        return updated;
    }

    public Site claimReward(BlockPos pos, int variant) {
        Site current = this.getOrCreate(pos, variant);
        if (current.rewardGranted()) {
            return current;
        }

        Site updated = current.withRewardGranted();
        this.sites.put(pos.asLong(), updated);
        this.setDirty();
        return updated;
    }

    public record Site(BlockPos pos, UUID id, int variant, int stage, int tasks, boolean rewardGranted) {
        private static final Codec<Site> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Site::pos),
                UUIDUtil.CODEC.fieldOf("id").forGetter(Site::id),
                Codec.intRange(0, 2).optionalFieldOf("variant", 0).forGetter(Site::variant),
                Codec.intRange(0, 3).optionalFieldOf("stage", 0).forGetter(Site::stage),
                Codec.intRange(0, RestorationTask.ALL_MASK).optionalFieldOf("tasks", 0).forGetter(Site::tasks),
                Codec.BOOL.optionalFieldOf("reward_granted", false).forGetter(Site::rewardGranted)
        ).apply(instance, Site::new));

        public HearthStage hearthStage() {
            return HearthStage.byId(this.stage);
        }

        public Site withStage(HearthStage stage) {
            return new Site(this.pos, this.id, this.variant, stage.id(), this.tasks, this.rewardGranted);
        }

        public Site withTasksAndStage(int tasks, HearthStage stage) {
            return new Site(this.pos, this.id, this.variant, stage.id(), tasks, this.rewardGranted);
        }

        public Site withRewardGranted() {
            return new Site(this.pos, this.id, this.variant, this.stage, this.tasks, true);
        }
    }
}
