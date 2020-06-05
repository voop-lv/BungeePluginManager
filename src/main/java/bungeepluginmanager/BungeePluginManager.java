package bungeepluginmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePluginManager extends Plugin {
	public static BungeePluginManager instance;
	public static BungeePluginManager get() { return instance; }
	@Override
	public void onLoad() {
		instance = this;
		try {
			ReflectionUtils.setFieldValue(ProxyServer.getInstance().getPluginManager(), "eventBus", new ModifiedPluginEventBus());
		} catch (IllegalAccessException | NoSuchFieldException e) {
			getLogger().log(Level.SEVERE, "Unable to inject modified command bus, completing plugin async intents won't work", e);
		}
	}
	public static List TabshowNone() {
		List<String> none = new ArrayList<String>();
		return none;
	}
	@Override
	public void onEnable() {
		getProxy().getPluginManager().registerCommand(this, new Commands());
		getProxy().getLogger().info("BungeePluginManager Loaded!");
	}

}
