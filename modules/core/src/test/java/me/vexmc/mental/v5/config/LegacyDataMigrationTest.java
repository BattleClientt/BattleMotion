package me.vexmc.mental.v5.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyDataMigrationTest {

    @TempDir
    Path plugins;

    @Test
    void copiesTheMentalFolderOnFirstBoot() throws Exception {
        Path legacy = plugins.resolve("Mental");
        Files.createDirectories(legacy.resolve("profiles"));
        Files.writeString(legacy.resolve("config.yml"), "config-version: 3\n");
        Files.writeString(legacy.resolve("profiles/mine.yml"), "name: mine\n");
        Path data = plugins.resolve("BattleMotion");

        List<String> log = new ArrayList<>();
        assertTrue(LegacyDataMigration.run(data, log::add));

        assertEquals("config-version: 3\n", Files.readString(data.resolve("config.yml")));
        assertEquals("name: mine\n", Files.readString(data.resolve("profiles/mine.yml")));
        assertTrue(Files.exists(legacy.resolve("config.yml")), "the old folder must stay untouched");
        assertEquals(1, log.size());
    }

    @Test
    void neverOverwritesAnExistingBattleMotionConfig() throws Exception {
        Files.createDirectories(plugins.resolve("Mental"));
        Files.writeString(plugins.resolve("Mental/config.yml"), "old\n");
        Path data = plugins.resolve("BattleMotion");
        Files.createDirectories(data);
        Files.writeString(data.resolve("config.yml"), "new\n");

        assertFalse(LegacyDataMigration.run(data, m -> {}));
        assertEquals("new\n", Files.readString(data.resolve("config.yml")));
    }

    @Test
    void doesNothingWithoutALegacyFolder() {
        assertFalse(LegacyDataMigration.run(plugins.resolve("BattleMotion"), m -> {}));
        assertFalse(Files.exists(plugins.resolve("BattleMotion")));
    }
}
