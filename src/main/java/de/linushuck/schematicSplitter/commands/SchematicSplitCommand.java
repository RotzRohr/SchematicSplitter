package de.linushuck.schematicSplitter.commands;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import de.linushuck.schematicSplitter.SchematicSplitter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SchematicSplitCommand implements CommandExecutor, TabCompleter {
    
    private final SchematicSplitter plugin;
    
    public SchematicSplitCommand(SchematicSplitter plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage("Usage: /schematic split <filename> <x-chunks> <y-chunks> [x] [y] [z]");
            player.sendMessage("Optional: Provide starting coordinates for manual pasting instructions");
            return true;
        }
        
        final String fileName = args[0];
        final int xChunks, yChunks;
        int startX = 0, startY = 100, startZ = 0;
        
        try {
            xChunks = Integer.parseInt(args[1]);
            yChunks = Integer.parseInt(args[2]);
            
            if (xChunks < 1 || yChunks < 1) {
                player.sendMessage("Chunk counts must be at least 1!");
                return true;
            }
            
            // Parse optional starting coordinates
            if (args.length >= 6) {
                startX = Integer.parseInt(args[3]);
                startY = Integer.parseInt(args[4]);
                startZ = Integer.parseInt(args[5]);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid numbers! Please use integers for chunk counts and coordinates.");
            return true;
        }
        
        File inputDir = new File(plugin.getDataFolder(), "Input");
        File schematicFile = new File(inputDir, fileName + ".schem");
        if (!schematicFile.exists()) {
            schematicFile = new File(inputDir, fileName + ".schematic");
            if (!schematicFile.exists()) {
                player.sendMessage("Schematic file not found in Input folder: " + fileName);
                player.sendMessage("Place your schematic files in: " + inputDir.getPath());
                return true;
            }
        }
        
        final File finalSchematicFile = schematicFile;
        final int finalStartX = startX;
        final int finalStartY = startY;
        final int finalStartZ = startZ;
        
        player.sendMessage("Starting to split schematic: " + fileName + " into " + xChunks + "x" + yChunks + " chunks...");
        if (args.length >= 6) {
            player.sendMessage("Using start position: " + startX + ", " + startY + ", " + startZ);
        }
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                splitSchematic(finalSchematicFile, fileName, xChunks, yChunks, finalStartX, finalStartY, finalStartZ);
                plugin.getServer().getScheduler().runTask(plugin, () -> 
                    player.sendMessage("Successfully split schematic into " + (xChunks * yChunks) + " parts!")
                );
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to split schematic: " + e.getMessage());
                e.printStackTrace();
                plugin.getServer().getScheduler().runTask(plugin, () -> 
                    player.sendMessage("Failed to split schematic: " + e.getMessage())
                );
            }
        });
        
        return true;
    }
    
    private void splitSchematic(File schematicFile, String baseName, int xChunks, int yChunks, int startX, int startY, int startZ) throws Exception {
        Clipboard clipboard;
        com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format;
        
        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            format = ClipboardFormats.findByFile(schematicFile);
            ClipboardReader reader = format.getReader(fis);
            clipboard = reader.read();
        }
        
        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();
        BlockVector3 origin = clipboard.getOrigin();
        
        int width = max.x() - min.x() + 1;
        int height = max.y() - min.y() + 1;
        int length = max.z() - min.z() + 1;
        
        int chunkWidth = (int) Math.ceil((double) width / xChunks);
        int chunkLength = (int) Math.ceil((double) length / yChunks);
        
        File outputDir = new File(plugin.getDataFolder(), "Output/" + baseName + "_split");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        List<String> splitFiles = new ArrayList<>();
        
        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                int chunkStartX = min.x() + (x * chunkWidth);
                int chunkStartZ = min.z() + (y * chunkLength);
                int chunkEndX = Math.min(chunkStartX + chunkWidth - 1, max.x());
                int chunkEndZ = Math.min(chunkStartZ + chunkLength - 1, max.z());
                
                BlockVector3 chunkMin = BlockVector3.at(chunkStartX, min.y(), chunkStartZ);
                BlockVector3 chunkMax = BlockVector3.at(chunkEndX, max.y(), chunkEndZ);
                
                CuboidRegion region = new CuboidRegion(chunkMin, chunkMax);
                
                // Create a new clipboard for this chunk
                com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard chunkClipboard = 
                    new com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard(
                        new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(chunkEndX - chunkStartX, height - 1, chunkEndZ - chunkStartZ))
                    );
                
                // Copy blocks from the original clipboard to the chunk clipboard
                for (int rx = 0; rx <= chunkEndX - chunkStartX; rx++) {
                    for (int ry = 0; ry < height; ry++) {
                        for (int rz = 0; rz <= chunkEndZ - chunkStartZ; rz++) {
                            BlockVector3 sourcePos = BlockVector3.at(chunkStartX + rx, min.y() + ry, chunkStartZ + rz);
                            if (clipboard.getRegion().contains(sourcePos)) {
                                BaseBlock block = clipboard.getFullBlock(sourcePos);
                                BlockVector3 targetPos = BlockVector3.at(rx, ry, rz);
                                chunkClipboard.setBlock(targetPos, block);
                            }
                        }
                    }
                }
                
                // Set the origin at (0,0,0) for consistent pasting
                chunkClipboard.setOrigin(BlockVector3.ZERO);
                
                // Always use the modern .schem format for output
                String chunkFileName = baseName + "_split_" + x + "_" + y + ".schem";
                File chunkFile = new File(outputDir, chunkFileName);
                splitFiles.add(chunkFileName);
                
                // Get the Sponge schematic format (v3) for writing
                com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat spongeFormat = 
                    ClipboardFormats.findByAlias("schem");
                
                try (FileOutputStream fos = new FileOutputStream(chunkFile);
                     ClipboardWriter writer = spongeFormat.getWriter(fos)) {
                    writer.write(chunkClipboard);
                }
            }
        }
        
        generateInstructions(outputDir, baseName, xChunks, yChunks, chunkWidth, chunkLength, splitFiles, startX, startY, startZ);
        
        // Save metadata for proper pasting
        saveMetadata(outputDir, baseName, xChunks, yChunks, chunkWidth, chunkLength);
    }
    
    private void generateInstructions(File outputDir, String baseName, int xChunks, int yChunks, 
                                    int chunkWidth, int chunkLength, List<String> splitFiles, 
                                    int startX, int startY, int startZ) throws Exception {
        File instructionsFile = new File(outputDir, "instructions.md");
        StringBuilder instructions = new StringBuilder();
        
        instructions.append("# Schematic Split Instructions\n\n");
        instructions.append("This schematic has been split into ").append(xChunks * yChunks)
                   .append(" chunks to prevent server crashes during pasting.\n\n");
        instructions.append("## Original Schematic\n");
        instructions.append("- **Name:** ").append(baseName).append("\n");
        instructions.append("- **Grid:** ").append(xChunks).append("x").append(yChunks).append("\n");
        instructions.append("- **Chunk Size:** ~").append(chunkWidth).append("x").append(chunkLength).append(" blocks\n\n");
        
        instructions.append("## Manual Pasting Instructions\n\n");
        instructions.append("Execute these commands in order:\n\n");
        instructions.append("```\n");
        instructions.append("/gamemode spectator").append("\n\n");

        int index = 0;
        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                int xOffset = x * chunkWidth;
                int zOffset = y * chunkLength;
                
                instructions.append("# Chunk ").append(x).append(",").append(y).append("\n");
                instructions.append("/tp ").append(startX + xOffset).append(" ").append(startY).append(" ")
                           .append(startZ + zOffset).append("\n");
                instructions.append("//schem load ").append(splitFiles.get(index)).append("\n");
                instructions.append("//paste -a\n");
                instructions.append("\n");
                index++;
            }
        }
        
        instructions.append("```\n\n");
        instructions.append("## Automatic Pasting (Paper Server)\n\n");
        instructions.append("If you're on a Paper server with this plugin installed, use:\n");
        instructions.append("```\n");
        instructions.append("/schematicsplitter paste ").append(baseName).append("\n");
        instructions.append("```\n\n");
        instructions.append("This will automatically paste all chunks with proper delays to prevent server lag.\n");
        
        try (FileOutputStream fos = new FileOutputStream(instructionsFile)) {
            fos.write(instructions.toString().getBytes());
        }
    }
    
    private void saveMetadata(File outputDir, String baseName, int xChunks, int yChunks, 
                             int chunkWidth, int chunkLength) throws Exception {
        File metadataFile = new File(outputDir, "metadata.yml");
        List<String> metadata = new ArrayList<>();
        metadata.add("# Schematic split metadata");
        metadata.add("schematic: " + baseName);
        metadata.add("grid:");
        metadata.add("  x: " + xChunks);
        metadata.add("  y: " + yChunks);
        metadata.add("chunk-size:");
        metadata.add("  width: " + chunkWidth);
        metadata.add("  length: " + chunkLength);
        
        try (FileOutputStream fos = new FileOutputStream(metadataFile)) {
            for (String line : metadata) {
                fos.write((line + "\n").getBytes());
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            File schematicDir = new File(plugin.getDataFolder(), "Input");
            if (schematicDir.exists() && schematicDir.isDirectory()) {
                for (File file : schematicDir.listFiles()) {
                    if (file.isFile() && (file.getName().endsWith(".schem") || file.getName().endsWith(".schematic"))) {
                        String name = file.getName();
                        name = name.substring(0, name.lastIndexOf('.'));
                        if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                            completions.add(name);
                        }
                    }
                }
            }
        } else if (args.length == 2 || args.length == 3) {
            completions.add("2");
            completions.add("4");
            completions.add("8");
        }
        
        return completions;
    }
}