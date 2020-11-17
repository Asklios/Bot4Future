package main.java.commands.administation;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoleSortCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        if (message.getMentionedRoles().size() < 2) {
            channel.sendMessage("Es muss eine start und eine end Rolle angegeben werden. `%sortroles @startrole @endrole`")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        //%sortroles @startrole @endrole
        Guild guild = channel.getGuild();
        List<Role> roles = guild.getRoles();

        Role startRole = message.getMentionedRoles().get(0);
        Role endRole = message.getMentionedRoles().get(1);

        int startRoleIndex = roles.indexOf(startRole);
        int endRoleIndex = roles.indexOf(endRole);

        if (endRoleIndex - startRoleIndex <= 1) {
            channel.sendMessage("Die `@StartRolle` muss höher als die `@EndRolle` sein. Zwischen ihnen müssen sich min. 2 Rollen befinden.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }


        //zu sortierende Rollen in orginaler Sortierung
        List<Role> sortRoles = new ArrayList<>(roles.subList(startRoleIndex + 1, endRoleIndex));
        sortRoles.sort(Comparator.comparing(Role::getName));
        Collections.reverse(sortRoles);

        RoleOrderAction modifyRoles = guild.modifyRolePositions();

        for (int i = 0; i < sortRoles.size(); i++) {
            Role role = sortRoles.get(i);
            int newIndex = endRole.getPositionRaw() + i;
            int oldIndex = roles.indexOf(role);

            if (newIndex == oldIndex) {
                continue;
            }
            modifyRoles.selectPosition(role).moveTo(newIndex).complete();
        }

        channel.sendMessage("Die Rollen wurden sortiert.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));

    }
}
