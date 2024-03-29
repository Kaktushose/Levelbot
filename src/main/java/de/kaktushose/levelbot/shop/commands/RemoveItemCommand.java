package de.kaktushose.levelbot.shop.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.Item;
import de.kaktushose.levelbot.util.NumberEmojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@CommandController({"remove", "rm"})
@Permission("moderator")
public class RemoveItemCommand {

    @Inject
    private ShopService shopService;
    @Inject
    private EmbedCache embedCache;
    @Inject
    private Levelbot levelbot;

    @Command(
            name = "Item entfernen",
            usage = "{prefix}remove <member>",
            desc = "Entfernt ein Item aus dem Besitz eines Benutzers",
            category = "Moderation"
    )
    public void onRemoveItem(CommandEvent event, Member member) {
        List<Item> items = shopService.getItems(member.getIdLong());
        AtomicInteger counter = new AtomicInteger(1);
        List<String> emotes = new ArrayList<>();
        EmbedBuilder builder = embedCache.getEmbed("removeItem")
                .injectValue("user", member.getAsMention())
                .toEmbedBuilder();

        items.forEach(item -> {
            String emote = EmoteType.getNumber(counter.getAndIncrement()).unicode;
            emotes.add(emote);
            builder.addField(emote + ": " + item.getName(), "", false);
        });

        event.reply(builder, chooseMessage -> {
            emotes.forEach(emote -> chooseMessage.addReaction(emote).queue());

            ReactionWaiter reactionWaiter = new ReactionWaiter(chooseMessage, event.getMember(), emotes);
            reactionWaiter.onEvent(reactionEvent -> {
                Item forRemoval;
                switch (reactionEvent.getEmote()) {
                    case NumberEmojis.ONE:
                        forRemoval = items.get(0);
                        break;
                    case NumberEmojis.TWO:
                        forRemoval = items.get(1);
                        break;
                    case NumberEmojis.THREE:
                        forRemoval = items.get(2);
                        break;
                    case NumberEmojis.FOUR:
                        forRemoval = items.get(3);
                        break;
                    case NumberEmojis.FIVE:
                        forRemoval = items.get(4);
                        break;
                    default:
                        return;
                }
                shopService.removeItem(member.getIdLong(), forRemoval.getItemId());
                reactionWaiter.stopWaiting(false);
                chooseMessage.delete().queue();
                event.reply(embedCache.getEmbed("removeItemSuccess").injectValue("user", member.getAsMention()));
            });
        });
    }
}
