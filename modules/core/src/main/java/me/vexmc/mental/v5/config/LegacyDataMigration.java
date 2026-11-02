package me.vexmc.mental.v5.config;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * One-time carry-over of an upstream Mental installation's data folder.
 *
 * <p>Battle Motion keeps its files in {@code plugins/BattleMotion/}. A server that
 * previously ran Mental has its configuration in {@code plugins/Mental/}; on the
 * first boot with no Battle Motion configuration yet, that folder is COPIED (never
 * moved or deleted — the old folder stays untouched as a backup) so the admin keeps
 * their profiles, presets and settings. Once {@code plugins/BattleMotion/config.yml}
 * exists this is a no-op, so it can never overwrite anything.</p>
 */
public final class LegacyDataMigration {

    /** The data-folder name upstream Mental used. */
    public static final String LEGACY_FOLDER = "Mental";

    private LegacyDataMigration() {}

    /**
     * Copies {@code <plugins>/Mental} into {@code dataDir} when {@code dataDir} holds no
     * {@code config.yml} yet and the legacy folder exists.
     *
     * @return {@code true} when files were copied
     */
    public static boolean run(@NotNull Path dataDir, @NotNull Consumer<String> log) {
        Path pluginsDir = dataDir.toAbsolutePath().getParent();
        if (pluginsDir == null) {
            return false;
        }
        Path legacy = pluginsDir.resolve(LEGACY_FOLDER);
        if (legacy.equals(dataDir.toAbsolutePath()) || !Files.isDirectory(legacy)) {
            return false;
        }
        if (Files.exists(dataDir.resolve("config.yml"))) {
            return false;
        }
        try {
            copyTree(legacy, dataDir);
            log.accept("Copied the existing Mental configuration from " + legacy.getFileName()
                    + "/ to " + dataDir.getFileName() + "/ (the old folder was left untouched).");
            return true;
        } catch (IOException e) {
            log.accept("Could not copy the Mental configuration from " + legacy.getFileName()
                    + "/ (" + e.getMessage() + ") — starting with fresh defaults.");
            return false;
        }
    }

    private static void copyTree(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(to.resolve(from.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
