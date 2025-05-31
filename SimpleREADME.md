# SchematicSplitter - Quick Start Guide

A plugin that splits large WorldEdit schematics into smaller chunks to prevent server crashes.

## Installation

1. Install [FastAsyncWorldEdit](https://intellectualsites.github.io/download/fawe.html) or [WorldEdit](https://dev.bukkit.org/projects/worldedit)
2. Download SchematicSplitter.jar and put it in your `plugins` folder
3. Restart your server

## Example: Splitting and Pasting a Castle

### 1. Place your schematic
Put your schematic file in:
```
plugins/SchematicSplitter/Input/castle.schem
```

### 2. Split the schematic
Split it into a 4x4 grid (16 chunks):
```
/schematicsplitter split castle 4 4
```

### 3. Paste the schematic
Stand where you want it and run:
```
/schematicsplitter paste castle
```

**Using WorldEdit?** You'll need to paste each chunk manually:
```
/schematicsplitter paste castle      # Start session
/schematicsplitter nexttile         # Paste chunk 1
/schematicsplitter nexttile         # Paste chunk 2
# ... continue until done
```

## Manual Pasting (Without This Plugin)

If you're on a server without SchematicSplitter, you can still paste the split chunks manually using WorldEdit. After splitting, check the `instructions.md` file in the output folder:

```
plugins/SchematicSplitter/Output/castle_split/instructions.md
```

It contains commands like this:

```
# Starting position
/tp 0 100 0

# Chunk 0,0
/tp 0 100 0
//schem load castle_split_0_0
//paste -a

# Chunk 0,1
/tp 0 100 145
//schem load castle_split_0_1
//paste -a

# ... and so on for each chunk
```

Just copy and run these commands in order. The `/tp` commands position you correctly for each chunk.

## That's it!

- Use `//undo` to undo the paste
- Larger schematics? Use bigger grids like 8x8 or 16x16
- Need help? Use `/schematicsplitter` to see all commands

For detailed documentation, see the full [README.md](README.md)