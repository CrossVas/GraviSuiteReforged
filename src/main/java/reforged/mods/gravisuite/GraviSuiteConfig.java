package reforged.mods.gravisuite;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import reforged.mods.gravisuite.utils.Refs;

import java.io.File;

public class GraviSuiteConfig {

    public static Configuration id_config;
    public static Configuration main_config;
    public static String languages;
    public static int magnet_range = 8;
    public static boolean log_wrench = false;
    public static boolean enable_hud = true;
    public static boolean use_fixed_values = true;
    public static int hud_position = 1;

    public static int hud_pos_energy_x = 3;
    public static int hud_pos_energy_y = 3;

    public static int hud_pos_jetpack_x = 3;
    public static int hud_pos_jetpack_y = 15;

    public static int hud_pos_gravi_x = 3;
    public static int hud_pos_gravi_y = 15;

    public static int SUPERCONDUCTOR_COVER_ID = 30219;
    public static int SUPERCONDUCTOR_ID = 30220;
    public static int COOLING_CORE_ID = 30221;
    public static int GRAVI_ENGINE_ID = 30222;
    public static int MAGNETRON_ID = 30223;
    public static int VAJRA_CORE_ID = 30224;
    public static int ENGINE_BOOSTER_ID = 30225;
    public static int ADVANCED_DIAMOND_DRILL = 30226;
    public static int ADVANCED_IRIDIUM_DRILL = 30227;
    public static int ADVANCED_CHAINSAW_ID = 30228;
    public static int VAJRA_ID = 30229;
    public static int MAGNET_ID = 30230;
    public static int GRAVI_TOOL_ID = 30231;
    public static int ADVANCED_LAPPACK_ID = 30232;
    public static int ULTIMATE_LAPPACK_ID = 30233;
    public static int ADVANCED_JETPACK_ID = 30234;
    public static int ADVANCED_NANO_ID = 30235;
    public static int ADVANCED_QUANT_ID = 30236;

