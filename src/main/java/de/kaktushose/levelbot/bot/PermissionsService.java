package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.annotations.Component;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandContext;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@Component
public class PermissionsService implements PermissionsProvider {

    @Inject
    private UserService userService;

    @Override
    public boolean isMuted(@NotNull User user, @NotNull CommandContext context) {
        return userService.isMuted(user);
    }

    @Override
    public boolean hasPermission(@NotNull User user, @NotNull CommandContext context) {
        for (String permission : context.getCommand().getPermissions()) {
            if (!userService.hasPermission(user, getLevelByName(permission))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull CommandContext context) {
        return hasPermission(member.getUser(), context);
    }

    // TODO replace with Enum
    private int getLevelByName(String name) {
        switch (name) {
            case "moderator":
                return 2;
            case "admin":
                return 3;
            default:
                //for security reasons, commands with an unknown permission string can only be executed by level owner
                return 4;
        }
    }

}