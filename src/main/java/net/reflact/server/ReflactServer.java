package net.reflact.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.Auth;
import net.reflact.engine.ReflactEngine;

public class ReflactServer {
    public static void main(String[] args) {
        // Initialize the server with Online Mode
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());
        
        // Initialize our engine
        ReflactEngine.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Use AnvilLoader for persistence. 'world' is the folder name.
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(new AnvilLoader("world"));
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Shutdown hook to save chunks
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            System.out.println("Saving world...");
            instanceContainer.saveChunksToStorage();
        });

        System.out.println("Starting server on port 25565...");
        minecraftServer.start("0.0.0.0", 25565);
    }
}
