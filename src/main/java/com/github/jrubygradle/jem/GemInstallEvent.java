package com.github.jrubygradle.jem;

import java.io.File;

/**
 * Interface defining the appropriate callback even to hook into the
 * {@code GemInstaller}
 */
public interface GemInstallEvent {
    public void onSuccess(Gem gem, File installationDir);
    public void onFailure(Gem gem, File installationDir);
}
