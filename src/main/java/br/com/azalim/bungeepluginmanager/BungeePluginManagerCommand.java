package br.com.azalim.bungeepluginmanager;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.yaml.snakeyaml.Yaml;

public final class BungeePluginManagerCommand extends Command {

    public BungeePluginManagerCommand() {
        super("bungeepluginmanager", "bungeepluginmanager.cmds", "bpm");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(textWithColor("Usage: /bpm <unload;load;reload> <plugin-name>.", ChatColor.RED));
            return;
        }

        switch (args[0].toLowerCase()) {

            case "unload": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin == null) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), ChatColor.RED));
                    return;
                }

                PluginUtils.unloadPlugin(plugin);
                sender.sendMessage(textWithColor(String.format("Plugin '%s' unloaded.", plugin.getDescription().getName()), ChatColor.YELLOW));
                break;
            }

            case "load": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin != null) {
                    sender.sendMessage(textWithColor("Plugin is already loaded", ChatColor.RED));
                    return;
                }

                File file = findFile(args[1]);

                if (!file.exists()) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), ChatColor.RED));
                    return;
                }

                boolean success = PluginUtils.loadPlugin(file);

                if (success) {
                    sender.sendMessage(textWithColor("Plugin loaded.", ChatColor.YELLOW));
                } else {
                    sender.sendMessage(textWithColor("Failed to load plugin, see console for more informations.", ChatColor.RED));
                }

                break;
            }

            case "reload": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin == null) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), ChatColor.RED));
                    return;
                }

                File pluginfile = plugin.getFile();
                PluginUtils.unloadPlugin(plugin);

                boolean success = PluginUtils.loadPlugin(pluginfile);

                if (success) {
                    sender.sendMessage(textWithColor("Plugin reloaded.", ChatColor.YELLOW));
                } else {
                    sender.sendMessage(textWithColor("Failed to reload plugin, see console for more informations.", ChatColor.RED));
                }

            }
        }
    }

    static Plugin findPlugin(String pluginname) {
        for (Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            if (plugin.getDescription().getName().equalsIgnoreCase(pluginname)) {
                return plugin;
            }
        }
        return null;
    }

    static File findFile(String pluginname) {

        File folder = ProxyServer.getInstance().getPluginsFolder();

        if (folder.exists()) {

            File[] pluginFiles = folder.listFiles((File file) -> file.isFile() && file.getName().endsWith(".jar"));

            for (File file : pluginFiles) {

                try (JarFile jar = new JarFile(file)) {

                    JarEntry configurationFile = jar.getJarEntry("bungee.yml");

                    if (configurationFile == null) {
                        configurationFile = jar.getJarEntry("plugin.yml");
                    }

                    try (InputStream in = jar.getInputStream(configurationFile)) {
                        final PluginDescription desc = new Yaml().loadAs(in, PluginDescription.class);
                        if (desc.getName().equalsIgnoreCase(pluginname)) {
                            return file;
                        }
                    }

                } catch (Throwable ex) {
                }

            }
        }

        return new File(folder, pluginname + ".jar");
    }

    static TextComponent textWithColor(String message, ChatColor color) {
        TextComponent text = new TextComponent(message);
        text.setColor(color);
        return text;
    }

}
