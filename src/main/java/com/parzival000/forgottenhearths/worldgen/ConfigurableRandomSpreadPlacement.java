package com.parzival000.forgottenhearths.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import com.parzival000.forgottenhearths.registry.ModWorldgen;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public final class ConfigurableRandomSpreadPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<ConfigurableRandomSpreadPlacement> CODEC = RecordCodecBuilder
            .<ConfigurableRandomSpreadPlacement>mapCodec(instance -> placementCodec(instance)
                    .and(instance.group(
                            Codec.intRange(1, 4096).fieldOf("spacing").forGetter(ConfigurableRandomSpreadPlacement::baseSpacing),
                            Codec.intRange(0, 4096).fieldOf("separation").forGetter(ConfigurableRandomSpreadPlacement::baseSeparation),
                            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                    .forGetter(ConfigurableRandomSpreadPlacement::baseSpreadType)
                    ))
                    .apply(instance, ConfigurableRandomSpreadPlacement::new))
            .validate(ConfigurableRandomSpreadPlacement::validatePlacement);

    private final int baseSpacing;
    private final int baseSeparation;
    private final RandomSpreadType baseSpreadType;

    @SuppressWarnings("deprecation")
    public ConfigurableRandomSpreadPlacement(
            Vec3i locateOffset,
            StructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<StructurePlacement.ExclusionZone> exclusionZone,
            int spacing,
            int separation,
            RandomSpreadType spreadType
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.baseSpacing = spacing;
        this.baseSeparation = separation;
        this.baseSpreadType = spreadType;
    }

    private static DataResult<ConfigurableRandomSpreadPlacement> validatePlacement(
            ConfigurableRandomSpreadPlacement placement
    ) {
        return placement.baseSpacing <= placement.baseSeparation
                ? DataResult.error(() -> "Spacing must be larger than separation")
                : DataResult.success(placement);
    }

    private int baseSpacing() {
        return this.baseSpacing;
    }

    private int baseSeparation() {
        return this.baseSeparation;
    }

    private RandomSpreadType baseSpreadType() {
        return this.baseSpreadType;
    }

    @Override
    public int spacing() {
        return ForgottenHearthsConfig.HOMESTEAD_SPACING.getAsInt();
    }

    @Override
    public int separation() {
        return Math.min(this.baseSeparation, this.spacing() - 1);
    }

    @Override
    public RandomSpreadType spreadType() {
        return this.baseSpreadType;
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int sourceX, int sourceZ) {
        int spacing = this.spacing();
        int spacedGridX = Math.floorDiv(sourceX, spacing);
        int spacedGridZ = Math.floorDiv(sourceZ, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, spacedGridX, spacedGridZ, this.salt());
        int limit = spacing - this.separation();
        int spreadX = this.baseSpreadType.evaluate(random, limit);
        int spreadZ = this.baseSpreadType.evaluate(random, limit);
        return new ChunkPos(spacedGridX * spacing + spreadX, spacedGridZ * spacing + spreadZ);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int sourceX, int sourceZ) {
        if (!ForgottenHearthsConfig.GENERATE_HOMESTEADS.getAsBoolean()) {
            return false;
        }
        ChunkPos candidate = this.getPotentialStructureChunk(state.getLevelSeed(), sourceX, sourceZ);
        return candidate.x() == sourceX && candidate.z() == sourceZ;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModWorldgen.CONFIGURABLE_RANDOM_SPREAD.get();
    }
}
