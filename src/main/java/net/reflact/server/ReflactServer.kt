package net.reflact.server

import net.minestom.server.MinecraftServer
import net.minestom.server.Auth
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
// import net.minestom.server.extras.MojangAuth
import net.reflact.engine.ReflactEngine
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.awt.GraphicsEnvironment
import net.reflact.server.gui.ServerConsole
import kotlin.concurrent.thread

object ReflactServer {
    private val LOGGER = LoggerFactory.getLogger(ReflactServer::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        // Initialize the server
        val minecraftServer = MinecraftServer.init(Auth.Online())

        // Initialize our engine
        ReflactEngine.init()

        val instanceManager = MinecraftServer.getInstanceManager()
        // Use AnvilLoader for persistence. 'world' is the folder name.
        val instanceContainer = instanceManager.createInstanceContainer(AnvilLoader("world"))
        instanceContainer.setGenerator { unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK) }
        instanceContainer.setChunkSupplier(::LightingChunk)

        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player = event.player
            event.spawningInstance = instanceContainer
            player.respawnPoint = Pos(0.0, 42.0, 0.0)

            // Register channels so client knows it can send packets
            ReflactEngine.getNetworkManager().onPlayerConfiguration(player)
        }

        // Shutdown hook to save chunks
        MinecraftServer.getSchedulerManager().buildShutdownTask {
            LOGGER.info("Saving world...")
            instanceContainer.saveChunksToStorage().join()
        }

        // Spawn NPCs
        // NpcManager.spawnAll logic needs to be implemented or accessed correctly if added
        ReflactEngine.getNpcManager().spawnNpcs(instanceContainer)

        // Initialize Server Console
        if (!GraphicsEnvironment.isHeadless()) {
            ServerConsole().start()
        } else {
            LOGGER.info("Headless environment detected. GUI Console disabled.")
        }

        // Console handler (stdin fallback)
        thread(name = "Console-Thread") {
            try {
                BufferedReader(InputStreamReader(System.`in`)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.trim().isEmpty()) continue
                        MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().consoleSender, line!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        LOGGER.info("Starting server on port 25565 (Online Mode Enabled)...")
        minecraftServer.start("0.0.0.0", 25565)
    }
}
