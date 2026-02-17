package com.talania.core.debug;

import java.util.EnumSet;
import java.util.Set;

/**
 * Global settings for Talania debug tooling.
 */
public final class DebugSettings {
    public boolean enableChatOutput = true;
    public boolean enableUiOutput = true;
    public boolean logToConsole = false;
    public int rateLimitMs = 0;
    public int combatLogMaxEntries = 200;
    public Set<DebugCategory> defaultEnabledCategories = EnumSet.noneOf(DebugCategory.class);
}
