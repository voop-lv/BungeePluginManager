package bungeepluginmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.String.format;
import static net.md_5.bungee.api.ChatColor.*;

public final class BungeePluginManagerCommand extends Command {

    BungeePluginManagerCommand() {
        super("bungeepluginmanager", "bungeepluginmanager.cmds", "bpm", "bungeepm");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 1) {
            sendHelp(sender);
            return;
        }
        final String cmd = args[0].toLowerCase();
        switch (cmd) {
            case "help":
            case "h":
                sendHelp(sender);
                break;
            case "unload":
            case "ul":
                if (args.length < 2) {
                    sender.sendMessage(textWithColor("Usage: /bpm unload <plugin>", RED));
                }
                unloadPlugin(sender, args[1]);
                break;
            case "load":
            case "l":
                if (args.length < 2) {
                    sender.sendMessage(textWithColor("Usage: /bpm load <plugin>", RED));
                }
                loadPlugin(sender, args[1]);
                break;
            case "reload":
            case "r":
                if (args.length < 2) {
                    sender.sendMessage(textWithColor("Usage: /bpm reload <plugin>", RED));
                }
                reloadPlugin(sender, args[1]);
                break;
            case "list":
            case "ls":
                listPlugins(sender);
                break;
            default:
                sender.sendMessage(textWithColor("Command not found. Type /bpm help to see available commands.", RED));
                break;
        }
    }

    private void unloadPlugin(CommandSender sender, String pluginName) {
        Plugin plugin = findPlugin(pluginName);
        if (plugin == null) {
            sender.sendMessage(textWithColor(format("Plugin '%s' not found.", pluginName), RED));
            return;
        }
        PluginUtils.unloadPlugin(plugin);
        sender.sendMessage(textWithColor(format("Plugin '%s' unloaded.", plugin.getDescription().getName()), YELLOW));
    }

    private void loadPlugin(CommandSender sender, String pluginName) {
        Plugin plugin = findPlugin(pluginName);
        if (plugin != null) {
            sender.sendMessage(textWithColor("Plugin is already loaded", RED));
            return;
        }
        File file = findFile(pluginName);
        if (!file.exists()) {
            sender.sendMessage(textWithColor(format("Plugin '%s' not found.", pluginName), RED));
            return;
        }
        boolean success = PluginUtils.loadPlugin(file);
        if (success) {
            sender.sendMessage(textWithColor("Plugin loaded.", YELLOW));
        } else {
            sender.sendMessage(textWithColor("Failed to load plugin, see console for more details.", RED));
        }
    }

    private void reloadPlugin(CommandSender sender, String pluginName) {
        Plugin plugin = findPlugin(pluginName);
        if (plugin == null) {
            sender.sendMessage(textWithColor(format("Plugin '%s' not found.", pluginName), RED));
            return;
        }
        File pluginFile = plugin.getFile();
        PluginUtils.unloadPlugin(plugin);
        boolean success = PluginUtils.loadPlugin(pluginFile);
        if (success) {
            sender.sendMessage(textWithColor("Plugin reloaded.", YELLOW));
        } else {
            sender.sendMessage(textWithColor("Failed to reload plugin, see console for more details.", RED));
        }
    }

    private void listPlugins(CommandSender sender) {
        Collection<Plugin> plugins = ProxyServer.getInstance().getPluginManager().getPlugins();
        ComponentBuilder builder = new ComponentBuilder("Plugins[" + plugins.size() + "]: ");
        plugins.forEach(plugin -> builder.append(plugin.getDescription().getName()).color(GREEN).append(",").color(WHITE));
        sender.sendMessage(builder.create());
    }

    static Plugin findPlugin(String pluginName) {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getDescription().getName().equalsIgnoreCase(pluginName))
                .findFirst().orElse(null);
    }

    static File findFile(String pluginName) {

        File folder = ProxyServer.getInstance().getPluginsFolder();

        if (!folder.exists()) {
            return new File(folder, pluginName + ".jar");
        }

        File[] pluginFiles = folder.listFiles((File file) -> file.isFile() && file.getName().endsWith(".jar"));
        if (pluginFiles == null) {
            return new File(folder, pluginName + ".jar");
        }
        for (File file : pluginFiles) {
            try (JarFile jar = new JarFile(file)) {

                JarEntry configurationFile = jar.getJarEntry("bungee.yml");

                if (configurationFile == null) {
                    configurationFile = jar.getJarEntry("plugin.yml");
                }

                try (InputStream in = jar.getInputStream(configurationFile)) {

                    final PluginDescription desc = new Yaml().loadAs(in, PluginDescription.class);

                    if (desc.getName().equalsIgnoreCase(pluginName)) {
                        return file;
                    }
                }
            } catch (Exception error) {
                // Ignored
            }
        }
        return new File(folder, pluginName + ".jar");
    }

    static TextComponent textWithColor(String message, ChatColor color) {
        TextComponent text = new TextComponent(message);
        text.setColor(color);
        return text;
    }
    static void sendHelp(CommandSender sender) {
        ComponentBuilder builder = new ComponentBuilder("\n");
        builder.append("---- BungeePluginManager ----\n").color(GOLD).bold(true);
        builder.append("/bpm help: ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "bpm help")).color(GOLD).bold(true).append("Display this message\n", FormatRetention.NONE);
        builder.append("/bpm load ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "bpm load ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Loads a plugin\n", FormatRetention.NONE);
        builder.append("/bpm unload ").color(GOLD).append("<plugin>: ").color(GREEN).append("Unloads a plugin\n", FormatRetention.NONE);
        builder.append("/bpm reload ").color(GOLD).append("<plugin>: ").color(GREEN).append("Reloads a plugin\n", FormatRetention.NONE);
        builder.append("/bpm list: ").color(GOLD).append("List all plugins on the bungee", FormatRetention.NONE);

        sender.sendMessage(builder.create());
    }

}
