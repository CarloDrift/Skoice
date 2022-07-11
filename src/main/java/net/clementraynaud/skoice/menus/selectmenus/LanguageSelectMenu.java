/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.lang.LangInfo;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageSelectMenu extends SelectMenu {

    private final Skoice plugin;

    public LanguageSelectMenu(Skoice plugin) {
        super(plugin.getLang(), false);
        this.plugin = plugin;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> options = new ArrayList<>();
        for (LangInfo option : LangInfo.values()) {
            options.add(SelectOption.of(option.getFullName(), option.name())
                    .withDescription(option.name().equals(LangInfo.EN.name())
                            ? super.lang.getMessage("discord.select-option.default.description")
                            : null)
                    .withEmoji(option.getEmoji()));
        }
        return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("language-selection")
                .addOptions(options)
                .setDefaultValues(Collections.singleton(this.plugin.getConfiguration().getFile().getString(ConfigurationField.LANG.toString()))).build();
    }
}
