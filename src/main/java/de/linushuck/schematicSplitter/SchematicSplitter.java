package de.linushuck.schematicSplitter;

import de.linushuck.schematicSplitter.commands.SchematicSplitterCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SchematicSplitter extends JavaPlugin {
    
    private boolean hasFAWE = false;
    private boolean hasWorldEdit = false;
    
    @Override
    public void onEnable() {
        // Check for FAWE first (preferred)
        if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            hasFAWE = true;
            getLogger().info("FastAsyncWorldEdit detected! Using FAWE for operations.");
        } else if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            hasWorldEdit = true;
            getLogger().info("WorldEdit detected! Using WorldEdit for operations.");
        } else {
            getLogger().severe("Neither FastAsyncWorldEdit nor WorldEdit found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Save default config
        saveDefaultConfig();
        
        // Create input/output directories
        File inputDir = new File(getDataFolder(), "Input");
        File outputDir = new File(getDataFolder(), "Output");
        if (!inputDir.exists()) {
            inputDir.mkdirs();
            getLogger().info("Created Input directory at: " + inputDir.getPath());
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            getLogger().info("Created Output directory at: " + outputDir.getPath());
        }
        
        // Register commands
        SchematicSplitterCommand commandHandler = new SchematicSplitterCommand(this);
        getCommand("schematicsplitter").setExecutor(commandHandler);
        getCommand("schematicsplitter").setTabCompleter(commandHandler);
        
        getLogger().info("SchematicSplitter has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("SchematicSplitter has been disabled!");
    }
    
    public boolean hasFAWE() {
        return hasFAWE;
    }
    
    public boolean hasWorldEdit() {
        return hasWorldEdit;
    }
}
