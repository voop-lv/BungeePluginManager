package bungeepluginmanager;

import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventBus;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ModifiedPluginEventBus extends EventBus {

    private static final Set<AsyncEvent<?>> UNCOMPLETED_EVENTS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Object LOCK = new Object();

    static void completeIntents(Plugin plugin) {
        synchronized (LOCK) {
            UNCOMPLETED_EVENTS.forEach(event -> {
                try {
                    event.completeIntent(plugin);
                } catch (Exception error) {
                    // Ignored
                }
            });
        }
    }

    @Override
    public void post(Object event) {
        if (event instanceof AsyncEvent) {
            synchronized (LOCK) {
                UNCOMPLETED_EVENTS.add((AsyncEvent<?>) event);
            }
        }
        super.post(event);
    }

}
