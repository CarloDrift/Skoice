package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.selectmenus.LanguageSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ModeSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ServerSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ToggleSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.VoiceChannelSelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Menu {

    public static final String CLOSE_BUTTON_ID = "close";

    private final Skoice plugin;
    private final String name;
    private final String parentName;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final String[] fields;
    private SelectMenu selectMenu;

    public Menu(Skoice plugin, ConfigurationSection menu) {
        this.plugin = plugin;
        this.name = menu.getName();
        this.parentName = !menu.getParent().equals(menu.getRoot()) ? menu.getParent().getName() : this.name;
        this.emoji = MenuEmoji.valueOf(!menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("emoji").toUpperCase()
                : menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = menu.getStringList("fields").toArray(new String[0]);
    }

    public Message build(String... args) {
        return new MessageBuilder().setEmbeds(this.getEmbed(args))
                .setActionRows(this.getActionRows()).build();
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
                .setColor(this.type.getColor())
                .setFooter(this.plugin.getLang().getMessage("discord.menu.footer"),
                        "https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409");
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
        if (!"mode".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    embed.addField(menu.getTitle(true), menu.getDescription(true), true);
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
        switch (this.name) {
            case "server":
                this.selectMenu = new ServerSelectMenu(this.plugin);
                break;
            case "voice-channel":
                this.selectMenu = new VoiceChannelSelectMenu(this.plugin);
                break;
            case "mode":
                this.selectMenu = new ModeSelectMenu(this.plugin);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(this.plugin);
                break;
            case "action-bar-alert":
                this.selectMenu = new ToggleSelectMenu(this.plugin.getLang(), this.name,
                        this.plugin.getConfiguration().getFile()
                                .getBoolean(ConfigurationField.ACTION_BAR_ALERT.toString()), true);
                break;
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(this.plugin.getLang(), this.name,
                        this.plugin.getConfiguration().getFile()
                                .getBoolean(ConfigurationField.CHANNEL_VISIBILITY.toString()), false);
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
        if (this.parent != null && (this.plugin.getBot().getStatus() == BotStatus.READY || "language".equals(this.name))) {
            buttons.add(Button.secondary(this.parent, "← " + this.plugin.getLang().getMessage("discord.button-label.back")));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name, "⟳ " + this.plugin.getLang().getMessage("discord.button-label.refresh")));
        }
        buttons.addAll(this.getAdditionalButtons());
        if (!"mode".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.name, menu.getTitle(false))
                            .withEmoji(menu.emoji.get())
                            : Button.secondary(menu.name, menu.getTitle(false))
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
                if (!"language".equals(this.name)) {
                    Menu languageMenu = this.plugin.getBot().getMenu("language");
                    buttons.add(Button.secondary(languageMenu.name, languageMenu.getTitle(false))
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
        if ("incomplete-configuration-server-manager".equals(this.name)) {
            return Collections.singletonList(Button.primary("resume-configuration",
                            this.plugin.getLang().getMessage("discord.button-label.resume-configuration"))
                    .withEmoji(MenuEmoji.ARROW_FORWARD.get()));
        } else if ("permissions".equals(this.name)) {
            return Collections.singletonList(Button.link("https://discord.com/api/oauth2/authorize?client_id="
                            + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                            + "&permissions=8&scope=bot%20applications.commands", "Update Permissions")
                    .withEmoji(this.emoji.get()));
        } else if ("mode".equals(this.name) && this.plugin.getBot().getStatus() == BotStatus.READY) {
            return Collections.singletonList(Button.primary("customize",
                            this.plugin.getLang().getMessage("discord.field.customize.title"))
                    .withEmoji(MenuEmoji.PENCIL2.get()));
        }
        return Collections.emptyList();
    }
}
