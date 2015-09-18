package com.github.jrubygradle.jem;

import java.io.File;

/**
 * Interface defining the appropriate callback even to hook into the
 * {@code GemInstaller}
 */
public interface GemInstallEvent {
    /**
     * @param result Instance of {@code GemInstallResult}
     * @return true if the installation should continue
     */
    public boolean onInstall(GemInstallResult result);
}
