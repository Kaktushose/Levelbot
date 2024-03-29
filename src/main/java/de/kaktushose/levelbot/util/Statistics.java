package de.kaktushose.levelbot.util;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelStatistics;
import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Statistics {

    private static final Logger log = LoggerFactory.getLogger(Statistics.class);
    private final Levelbot levelbot;
    private final String youtubeApiKey;
    private int totalMemberCount;
    private int onlineMemberCount;
    private int lsMemberCount;
    private int etsMemberCount;
    private int notrufMemberCount;
    private int omsiMemberCount;
    private int wrsMemberCount;
    private int gtaMemberCount;
    private int boosterMemberCount;
    private int boosterCount;
    private int premiumMemberCount;
    private String ytFollowerCount;
    private String ytVideoCount;
    private String ytViewCount;
    private String boosterMemberList;
    private String premiumMemberList;

    public Statistics(Levelbot levelbot, long guildId) {
        this.levelbot = levelbot;
        youtubeApiKey = levelbot.getSettingsService().getYoutubeApiKey(guildId);
    }

    public Task<Statistics> queryStatistics() {
        Guild guild = levelbot.getGuild();
        CompletableFuture<Statistics> future = new CompletableFuture<>();
        Task<List<Member>> reference = guild.loadMembers()
                .onSuccess(members -> {
                    // general member count
                    totalMemberCount = members.size();
                    onlineMemberCount = (int) members.stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count();
                    // game count
                    lsMemberCount = getGameCount(members, "Farming Simulator");
                    etsMemberCount = getGameCount(members, "Euro Truck Simulator");
                    notrufMemberCount = getGameCount(members, "Notruf 112");
                    omsiMemberCount = getGameCount(members, "OMSI");
                    wrsMemberCount = getGameCount(members, "Winter Resort Simulator");
                    gtaMemberCount = getGameCount(members, "Grand Theft Auto");
                    // booster
                    List<Member> boosterList = members.stream().filter(member -> member.getTimeBoosted() != null).collect(Collectors.toList());
                    boosterMemberCount = boosterList.size();
                    StringBuilder boosters = new StringBuilder();
                    boosterList.forEach(member -> boosters.append(member.getAsMention()).append(", "));
                    boosterMemberList = boosters.length() > 1 ? boosters.substring(0, boosters.length() - 2) : "";
                    boosterCount = guild.getBoostCount();
                    // premium
                    List<Member> premiumMembers = members.stream()
                            .filter(member -> member.getRoles().contains(guild.getRolesByName("premium", true).get(0)))
                            .collect(Collectors.toList());
                    premiumMemberCount = premiumMembers.size();
                    StringBuilder premium = new StringBuilder();
                    premiumMembers.forEach(member -> premium.append(member.getAsMention()).append(", "));
                    premiumMemberList = premium.length() > 1 ? premium.substring(0, premium.length() - 2) : "";
                    // yt
                    try {
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
                        ChannelStatistics channelStatistics = getYoutubeStatistics();
                        ytFollowerCount = numberFormat.format(channelStatistics.getSubscriberCount().intValue());
                        ytVideoCount = numberFormat.format(channelStatistics.getVideoCount().intValue());
                        ytViewCount = numberFormat.format(channelStatistics.getViewCount().intValue());
                    } catch (IOException e) {
                        log.error("Unable to query youtube follower count!", e);
                    }
                    future.complete(this);
                })
                .onError(future::completeExceptionally);
        return new GatewayTask<>(future, reference::cancel);
    }

    private int getGameCount(List<Member> members, String name) {
        int count = 0;
        for (Member member : members) {
            if (member.getActivities().isEmpty()) {
                continue;
            }
            if (member.getActivities().get(0).getName().contains(name)) {
                count++;
            }
        }
        return count;
    }

    private ChannelStatistics getYoutubeStatistics() throws IOException {
        HttpRequestInitializer httpRequestInitializer = request -> {
        };
        YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequestInitializer)
                .setApplicationName("Levelbot")
                .build();
        YouTube.Channels.List search = youTube.channels().list("statistics");
        search.setForUsername("nordrheintvplay");
        search.setKey(youtubeApiKey);
        ChannelListResponse response = search.execute();
        return response.getItems().get(0).getStatistics();
    }
}
