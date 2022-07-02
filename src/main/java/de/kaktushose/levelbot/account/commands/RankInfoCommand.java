package de.kaktushose.levelbot.account.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import de.kaktushose.levelbot.Levelbot;
import net.dv8tion.jda.api.entities.Member;

@CommandController(value = {"info", "rank", "konto"}, category = "Levelsystem")
public class RankInfoCommand {

    @Inject
    private Levelbot levelbot;

    @Command(name = "Kontoinformation abrufen", desc = "Zeigt die Kontoinformationen zu einem User an")
    public void onRankInfo(CommandEvent event, @Optional Member member) {
        Member target = member == null ? event.getMember() : member;
        event.reply(levelbot.generateRankInfo(target.getUser(), false));
    }
}
