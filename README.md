# SchematicSplitter

A Paper plugin that splits large WorldEdit schematics into smaller chunks to prevent server crashes during pasting.

## Features

- Split schematics into configurable X×Y grid chunks
- Automatic asynchronous pasting with configurable delays
- Works with both WorldEdit and FastAsyncWorldEdit (FAWE preferred)
- Generates manual pasting instructions for non-Paper servers

## Commands

### Split a Schematic
```
/schematicsplitter split <filename> <x-chunks> <y-chunks>
```
Example: `/schematicsplitter split castle 4 4` - Splits castle.schem into a 4×4 grid (16 chunks)

### Paste Split Schematic (Paper servers only)
```
/schematicsplitter paste <schematic-name>
```
Example: `/schematicsplitter paste castle` - Automatically pastes all chunks with delays

## Permissions

- `schematicsplitter.use` - Allows using schematicsplitter commands (default: op)
- `schematicsplitter.split` - Allows splitting schematics (default: op)
- `schematicsplitter.paste` - Allows automatic pasting (default: op)

## Configuration

Edit `config.yml` to customize:
- `paste-delay`: Delay between chunks in ticks (20 ticks = 1 second)
- `announce-chunks`: Show progress messages during pasting
- `max-chunks`: Maximum chunks allowed per operation

## Build Instructions

Requires Java 21 and Maven:
```bash
mvn clean package
```

The compiled JAR will be in `target/SchematicSplitter-1.jar`

## Installation

1. Install either WorldEdit or FastAsyncWorldEdit on your server
2. Place SchematicSplitter.jar in your plugins folder
3. Restart the server

## Usage

1. Place your schematic files in `plugins/SchematicSplitter/Input/`
2. Run `/schematicsplitter split <name> <x-chunks> <y-chunks>` to split the schematic
3. Split files will be saved to `plugins/SchematicSplitter/Output/<name>_split/`
4. Use `/schematicsplitter paste <name>` to automatically paste the split schematic

## Folder Structure

### Input Folder
Place your schematic files (.schem or .schematic) here:
```
plugins/SchematicSplitter/Input/
```

### Output Folder
Split schematics are saved to:
```
plugins/SchematicSplitter/Output/<name>_split/
├── <name>_split_0_0.schem
├── <name>_split_0_1.schem
├── ...
└── instructions.md
```

The `instructions.md` file contains manual pasting commands for servers without this plugin.