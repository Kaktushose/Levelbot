package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.services.SettingsService;

@CommandController(value = {"botinfo", "credits"}, category = "Sonstiges")
public class BotInfoCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Bot Information",
            usage = "{prefix}botinfo",
            desc = "Zeigt allgemeine Informationen über den Bot an"
    )
    public void onBotInfo(CommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        event.reply(embedCache.getEmbed("botInfo")
                .injectValue("prefix", settingsService.getBotPrefix(guildId))
                .injectValue("version", settingsService.getVersion(guildId))
        );
    }
}
