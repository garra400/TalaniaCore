package com.talania.core.ui.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Custom HUD overlay for energy shield values.
 */
public final class EnergyShieldHud extends CustomUIHud {
    private float current;
    private float max;

    public EnergyShieldHud(PlayerRef playerRef) {
        super(playerRef);
    }

    public void updateValues(float current, float max) {
        this.current = current;
        this.max = max;
        UICommandBuilder builder = new UICommandBuilder();
        apply(builder);
        update(false, builder);
    }

    @Override
    protected void build(UICommandBuilder commandBuilder) {
        commandBuilder.append("Hud/TalaniaEnergyShieldHud.ui");
        apply(commandBuilder);
    }

    private void apply(UICommandBuilder commandBuilder) {
        boolean visible = max > 0.0f;
        float clampedMax = Math.max(0.0f, max);
        float clampedCurrent = Math.max(0.0f, Math.min(current, clampedMax));
        float percent = clampedMax <= 0.0f ? 0.0f : clampedCurrent / clampedMax;
        commandBuilder.set("#EnergyShieldRoot.Visible", visible);
        commandBuilder.set("#EnergyShieldBar.Value", percent);
    }
}
