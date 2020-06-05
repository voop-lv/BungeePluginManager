package bungeepluginmanager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.yaml.snakeyaml.Yaml;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import static net.md_5.bungee.api.ChatColor.*;

public class Commands extends Command implements TabExecutor {

	public Commands() {
		super("bungeepluginmanager", "bungeepluginmanager.cmds", "bpm");
	}
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (sender.hasPermission("bungeepluginmanager.cmds")) {
			List<String> result = new ArrayList<String>();
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("")) {
					result.add("help");
					result.add("list");
					result.add("load");
					result.add("reload");
					result.add("unload");
					return result;
				}
				if (args[0].startsWith("l")) {
					result.add("list");
					result.add("load");
					return result;
				}
				if ((args[0].startsWith("h")) || (args[0].endsWith("help"))) {
					result.add("help");
					return result;
				}
				if ((args[0].startsWith("li")) || (args[0].endsWith("list"))) {
					result.add("list");
					return result;
				}
				if ((args[0].startsWith("lo")) || (args[0].endsWith("load"))) {
					result.add("load");
					return result;
				}
				if ((args[0].startsWith("r")) || (args[0].endsWith("reload"))) {
					result.add("reload");
					return result;
				}
				if ((args[0].startsWith("u")) || (args[0].endsWith("unload"))) {
					result.add("unload");
					return result;
				}
			}
			if (args.length == 2) {
				if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("list"))) {
					return BungeePluginManager.TabshowNone();
				}
				if ((args[0].equalsIgnoreCase("reload")) || (args[0].equalsIgnoreCase("unload"))) {
					List<String> plugin = new ArrayList<String>();
					for (Plugin pluginlist : ProxyServer.getInstance().getPluginManager().getPlugins()) {
						String pluglist = pluginlist.getDescription().getName();
						plugin.add(pluglist);
					}
					return plugin;
				}
				if (args[0].equalsIgnoreCase("load")) {
					List<String> jarfile = new ArrayList<String>();
					File parentFolder = new File (BungeePluginManager.get().getDataFolder().getParent());
					for (File jar : parentFolder.listFiles()) {
						if (jar.isFile()) {
							try {
								String filename = jar.getName();
								filename = filename.trim().replaceAll(".jar", "");
								jarfile.add(filename);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					return jarfile;
				}
			}
			if (args.length >= 3) {
				return BungeePluginManager.TabshowNone();
			}
		} else {
			return BungeePluginManager.TabshowNone();
		}
		return BungeePluginManager.TabshowNone();
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendHelp(sender);
			return;
		} else if (args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(textWithColor(ProxyServer.getInstance().getPluginManager().getPlugins().stream().map(plugin -> plugin.getDescription().getName()).collect(Collectors.joining(", ")), ChatColor.GREEN));
			return;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (args.length == 1) {
				ComponentBuilder builder = new ComponentBuilder("");
				builder.append("---- BungeePluginManager ----\n").color(GOLD).bold(true);
				builder.append("/bpm reload ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm reload ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Reloads a plugin\n", ComponentBuilder.FormatRetention.NONE);
				sender.sendMessage(builder.create());
				return;
			} else {
				Plugin plugin = findPlugin(args[1]);
				if (plugin == null) {
					sender.sendMessage(textWithColor("Plugin not found", ChatColor.RED));
					return;
				}
				File pluginfile = plugin.getFile();

				Exception unloadError = PluginUtils.unloadPlugin(plugin);
				if (unloadError != null) {
					sender.sendMessage(textWithColor("Errors occured while disabling plugin, see console for more details", ChatColor.RED));
					unloadError.printStackTrace();
				}
				try {
					PluginUtils.loadPlugin(pluginfile);
					sender.sendMessage(textWithColor("Plugin reloaded", ChatColor.YELLOW));
				} catch (Throwable t) {
					sender.sendMessage(textWithColor("Error occured while loading plugin, see console for more details", ChatColor.RED));
					t.printStackTrace();
				}
				return;
			}
		} else if (args[0].equalsIgnoreCase("load")) {
			if (args.length == 1) {
				ComponentBuilder builder = new ComponentBuilder("");
				builder.append("---- BungeePluginManager ----\n").color(GOLD).bold(true);
				builder.append("/bpm load ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm load ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Loads a plugin\n", ComponentBuilder.FormatRetention.NONE);
				sender.sendMessage(builder.create());
				return;
			} else {
				Plugin plugin = findPlugin(args[1]);
				if (plugin != null) {
					sender.sendMessage(textWithColor("Plugin is already loaded", ChatColor.RED));
					return;
				}
				File file = findFile(args[1]);
				if (!file.exists()) {
					sender.sendMessage(textWithColor("Plugin not found", ChatColor.RED));
					return;
				}

				try {
					PluginUtils.loadPlugin(file);
					sender.sendMessage(textWithColor("Plugin loaded", ChatColor.YELLOW));
				} catch (Throwable t) {
					sender.sendMessage(textWithColor("Error occured while loading plugin, see console for more details", ChatColor.RED));
					t.printStackTrace();
				}
				return;
			}
		} else if (args[0].equalsIgnoreCase("unload")) {
			if (args.length == 1) {
				ComponentBuilder builder = new ComponentBuilder("");
				builder.append("---- BungeePluginManager ----\n").color(GOLD).bold(true);
				builder.append("/bpm unload ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm unload ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Unloads a plugin\n", ComponentBuilder.FormatRetention.NONE);
				sender.sendMessage(builder.create());
				return;
			} else {
				Plugin plugin = findPlugin(args[1]);
				if (plugin == null) {
					sender.sendMessage(textWithColor("Plugin not found", ChatColor.RED));
					return;
				}
				Exception unloadError = PluginUtils.unloadPlugin(plugin);
				sender.sendMessage(textWithColor(GRAY + ITALIC.toString() + args[1] + " unloaded", ChatColor.YELLOW));
				if (unloadError != null) {
					sender.sendMessage(textWithColor("Errors occured while disabling plugin, see console for more details", ChatColor.RED));
					unloadError.printStackTrace();
				}
				return;
			}
		} else if (args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return;
		} else {
			sender.sendMessage(new TextComponent(RED + "Error: " + DARK_RED + "Sub-Command Not Found!"));
			return;
		}
	}

	private static Plugin findPlugin(String pluginname) {
		for (Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins()) {
			if (plugin.getDescription().getName().equalsIgnoreCase(pluginname)) {
				return plugin;
			}
		}
		return null;
	}

	private static File findFile(String pluginname) {
		File folder = ProxyServer.getInstance().getPluginsFolder();
		if (folder.exists()) {
			for (File file : folder.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".jar")) {
					try (JarFile jar = new JarFile(file)) {
						JarEntry pdf = jar.getJarEntry("bungee.yml");
						if (pdf == null) {
							pdf = jar.getJarEntry("plugin.yml");
						}
						try (InputStream in = jar.getInputStream(pdf)) {
							final PluginDescription desc = new Yaml().loadAs(in, PluginDescription.class);
							if (desc.getName().equalsIgnoreCase(pluginname)) {
								return file;
							}
						}
					} catch (Throwable ex) {
					}
				}
			}
		}
		return new File(folder, pluginname+".jar");
	}
	private static TextComponent textWithColor(String message, ChatColor color) {
		TextComponent text = new TextComponent(message);
		text.setColor(color);
		return text;
	}
	private static void sendHelp(CommandSender sender) {
		ComponentBuilder builder = new ComponentBuilder("");
		builder.append("---- BungeePluginManager ----\n").color(GOLD).bold(true);
		builder.append("/bpm help: ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bpm help")).color(GOLD).bold(false).append("Display this message\n", ComponentBuilder.FormatRetention.NONE);
		builder.append("/bpm load ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm load ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Loads a plugin\n", ComponentBuilder.FormatRetention.NONE);
		builder.append("/bpm unload ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm unload ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Unloads a plugin\n", ComponentBuilder.FormatRetention.NONE);
		builder.append("/bpm reload ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bpm reload ")).color(GOLD).append("<plugin>: ").color(GREEN).append("Reloads a plugin\n", ComponentBuilder.FormatRetention.NONE);
		builder.append("/bpm list: ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bpm list")).color(GOLD).append("List all plugins on the bungee", ComponentBuilder.FormatRetention.NONE);
		sender.sendMessage(builder.create());
	}
}
