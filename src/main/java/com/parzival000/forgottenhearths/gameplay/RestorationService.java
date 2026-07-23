package com.parzival000.forgottenhearths.gameplay;

import com.parzival000.forgottenhearths.advancement.ModAdvancements;
import com.parzival000.forgottenhearths.block.ForgottenHearthBlock;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.block.RestorationMarkerBlock;
import com.parzival000.forgottenhearths.block.RestorationTask;
import com.parzival000.forgottenhearths.block.entity.ForgottenHearthBlockEntity;
import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import com.parzival000.forgottenhearths.registry.ModBlocks;
import com.parzival000.forgottenhearths.registry.ModItems;
import com.parzival000.forgottenhearths.world.HearthSiteSavedData;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class RestorationService {
    private static final int HEARTH_SEARCH_RADIUS = 12;

    public static InteractionResult useHearth(
            Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof ForgottenHearthBlockEntity hearth)) {
            return InteractionResult.PASS;
        }

        HearthSiteSavedData data = HearthSiteSavedData.get(serverLevel);
        HearthSiteSavedData.Site site = data.getOrCreate(pos, hearth.variant());
        hearth.syncFromSite(site);

        return switch (site.hearthStage()) {
            case FORGOTTEN -> discover(data, pos, serverPlayer, hearth);
            case DISCOVERED -> tryRekindle(data, pos, serverPlayer, hand, held, hearth);
            case REKINDLED -> showRestorationStatus(serverLevel, serverPlayer, hearth);
            case RESTORED -> showRestoredStatus(serverPlayer);
        };
    }

    public static InteractionResult useMarker(
            Level level, BlockPos markerPos, Player player, ItemStack held, RestorationTask task
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockState markerState = serverLevel.getBlockState(markerPos);
        if (!markerState.is(ModBlocks.RESTORATION_MARKER.get())
                || markerState.getValue(RestorationMarkerBlock.TASK) != task) {
            status(serverPlayer, "message.forgotten_hearths.marker.obstructed");
            return InteractionResult.SUCCESS;
        }

        ForgottenHearthBlockEntity hearth = findNearestHearth(serverLevel, markerPos);
        if (hearth == null) {
            status(serverPlayer, "message.forgotten_hearths.marker.no_hearth");
            return InteractionResult.SUCCESS;
        }

        HearthSiteSavedData data = HearthSiteSavedData.get(serverLevel);
        HearthSiteSavedData.Site site = data.getOrCreate(hearth.getBlockPos(), hearth.variant());
        hearth.syncFromSite(site);
        if (site.hearthStage() != HearthStage.REKINDLED) {
            status(serverPlayer, "message.forgotten_hearths.marker.hearth_not_ready");
            return InteractionResult.SUCCESS;
        }

        Item required = requiredItem(task);
        if (!held.is(required)) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.forgotten_hearths.marker.requires", required.getName(new ItemStack(required))), true
            );
            return InteractionResult.SUCCESS;
        }

        if ((site.tasks() & task.mask()) != 0) {
            status(serverPlayer, "message.forgotten_hearths.marker.already_restored");
            return InteractionResult.SUCCESS;
        }

        BlockState restoredState = restoredState(task);
        if (!serverLevel.setBlock(markerPos, restoredState, Block.UPDATE_ALL)) {
            status(serverPlayer, "message.forgotten_hearths.marker.obstructed");
            return InteractionResult.SUCCESS;
        }

        held.consume(1, serverPlayer);
        HearthSiteSavedData.Site updated = data.completeTask(hearth.getBlockPos(), hearth.variant(), task);
        hearth.syncFromSite(updated);
        ModAdvancements.grant(serverPlayer, ModAdvancements.RELIC);
        serverLevel.playSound(null, markerPos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 0.9F, 0.9F + serverLevel.getRandom().nextFloat() * 0.2F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, markerPos.getX() + 0.5, markerPos.getY() + 0.6, markerPos.getZ() + 0.5, 7, 0.25, 0.2, 0.25, 0.02);

        if (updated.hearthStage() == HearthStage.RESTORED) {
            completeRestoration(data, serverLevel, serverPlayer, hearth, updated);
        } else {
            int remaining = 3 - Integer.bitCount(updated.tasks());
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.forgotten_hearths.marker.restored", remaining), true
            );
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult discover(
            HearthSiteSavedData data, BlockPos pos, ServerPlayer player, ForgottenHearthBlockEntity hearth
    ) {
        HearthSiteSavedData.Site updated = data.setStage(pos, hearth.variant(), HearthStage.FORGOTTEN, HearthStage.DISCOVERED);
        hearth.syncFromSite(updated);
        ModAdvancements.grant(player, ModAdvancements.DISCOVERED);
        player.sendSystemMessage(
                Component.translatable(
                        "message.forgotten_hearths.discovered",
                        ForgottenHearthsConfig.REQUIRED_LOGS.getAsInt(),
                        ForgottenHearthsConfig.REQUIRED_BRICKS.getAsInt()
                ),
                true
        );
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryRekindle(
            HearthSiteSavedData data,
            BlockPos pos,
            ServerPlayer player,
            InteractionHand hand,
            ItemStack held,
            ForgottenHearthBlockEntity hearth
    ) {
        int requiredLogs = ForgottenHearthsConfig.REQUIRED_LOGS.getAsInt();
        int requiredBricks = ForgottenHearthsConfig.REQUIRED_BRICKS.getAsInt();
        int logs = countMatching(player.getInventory(), stack -> stack.is(ItemTags.LOGS));
        int bricks = countMatching(player.getInventory(), stack -> stack.is(Items.BRICK));

        if (!held.is(Items.FLINT_AND_STEEL)) {
            player.sendSystemMessage(
                    Component.translatable("message.forgotten_hearths.requirements", requiredLogs, requiredBricks), true
            );
            return InteractionResult.SUCCESS;
        }
        if (logs < requiredLogs || bricks < requiredBricks) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.forgotten_hearths.missing_materials",
                            Math.max(0, requiredLogs - logs),
                            Math.max(0, requiredBricks - bricks)
                    ),
                    true
            );
            return InteractionResult.SUCCESS;
        }

        if (!player.hasInfiniteMaterials()) {
            consumeMatching(player.getInventory(), stack -> stack.is(ItemTags.LOGS), requiredLogs);
            consumeMatching(player.getInventory(), stack -> stack.is(Items.BRICK), requiredBricks);
            held.hurtAndBreak(1, player, hand);
        }

        HearthSiteSavedData.Site updated = data.setStage(pos, hearth.variant(), HearthStage.DISCOVERED, HearthStage.REKINDLED);
        hearth.syncFromSite(updated);
        player.level().playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.85F);
        player.level().sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.55, pos.getZ() + 0.5, 18, 0.3, 0.2, 0.3, 0.04);
        player.level().sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.7, 0.5, 0.7, 0.015);
        player.level().gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        ModAdvancements.grant(player, ModAdvancements.DISCOVERED);
        ModAdvancements.grant(player, ModAdvancements.REKINDLED);
        status(player, "message.forgotten_hearths.rekindled");
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult showRestorationStatus(
            ServerLevel level, ServerPlayer player, ForgottenHearthBlockEntity hearth
    ) {
        if (hasObstructedRestoration(level, hearth)) {
            status(player, "message.forgotten_hearths.marker.positions_obstructed");
            return InteractionResult.SUCCESS;
        }
        int remaining = 3 - Integer.bitCount(hearth.tasks());
        player.sendSystemMessage(
                Component.translatable("message.forgotten_hearths.restoration_status", remaining), true
        );
        return InteractionResult.SUCCESS;
    }

    private static boolean hasObstructedRestoration(ServerLevel level, ForgottenHearthBlockEntity hearth) {
        BlockState hearthState = level.getBlockState(hearth.getBlockPos());
        if (!hearthState.is(ModBlocks.FORGOTTEN_HEARTH.get())) {
            return true;
        }

        Direction forward = hearthState.getValue(ForgottenHearthBlock.FACING);
        Direction side = forward.getClockWise();
        for (RestorationTask task : RestorationTask.values()) {
            if ((hearth.tasks() & task.mask()) != 0) {
                continue;
            }
            BlockPos expectedPos = switch (task) {
                case KETTLE -> hearth.getBlockPos().relative(forward);
                case CROCK -> hearth.getBlockPos().relative(side, 4);
                case QUILT -> hearth.getBlockPos().relative(side, 4).relative(forward, 4);
            };
            BlockState state = level.getBlockState(expectedPos);
            if (!state.is(ModBlocks.RESTORATION_MARKER.get())
                    || state.getValue(RestorationMarkerBlock.TASK) != task) {
                return true;
            }
        }
        return false;
    }

    private static InteractionResult showRestoredStatus(ServerPlayer player) {
        ModAdvancements.grant(player, ModAdvancements.DISCOVERED);
        ModAdvancements.grant(player, ModAdvancements.RELIC);
        ModAdvancements.grant(player, ModAdvancements.REKINDLED);
        ModAdvancements.grant(player, ModAdvancements.RESTORED);
        status(player, "message.forgotten_hearths.restored");
        return InteractionResult.SUCCESS;
    }

    private static void completeRestoration(
            HearthSiteSavedData data,
            ServerLevel level,
            ServerPlayer player,
            ForgottenHearthBlockEntity hearth,
            HearthSiteSavedData.Site site
    ) {
        if (!site.rewardGranted()) {
            site = data.claimReward(hearth.getBlockPos(), hearth.variant());
            hearth.syncFromSite(site);
            ItemStack fragment = new ItemStack(ModItems.RECIPE_FRAGMENT.get());
            if (!player.addItem(fragment)) {
                player.drop(fragment, false);
            }
        }

        ModAdvancements.grant(player, ModAdvancements.DISCOVERED);
        ModAdvancements.grant(player, ModAdvancements.RELIC);
        ModAdvancements.grant(player, ModAdvancements.REKINDLED);
        ModAdvancements.grant(player, ModAdvancements.RESTORED);
        level.playSound(null, hearth.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.75F, 1.15F);
        level.sendParticles(
                ParticleTypes.END_ROD,
                hearth.getBlockPos().getX() + 0.5,
                hearth.getBlockPos().getY() + 1.0,
                hearth.getBlockPos().getZ() + 0.5,
                24,
                1.8,
                0.8,
                1.8,
                0.025
        );
        status(player, "message.forgotten_hearths.restoration_complete");
    }

    private static @org.jspecify.annotations.Nullable ForgottenHearthBlockEntity findNearestHearth(
            ServerLevel level, BlockPos markerPos
    ) {
        ForgottenHearthBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(
                markerPos.offset(-HEARTH_SEARCH_RADIUS, -4, -HEARTH_SEARCH_RADIUS),
                markerPos.offset(HEARTH_SEARCH_RADIUS, 4, HEARTH_SEARCH_RADIUS)
        )) {
            if (level.getBlockEntity(candidate) instanceof ForgottenHearthBlockEntity hearth) {
                double distance = candidate.distSqr(markerPos);
                if (distance < nearestDistance) {
                    nearest = hearth;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    private static Item requiredItem(RestorationTask task) {
        return switch (task) {
            case KETTLE -> ModItems.MENDED_KETTLE.get();
            case CROCK -> ModItems.MENDED_CROCK.get();
            case QUILT -> ModItems.PATCHWORK_CLOTH.get();
        };
    }

    private static BlockState restoredState(RestorationTask task) {
        return switch (task) {
            case KETTLE -> ModBlocks.HEARTH_KETTLE.get().defaultBlockState();
            case CROCK -> ModBlocks.MENDED_CROCK.get().defaultBlockState();
            case QUILT -> ModBlocks.PATCHWORK_QUILT.get().defaultBlockState();
        };
    }

    private static int countMatching(Inventory inventory, Predicate<ItemStack> predicate) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (predicate.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeMatching(Inventory inventory, Predicate<ItemStack> predicate, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!predicate.test(stack)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        inventory.setChanged();
    }

    private static void status(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(key), true);
    }

    private RestorationService() {
    }
}
