# VS Code Settings Guide

## Workspace vs User Settings

This project uses a VS Code workspace file (`docfix.code-workspace`) that contains only project-specific settings that should be shared across all developers.

## User-Specific Settings

Personal preferences (such as font sizes, theme preferences, etc.) should be configured in your **User Settings**, not in the workspace file.

### Where to Put User Settings

You have multiple options for configuring your personal VS Code settings depending on your environment:

#### Option 1: Global User Settings (for local VS Code)
1. Open VS Code Command Palette (`Ctrl+Shift+P` or `Cmd+Shift+P` on Mac)
2. Type "Preferences: Open User Settings (JSON)"
3. Add your personal preferences there

Example user settings that were removed from the workspace file:
```json
{
  "terminal.integrated.fontSize": 16
}
```

#### Option 2: GitHub Codespaces User Settings (Recommended for @elharo)
If you're using GitHub Codespaces, your user settings are stored in your GitHub account:

1. In your Codespace, open VS Code Command Palette (`Ctrl+Shift+P` or `Cmd+Shift+P` on Mac)
2. Type "Preferences: Open User Settings (JSON)"
3. Add your personal preferences there
4. These settings will sync across all your Codespaces via your GitHub account

Alternatively, you can configure settings in your dotfiles repository that GitHub Codespaces will automatically apply. See [Personalizing GitHub Codespaces for your account](https://docs.github.com/en/codespaces/customizing-your-codespace/personalizing-github-codespaces-for-your-account) for more information.

#### Option 3: Local Settings (for project-specific user settings)
If you want settings that apply only to this project but are specific to your user account:

1. Create or edit `.vscode/settings.json` in your local clone
2. Add your preferences there
3. This file is already in `.gitignore`, so it won't be committed

### What Belongs in the Workspace File

The workspace file (`docfix.code-workspace`) should only contain:
- Folder configurations
- Project-specific settings that all developers should use
- Build/test task configurations (currently in `.vscode/tasks.json`)

### What Belongs in User Settings

Personal preferences such as:
- Font sizes (`terminal.integrated.fontSize`, `editor.fontSize`)
- Theme/color preferences
- Editor behavior preferences (word wrap, minimap, etc.)
- Keybindings
- Extension-specific settings that are personal preferences
