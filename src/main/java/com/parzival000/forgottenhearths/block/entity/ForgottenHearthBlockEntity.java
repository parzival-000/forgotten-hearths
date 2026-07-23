package com.parzival000.forgottenhearths.block.entity;

import com.parzival000.forgottenhearths.block.ForgottenHearthBlock;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.config.ForgottenHearthsConfig;
import com.parzival000.forgottenhearths.registry.ModBlockEntities;
import com.parzival000.forgottenhearths.registry.ModEffects;
import com.parzival000.forgottenhearths.world.HearthSiteSavedData;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class ForgottenHearthBlockEntity extends BlockEntity {
    private @Nullable UUID siteId;
    private int variant;
    private HearthStage stage = HearthStage.FORGOTTEN;
    private int tasks;

    public ForgottenHearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGOTTEN_HEARTH.get(), pos, state);
    }

    public int variant() {
        return this.variant;
    }

    public HearthStage stage() {
        return this.stage;
    }

    public int tasks() {
        return this.tasks;
    }

    public void syncFromSite(HearthSiteSavedData.Site site) {
        this.siteId = site.id();
        this.variant = site.variant();
        this.stage = site.hearthStage();
        this.tasks = site.tasks();
        this.setChanged();

        if (this.level instanceof ServerLevel serverLevel) {
            BlockState state = this.getBlockState();
            BlockState updatedState = state.hasProperty(ForgottenHearthBlock.STAGE)
                    ? state.setValue(ForgottenHearthBlock.STAGE, this.stage)
                    : state;
            if (updatedState != state) {
                serverLevel.setBlock(this.worldPosition, updatedState, Block.UPDATE_ALL);
            } else {
                serverLevel.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgottenHearthBlockEntity hearth) {
        if (!(level instanceof ServerLevel serverLevel)
                || hearth.stage != HearthStage.RESTORED
                || !ForgottenHearthsConfig.COMFORT_ENABLED.getAsBoolean()) {
            return;
        }

        int refresh = ForgottenHearthsConfig.COMFORT_REFRESH_TICKS.getAsInt();
        if (Math.floorMod(serverLevel.getGameTime() + pos.asLong(), refresh) != 0) {
            return;
        }

        double radius = ForgottenHearthsConfig.COMFORT_RADIUS.getAsDouble();
        int duration = refresh + 30;
        AABB area = new AABB(pos).inflate(radius);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.addEffect(new MobEffectInstance(ModEffects.COMFORTED, duration, 0, true, true, true));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.siteId = input.read("site_id", UUIDUtil.CODEC).orElse(null);
        this.variant = input.getIntOr("variant", 0);
        this.stage = HearthStage.byId(input.getIntOr("stage", 0));
        this.tasks = input.getIntOr("tasks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.siteId != null) {
            output.store("site_id", UUIDUtil.CODEC, this.siteId);
        }
        output.putInt("variant", this.variant);
        output.putInt("stage", this.stage.id());
        output.putInt("tasks", this.tasks);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
