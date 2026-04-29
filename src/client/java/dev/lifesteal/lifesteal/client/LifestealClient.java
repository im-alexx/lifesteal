package dev.lifesteal.lifesteal.client;

import dev.lifesteal.lifesteal.client.screen.MeteorDetectedScreen;
import dev.lifesteal.lifesteal.client.discord.DiscordRpcManager;
import dev.lifesteal.lifesteal.config.LifestealClientConfig;
import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class LifestealClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("lifesteal-client");
    private static final BlockedClientSignature[] BLOCKED_CLIENTS = new BlockedClientSignature[] {
            new BlockedClientSignature("Meteor Client", "meteor-client", "meteor_client", "meteorclient"),
            new BlockedClientSignature("Wurst", "wurst", "wurstclient", "wurst-client"),
            new BlockedClientSignature("Krypton Client", "krypton-client", "krypton_client", "kryptonclient")
    };
    private static final int WARNING_DELAY_TICKS = 40;
    private static String detectedBlockedClientName;
    private static boolean warningShown;
    private static int warningDelayTicks;
    private static int rpcConfigSyncTicker;
    private static LifestealClientConfig clientConfig;

    @Override
    public void onInitializeClient() {
        clientConfig = LifestealClientConfig.load();
        clientConfig.save();

        detectedBlockedClientName = findBlockedClient();
        LOGGER.info("Blocked client detection at init: {}", detectedBlockedClientName == null ? "none" : detectedBlockedClientName);
        warningDelayTicks = detectedBlockedClientName != null ? WARNING_DELAY_TICKS : 0;

        ClientTickEvents.END_CLIENT_TICK.register(LifestealClient::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> DiscordRpcManager.shutdown());
    }

    private static void onClientTick(MinecraftClient client) {
        if (++rpcConfigSyncTicker >= 20) {
            rpcConfigSyncTicker = 0;
            LifestealClientConfig.loadDiscordRpcToggle().ifPresent(value -> {
                if (clientConfig.enableCustomDiscordRpc != value) {
                    clientConfig.enableCustomDiscordRpc = value;
                    LOGGER.info("Synced RPC toggle from disk: enableCustomDiscordRpc={}", value);
                    if (!value) {
                        DiscordRpcManager.shutdown();
                    } else {
                        DiscordRpcManager.resetState();
                    }
                }
            });
        }

        DiscordRpcManager.tick(client, clientConfig.enableCustomDiscordRpc);

        if (detectedBlockedClientName == null || warningShown) {
            return;
        }

        if (warningDelayTicks > 0) {
            warningDelayTicks--;
            return;
        }

        if (client.getOverlay() != null) {
            return;
        }

        if (client.currentScreen == null && client.world == null) {
            return;
        }

        warningShown = true;
        LOGGER.info("Opening blocked client warning screen on tick for {}.", detectedBlockedClientName);
        client.setScreen(new MeteorDetectedScreen(detectedBlockedClientName));
    }

    public static LifestealClientConfig getClientConfig() {
        if (clientConfig == null) {
            clientConfig = LifestealClientConfig.load();
        }
        return clientConfig;
    }

    public static void applyClientConfig(LifestealClientConfig updatedConfig) {
        boolean oldRpcEnabled = clientConfig != null && clientConfig.enableCustomDiscordRpc;
        clientConfig = updatedConfig.copy();
        clientConfig.save();
        LOGGER.info("Applied client config. enableCustomDiscordRpc={}", clientConfig.enableCustomDiscordRpc);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getServer() != null && !client.getServer().isDedicated()) {
            LifestealConfig.applySingleplayerOverrides(clientConfig);
        }
        if (!clientConfig.enableCustomDiscordRpc) {
            DiscordRpcManager.shutdown();
        } else if (!oldRpcEnabled) {
            DiscordRpcManager.resetState();
        }
    }

    private static String findBlockedClient() {
        FabricLoader loader = FabricLoader.getInstance();
        for (BlockedClientSignature signature : BLOCKED_CLIENTS) {
            for (String modId : signature.modIds) {
                if (loader.isModLoaded(modId)) {
                    return signature.displayName;
                }
            }
        }

        for (ModContainer modContainer : loader.getAllMods()) {
            String normalizedId = normalize(modContainer.getMetadata().getId());
            String normalizedName = normalize(modContainer.getMetadata().getName());
            for (BlockedClientSignature signature : BLOCKED_CLIENTS) {
                if (signature.matches(normalizedId, normalizedName)) {
                    return signature.displayName;
                }
            }
        }
        return null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static final class BlockedClientSignature {
        private final String displayName;
        private final String[] modIds;
        private final String[] normalizedMatchers;

        private BlockedClientSignature(String displayName, String... modIds) {
            this.displayName = displayName;
            this.modIds = modIds;
            this.normalizedMatchers = new String[modIds.length];
            for (int i = 0; i < modIds.length; i++) {
                this.normalizedMatchers[i] = normalize(modIds[i]);
            }
        }

        private boolean matches(String normalizedId, String normalizedName) {
            for (String matcher : normalizedMatchers) {
                if (normalizedId.equals(matcher) || normalizedName.equals(matcher)) {
                    return true;
                }
                if (matcher.length() >= 6 && (normalizedId.contains(matcher) || normalizedName.contains(matcher))) {
                    return true;
                }
            }
            return false;
        }
    }
}
