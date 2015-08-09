package com.github.jrubygradle.groovygem.internal

import org.apache.commons.io.IOUtils
import org.apache.commons.vfs2.FileNotFolderException
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic

import com.github.jrubygradle.groovygem.Gem
import com.github.jrubygradle.groovygem.GemInstaller.DuplicateBehavior

import java.nio.file.Files

@CompileStatic
class GemInstaller {
    static final List<String> GEM_HOME_DIRS = ['bin', 'build_info', 'cache', 'doc',
                                        'extensions', 'gems', 'specifications']

    protected Logger logger = LoggerFactory.getLogger(this.class)
    protected File installDirectory
    protected List<File> gems
    protected FileSystemManager fileSystemManager

    GemInstaller(String installDir, List<File> gems) {
        this.installDirectory = new File(installDir)
        this.gems = gems
        this.fileSystemManager = VFS.manager
    }

    /** Install and overwrite anything that stands in the way */
    void install() {
        install(DuplicateBehavior.OVERWRITE)
    }

    void install(DuplicateBehavior onDuplicateBehavior) {
        if (!mkdirs()) {
            /* raise some exception? */
        }

        gems.each { File gem ->
            installGem(installDirectory, gem, onDuplicateBehavior)
        }
    }

    boolean installGem(File installDir, File gem, DuplicateBehavior onDuplicate) {
        /* TODO: isValidGem? */
        cacheGemInInstallDir(installDir, gem)

        FileObject gemTar = fileSystemManager.resolveFile("tar:${gem}")
        FileObject tempTar = fileSystemManager.resolveFile("tmp://data.tar.gz")
        /* http://wiki.apache.org/commons/ExtractAndDecompressGzipFiles */
        FileObject metadata  = fileSystemManager.resolveFile("gz:tar:${gem}!/metadata.gz!metadata")

        Gem gemMetadata = Gem.fromFile(metadata.content.inputStream)
        logger.info("We've processed metadata for ${gemMetadata.name} at version ${gemMetadata.version}")

        long size = gemTar.getChild('data.tar.gz').content.write(tempTar)
        logger.info("Extracted data.tar.gz from ${gem} (${size} bytes)")

        FileObject dataTar = fileSystemManager.resolveFile("tgz:${tempTar}")
        logger.info("The contents of our data.tar.gz: ${dataTar.children}")

        extractSpecification(installDir, dataTar, gemMetadata)

        //FileObject metadata = fileSystemManager.resolveFile("gz:tar:${gem.absolutePath}!/metadata.gz!metadata")
        //StringWriter writer = new StringWriter()
        //IOUtils.copy(metadata.content.inputStream, writer, null)

        return true
    }

    /**
     * Create the requisite directories to map a GEM_HOME structure, namely:
     *   bin/
     *   build_info/
     *   cache/
     *   doc/
     *   extensions/
     *   gems/
     *   specifications/
     *
     * @return True if all directories were created successfully
     */
    boolean mkdirs() {
        boolean success = true
        GEM_HOME_DIRS.each { String dirName ->
            File newDir = new File(installDirectory, dirName)
            logger.info("Attempting to create: ${newDir.absolutePath}")

            if (!newDir.mkdirs()) {
                logger.error("Failed to make ${newDir.absolutePath}, bailing on mkdirs()")
                return false
            }
        }
        return success
    }

    /**
     * Primarily meant to be an internal method which will determine whether the
     * given {@code java.io.File} is a valid gem archive or not. This includes looking
     * inside it to see that it is a legitimate tar file
     *
     * @param gemFile Fully formed {@code java.io.File} object referencing a gem
     * @return true if the file does in fact walk and talk like a gem
     */
    boolean isValidGem(File gemFile) {
        logger.info("Validating gem ${gemFile}")

        /* If it doesn't end with gem, let's not even consider it a gem file */
        if (!gemFile.absolutePath.endsWith('.gem')) {
            return false
        }

        FileObject fo = fileSystemManager.resolveFile("tar:${gemFile}")

        try {
            return fo.children.size() > 0
        }
        catch (FileNotFolderException ex) {
            /*
             * if we've received this exception its because the gem file, aka
             * tar file isn't actually legit
             */
            return false
        }
    }

    /** Cache the gem in GEM_HOME/cache */
    protected void cacheGemInInstallDir(File installDir, File gem) {
        File cacheDir = new File(installDir, 'cache')
        Files.copy(gem.toPath(), (new File(cacheDir, gem.name)).toPath())
    }

    /** Extract the gemspec file from the dataTarGz provided into the ${installDir}/specifications */
    protected void extractSpecification(File installDir, FileObject dataTarGz, Gem gem) {
        FileObject gemspec = dataTarGz.getChild("${gem.name}.gemspec")
        String outputFile = "${gem.name}-${gem.version}.gemspec"

        File specification = new File(installDir, ['specifications', outputFile].join(File.separator))

        IOUtils.copy(gemspec.content.inputStream, specification.newOutputStream())
    }
}