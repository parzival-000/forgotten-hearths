package com.parzival000.forgottenhearths.gametest;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class WorldgenValidation {
    public static final String PROPERTY = "forgottenhearths.worldgenProbe";

    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }

        ServerLevel level = event.getServer().overworld();
        Holder.Reference<Structure> structure = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .get(ForgottenHearths.id("forgotten_homestead"))
                .orElseThrow(() -> new IllegalStateException("Forgotten Hearths structure is not registered"));
        ChunkGeneratorStructureState generatorState = level.getChunkSource().getGeneratorState();
        List<StructurePlacement> placements = generatorState.getPlacementsForStructure(structure);
        RandomSpreadStructurePlacement placement = placements.stream()
                .filter(RandomSpreadStructurePlacement.class::isInstance)
                .map(RandomSpreadStructurePlacement.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forgotten Hearths random-spread placement is missing"));

        BlockPos spawnPos = event.getServer().getRespawnData().pos();
        ChunkPos origin = new ChunkPos(
                SectionPos.blockToSectionCoord(spawnPos.getX()),
                SectionPos.blockToSectionCoord(spawnPos.getZ())
        );
        StructureStart found = null;
        int checked = 0;
        int allowedBiomes = 0;
        for (int radius = 0; radius <= 3 && found == null; radius++) {
            for (int x = -radius; x <= radius && found == null; x++) {
                for (int z = -radius; z <= radius && found == null; z++) {
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }
                    ChunkPos candidate = placement.getPotentialStructureChunk(
                            generatorState.getLevelSeed(),
                            origin.x() + placement.spacing() * x,
                            origin.z() + placement.spacing() * z
                    );
                    Holder<net.minecraft.world.level.biome.Biome> biome = level.getChunkSource()
                            .getGenerator()
                            .getBiomeSource()
                            .getNoiseBiome(
                            candidate.x() * 4 + 2,
                            level.getSeaLevel() >> 2,
                            candidate.z() * 4 + 2,
                            level.getChunkSource().randomState().sampler()
                    );
                    if (structure.value().biomes().contains(biome)) {
                        allowedBiomes++;
                    }
                    ChunkAccess chunk = level.getChunk(candidate.x(), candidate.z(), ChunkStatus.STRUCTURE_STARTS);
                    StructureStart start = level.structureManager().getStartForStructure(
                            SectionPos.bottomOf(chunk), structure.value(), chunk
                    );
                    checked++;
                    if (start != null && start.isValid()) {
                        found = start;
                    }
                }
            }
        }
        if (found == null) {
            throw new IllegalStateException(
                    "Forgotten Hearths did not naturally start in " + checked
                            + " candidate chunks; " + allowedBiomes + " used allowed biomes"
            );
        }
        BoundingBox box = found.getBoundingBox();
        for (int chunkX = SectionPos.blockToSectionCoord(box.minX());
                chunkX <= SectionPos.blockToSectionCoord(box.maxX());
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(box.minZ());
                    chunkZ <= SectionPos.blockToSectionCoord(box.maxZ());
                    chunkZ++) {
                level.getChunk(chunkX, chunkZ, ChunkStatus.FULL);
            }
        }
        int hearths = 0;
        int markers = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()
        )) {
            if (level.getBlockState(pos).is(ModBlocks.FORGOTTEN_HEARTH.get())) {
                hearths++;
            } else if (level.getBlockState(pos).is(ModBlocks.RESTORATION_MARKER.get())) {
                markers++;
            }
        }
        if (hearths != 1 || markers != 3) {
            throw new IllegalStateException(
                    "Forgotten Hearths worldgen placed " + hearths + " hearths and " + markers + " restoration markers"
            );
        }
        ForgottenHearths.LOGGER.info(
                "Dedicated worldgen probe placed a complete homestead at chunk {} after {} candidates",
                found.getChunkPos(),
                checked
        );
        event.getServer().halt(false);
    }

    private WorldgenValidation() {
    }
}
