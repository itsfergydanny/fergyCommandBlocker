package com.fergydanny.fergycommandblocker;

import com.fergydanny.fergycommandblocker.listeners.CommandListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class FergyCommandBlocker extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
    }
}
