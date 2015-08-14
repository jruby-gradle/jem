package com.github.jrubygradle.jem.internal;

import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.importer.TarImporter;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrubygradle.jem.Gem;
import com.github.jrubygradle.jem.GemInstaller.DuplicateBehavior;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class GemInstaller {
    public static final String[] GEM_HOME_DIRS = {"bin", "build_info", "cache", "doc",
            "extensions", "gems", "specifications"};

    protected Logger logger = LoggerFactory.getLogger(GemInstaller.class);
    protected File installDirectory;
    protected List<File> gems;

    public GemInstaller(String installDir, List<File> gems) {
        this.installDirectory = new File(installDir);
        this.gems = gems;
    }

    /** Install and overwrite anything that stands in the way */
    public void install() {
        install(DuplicateBehavior.OVERWRITE);
    }

    public void install(DuplicateBehavior onDuplicateBehavior) {
        if (!mkdirs()) {
            /* raise some exception? */
        }

        for (File gem : this.gems) {
            installGem(installDirectory, gem, onDuplicateBehavior);
        }
    }

    public boolean installGem(File installDir, File gem, DuplicateBehavior onDuplicate) {
        /* TODO: isValidGem? */
        try {
            cacheGemInInstallDir(installDir, gem);
        }
        catch (IOException ex) {
            logger.error("Failed to cache our gem in %s", installDir, ex);
            return false;
        }

        Gem gemMetadata;
        GenericArchive gemArchive = ShrinkWrap.create(TarImporter.class)
                                        .importFrom(gem).as(GenericArchive.class);
        Node metadata = gemArchive.get("metadata.gz");
        GenericArchive dataArchive = gemArchive.getAsType(GenericArchive.class,
                "data.tar.gz",
                ArchiveFormat.TAR_GZ);

        try {
            gemMetadata = Gem.fromFile(new GZIPInputStream(metadata.getAsset().openStream()));
        }
        catch (IOException ex) {
            logger.error("Failed to process the metadata", ex);
            return false;
        }
        logger.info(String.format("We've processed metadata for %s at version %s",
                gemMetadata.name, gemMetadata.version.version));

        try {
            extractSpecification(installDir, gemMetadata);
        }
        catch (Exception ex) {
            logger.error(String.format("Could not extract the gem specification for %s into %s",
                    gemMetadata.name, installDir), ex);
        }

        extractData(installDir, dataArchive, gemMetadata);

        try {
            extractExecutables(installDir, dataArchive, gemMetadata);
        }
        catch (Exception ex) {
            logger.error(String.format("Could not extract the gem executables for %s into %s",
                    gemMetadata.name, installDir), ex);
        }

        return true;
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
    public boolean mkdirs() {
        boolean success = true;

        for (String dirName : GEM_HOME_DIRS) {
            File newDir = new File(installDirectory, dirName);
            logger.info(String.format("Attempting to create: %s", newDir.getAbsolutePath()));

            if (!newDir.mkdirs()) {
                logger.error(String.format("Failed to make %s, bailing on mkdirs()", newDir.getAbsolutePath()));
                return false;
            }
        }
        return success;
    }

    /**
     * Primarily meant to be an internal method which will determine whether the
     * given {@code java.io.File} is a valid gem archive or not. This includes looking
     * inside it to see that it is a legitimate tar file
     *
     * @param gemFile Fully formed {@code java.io.File} object referencing a gem
     * @return true if the file does in fact walk and talk like a gem
     */
    public boolean isValidGem(File gemFile) {
        logger.info(String.format("Validating gem %s", gemFile));

        /* If it doesn"t end with gem, let"s not even consider it a gem file */
        if (!gemFile.getAbsolutePath().endsWith(".gem")) {
            return false;
        }

        return false;
    }

    public String gemFullName(Gem gem) {
        String fullName = String.format("%s-%s", gem.name, gem.version.version);

        if ((gem.platform instanceof String) && !(gem.platform.equals("ruby"))) {
            fullName = String.format("%s-%s", fullName, gem.platform);
        }

        return fullName;
    }

    /** Cache the gem in GEM_HOME/cache */
    protected void cacheGemInInstallDir(File installDir, File gem) throws IOException {
        File cacheDir = new File(installDir, "cache");
        Files.copy(gem.toPath(), (new File(cacheDir, gem.getName())).toPath());
    }

    /** Extract the gemspec file from the {@code Gem} provided into the ${installDir}/specifications */
    protected void extractSpecification(File installDir, Gem gem) throws Exception {
        String outputFileName = String.format("%s.gemspec", gemFullName(gem));
        File specDir = new File(installDir, "specifications");
        FileOutputStream output = new FileOutputStream(new File(specDir, outputFileName));

        PrintWriter writer = new PrintWriter(output);
        writer.write(gem.toRuby());
        writer.flush();
    }

    /** Extract the data.tar.gz contents into gems/full-name/* */
    protected void extractData(File installDir, GenericArchive dataTarGz, Gem gem) {
        File outputDir = new File(installDir, "gems");
        outputDir.mkdirs();

        dataTarGz.as(ExplodedExporter.class).exportExploded(outputDir, gemFullName(gem));
    }

    /** Extract the executables from the specified bindir */
    protected void extractExecutables(File installDir, GenericArchive dataTarGz, Gem gem) throws Exception {
        /*
         * default to "bin" if the bindir isn"t otherwise set, it"s not clear whether
         * it is always guaranteed to be set or not though
         */
        String binDir = gem.bindir;

        if (!(binDir instanceof String)) {
            binDir = "bin";
        }
        File bin = new File(installDir, binDir);
        bin.mkdirs();
        Pattern p = Pattern.compile("/" + binDir + "/(.*)?");

        for (Map.Entry<ArchivePath, Node> entry : dataTarGz.getContent().entrySet()) {
            ArchivePath path = entry.getKey();
            Node node = entry.getValue();
            Matcher m = p.matcher(path.get());

            if (m.matches()) {
                File fullOutputPath = new File(bin, m.toMatchResult().group(1));
                fullOutputPath.createNewFile();
                IOUtil.copy(node.getAsset().openStream(),
                        new FileOutputStream(fullOutputPath));
            }
        }
    }
}
