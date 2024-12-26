/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.storage;

import net.clementraynaud.skoice.Skoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YamlFile extends YamlConfiguration {

    private static final String FILE_HEADER = "Do not edit this file manually, otherwise Skoice could stop working properly. Use \"/skoice configure\" to configure Skoice.";
    protected final Skoice plugin;
    private final String fileName;
    private File file;

    protected YamlFile(Skoice plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void load() {
        this.file = new File(this.plugin.getDataFolder(), this.fileName + ".yml");
        try {
            if (this.file.exists() || this.file.createNewFile()) {
                this.load(this.file);
                this.save();
            }
        } catch (IOException ignored) {
        }
    }

    public void save() {
        this.options().header(YamlFile.FILE_HEADER);
        try {
            this.save(this.file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        super.set(path, value);
        this.save();
    }

    public void setDefault(String path, Object value) {
        if (!this.isSet(path)) {
            this.set(path, value);
        }
    }

    public void remove(String path) {
        this.set(path, null);
    }
}
