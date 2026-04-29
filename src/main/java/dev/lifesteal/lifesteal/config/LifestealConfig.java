package dev.lifesteal.lifesteal.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LifestealConfig {
    private static final Path CONFIG_PATH = Paths.get("config", "lifesteal.properties");

    private static final LifestealConfig DEFAULTS = new LifestealConfig();

    public boolean allowGodApples = false;
    public boolean allowStrengthII = false;
    public boolean allowSwiftnessII = false;
    public boolean allowDebuffPotions = false;
    public boolean allowInstantHealingPotions = false;
    public boolean allowTippedArrows = false;
    public boolean enableEnchantmentLimits = false;
    public boolean allowNetheriteUpgrades = true;
    public boolean balancedMace = false;
    public boolean lockMaceRecipe = true;
    public boolean disableEnderPearls = false;
    public boolean disableCrystalPVP = true;
    public boolean disableTotems = false;
    public boolean disableBedBombing = false;
    public boolean nerfTntMinecarts = true;
    public boolean allowTNTMinecarts = true;
    public boolean enableRiptideCooldown = false;
    public boolean hideInvisPlayerKillCredit = false;
    public int maxHearts = 20;
    public int withdrawnHeartValue = 1;
    public int riptideCooldown = 200;

    private static LifestealConfig INSTANCE = new LifestealConfig();

    public static LifestealConfig get() {
        return INSTANCE;
    }

    public static List<ConfigOption> getOptions() {
        return OPTIONS;
    }

    public static ConfigOption getOption(String key) {
        if (key == null) {
            return null;
        }
        for (ConfigOption option : OPTIONS) {
            if (option.key.equalsIgnoreCase(key)) {
                return option;
            }
        }
        return null;
    }

    public static LifestealConfig load() {
        LifestealConfig config = new LifestealConfig();

        try {
            Path legacyJsonPath = Paths.get("config", "lifesteal.json");
            if (!Files.exists(CONFIG_PATH) && Files.exists(legacyJsonPath)) {
                migrateFromJson(legacyJsonPath, config);
            }

            if (Files.exists(CONFIG_PATH)) {
                List<String> lines = Files.readAllLines(CONFIG_PATH);
                config.allowGodApples = getBoolean(lines, "allowGodApples", config.allowGodApples);
                config.allowStrengthII = getBoolean(lines, "allowStrengthII", config.allowStrengthII);
                config.allowSwiftnessII = getBoolean(lines, "allowSwiftnessII", config.allowSwiftnessII);
                config.allowDebuffPotions = getBoolean(lines, "allowDebuffPotions", config.allowDebuffPotions);
                if (getRawValue(lines, "allowDebuffPotions") == null) {
                    boolean legacyPoison = getBoolean(lines, "allowPoisonPotions", false);
                    boolean legacyInstantDamage = getBoolean(lines, "allowInstantDamagePotions", false);
                    config.allowDebuffPotions = legacyPoison || legacyInstantDamage;
                }
                config.allowInstantHealingPotions = getBoolean(lines, "allowInstantHealingPotions", config.allowInstantHealingPotions);
                config.allowTippedArrows = getBoolean(lines, "allowTippedArrows", config.allowTippedArrows);
                config.enableEnchantmentLimits = getBoolean(lines, "enableEnchantmentLimits", config.enableEnchantmentLimits);
                config.allowNetheriteUpgrades = getBoolean(lines, "allowNetheriteUpgrades", config.allowNetheriteUpgrades);
                config.balancedMace = getBoolean(lines, "balancedMace", config.balancedMace);
                config.lockMaceRecipe = getBoolean(lines, "lockMaceRecipe", config.lockMaceRecipe);
                config.disableEnderPearls = getBoolean(lines, "disableEnderPearls", config.disableEnderPearls);
                config.disableCrystalPVP = getBoolean(lines, "disableCrystalPVP",
                        getBoolean(lines, "disableEndCrystalDamage",
                                getBoolean(lines, "disableCrystalDamage", config.disableCrystalPVP)));
                config.disableTotems = getBoolean(lines, "disableTotems", config.disableTotems);
                config.disableBedBombing = getBoolean(lines, "disableBedBombing", config.disableBedBombing);
                config.nerfTntMinecarts = getBoolean(lines, "nerfTntMinecarts", config.nerfTntMinecarts);
                config.allowTNTMinecarts = getBoolean(lines, "allowTNTMinecarts", config.allowTNTMinecarts);
                config.enableRiptideCooldown = getBoolean(lines, "enableRiptideCooldown", config.enableRiptideCooldown);
                config.hideInvisPlayerKillCredit = getBoolean(lines, "hideInvisPlayerKillCredit", config.hideInvisPlayerKillCredit);
                config.maxHearts = getInt(lines, "maxHearts", config.maxHearts);
                config.withdrawnHeartValue = getInt(lines, "withdrawnHeartValue", config.withdrawnHeartValue);
                config.riptideCooldown = getInt(lines, "riptideCooldown", config.riptideCooldown);
            }
        } catch (Exception ignored) {
        }

        clamp(config);
        INSTANCE = config;
        save();
        return INSTANCE;
    }

    public static void applySingleplayerOverrides(LifestealClientConfig clientConfig) {
        LifestealConfig config = new LifestealConfig();
        clientConfig.applyTo(config);
        clamp(config);
        INSTANCE = config;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String contents = """
# Lifesteal Mod configuration.
# If enabled, enchanted golden apples do not give you any effects.
allowGodApples: %s
# If enabled, disables Strength II brewing and converts it to Strength I every time you try to apply it to yourself using commands.
allowStrengthII: %s
# If enabled, disables Swiftness II brewing and converts it to Swiftness I every time you try to apply it to yourself using commands.
allowSwiftnessII: %s
# If enabled, debuff potions (poison, weakness, instant damage) can be brewed and upgraded.
allowDebuffPotions: %s
# If enabled, instant healing potions can be brewed and upgraded.
allowInstantHealingPotions: %s
# If enabled, tipped arrows can apply potion effects.
allowTippedArrows: %s
# If enabled, limits enchantment levels to these for specific enchantments:
# Protection IV -> Protection III
# Sharpness V -> Sharpness IV
# Power V -> Power IV
enableEnchantmentLimits: %s
# If enabled, allows netherite upgrades for diamond armor, sword, and axe.
allowNetheriteUpgrades: %s
# If set to true, maces cannot be enchanted and have a 60-second attack cooldown (shown like ender pearls).
balancedMace: %s
# If enabled, blocks crafting recipes that output a mace.
lockMaceRecipe: %s
# Disables usage of ender pearls if enabled. Set to false by default
disableEnderPearls: %s
# Disables crystal PVP mechanics: end crystal damage and respawn anchor usage/placement.
disableCrystalPVP: %s
# Disables Totem of Undying death protection while keeping totems in inventories.
disableTotems: %s
# Disables bed explosions in Nether and End by blocking bed interaction there.
disableBedBombing: %s
# If enabled, TNT minecarts have reduced explosion radius and capped direct damage.
nerfTntMinecarts: %s
# If disabled, TNT minecart explosions are fully blocked.
allowTNTMinecarts: %s
# Enables cooldown for Riptide tridents
enableRiptideCooldown: %s
# If a player kills someone else with invisibility effect applied to them, it will hide them from kill credit message.
hideInvisPlayerKillCredit: %s
# Maximum amount of hearts. Defaults to 20
maxHearts: %s
# Hearts granted by each withdrawn/creative heart item.
withdrawnHeartValue: %s
# Cooldown for Riptide enchantment
riptideCooldown: %s
""".formatted(
                    INSTANCE.allowGodApples,
                    INSTANCE.allowStrengthII,
                    INSTANCE.allowSwiftnessII,
                    INSTANCE.allowDebuffPotions,
                    INSTANCE.allowInstantHealingPotions,
                    INSTANCE.allowTippedArrows,
                    INSTANCE.enableEnchantmentLimits,
                    INSTANCE.allowNetheriteUpgrades,
                    INSTANCE.balancedMace,
                    INSTANCE.lockMaceRecipe,
                    INSTANCE.disableEnderPearls,
                    INSTANCE.disableCrystalPVP,
                    INSTANCE.disableTotems,
                    INSTANCE.disableBedBombing,
                    INSTANCE.nerfTntMinecarts,
                    INSTANCE.allowTNTMinecarts,
                    INSTANCE.enableRiptideCooldown,
                    INSTANCE.hideInvisPlayerKillCredit,
                    INSTANCE.maxHearts,
                    INSTANCE.withdrawnHeartValue,
                    INSTANCE.riptideCooldown
            );
            Files.writeString(CONFIG_PATH, contents);
        } catch (Exception ignored) {
        }
    }

    private static boolean getBoolean(List<String> lines, String key, boolean defaultValue) {
        String value = getRawValue(lines, key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static int getInt(List<String> lines, String key, int defaultValue) {
        String value = getRawValue(lines, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static String getRawValue(List<String> lines, String key) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separatorIndex = trimmed.indexOf(':');
            if (separatorIndex < 0) {
                separatorIndex = trimmed.indexOf('=');
            }
            if (separatorIndex < 0) {
                continue;
            }
            String foundKey = trimmed.substring(0, separatorIndex).trim();
            if (!foundKey.equals(key)) {
                continue;
            }
            return trimmed.substring(separatorIndex + 1).trim();
        }
        return null;
    }

    private static void migrateFromJson(Path legacyJsonPath, LifestealConfig config) {
        try {
            String json = Files.readString(legacyJsonPath);
            config.allowGodApples = extractBoolean(json, "allowGodApples", config.allowGodApples);
            config.allowStrengthII = extractBoolean(json, "allowStrengthII", config.allowStrengthII);
            config.allowSwiftnessII = extractBoolean(json, "allowSwiftnessII", config.allowSwiftnessII);
            config.allowDebuffPotions = extractBoolean(json, "allowDebuffPotions", config.allowDebuffPotions);
            if (!json.contains("\"allowDebuffPotions\"")) {
                boolean legacyPoison = extractBoolean(json, "allowPoisonPotions", false);
                boolean legacyInstantDamage = extractBoolean(json, "allowInstantDamagePotions", false);
                config.allowDebuffPotions = legacyPoison || legacyInstantDamage;
            }
            config.allowInstantHealingPotions = extractBoolean(json, "allowInstantHealingPotions", config.allowInstantHealingPotions);
            config.allowTippedArrows = extractBoolean(json, "allowTippedArrows", config.allowTippedArrows);
            config.enableEnchantmentLimits = extractBoolean(json, "enableEnchantmentLimits", config.enableEnchantmentLimits);
            config.allowNetheriteUpgrades = extractBoolean(json, "allowNetheriteUpgrades", config.allowNetheriteUpgrades);
            config.balancedMace = extractBoolean(json, "balancedMace", config.balancedMace);
            config.lockMaceRecipe = extractBoolean(json, "lockMaceRecipe", config.lockMaceRecipe);
            config.disableEnderPearls = extractBoolean(json, "disableEnderPearls", config.disableEnderPearls);
            config.disableCrystalPVP = extractBoolean(json, "disableCrystalPVP",
                    extractBoolean(json, "disableEndCrystalDamage",
                            extractBoolean(json, "disableCrystalDamage", config.disableCrystalPVP)));
            config.disableTotems = extractBoolean(json, "disableTotems", config.disableTotems);
            config.disableBedBombing = extractBoolean(json, "disableBedBombing", config.disableBedBombing);
            config.nerfTntMinecarts = extractBoolean(json, "nerfTntMinecarts", config.nerfTntMinecarts);
            config.allowTNTMinecarts = extractBoolean(json, "allowTNTMinecarts", config.allowTNTMinecarts);
            config.enableRiptideCooldown = extractBoolean(json, "enableRiptideCooldown", config.enableRiptideCooldown);
            config.hideInvisPlayerKillCredit = extractBoolean(json, "hideInvisPlayerKillCredit", config.hideInvisPlayerKillCredit);
            config.maxHearts = extractInt(json, "maxHearts", config.maxHearts);
            config.withdrawnHeartValue = extractInt(json, "withdrawnHeartValue", config.withdrawnHeartValue);
            config.riptideCooldown = extractInt(json, "riptideCooldown", config.riptideCooldown);
            Files.deleteIfExists(legacyJsonPath);
        } catch (Exception ignored) {
        }
    }

    private static boolean extractBoolean(String json, String key, boolean fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    private static int extractInt(String json, String key, int fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void clamp(LifestealConfig config) {
        config.maxHearts = Math.max(1, Math.min(1000, config.maxHearts));
        config.withdrawnHeartValue = Math.max(1, Math.min(1000, config.withdrawnHeartValue));
        config.riptideCooldown = Math.max(0, Math.min(72000, config.riptideCooldown));
    }

    public static final class ConfigOption {
        public enum Type {
            BOOLEAN,
            INTEGER
        }

        public final String key;
        public final String description;
        public final Type type;
        private final boolean defaultBooleanValue;
        private final int defaultIntegerValue;
        private final BooleanSupplier booleanGetter;
        private final Consumer<Boolean> booleanSetter;
        private final IntSupplier integerGetter;
        private final IntConsumer integerSetter;
        private final int minInteger;
        private final int maxInteger;

        private ConfigOption(String key, String description, boolean defaultValue, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.key = key;
            this.description = description;
            this.type = Type.BOOLEAN;
            this.defaultBooleanValue = defaultValue;
            this.defaultIntegerValue = 0;
            this.booleanGetter = getter;
            this.booleanSetter = setter;
            this.integerGetter = null;
            this.integerSetter = null;
            this.minInteger = 0;
            this.maxInteger = 0;
        }

        private ConfigOption(String key, String description, int defaultValue, int minValue, int maxValue, IntSupplier getter, IntConsumer setter) {
            this.key = key;
            this.description = description;
            this.type = Type.INTEGER;
            this.defaultBooleanValue = false;
            this.defaultIntegerValue = defaultValue;
            this.booleanGetter = null;
            this.booleanSetter = null;
            this.integerGetter = getter;
            this.integerSetter = setter;
            this.minInteger = minValue;
            this.maxInteger = maxValue;
        }

        public boolean currentBooleanValue() {
            return booleanGetter != null && booleanGetter.getAsBoolean();
        }

        public int currentIntegerValue() {
            return integerGetter != null ? integerGetter.getAsInt() : 0;
        }

        public boolean defaultBooleanValue() {
            return defaultBooleanValue;
        }

        public int defaultIntegerValue() {
            return defaultIntegerValue;
        }

        public int minInteger() {
            return minInteger;
        }

        public int maxInteger() {
            return maxInteger;
        }

        public String currentValueAsText() {
            return type == Type.BOOLEAN
                    ? Boolean.toString(currentBooleanValue())
                    : Integer.toString(currentIntegerValue());
        }

        public boolean isDefaultValue() {
            if (type == Type.BOOLEAN) {
                return currentBooleanValue() == defaultBooleanValue;
            }
            return currentIntegerValue() == defaultIntegerValue;
        }

        public void setValueFromString(String value) {
            if (type == Type.BOOLEAN) {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("Expected true or false.");
                }
                booleanSetter.accept(Boolean.parseBoolean(value));
                return;
            }

            int parsed;
            try {
                parsed = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Expected a number.");
            }

            if (parsed < minInteger || parsed > maxInteger) {
                throw new IllegalArgumentException("Value must be between " + minInteger + " and " + maxInteger + ".");
            }
            integerSetter.accept(parsed);
        }
    }

    private static final List<ConfigOption> OPTIONS = List.of(
            new ConfigOption(
                    "allowGodApples",
                    "Disables enchanted golden apple effects.",
                    DEFAULTS.allowGodApples,
                    () -> LifestealConfig.get().allowGodApples,
                    value -> LifestealConfig.get().allowGodApples = value
            ),
            new ConfigOption(
                    "allowStrengthII",
                    "Disables Strength II brewing and downgrades command-applied Strength II to Strength I.",
                    DEFAULTS.allowStrengthII,
                    () -> LifestealConfig.get().allowStrengthII,
                    value -> LifestealConfig.get().allowStrengthII = value
            ),
            new ConfigOption(
                    "allowSwiftnessII",
                    "Disables Swiftness II brewing and downgrades command-applied Swiftness II to Swiftness I.",
                    DEFAULTS.allowSwiftnessII,
                    () -> LifestealConfig.get().allowSwiftnessII,
                    value -> LifestealConfig.get().allowSwiftnessII = value
            ),
            new ConfigOption(
                    "allowDebuffPotions",
                    "Allows brewing and upgrading debuff potions (poison, weakness, instant damage).",
                    DEFAULTS.allowDebuffPotions,
                    () -> LifestealConfig.get().allowDebuffPotions,
                    value -> LifestealConfig.get().allowDebuffPotions = value
            ),
            new ConfigOption(
                    "allowInstantHealingPotions",
                    "Allows brewing and upgrading instant healing potions.",
                    DEFAULTS.allowInstantHealingPotions,
                    () -> LifestealConfig.get().allowInstantHealingPotions,
                    value -> LifestealConfig.get().allowInstantHealingPotions = value
            ),
            new ConfigOption(
                    "allowTippedArrows",
                    "Allows tipped arrows to apply potion effects.",
                    DEFAULTS.allowTippedArrows,
                    () -> LifestealConfig.get().allowTippedArrows,
                    value -> LifestealConfig.get().allowTippedArrows = value
            ),
            new ConfigOption(
                    "enableEnchantmentLimits",
                    "Limits specific enchantments to lower max levels (Protection III, Sharpness IV, Power IV).",
                    DEFAULTS.enableEnchantmentLimits,
                    () -> LifestealConfig.get().enableEnchantmentLimits,
                    value -> LifestealConfig.get().enableEnchantmentLimits = value
            ),
            new ConfigOption(
                    "allowNetheriteUpgrades",
                    "Disables upgrading diamond armor and weapons to netherite (weapons: swords and axes).",
                    DEFAULTS.allowNetheriteUpgrades,
                    () -> LifestealConfig.get().allowNetheriteUpgrades,
                    value -> LifestealConfig.get().allowNetheriteUpgrades = value
            ),
            new ConfigOption(
                    "balancedMace",
                    "If enabled, maces cannot be enchanted and have a 60-second attack cooldown.",
                    DEFAULTS.balancedMace,
                    () -> LifestealConfig.get().balancedMace,
                    value -> LifestealConfig.get().balancedMace = value
            ),
            new ConfigOption(
                    "lockMaceRecipe",
                    "Blocks crafting recipes that output a mace.",
                    DEFAULTS.lockMaceRecipe,
                    () -> LifestealConfig.get().lockMaceRecipe,
                    value -> LifestealConfig.get().lockMaceRecipe = value
            ),
            new ConfigOption(
                    "disableEnderPearls",
                    "Disables ender pearls from being used",
                    DEFAULTS.disableEnderPearls,
                    () -> LifestealConfig.get().disableEnderPearls,
                    value -> LifestealConfig.get().disableEnderPearls = value
            ),
            new ConfigOption(
                    "disableCrystalPVP",
                    "Disables end crystal damage and respawn anchor PVP usage.",
                    DEFAULTS.disableCrystalPVP,
                    () -> LifestealConfig.get().disableCrystalPVP,
                    value -> LifestealConfig.get().disableCrystalPVP = value
            ),
            new ConfigOption(
                    "disableTotems",
                    "Removes all totems from player inventories every tick.",
                    DEFAULTS.disableTotems,
                    () -> LifestealConfig.get().disableTotems,
                    value -> LifestealConfig.get().disableTotems = value
            ),
            new ConfigOption(
                    "disableBedBombing",
                    "Disables bed explosions in the Nether and End.",
                    DEFAULTS.disableBedBombing,
                    () -> LifestealConfig.get().disableBedBombing,
                    value -> LifestealConfig.get().disableBedBombing = value
            ),
            new ConfigOption(
                    "nerfTntMinecarts",
                    "Reduces TNT minecart explosion power and caps direct damage.",
                    DEFAULTS.nerfTntMinecarts,
                    () -> LifestealConfig.get().nerfTntMinecarts,
                    value -> LifestealConfig.get().nerfTntMinecarts = value
            ),
            new ConfigOption(
                    "allowTNTMinecarts",
                    "Allows TNT minecarts to explode. If disabled, TNT minecart explosions are blocked.",
                    DEFAULTS.allowTNTMinecarts,
                    () -> LifestealConfig.get().allowTNTMinecarts,
                    value -> LifestealConfig.get().allowTNTMinecarts = value
            ),
            new ConfigOption(
                    "enableRiptideCooldown",
                    "Enables cooldown for Riptide tridents",
                    DEFAULTS.enableRiptideCooldown,
                    () -> LifestealConfig.get().enableRiptideCooldown,
                    value -> LifestealConfig.get().enableRiptideCooldown = value
            ),
            new ConfigOption(
                    "hideInvisPlayerKillCredit",
                    "If a player kills someone else with invisibility effect applied to them, it will hide them from kill credit message.",
                    DEFAULTS.hideInvisPlayerKillCredit,
                    () -> LifestealConfig.get().hideInvisPlayerKillCredit,
                    value -> LifestealConfig.get().hideInvisPlayerKillCredit = value
            ),
            new ConfigOption(
                    "maxHearts",
                    "The max amount of hearts, default being 20",
                    DEFAULTS.maxHearts,
                    1,
                    1000,
                    () -> LifestealConfig.get().maxHearts,
                    value -> LifestealConfig.get().maxHearts = value
            ),
            new ConfigOption(
                    "withdrawnHeartValue",
                    "Hearts granted by each withdrawn/creative heart item.",
                    DEFAULTS.withdrawnHeartValue,
                    1,
                    1000,
                    () -> LifestealConfig.get().withdrawnHeartValue,
                    value -> LifestealConfig.get().withdrawnHeartValue = value
            ),
            new ConfigOption(
                    "riptideCooldown",
                    "Cooldown for Riptide enchantment",
                    DEFAULTS.riptideCooldown,
                    0,
                    72000,
                    () -> LifestealConfig.get().riptideCooldown,
                    value -> LifestealConfig.get().riptideCooldown = value
            )
    );
}
