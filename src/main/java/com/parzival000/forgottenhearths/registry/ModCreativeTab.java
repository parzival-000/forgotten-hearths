package com.parzival000.forgottenhearths.registry;

import com.parzival000.forgottenhearths.ForgottenHearths;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, ForgottenHearths.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "forgotten_hearths",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.forgotten_hearths"))
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .icon(() -> ModItems.BLACKENED_KETTLE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BLACKENED_KETTLE.get());
                        output.accept(ModItems.MENDED_KETTLE.get());
                        output.accept(ModItems.CRACKED_CROCK.get());
                        output.accept(ModItems.MENDED_CROCK.get());
                        output.accept(ModItems.PATCHWORK_CLOTH.get());
                        output.accept(ModItems.RECIPE_FRAGMENT.get());
                        output.accept(ModItems.HEARTH_PORRIDGE.get());
                        output.accept(ModItems.ROOT_STEW.get());
                        output.accept(ModItems.HONEYED_OATCAKE.get());
                    })
                    .build()
    );

    private ModCreativeTab() {
    }
}
