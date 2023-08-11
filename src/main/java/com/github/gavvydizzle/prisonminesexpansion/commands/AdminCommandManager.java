package com.github.gavvydizzle.prisonminesexpansion.commands;

import com.github.gavvydizzle.prisonminesexpansion.commands.admin.Help;
import com.github.gavvydizzle.prisonminesexpansion.commands.admin.LuckyMine;
import com.github.gavvydizzle.prisonminesexpansion.commands.admin.Reload;
import com.github.gavvydizzle.prisonminesexpansion.luckymine.LuckyMineManager;
import com.github.mittenmc.serverutils.CommandManager;
import org.bukkit.command.PluginCommand;

public class AdminCommandManager extends CommandManager {

    public AdminCommandManager(PluginCommand command, LuckyMineManager luckyMineManager) {
        super(command);

        registerCommand(new Help(this));
        registerCommand(new LuckyMine(this, luckyMineManager));
        registerCommand(new Reload(this, luckyMineManager));
        sortCommands();
    }
}