package com.talania.core.profile.api;

/**
 * Snapshot of stored class progression.
 */
public record ClassProgressInfo(
        int level,
        long xp
) {
}
