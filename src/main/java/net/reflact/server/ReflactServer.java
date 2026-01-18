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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflactServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReflactServer.class);

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
      LOGGER.info("Saving world...");
      instanceContainer.saveChunksToStorage();
    });

    // Initialize Server Console
    if (!java.awt.GraphicsEnvironment.isHeadless()) {
      new net.reflact.server.gui.ServerConsole().start();
    } else {
      LOGGER.info("Headless environment detected. GUI Console disabled.");
    }

    // Console handler (stdin fallback)
    new Thread(() -> {
      try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.trim().isEmpty())
            continue;
          MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().getConsoleSender(), line);
        }
      } catch (java.io.IOException e) {
        e.printStackTrace();
      }
    }, "Console-Thread").start();

    LOGGER.info("Starting server on port 25565 (Online Mode Enabled)...");
    minecraftServer.start("0.0.0.0", 25565);
  }
}
