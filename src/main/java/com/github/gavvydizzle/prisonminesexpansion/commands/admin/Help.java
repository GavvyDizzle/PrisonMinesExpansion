package com.github.gavvydizzle.prisonminesexpansion.commands.admin;

import com.github.gavvydizzle.prisonminesexpansion.commands.AdminCommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Help extends SubCommand {

    private final AdminCommandManager commandManager;

    public Help(AdminCommandManager commandManager) {
        this.commandManager = commandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + commandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage("-----(PrisonMinesExpansion Admin Commands)-----");
        ArrayList<SubCommand> subCommands = commandManager.getSubcommands();
        for (SubCommand subCommand : subCommands) {
            sender.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + subCommand.getDescription());
        }
        sender.sendMessage("-----(PrisonMinesExpansion Admin Commands)-----");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
