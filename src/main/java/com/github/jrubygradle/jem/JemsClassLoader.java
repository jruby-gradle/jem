package com.github.jrubygradle.jem;

import com.github.jrubygradle.jem.internal.GemInstaller;
import com.github.jrubygradle.jem.GemInstaller.DuplicateBehavior;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Collections;

/**
 * Created by cmeier on 8/15/15.
 */
public class JemsClassLoader extends URLClassLoader {

    private final File jemsDir;

    public static JemsClassLoader create() throws IOException {
        return new JemsClassLoader(Files.createTempDirectory("jems").toFile());
    }

    public JemsClassLoader(File jemsDir) throws IOException {
        super(new URL[] { jemsDir.toURI().toURL() });
        this.jemsDir = jemsDir;
    }

    public Gem addJem(String jem) throws IOException {
        return addJem(new File(jem));
    }
    public Gem addJem(File jem) throws IOException {
        GemInstaller installer = new GemInstaller(jemsDir.getAbsolutePath(), Collections.<File>emptyList());
        installer.mkdirs();
        Gem gemspec = installer.installGem(jemsDir, jem, DuplicateBehavior.OVERWRITE);
        String dir = "gems/" + installer.gemFullName(gemspec);
        createDirInfoFile(dir);
        for(String path: gemspec.requirePaths){
            createDirInfoFile(dir + "/" + path);
        }
        return gemspec;
    }

    private void createDirInfoFile(String dir) throws IOException {
        try (FileWriter out = new FileWriter(new File(jemsDir, dir + "/.jrubydir"))){
            out.append(".");
        }
    }
}
