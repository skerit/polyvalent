package rocks.blackblock.polyvalent.server;

import com.mojang.brigadier.Command;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.misc.PolyDumper;
import io.github.theepicblock.polymc.impl.misc.logging.CommandSourceLogger;
import io.github.theepicblock.polymc.impl.misc.logging.ErrorTrackerWrapper;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import rocks.blackblock.polyvalent.PolyvalentServer;

import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers the polymc commands.
 */
public class PolyvalentCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("polyvalent").requires(source -> source.hasPermissionLevel(2))
                    .then(literal("generate")
                            .then(literal("resources")
                                    .executes((context -> {
                                        SimpleLogger commandSource = new CommandSourceLogger(context.getSource(), true);
                                        ErrorTrackerWrapper logger = new ErrorTrackerWrapper(PolyMc.LOGGER);
                                        try {
                                            ResourcePackGenerator.generate(PolyvalentServer.getMainMap(), "resource", logger);
                                        } catch (Exception e) {
                                            commandSource.error("An error occurred whilst trying to generate the resource pack! Please check the console.");
                                            e.printStackTrace();
                                            return 0;
                                        }
                                        if (logger.errors != 0) {
                                            commandSource.error("There have been errors whilst generating the resource pack. These are usually completely normal. It only means that PolyMc couldn't find some of the textures or models. See the console for more info.");
                                        }
                                        commandSource.info("Finished generating resource pack");
                                        commandSource.warn("Before hosting this resource pack, please make sure you have the legal right to redistribute the assets inside.");
                                        return Command.SINGLE_SUCCESS;
                                    })))
                            .then(literal("polyDump")
                                    .executes((context) -> {
                                        SimpleLogger logger = new CommandSourceLogger(context.getSource(), true);
                                        try {
                                            PolyDumper.dumpPolyMap(PolyvalentServer.getMainMap(), "PolyvalentDump.txt", logger);
                                        } catch (IOException e) {
                                            logger.error(e.getMessage());
                                            return 0;
                                        } catch (Exception e) {
                                            logger.info("An error occurred whilst trying to generate the poly dump! Please check the console.");
                                            e.printStackTrace();
                                            return 0;
                                        }
                                        logger.info("Finished generating poly dump");
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            ));
        });
    }
}
