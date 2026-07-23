package com.parzival000.forgottenhearths.gametest;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.ForgottenHearthBlock;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.block.RestorationMarkerBlock;
import com.parzival000.forgottenhearths.block.RestorationTask;
import com.parzival000.forgottenhearths.gameplay.RestorationService;
import com.parzival000.forgottenhearths.registry.ModBlocks;
import com.parzival000.forgottenhearths.registry.ModItems;
import com.parzival000.forgottenhearths.world.HearthSiteSavedData;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(
            Registries.TEST_FUNCTION, ForgottenHearths.MOD_ID
    );

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MARKER_SAFETY =
            TEST_FUNCTIONS.register("marker_safety", () -> ModGameTests::markerSafety);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RESTORATION_LOOP =
            TEST_FUNCTIONS.register("restoration_loop", () -> ModGameTests::restorationLoop);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRUCTURE_TEMPLATES =
            TEST_FUNCTIONS.register("structure_templates", () -> ModGameTests::structureTemplates);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PERSISTENCE_PROBE =
            TEST_FUNCTIONS.register("persistence_probe", () -> ModGameTests::persistenceProbe);

    @SuppressWarnings("removal")
    private static void markerSafety(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(1, 1, 1);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos, Blocks.STONE);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack kettle = new ItemStack(ModItems.MENDED_KETTLE.get());
        RestorationService.useMarker(helper.getLevel(), absolutePos, player, kettle, RestorationTask.KETTLE);

        helper.assertBlockPresent(Blocks.STONE, relativePos);
        if (kettle.getCount() != 1) {
            helper.fail("A restoration item was consumed at an unrelated block");
        }
        helper.succeed();
    }

    @SuppressWarnings("removal")
    private static void restorationLoop(GameTestHelper helper) {
        BlockPos hearthPos = new BlockPos(2, 1, 2);
        BlockPos kettlePos = new BlockPos(3, 1, 2);
        BlockPos crockPos = new BlockPos(2, 1, 3);
        BlockPos quiltPos = new BlockPos(3, 1, 3);
        helper.setBlock(hearthPos, ModBlocks.FORGOTTEN_HEARTH.get());
        helper.setBlock(kettlePos, marker(RestorationTask.KETTLE));
        helper.setBlock(crockPos, marker(RestorationTask.CROCK));
        helper.setBlock(quiltPos, marker(RestorationTask.QUILT));

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        RestorationService.useHearth(
                helper.getLevel(), helper.absolutePos(hearthPos), player, InteractionHand.MAIN_HAND, ItemStack.EMPTY
        );

        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel);
        player.getInventory().add(new ItemStack(Items.OAK_LOG, 4));
        player.getInventory().add(new ItemStack(Items.BRICK, 4));
        RestorationService.useHearth(
                helper.getLevel(), helper.absolutePos(hearthPos), player, InteractionHand.MAIN_HAND, flintAndSteel
        );

        restoreMarker(helper, player, kettlePos, ModItems.MENDED_KETTLE.get().getDefaultInstance(), RestorationTask.KETTLE);
        restoreMarker(helper, player, crockPos, ModItems.MENDED_CROCK.get().getDefaultInstance(), RestorationTask.CROCK);
        restoreMarker(helper, player, quiltPos, ModItems.PATCHWORK_CLOTH.get().getDefaultInstance(), RestorationTask.QUILT);

        helper.assertBlockProperty(hearthPos, ForgottenHearthBlock.STAGE, HearthStage.RESTORED);
        helper.assertBlockPresent(ModBlocks.HEARTH_KETTLE.get(), kettlePos);
        helper.assertBlockPresent(ModBlocks.MENDED_CROCK.get(), crockPos);
        helper.assertBlockPresent(ModBlocks.PATCHWORK_QUILT.get(), quiltPos);
        HearthSiteSavedData.Site site = HearthSiteSavedData.get(helper.getLevel())
                .getOrCreate(helper.absolutePos(hearthPos), 0);
        if (!site.rewardGranted() || site.tasks() != RestorationTask.ALL_MASK) {
            helper.fail("The restored hearth did not persist its completed state");
        }
        if (countItem(player.getInventory(), ModItems.RECIPE_FRAGMENT.get()) != 1) {
            helper.fail("The one-time recipe fragment reward was not granted");
        }

        RestorationService.useHearth(
                helper.getLevel(), helper.absolutePos(hearthPos), player, InteractionHand.MAIN_HAND, ItemStack.EMPTY
        );
        if (countItem(player.getInventory(), ModItems.RECIPE_FRAGMENT.get()) != 1) {
            helper.fail("Repeated hearth interaction duplicated the completion reward");
        }

        ServerPlayer secondPlayer = helper.makeMockServerPlayerInLevel();
        ItemStack secondCloth = ModItems.PATCHWORK_CLOTH.get().getDefaultInstance();
        RestorationService.useMarker(
                helper.getLevel(), helper.absolutePos(quiltPos), secondPlayer, secondCloth, RestorationTask.QUILT
        );
        if (secondCloth.getCount() != 1
                || countItem(secondPlayer.getInventory(), ModItems.RECIPE_FRAGMENT.get()) != 0) {
            helper.fail("A repeated multiplayer interaction consumed an item or duplicated the reward");
        }
        helper.succeed();
    }

    private static void restoreMarker(
            GameTestHelper helper,
            ServerPlayer player,
            BlockPos relativePos,
            ItemStack item,
            RestorationTask task
    ) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        RestorationService.useMarker(helper.getLevel(), helper.absolutePos(relativePos), player, item, task);
    }

    private static void structureTemplates(GameTestHelper helper) {
        assertTemplate(helper, "homestead/woodland_cottage");
        assertTemplate(helper, "homestead/plains_farmstead");
        assertTemplate(helper, "homestead/taiga_shelter");
        helper.succeed();
    }

    private static void persistenceProbe(GameTestHelper helper) {
        BlockPos probePos = new BlockPos(1_234_567, 80, -1_234_567);
        HearthSiteSavedData data = HearthSiteSavedData.get(helper.getLevel());
        HearthSiteSavedData.Site site = data.getOrCreate(probePos, 2);
        if (site.hearthStage() == HearthStage.RESTORED) {
            if (site.tasks() != RestorationTask.ALL_MASK || !site.rewardGranted() || site.variant() != 2) {
                helper.fail("The reloaded persistence probe was incomplete");
            }
            ForgottenHearths.LOGGER.info("Persistence probe reloaded completed site {}", site.id());
            helper.succeed();
            return;
        }
        if (site.hearthStage() != HearthStage.FORGOTTEN || site.tasks() != 0 || site.rewardGranted()) {
            helper.fail("The persistence probe loaded an unexpected partial state");
        }

        data.setStage(probePos, 2, HearthStage.FORGOTTEN, HearthStage.DISCOVERED);
        data.setStage(probePos, 2, HearthStage.DISCOVERED, HearthStage.REKINDLED);
        data.completeTask(probePos, 2, RestorationTask.KETTLE);
        data.completeTask(probePos, 2, RestorationTask.CROCK);
        site = data.completeTask(probePos, 2, RestorationTask.QUILT);
        site = data.claimReward(probePos, 2);
        if (site.hearthStage() != HearthStage.RESTORED
                || site.tasks() != RestorationTask.ALL_MASK
                || !site.rewardGranted()) {
            helper.fail("The persistence probe could not seed a completed state");
        }
        ForgottenHearths.LOGGER.info("Persistence probe seeded completed site {}", site.id());
        helper.succeed();
    }

    private static void assertTemplate(GameTestHelper helper, String path) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(ForgottenHearths.id(path))
                .orElseThrow(() -> helper.assertionException(
                        net.minecraft.network.chat.Component.literal("Missing structure template: " + path)
                ));
        if (template.getSize().getX() < 1 || template.getSize().getY() < 1 || template.getSize().getZ() < 1) {
            helper.fail("Structure template has no usable volume: " + path);
        }
    }

    private static int countItem(Inventory inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static net.minecraft.world.level.block.state.BlockState marker(RestorationTask task) {
        return ModBlocks.RESTORATION_MARKER.get().defaultBlockState()
                .setValue(RestorationMarkerBlock.TASK, task);
    }

    private ModGameTests() {
    }
}
