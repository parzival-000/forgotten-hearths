package com.parzival000.forgottenhearths.block;

import com.mojang.serialization.MapCodec;
import com.parzival000.forgottenhearths.gameplay.RestorationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RestorationMarkerBlock extends Block {
    public static final MapCodec<RestorationMarkerBlock> CODEC = simpleCodec(RestorationMarkerBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<RestorationTask> TASK = EnumProperty.create("task", RestorationTask.class);
    private static final VoxelShape KETTLE_SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 3, 13),
            Block.box(5, 3, 5, 11, 8, 11)
    );
    private static final VoxelShape CROCK_SHAPE = Block.box(3, 0, 3, 13, 8, 13);
    private static final VoxelShape QUILT_SHAPE = Block.box(1, 0, 1, 15, 3, 15);

    public RestorationMarkerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TASK, RestorationTask.KETTLE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(TASK)) {
            case KETTLE -> KETTLE_SHAPE;
            case CROCK -> CROCK_SHAPE;
            case QUILT -> QUILT_SHAPE;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult
    ) {
        return RestorationService.useMarker(level, pos, player, ItemStack.EMPTY, state.getValue(TASK));
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        return RestorationService.useMarker(level, pos, player, stack, state.getValue(TASK));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TASK);
    }
}
