package de.linushuck.schematicSplitter.commands;

import de.linushuck.schematicSplitter.SchematicSplitter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematicSplitterCommand implements CommandExecutor, TabCompleter {
    
    private final SchematicSplitter plugin;
    private final SchematicSplitCommand splitCommand;
    private final SchematicPasteCommand pasteCommand;
    
    public SchematicSplitterCommand(SchematicSplitter plugin) {
        this.plugin = plugin;
        this.splitCommand = new SchematicSplitCommand(plugin);
        this.pasteCommand = new SchematicPasteCommand(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "split":
                if (!player.hasPermission("schematicsplitter.split")) {
                    player.sendMessage("You don't have permission to split schematics!");
                    return true;
                }
                return splitCommand.onCommand(sender, command, label, subArgs);
                
            case "paste":
                if (!player.hasPermission("schematicsplitter.paste")) {
                    player.sendMessage("You don't have permission to paste schematics!");
                    return true;
                }
                // Remove the "paste" argument since SchematicPasteCommand expects it
                return pasteCommand.onCommand(sender, command, label, args);
                
            default:
                showHelp(player);
                return true;
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage("SchematicSplitter Commands:");
        player.sendMessage("/schematicsplitter split <name> <x-chunks> <y-chunks> - Split a schematic");
        player.sendMessage("/schematicsplitter paste <name> - Paste a split schematic");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if ("split".startsWith(args[0].toLowerCase())) {
                completions.add("split");
            }
            if ("paste".startsWith(args[0].toLowerCase())) {
                completions.add("paste");
            }
        } else if (args.length > 1) {
            String subCommand = args[0].toLowerCase();
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            
            if (subCommand.equals("split")) {
                return splitCommand.onTabComplete(sender, command, alias, subArgs);
            } else if (subCommand.equals("paste")) {
                return pasteCommand.onTabComplete(sender, command, alias, args);
            }
        }
        
        return completions;
    }
}