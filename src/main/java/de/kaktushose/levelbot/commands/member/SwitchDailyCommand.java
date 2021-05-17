package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

@CommandController("täglich")
public class SwitchDailyCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private UserService userService;

    @Command(
            name = "Täglich Command",
            usage = "{prefix}täglich",
            desc = "Aktiviert bzw. deaktiviert die täglichen Kontoinformationen",
            category = "Levelsystem"
    )
    public void onSwitchDaily(CommandEvent event) {
        if (!userService.switchDaily(event.getAuthor().getIdLong())) {
            event.reply(embedCache.getEmbed("switchDailySuccess").injectValue("action", "deaktiviert"));
        } else {
            event.getAuthor().openPrivateChannel()
                    .flatMap(privateChannel ->
                            privateChannel.sendMessage(embedCache.getEmbed("switchDailySuccess")
                                    .injectValue("action", "aktiviert")
                                    .toMessageEmbed()
                            )
                    )
                    .queue(success -> event.getMessage().delete().queue(),
                            new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                                userService.switchDaily(event.getAuthor().getIdLong());
                                event.reply(embedCache.getEmbed("switchDailyError"));
                            })
                    );
        }
    }
}