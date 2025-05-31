package de.linushuck.schematicSplitter.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.linushuck.schematicSplitter.SchematicSplitter;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematicPasteCommand implements CommandExecutor, TabCompleter {
    
    private final SchematicSplitter plugin;
    
    public SchematicPasteCommand(SchematicSplitter plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length < 2 || !args[0].equalsIgnoreCase("paste")) {
            player.sendMessage("Usage: /schematicsplitter paste <schematic-name>");
            return true;
        }
        
        String schematicName = args[1];
        File splitDir = new File(plugin.getDataFolder(), "Output/" + schematicName + "_split");
        
        if (!splitDir.exists() || !splitDir.isDirectory()) {
            player.sendMessage("No split schematic found with name: " + schematicName);
            player.sendMessage("Make sure you've split it first with /schematic split");
            return true;
        }
        
        // Find all split files (.schem format)
        File[] splitFiles = splitDir.listFiles((dir, name) -> 
            name.startsWith(schematicName + "_split_") && name.endsWith(".schem")
        );
        
        if (splitFiles == null || splitFiles.length == 0) {
            player.sendMessage("No split files found in the directory!");
            return true;
        }
        
        // Sort files by their x,y coordinates
        Arrays.sort(splitFiles, (a, b) -> {
            String nameA = a.getName().replace(".schem", "");
            String nameB = b.getName().replace(".schem", "");
            String[] partsA = nameA.split("_");
            String[] partsB = nameB.split("_");
            
            int xA = Integer.parseInt(partsA[partsA.length - 2]);
            int yA = Integer.parseInt(partsA[partsA.length - 1]);
            int xB = Integer.parseInt(partsB[partsB.length - 2]);
            int yB = Integer.parseInt(partsB[partsB.length - 1]);
            
            if (xA != xB) return Integer.compare(xA, xB);
            return Integer.compare(yA, yB);
        });
        
        Location startLocation = player.getLocation();
        int pasteDelay = plugin.getConfig().getInt("paste-delay", 20);
        boolean announceChunks = plugin.getConfig().getBoolean("announce-chunks", true);
        int maxChunks = plugin.getConfig().getInt("max-chunks", 100);
        
        if (splitFiles.length > maxChunks) {
            player.sendMessage("Too many chunks! Maximum allowed: " + maxChunks);
            return true;
        }
        
        player.sendMessage("Starting to paste " + splitFiles.length + " chunks...");
        
        // Read chunk dimensions from metadata file
        try {
            int chunkWidth, chunkLength;
            
            // Try to read metadata file first
            File metadataFile = new File(splitDir, "metadata.yml");
            if (metadataFile.exists()) {
                // Read metadata
                List<String> lines = new ArrayList<>();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(metadataFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
                
                // Parse chunk dimensions from metadata
                chunkWidth = 0;
                chunkLength = 0;
                for (String line : lines) {
                    if (line.trim().startsWith("width:")) {
                        chunkWidth = Integer.parseInt(line.split(":")[1].trim());
                    } else if (line.trim().startsWith("length:")) {
                        chunkLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }
                
                if (chunkWidth == 0 || chunkLength == 0) {
                    throw new Exception("Invalid metadata file");
                }
            } else {
                // Fallback to reading dimensions from first chunk
                Clipboard firstClipboard;
                try (FileInputStream fis = new FileInputStream(splitFiles[0])) {
                    ClipboardReader reader = ClipboardFormats.findByFile(splitFiles[0]).getReader(fis);
                    firstClipboard = reader.read();
                }
                
                BlockVector3 dimensions = firstClipboard.getDimensions();
                chunkWidth = dimensions.x();
                chunkLength = dimensions.z();
                
                player.sendMessage("Warning: No metadata file found, using chunk dimensions which may cause gaps!");
            }
            
            // Parse grid size from file names
            int maxX = 0, maxY = 0;
            for (File file : splitFiles) {
                String name = file.getName().replace(".schem", "");
                String[] parts = name.split("_");
                int x = Integer.parseInt(parts[parts.length - 2]);
                int y = Integer.parseInt(parts[parts.length - 1]);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
            
            // Start pasting chunks
            pasteChunksAsync(player, splitFiles, startLocation, chunkWidth, chunkLength, 
                           pasteDelay, announceChunks, 0, maxX + 1, maxY + 1);
            
        } catch (Exception e) {
            player.sendMessage("Failed to read chunk dimensions: " + e.getMessage());
            plugin.getLogger().severe("Error reading schematic: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void pasteChunksAsync(Player player, File[] files, Location startLoc, 
                                 int chunkWidth, int chunkLength, int delay, 
                                 boolean announce, int index, int xChunks, int yChunks) {
        if (index >= files.length) {
            player.sendMessage("Finished pasting all " + files.length + " chunks!");
            return;
        }
        
        if (!player.isOnline()) {
            plugin.getLogger().info("Player went offline, stopping paste operation.");
            return;
        }
        
        File currentFile = files[index];
        String name = currentFile.getName().replace(".schem", "");
        String[] parts = name.split("_");
        int x = Integer.parseInt(parts[parts.length - 2]);
        int y = Integer.parseInt(parts[parts.length - 1]);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Clipboard clipboard;
                try (FileInputStream fis = new FileInputStream(currentFile)) {
                    ClipboardReader reader = ClipboardFormats.findByFile(currentFile).getReader(fis);
                    clipboard = reader.read();
                }
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        // Calculate paste location
                        Location pasteLoc = startLoc.clone();
                        pasteLoc.add(x * chunkWidth, 0, y * chunkLength);
                        
                        // Create edit session
                        BukkitWorld world = new BukkitWorld(pasteLoc.getWorld());
                        EditSession editSession = WorldEdit.getInstance().newEditSession(world);
                        
                        // Paste the clipboard
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        Operations.complete(holder
                            .createPaste(editSession)
                            .to(BlockVector3.at(pasteLoc.getBlockX(), pasteLoc.getBlockY(), pasteLoc.getBlockZ()))
                            .ignoreAirBlocks(false)
                            .build());
                        
                        editSession.close();
                        
                        if (announce) {
                            player.sendMessage("Pasted chunk " + (index + 1) + "/" + files.length + 
                                             " at position (" + x + "," + y + ")");
                        }
                        
                        // Schedule next chunk
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> 
                            pasteChunksAsync(player, files, startLoc, chunkWidth, chunkLength, 
                                           delay, announce, index + 1, xChunks, yChunks), 
                            delay
                        );
                        
                    } catch (Exception e) {
                        player.sendMessage("Failed to paste chunk " + (index + 1) + ": " + e.getMessage());
                        plugin.getLogger().severe("Error pasting chunk: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage("Failed to load chunk " + (index + 1) + ": " + e.getMessage());
                    plugin.getLogger().severe("Error loading chunk: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if ("paste".startsWith(args[0].toLowerCase())) {
                completions.add("paste");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("paste")) {
            File splitsDir = new File(plugin.getDataFolder(), "Output");
            if (splitsDir.exists() && splitsDir.isDirectory()) {
                for (File dir : splitsDir.listFiles()) {
                    if (dir.isDirectory() && dir.getName().endsWith("_split")) {
                        String name = dir.getName().replace("_split", "");
                        if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                }
            }
        }
        
        return completions;
    }
}