    public static void initConfig() {
        id_config = new Configuration(new File(Minecraft.getMinecraftDir(), "/config/gravisuite/ids.cfg"));
        id_config.load();

        SUPERCONDUCTOR_COVER_ID = getId("supercondustor_cover", SUPERCONDUCTOR_COVER_ID, "supercondustor_cover");
        SUPERCONDUCTOR_ID = getId("superconductor", SUPERCONDUCTOR_ID, "superconductor");
        COOLING_CORE_ID = getId("cooling_core", COOLING_CORE_ID, "cooling_core_id");
        GRAVI_ENGINE_ID = getId("gravi_engine", GRAVI_ENGINE_ID, "gravi_engine_id");
        MAGNETRON_ID = getId("magnetron", MAGNETRON_ID, "magnetron_id");
        VAJRA_CORE_ID = getId("vajra_core", VAJRA_CORE_ID, "vajra_core_id");
        ENGINE_BOOSTER_ID = getId("engine_booster", ENGINE_BOOSTER_ID, "engine_booster_id");

        ADVANCED_DIAMOND_DRILL = getId("advanced_diamond_drill", ADVANCED_DIAMOND_DRILL, "Advanced Diamond Drill ID");
        ADVANCED_IRIDIUM_DRILL = getId("advanced_iridium_drill", ADVANCED_IRIDIUM_DRILL, "Advanced Iridium Drill ID");
        ADVANCED_CHAINSAW_ID = getId("advanced_chainsaw", ADVANCED_CHAINSAW_ID, "advanced_chainsaw_id");
        GRAVI_TOOL_ID = getId("gravitool_id", GRAVI_TOOL_ID, "gravitool_id");
        VAJRA_ID = getId("vajra", VAJRA_ID, "vajra_id");
        MAGNET_ID = getId("magnet", MAGNET_ID, "magnet_id");

        ADVANCED_LAPPACK_ID = getId("advanced_lappack", ADVANCED_LAPPACK_ID, "advanced_lappack_id");
        ULTIMATE_LAPPACK_ID = getId("utlimate_lappack", ULTIMATE_LAPPACK_ID, "utlimate_lappack_id");
        ADVANCED_JETPACK_ID = getId("advanced_jetpack_id", ADVANCED_JETPACK_ID, "advanced_jetpack_id");
        ADVANCED_NANO_ID = getId("advanced_nano_id", ADVANCED_NANO_ID, "advanced_nano_id");
        ADVANCED_QUANT_ID = getId("advanced_quant_id", ADVANCED_QUANT_ID, "advanced_quant_id");

        if (id_config.hasChanged()) id_config.save();

        main_config = new Configuration(new File(Minecraft.getMinecraftDir(), "/config/gravisuite/common.cfg"));

        enable_hud = getBoolean(Refs.hud, "enable_hud", enable_hud, "Should GraviSuite display the HUD with info about electric armor?");
        use_fixed_values = getBoolean(Refs.hud, "enable_hud_fixed", use_fixed_values, "Should GraviSuite HUD use fixed values from below?");
        hud_position = getInt(Refs.hud, "hud_pos", 1, 4, hud_position, "Fixed HUD Position. [1] - TOP_LEFT, [2] - TOP_RIGHT, [3] - BOTTOM_LEFT, [4] - BOTTOM_RIGHT");

        hud_pos_energy_x = getInt(Refs.hud, "hud_pos_energy_x", 0, Integer.MAX_VALUE, hud_pos_energy_x, "X Pos for energy status info.");
        hud_pos_energy_y = getInt(Refs.hud, "hud_pos_energy_y", 0, Integer.MAX_VALUE, hud_pos_energy_y, "Y Pos for energy status info.");

        hud_pos_jetpack_x = getInt(Refs.hud, "hud_pos_jetpack_x", 0, Integer.MAX_VALUE, hud_pos_jetpack_x, "X Pos for jetpack status info.");
        hud_pos_jetpack_y = getInt(Refs.hud, "hud_pos_jetpack_y", 0, Integer.MAX_VALUE, hud_pos_jetpack_y, "Y Pos for jetpack status info.");

        hud_pos_gravi_x = getInt(Refs.hud, "hud_pos_gravi_x", 0, Integer.MAX_VALUE, hud_pos_gravi_x, "X Pos for Gravitational Chestplate status info.");
        hud_pos_gravi_y = getInt(Refs.hud, "hud_pos_gravi_y", 0, Integer.MAX_VALUE, hud_pos_gravi_y, "Y Pos for Gravitational Chestplate status info.");

        log_wrench = getBoolean(Refs.general, "enable_wrench_logging", log_wrench, "Should GraviTool Wrench be logged? [Debug purposes only!]");
        languages = getString(Refs.general, "localization_list", "en_US", "Supported localizations. Place your <name>.lang file in gravisuite/lang folder and list <name> here. Format: no spaces, comma separated. Ex: <name>,<name>");
        magnet_range = getInt(Refs.general, "magnet_range", 1, 16, magnet_range, "Magnet Range.");

        if (main_config.hasChanged()) main_config.save();
    }

    private static int getInt(String cat, String tag, int min, int max, int defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = main_config.get(cat, tag, defaultValue);
        prop.comment = comment + "Min: " + min + ", Max: " + max + ", Default: " + defaultValue;
        int value = prop.getInt(defaultValue);
        value = Math.max(value, min);
        value = Math.min(value, max);
        prop.set(Integer.toString(value));
        return value;
    }

    public static String getString(String cat, String tag, String defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = main_config.get(cat, tag, defaultValue);
        prop.comment = comment + "Default: " + defaultValue;
        return prop.getString();
    }

    public static int getId(String tag, int defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = id_config.get(Refs.IDs, tag, defaultValue);
        prop.comment = comment + "Default: " + defaultValue;
        int value = prop.getInt(defaultValue);
        prop.set(Integer.toString(value));
        return value;
    }

    private static boolean getBoolean(String cat, String tag, boolean defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = main_config.get(cat, tag, defaultValue);
        prop.comment = comment + "Default: " + defaultValue;
        return prop.getBoolean(defaultValue);
    }
}
