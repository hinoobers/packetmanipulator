package org.hinoob.pma;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketManipulator extends JavaPlugin {

    private static PacketManipulator instance;

    private WebServer webServer;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());

        this.webServer = new WebServer(this);
        webServer.start(getConfig().getInt("managementPort", 8080));
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public static PacketManipulator getInstance() {
        return instance;
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
