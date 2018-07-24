package bungeepluginmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeePluginManager extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new BungeePluginManagerCommand());
        ReflectionUtils.setFieldValue(ProxyServer.getInstance().getPluginManager(), "eventBus", new ModifiedPluginEventBus());
    }

}
