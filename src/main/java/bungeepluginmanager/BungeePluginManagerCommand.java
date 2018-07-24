package bungeepluginmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.WHITE;
import static net.md_5.bungee.api.ChatColor.YELLOW;
import static net.md_5.bungee.api.ChatColor.translateAlternateColorCodes;

public final class BungeePluginManagerCommand extends Command {

    BungeePluginManagerCommand() {
        super("bungeepluginmanager", "bungeepluginmanager.cmds", "bpm");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "help": sendHelp(sender);

            case "unload": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin == null) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), RED));
                    return;
                }

                PluginUtils.unloadPlugin(plugin);
                sender.sendMessage(textWithColor(String.format("Plugin '%s' unloaded.", plugin.getDescription().getName()), YELLOW));
                break;
            }

            case "load": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin != null) {
                    sender.sendMessage(textWithColor("Plugin is already loaded", RED));
                    return;
                }

                File file = findFile(args[1]);

                if (!file.exists()) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), RED));
                    return;
                }

                boolean success = PluginUtils.loadPlugin(file);

                if (success) {
                    sender.sendMessage(textWithColor("Plugin loaded.", YELLOW));
                } else {
                    sender.sendMessage(textWithColor("Failed to load plugin, see console for more informations.", RED));
                }

                break;
            }

            case "reload": {

                Plugin plugin = findPlugin(args[1]);

                if (plugin == null) {
                    sender.sendMessage(textWithColor(String.format("Plugin '%s' not found.", args[1]), RED));
                    return;
                }

                File pluginfile = plugin.getFile();
                PluginUtils.unloadPlugin(plugin);

                boolean success = PluginUtils.loadPlugin(pluginfile);

                if (success) {
                    sender.sendMessage(textWithColor("Plugin reloaded.", YELLOW));
                } else {
                    sender.sendMessage(textWithColor("Failed to reload plugin, see console for more informations.", RED));
                }
                break;

            }
            case "list": {
                Collection<Plugin> plugins = ProxyServer.getInstance().getPluginManager().getPlugins();

                ComponentBuilder builder = new ComponentBuilder("Plugins[" + plugins.size() + "]: ");
                plugins.forEach(plugin -> builder.append(plugin.getDescription().getName()).color(GREEN).append(",").color(WHITE));

                sender.sendMessage(builder.create());
                break;

            }
            default: {
                sender.sendMessage(textWithColor("Command not found. Type /bpm help to see available commands.", RED));
            }
        }
    }

    static Plugin findPlugin(String pluginName) {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.getDescription().getName().equalsIgnoreCase(pluginName))
                .findFirst().orElse(null);
    }

    static File findFile(String pluginname) {

        File folder = ProxyServer.getInstance().getPluginsFolder();

        if (folder.exists()) {

            File[] pluginFiles = folder.listFiles((File file) -> file.isFile() && file.getName().endsWith(".jar"));
            if (pluginFiles != null) {
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

                    } catch (Throwable ignored) {
                    }


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
    static void sendHelp(CommandSender sender) {
        // TODO: Use Chat Component API
        sender.sendMessage(translateAlternateColorCodes('&',"&6&l---- BungeePluginManager ----"));
        sender.sendMessage(translateAlternateColorCodes('&',""));
        sender.sendMessage(translateAlternateColorCodes('&',"&6/bpm help: &fDisplay this message"));
        sender.sendMessage(translateAlternateColorCodes('&',"&6/bpm load &a<plugin>: &fLoads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&',"&6/bpm unload &a<plugin>: &fUnloads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&',"&6/bpm reload &a<plugin>: &fReloads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&',"&6/bpm list: &fList all plugins on the bungee"));

    }

}
