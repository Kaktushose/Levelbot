package de.kaktushose.levelbot.listener;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.CollectEvent;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.services.EventService;
import de.kaktushose.levelbot.database.services.LevelService;
import de.kaktushose.levelbot.database.services.UserService;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.ItemCategory;
import de.kaktushose.levelbot.shop.data.items.ItemVariant;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LevelListener extends ListenerAdapter {

    private final LevelService levelService;
    private final EventService eventService;
    private final UserService userService;
    private final ShopService shopService;
    private final EmbedCache embedCache;
    private final Levelbot levelbot;

    public LevelListener(Levelbot levelbot) {
        this.levelService = levelbot.getLevelService();
        this.eventService = levelbot.getEventService();
        this.userService = levelbot.getUserService();
        this.shopService = levelbot.getShopService();
        this.embedCache = levelbot.getEmbedCache();
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        long guildId = event.getGuild().getIdLong();
        User author = event.getAuthor();
        long userId = author.getIdLong();
        if (author.isBot()) {
            return;
        }
        if (!levelService.isValidMessage(userId, guildId, event.getChannel().getIdLong())) {
            return;
        }
        if (event.getMessage().getContentStripped().length() < 10) {
            return;
        }

        TextChannel channel = levelbot.getBotChannel();

        if (eventService.isCollectEventActive(guildId)) {
            CollectEvent collectEvent = eventService.getActiveCollectEvent(guildId);
            long eventPoints = userService.increaseEventPoints(userId);

            if (eventPoints == 1) {
                guild.addRoleToMember(userId, guild.getRoleById(collectEvent.getRoleId())).queue();

                channel.sendMessage(author.getAsMention())
                        .and(channel.sendMessageEmbeds(embedCache.getEmbed("collectEventRoleReward")
                                .injectValue("user", author.getName())
                                .toMessageEmbed())
                        ).queue();
            } else if (eventPoints == 50) {
                shopService.addItem(userId, ItemCategory.PREMIUM, ItemVariant.BASIC);
                guild.addRoleToMember(userId, guild.getRoleById(386302591883018242L)).queue();
                channel.sendMessage(author.getAsMention())
                        .and(channel.sendMessageEmbeds(embedCache.getEmbed("collectEventItemReward")
                                .injectValue("user", author.getName())
                                .toMessageEmbed())
                        ).queue();
            } else if (eventPoints == 100) {
                guild.getTextChannelById(724727743446777896L).sendMessage(String.format("<@%d> hat 100 Schneeflocken gesammelt", userId)).queue();

                channel.sendMessage(author.getAsMention())
                        .and(channel.sendMessageEmbeds(embedCache.getEmbed("collectEventVoteReward")
                                .injectValue("user", author.getName())
                                .toMessageEmbed())
                        ).queue();
            }
        }

        Optional<Rank> optional = levelService.onValidMessage(userId);
        if (optional.isEmpty()) {
            return;
        }

        Rank currentRank = optional.get();
        Rank nextRank = levelService.getNextRank(userId);
        String rewards = levelService.applyRewards(userId, currentRank.getRankId());

        levelbot.addRankRole(userId, currentRank.getRankId());
        levelbot.removeRankRole(userId, levelService.getPreviousRank(userId).getRankId());

        String nextRankInfo = currentRank.equals(nextRank) ? "N/A" : String.format("<@&%d>", nextRank.getRoleId());
        String xp = currentRank.equals(nextRank) ? "0" : String.valueOf(nextRank.getBound());

        channel.sendMessage(author.getAsMention())
                .and(channel.sendMessageEmbeds(embedCache.getEmbed("levelUp")
                        .injectValue("user", author.getAsMention())
                        .injectValue("color", currentRank.getColor())
                        .injectValue("currentRank", guild.getRoleById(currentRank.getRoleId()).getAsMention())
                        .injectValue("nextRank", nextRankInfo)
                        .injectValue("reward", rewards)
                        .injectValue("xp", xp)
                        .toMessageEmbed())
                ).queue();
    }
}
