package com.github.jrubygradle.jem.internal

import org.jboss.shrinkwrap.api.ArchiveFormat
import org.jboss.shrinkwrap.api.ArchivePath
import org.jboss.shrinkwrap.api.GenericArchive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.Node
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter
import org.jboss.shrinkwrap.api.importer.TarImporter
import org.jboss.shrinkwrap.impl.base.io.IOUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic

import com.github.jrubygradle.jem.Gem
import com.github.jrubygradle.jem.GemInstaller.DuplicateBehavior

import java.nio.file.Files
import java.util.zip.GZIPInputStream

@CompileStatic
class GemInstaller {
    static final List<String> GEM_HOME_DIRS = ['bin', 'build_info', 'cache', 'doc',
                                        'extensions', 'gems', 'specifications']

    protected Logger logger = LoggerFactory.getLogger(this.class)
    protected File installDirectory
    protected List<File> gems

    GemInstaller(String installDir, List<File> gems) {
        this.installDirectory = new File(installDir)
        this.gems = gems
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

        GenericArchive gemArchive = ShrinkWrap.create(TarImporter).importFrom(gem).as(GenericArchive)
        Node metadata = gemArchive.get('metadata.gz')
        GenericArchive dataArchive = gemArchive.getAsType(GenericArchive.class,
                                                        "data.tar.gz",
                                                        ArchiveFormat.TAR_GZ);

        Gem gemMetadata = Gem.fromFile(new GZIPInputStream(metadata.asset.openStream()))
        logger.info("We've processed metadata for ${gemMetadata.name} at version ${gemMetadata.version}")

        extractSpecification(installDir, gemMetadata)
        extractData(installDir, dataArchive, gemMetadata)
        extractExecutables(installDir, dataArchive, gemMetadata)

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

        return false
    }

    String gemFullName(Gem gem) {
        String fullName = "${gem.name}-${gem.version.version}"

        if ((gem.platform) && (gem.platform != 'ruby')) {
            fullName = "${fullName}-${gem.platform}"
        }

        return fullName
    }

    /** Cache the gem in GEM_HOME/cache */
    protected void cacheGemInInstallDir(File installDir, File gem) {
        File cacheDir = new File(installDir, 'cache')
        Files.copy(gem.toPath(), (new File(cacheDir, gem.name)).toPath())
    }

    /** Extract the gemspec file from the {@code Gem} provided into the ${installDir}/specifications */
    protected void extractSpecification(File installDir, Gem gem) {
        String outputFileName = "${gemFullName(gem)}.gemspec"
        File outputFile = new File(installDir, ['specifications', outputFileName].join(File.separator))

        PrintWriter writer = new PrintWriter(outputFile.newOutputStream())
        writer.write(gem.toRuby())
        writer.flush()
    }

    /** Extract the data.tar.gz contents into gems/full-name/* */
    protected void extractData(File installDir, GenericArchive dataTarGz, Gem gem) {
        File outputDir = new File(installDir, 'gems')
        outputDir.mkdirs()

        dataTarGz.as(ExplodedExporter.class).exportExploded(outputDir, gemFullName(gem))
    }

    /** Extract the executables from the specified bindir */
    protected void extractExecutables(File installDir, GenericArchive dataTarGz, Gem gem) {
        /*
         * default to 'bin' if the bindir isn't otherwise set, it's not clear whether
         * it is always guaranteed to be set or not though
         */
        String binDir = gem.bindir ?: 'bin'
        File bin = new File(installDir, binDir)
        List<String> execs = gem.executables.collect { String ex -> [binDir, ex].join(File.separator) }

        bin.mkdirs()
        dataTarGz.content.each { ArchivePath path, Node node ->
            execs.each { String exec ->
                if (path.get().matches(/.*${exec}/)) {
                    IOUtil.copy(node.asset.openStream(), (new File(installDir, exec)).newOutputStream())
                }
            }
        }
    }
}
