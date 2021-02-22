package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.api.JsonEmbedFactory;
import com.github.kaktushose.jda.commands.api.Provider;
import com.github.kaktushose.jda.commands.entities.JDACommands;
import com.github.kaktushose.jda.commands.entities.JDACommandsBuilder;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Config;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.listener.JoinLeaveListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Levelbot implements Provider {

    private static final Logger log = LoggerFactory.getLogger(Levelbot.class);
    private final Database database;
    private final Config config;
    private final EmbedCache embedCache;
    private JDACommands jdaCommands;
    private JDA jda;
    private Guild guild;

    public Levelbot(GuildType guildType) {
        database = new Database();
        config = database.getGuildSettings(guildType.id);
        embedCache = new EmbedCache(new File("commandEmbeds.json"));
    }

    public Levelbot start() throws LoginException, InterruptedException {
        log.info("Bot is running at version {}", config.getVersion());

        embedCache.loadEmbedsToCache();

        log.debug("Starting jda...");
        String token = config.getBotToken();
        jda = JDABuilder.create(
                token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        ).disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS
        ).setChunkingFilter(
                ChunkingFilter.ALL
        ).setMemberCachePolicy(
                MemberCachePolicy.ALL
        ).setActivity(
                Activity.playing("starting...")
        ).setStatus(
                OnlineStatus.DO_NOT_DISTURB
        ).build().awaitReady();

        log.debug("Registering event listeners...");
        jda.addEventListener(new JoinLeaveListener(database));
        log.debug("Starting jda-commands");
        jdaCommands = new JDACommandsBuilder(jda)
                .setEmbedFactory(new JsonEmbedFactory(new File("errorEmbeds.json")))
                .addProvider(this)
                .build();
        jdaCommands.getDefaultSettings().setPrefix(config.getBotPrefix()).getHelpLabels().add("hilfe");

        log.debug("Applying permissions...");
        database.getUsers().findByPermissionLevel(0).forEach(botUser ->
                jdaCommands.getDefaultSettings().getMutedUsers().add(botUser.getUserId())
        );
        database.getUsers().findByPermissionLevel(2).forEach(botUser ->
                jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId())
        );
        database.getUsers().findByPermissionLevel(3).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                }
        );
        database.getUsers().findByPermissionLevel(4).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("owner").add(botUser.getUserId());
                }
        );

        guild = jda.getGuildById(496614159254028289L);

        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing("So bitteschön Toni"));

        return this;
    }

    public Levelbot stop() {
        jda.shutdown();
        jdaCommands.shutdown();
        return this;
    }

    public Levelbot indexMembers() {
        UserRepository userRepository = database.getUsers();
        List<Member> guildMembers = guild.getMembers();

        guildMembers.forEach(member -> {
            if (!userRepository.existsById(member.getIdLong())) {
                userRepository.save(new BotUser(member.getIdLong()));
            }
        });

        List<Long> guildMemberIds = guildMembers.stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
        userRepository.findAll().forEach(botUser -> {
            if (!guildMemberIds.contains(botUser.getUserId())) {
                userRepository.deleteById(botUser.getUserId());
            }
        });

        return this;
    }

    @Produces
    public Config getConfig() {
        return config;
    }

    @Produces
    public Database getDatabase() {
        return database;
    }

    @Produces
    public JDA getJda() {
        return jda;
    }

    @Produces
    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    public JDACommands getJdaCommands() {
        return jdaCommands;
    }

    public enum GuildType {

        TESTING(496614159254028289L),
        PRODUCTION(367353132772098048L);

        public final long id;

        GuildType(long id) {
            this.id = id;
        }

    }

}
