/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.selectmenus.ActionBarAlertsSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.IncludedPlayersSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.LanguageSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.LoginNotificationSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.RangeSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ServerSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ToggleSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.VoiceChannelSelectMenu;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Menu {

    public static final String CLOSE_BUTTON_ID = "close";
    public static final String MESSAGE_NOT_SHOWING_UP = "message-not-showing-up";

    private final Skoice plugin;
    private final String menuId;
    private final String parentName;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final String[] fields;
    private SelectMenu selectMenu;

    public Menu(Skoice plugin, ConfigurationSection menu) {
        this.plugin = plugin;
        this.menuId = menu.getName();
        this.parentName = !menu.getParent().equals(menu.getRoot()) ? menu.getParent().getName() : this.menuId;
        this.emoji = MenuEmoji.valueOf(!menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("emoji").toUpperCase()
                : menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = menu.getStringList("fields").toArray(new String[0]);
    }

    public MessageCreateData build(String... args) {
        this.plugin.getConfigurationMenu().setMenuId(this.menuId);
        return new MessageCreateBuilder().setEmbeds(this.getEmbed(args))
                .setComponents(this.getActionRows()).build();
    }

    private String getTitle(boolean withEmoji) {
        return withEmoji ? this.emoji + this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".title") :
                this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".title");
    }

    private String getDescription(boolean shortened) {
        if (this.plugin.getBot().getStatus() != BotStatus.READY && this.plugin.getLang().contains("discord.menu." + this.parentName + ".alternative-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".alternative-description");
        } else if (shortened && this.plugin.getLang().contains("discord.menu." + this.parentName + ".shortened-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".shortened-description");
        } else if (this.plugin.getLang().contains("discord.menu." + this.parentName + ".description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed(String... args) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(true))
                .setColor(this.type.getColor());

        if ("skoice-proximity-voice-chat".equals(this.menuId)) {
            embed.setFooter(this.plugin.getLang().getMessage("discord.menu.invite-footer"),
                    "https://avatars.githubusercontent.com/u/107434569?s=200&v=4");
        } else {
            embed.setFooter(this.plugin.getLang().getMessage("discord.menu.footer"),
                    "https://avatars.githubusercontent.com/u/107434569?s=200&v=4");
        }

        if (this.getDescription(false) != null) {
            embed.setDescription(this.getDescription(false));
        }
        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            StringBuilder author = new StringBuilder();
            String parentMenu = this.parent;
            while (parentMenu != null) {
                Menu menuParent = this.plugin.getBot().getMenu(parentMenu);
                author.insert(0, menuParent.getTitle(false) + " › ");
                parentMenu = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        if (!"range".equals(this.menuId)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                String description = menu.getDescription(true);
                if (menu.parent != null && menu.parent.equals(this.menuId) && description != null) {
                    embed.addField(menu.getTitle(true), description, true);
                }
            }
        }
        int startIndex = 0;
        for (String field : this.fields) {
            if ("customize".equals(field) && this.plugin.getBot().getStatus() != BotStatus.READY) {
                break;
            }
            MenuField menuField = this.plugin.getBot().getField(field);
            int endIndex = this.plugin.getLang().getAmountOfArgsRequired(menuField.getDescription());
            embed.addField(menuField.build(Arrays.copyOfRange(args, startIndex, endIndex)));
            startIndex = endIndex;
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows() {
        switch (this.menuId) {
            case "server":
                this.selectMenu = new ServerSelectMenu(this.plugin);
                break;
            case "voice-channel":
                this.selectMenu = new VoiceChannelSelectMenu(this.plugin);
                break;
            case "range":
                this.selectMenu = new RangeSelectMenu(this.plugin);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(this.plugin);
                break;
            case "login-notification":
                this.selectMenu = new LoginNotificationSelectMenu(this.plugin);
                break;
            case "included-players":
                this.selectMenu = new IncludedPlayersSelectMenu(this.plugin);
                break;
            case "action-bar-alerts":
                this.selectMenu = new ActionBarAlertsSelectMenu(this.plugin);
                break;
            case "tooltips":
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(this.plugin, this.menuId);
                break;
            default:
                List<Button> buttons = this.getButtons();
                if (!buttons.isEmpty()) {
                    return Collections.singletonList(ActionRow.of(buttons));
                }
                return Collections.emptyList();
        }
        return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons()));
    }

    private List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null && (this.plugin.getBot().getStatus() == BotStatus.READY || "language".equals(this.menuId))) {
            buttons.add(Button.secondary(this.parent, "← " + this.plugin.getLang().getMessage("discord.button-label.back")));
        }
        buttons.addAll(this.getAdditionalButtons());
        if (!"range".equals(this.menuId)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.menuId)) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.menuId, menu.getTitle(false))
                            .withEmoji(menu.emoji.get())
                            : Button.secondary(menu.menuId, menu.getTitle(false))
                            .withEmoji(menu.emoji.get()));
                }
            }
        }
        if (this.type == MenuType.DEFAULT) {
            if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                buttons.add(Button.danger(Menu.CLOSE_BUTTON_ID,
                                this.plugin.getLang().getMessage("discord.button-label.close"))
                        .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()));
            } else {
                buttons.add(Button.secondary(Menu.MESSAGE_NOT_SHOWING_UP,
                                this.plugin.getLang().getMessage("discord.button-label.message-not-showing-up"))
                        .withEmoji(MenuEmoji.QUESTION.get()));
                if (!"language".equals(this.menuId)) {
                    Menu languageMenu = this.plugin.getBot().getMenu("language");
                    buttons.add(Button.secondary(languageMenu.menuId, languageMenu.getTitle(false))
                            .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()));
                }
                buttons.add(Button.secondary(Menu.CLOSE_BUTTON_ID,
                                this.plugin.getLang().getMessage("discord.button-label.configure-later"))
                        .withEmoji(MenuEmoji.CLOCK3.get()));
            }
        }
        return buttons;
    }

    private List<Button> getAdditionalButtons() {
        List<Button> additionalButtons = new ArrayList<>();
        if ("incomplete-configuration-server-manager".equals(this.menuId)) {
            additionalButtons.add(Button.primary("resume-configuration",
                            this.plugin.getLang().getMessage("discord.button-label.resume-configuration"))
                    .withEmoji(MenuEmoji.ARROW_FORWARD.get()));
        } else if ("permissions".equals(this.menuId)) {
            additionalButtons.add(Button.link("https://discord.com/api/oauth2/authorize?client_id="
                            + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                            + "&permissions=8&scope=bot%20applications.commands", "Update Permissions")
                    .withEmoji(this.emoji.get()));
        } else if ("range".equals(this.menuId) && this.plugin.getBot().getStatus() == BotStatus.READY) {
            additionalButtons.add(Button.primary("customize",
                            this.plugin.getLang().getMessage("discord.field.customize.title"))
                    .withEmoji(MenuEmoji.PENCIL2.get()));
        } else if ("login-notification".equals(this.menuId)
                && LoginNotificationSelectMenu.REMIND_ONCE.equals(this.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString()))) {
            additionalButtons.add(Button.danger("clear-notified-players",
                            this.plugin.getLang().getMessage("discord.button-label.clear-notified-players"))
                    .withEmoji(MenuEmoji.WASTEBASKET.get()));
        } else if ("verification-code".equals(this.menuId)) {
            additionalButtons.add(Button.secondary(Menu.MESSAGE_NOT_SHOWING_UP,
                            this.plugin.getLang().getMessage("discord.button-label.message-not-showing-up"))
                    .withEmoji(MenuEmoji.QUESTION.get()));
        }
        return additionalButtons;
    }
}
