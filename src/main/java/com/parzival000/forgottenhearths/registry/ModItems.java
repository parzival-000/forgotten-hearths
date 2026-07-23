package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForgottenHearths.MOD_ID);

    public static final DeferredItem<Item> BLACKENED_KETTLE = flavor(
            "blackened_kettle", 1, Rarity.UNCOMMON, true
    );
    public static final DeferredItem<Item> MENDED_KETTLE = flavor(
            "mended_kettle", 1, Rarity.UNCOMMON, true
    );
    public static final DeferredItem<Item> CRACKED_CROCK = flavor(
            "cracked_crock", 8, Rarity.COMMON, true
    );
    public static final DeferredItem<Item> MENDED_CROCK = flavor(
            "mended_crock", 8, Rarity.UNCOMMON, true
    );
    public static final DeferredItem<Item> PATCHWORK_CLOTH = flavor(
            "patchwork_cloth", 16, Rarity.COMMON, true
    );
    public static final DeferredItem<Item> RECIPE_FRAGMENT = flavor(
            "recipe_fragment", 16, Rarity.UNCOMMON, false
    );

    public static final DeferredItem<Item> HEARTH_PORRIDGE = ITEMS.registerSimpleItem(
            "hearth_porridge",
            properties -> properties.stacksTo(1)
                    .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build())
                    .usingConvertsTo(Items.BOWL)
    );
    public static final DeferredItem<Item> ROOT_STEW = ITEMS.registerSimpleItem(
            "root_stew",
            properties -> properties.stacksTo(1)
                    .food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build())
                    .usingConvertsTo(Items.BOWL)
    );
    public static final DeferredItem<Item> HONEYED_OATCAKE = ITEMS.registerSimpleItem(
            "honeyed_oatcake",
            properties -> properties.food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())
    );

    public static final DeferredItem<BlockItem> HEARTH_KETTLE_BLOCK = ITEMS.registerSimpleBlockItem(
            "hearth_kettle", ModBlocks.HEARTH_KETTLE
    );
    public static final DeferredItem<BlockItem> MENDED_CROCK_BLOCK = ITEMS.registerSimpleBlockItem(
            "mended_crock_block", ModBlocks.MENDED_CROCK
    );
    public static final DeferredItem<BlockItem> PATCHWORK_QUILT_BLOCK = ITEMS.registerSimpleBlockItem(
            "patchwork_quilt", ModBlocks.PATCHWORK_QUILT
    );

    private static DeferredItem<Item> flavor(String name, int stackSize, Rarity rarity, boolean guided) {
        String tooltipKey = "item.forgotten_hearths." + name + ".tooltip";
        List<Component> lore = List.of(
                Component.translatable(tooltipKey).withStyle(style -> style.withColor(0x8E8173))
        );
        return ITEMS.registerSimpleItem(name, properties -> properties
                .stacksTo(stackSize)
                .rarity(rarity)
                .component(DataComponents.LORE, new ItemLore(lore)));
    }

    private ModItems() {
    }
}

