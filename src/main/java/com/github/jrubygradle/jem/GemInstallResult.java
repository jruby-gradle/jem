package com.github.jrubygradle.jem;

import java.io.File;

/**
 * Plain-old-Java-object containing properties to tell the consumer something about
 * an attempted (or successful) gem installation
 */
public class GemInstallResult {
    public static enum Type { SUCCESS, FAILURE };

    protected Type resultType;
    protected Gem gem;
    protected File gemFile;
    protected File installationDir;
    protected Exception exception;

    public GemInstallResult(Gem gem,
                            File gemFile,
                            File installDir,
                            Exception exception) {
        this.gem = gem;
        this.gemFile = gemFile;
        this.installationDir = installDir;
        this.exception = exception;
        this.resultType = Type.FAILURE;

        if (exception == null) {
            this.resultType = Type.SUCCESS;
        }
    }

    /**
     * @return Metadata about the gem, null if the metadata could not be passed
     */
    public Gem getGem() {
        return gem;
    }

    /**
     * @return In the case of a failed installation, this contains a caught exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return File object for the .gem file which was used for the install
     */
    public File getGemFile() {
        return gemFile;
    }

    /**
     * @return File object for the intallation dir used for the install
     */
    public File getInstallationDir() {
        return installationDir;
    }

    public Type getType() {
        return resultType;
    }
}
