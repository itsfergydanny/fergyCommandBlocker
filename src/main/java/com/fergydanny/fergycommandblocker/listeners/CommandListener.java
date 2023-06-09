package com.fergydanny.fergycommandblocker.listeners;

import com.fergydanny.fergycommandblocker.FergyCommandBlocker;
import com.fergydanny.fergycommandblocker.model.Matcher;
import com.fergydanny.fergycommandblocker.utils.Chat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class CommandListener implements Listener {
    private final FergyCommandBlocker plugin;

    private final boolean blockFromTab;

    private final String bypassPermission;
    private final String commandsMessage;
    private final String containsMessage;
    private final String consoleOnlyMessage;

    private final List<String> specificCommandsToBlock = new ArrayList<>();
    private final List<String> commandsToPartiallyMatch = new ArrayList<>();
    private final List<String> consoleOnlyCommands = new ArrayList<>();

    private final Set<String> tabBlockList = new HashSet<>();

    public CommandListener(FergyCommandBlocker plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();

        blockFromTab = config.getBoolean("hide-blocked-commands-from-tab");

        bypassPermission = config.getString("bypass-permission");
        commandsMessage = config.getString("messages.commands");
        containsMessage = config.getString("messages.contains");
        consoleOnlyMessage = config.getString("messages.console-only");

        for (String command : config.getStringList("commands")) {
            specificCommandsToBlock.add(command.toLowerCase());
            tabBlockList.add(command.replace("/", "").toLowerCase());
        }
        for (String command : config.getStringList("contains")) {
            commandsToPartiallyMatch.add(command.toLowerCase());
        }
        for (String command : config.getStringList("console-only")) {
            consoleOnlyCommands.add(command.toLowerCase());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandSendEvent event) {
        if (!blockFromTab) {
            return;
        }

        if (event.getPlayer().hasPermission(bypassPermission)) {
            return;
        }

        event.getCommands().removeAll(tabBlockList);

        Set<String> toRemove = new HashSet<>();
        for (String commandName : event.getCommands()) {
            if (!commandName.contains(":")) {
                if (commandsToPartiallyMatch.contains(commandName)) {
                    toRemove.add(commandName);
                    continue;
                }
            }

            String colonCommand = commandName.split(":")[0] + ":";
            if (commandsToPartiallyMatch.contains(colonCommand)) {
                toRemove.add(commandName);
            }
        }

        event.getCommands().removeAll(toRemove);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        String primaryCommandOnly = command.split(" ")[0];

        Matcher consoleOnlyMatcher = commandContainsBlockedString(command, consoleOnlyCommands);
        if (consoleOnlyMatcher.isMatched()) {
            event.setCancelled(true);
            if (!consoleOnlyMessage.isEmpty()) {
                player.sendMessage(Chat.format(consoleOnlyMessage));
            }
            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " has had their command \"" + consoleOnlyMatcher.getMessageMatched() + "\" cancelled as it is console-only.");
            return;
        }

        if (event.getPlayer().hasPermission(bypassPermission)) {
            return;
        }

        for (String commandToPartiallyMatch : commandsToPartiallyMatch) {
            if (primaryCommandOnly.contains(commandToPartiallyMatch)) {
                event.setCancelled(true);
                if (!containsMessage.isEmpty()) {
                    player.sendMessage(Chat.format(containsMessage.replace("{match}", commandToPartiallyMatch)));
                }
                plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " has had their command \"" + commandToPartiallyMatch + "\" cancelled as it contains a blocked command.");
                return;
            }
        }

        Matcher specificMatcher = commandContainsBlockedString(command, specificCommandsToBlock);
        if (specificMatcher.isMatched()) {
            event.setCancelled(true);
            if (!commandsMessage.isEmpty()) {
                player.sendMessage(Chat.format(commandsMessage.replace("{cmd}", specificMatcher.getMessageMatched())));
            }
            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " has had their command \"" + specificMatcher.getMessageMatched() + "\" cancelled as it is a blocked command.");
        }
    }

    private Matcher commandContainsBlockedString(String command, List<String> blockedStrings) {
        for (String blockedStr : blockedStrings) {
            String[] levels = command.split(" ");
            StringBuilder last = new StringBuilder();
            for (String level : levels) {
                String next = (last + " " + level).trim();
                if (blockedStr.equals(next)) {
                    return new Matcher(true, next);
                }
                last.append(next);
            }
        }

        return new Matcher(false, null);
    }
}
