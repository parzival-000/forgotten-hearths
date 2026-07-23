package com.parzival000.forgottenhearths.gametest;

import com.parzival000.forgottenhearths.ForgottenHearths;
import com.parzival000.forgottenhearths.block.HearthStage;
import com.parzival000.forgottenhearths.block.RestorationTask;
import com.parzival000.forgottenhearths.world.HearthSiteSavedData;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class PersistenceValidation {
    public static final String PROPERTY = "forgottenhearths.persistenceProbe";
    public static final BlockPos PROBE_POS = new BlockPos(1_234_567, 80, -1_234_567);

    public static void onServerStarted(ServerStartedEvent event) {
        String mode = System.getProperty(PROPERTY, "");
        if (mode.isEmpty()) {
            return;
        }

        HearthSiteSavedData data = HearthSiteSavedData.get(event.getServer().overworld());
        HearthSiteSavedData.Site site = data.getOrCreate(PROBE_POS, 2);
        switch (mode) {
            case "seed" -> {
                if (site.hearthStage() == HearthStage.FORGOTTEN) {
                    site = seedCompletedSite(data);
                }
                requireCompleted(site);
                ForgottenHearths.LOGGER.info("Dedicated persistence probe seeded completed site {}", site.id());
            }
            case "verify" -> {
                requireCompleted(site);
                ForgottenHearths.LOGGER.info("Dedicated persistence probe reloaded completed site {}", site.id());
            }
            default -> throw new IllegalArgumentException("Unknown Forgotten Hearths persistence probe mode: " + mode);
        }
        event.getServer().halt(false);
    }

    public static HearthSiteSavedData.Site seedCompletedSite(HearthSiteSavedData data) {
        data.setStage(PROBE_POS, 2, HearthStage.FORGOTTEN, HearthStage.DISCOVERED);
        data.setStage(PROBE_POS, 2, HearthStage.DISCOVERED, HearthStage.REKINDLED);
        data.completeTask(PROBE_POS, 2, RestorationTask.KETTLE);
        data.completeTask(PROBE_POS, 2, RestorationTask.CROCK);
        data.completeTask(PROBE_POS, 2, RestorationTask.QUILT);
        return data.claimReward(PROBE_POS, 2);
    }

    public static void requireCompleted(HearthSiteSavedData.Site site) {
        if (site.hearthStage() != HearthStage.RESTORED
                || site.tasks() != RestorationTask.ALL_MASK
                || !site.rewardGranted()
                || site.variant() != 2) {
            throw new IllegalStateException("Forgotten Hearths persistence probe did not reload its completed state");
        }
    }

    private PersistenceValidation() {
    }
}
