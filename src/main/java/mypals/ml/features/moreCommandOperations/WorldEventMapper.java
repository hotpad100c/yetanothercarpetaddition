package mypals.ml.features.moreCommandOperations;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class WorldEventMapper {
    public static final Map<String, Integer> WORLD_EVENT_MAP = new HashMap<>();
    static{
        WORLD_EVENT_MAP.put("JUKEBOX_STARTS_PLAYING", 1010);
        WORLD_EVENT_MAP.put("ZOMBIE_CONVERTS_TO_DROWNED", 1040);
        WORLD_EVENT_MAP.put("TRAVEL_THROUGH_PORTAL", 1032);
        WORLD_EVENT_MAP.put("SMASH_ATTACK", 2013);
        WORLD_EVENT_MAP.put("BLOCK_FINISHED_BRUSHING", 3008);
        WORLD_EVENT_MAP.put("VAULT_ACTIVATES", 3015);
        WORLD_EVENT_MAP.put("CRAFTER_SHOOTS", 2010);
        WORLD_EVENT_MAP.put("TRIAL_SPAWNER_SPAWNS_MOB", 3011);
        WORLD_EVENT_MAP.put("BONE_MEAL_USED", 1505);
        WORLD_EVENT_MAP.put("CHORUS_FLOWER_GROWS", 1033);
        WORLD_EVENT_MAP.put("FIRE_EXTINGUISHED", 1009);
        WORLD_EVENT_MAP.put("SPLASH_POTION_SPLASHED", 2002);
        WORLD_EVENT_MAP.put("BLOCK_BROKEN", 2001);
        WORLD_EVENT_MAP.put("OMINOUS_TRIAL_SPAWNER_DETECTS_PLAYER", 3019);
        WORLD_EVENT_MAP.put("COMPOSTER_USED", 1500);
        WORLD_EVENT_MAP.put("ZOMBIE_BREAKS_WOODEN_DOOR", 1021);
        WORLD_EVENT_MAP.put("INSTANT_SPLASH_POTION_SPLASHED", 2007);
        WORLD_EVENT_MAP.put("BEE_FERTILIZES_PLANT", 2011);
        WORLD_EVENT_MAP.put("SNIFFER_EGG_CRACKS", 3009);
        WORLD_EVENT_MAP.put("POINTED_DRIPSTONE_LANDS", 1045);
        WORLD_EVENT_MAP.put("SKELETON_CONVERTS_TO_STRAY", 1048);
        WORLD_EVENT_MAP.put("TRIAL_SPAWNER_TURNS_OMINOUS", 3020);
        WORLD_EVENT_MAP.put("CRAFTER_CRAFTS", 1049);
        WORLD_EVENT_MAP.put("WAX_REMOVED", 3004);
        WORLD_EVENT_MAP.put("ANVIL_LANDS", 1031);
        WORLD_EVENT_MAP.put("SMITHING_TABLE_USED", 1044);
        WORLD_EVENT_MAP.put("OMINOUS_ITEM_SPAWNER_SPAWNS_ITEM", 3021);
        WORLD_EVENT_MAP.put("ENTITY_WIND_CHARGE_THROW", 1051);
        WORLD_EVENT_MAP.put("DISPENSER_LAUNCHES_PROJECTILE", 1002);
        WORLD_EVENT_MAP.put("ENDER_DRAGON_BREAKS_BLOCK", 2008);
        WORLD_EVENT_MAP.put("WITHER_SHOOTS", 1024);
        WORLD_EVENT_MAP.put("CHORUS_FLOWER_DIES", 1034);
        WORLD_EVENT_MAP.put("JUKEBOX_STOPS_PLAYING", 1011);
        WORLD_EVENT_MAP.put("WET_SPONGE_DRIES_OUT", 2009);
        WORLD_EVENT_MAP.put("POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON", 1047);
        WORLD_EVENT_MAP.put("GHAST_WARNS", 1015);
        WORLD_EVENT_MAP.put("WITHER_SPAWNS", 1023);
        WORLD_EVENT_MAP.put("ANVIL_USED", 1030);
        WORLD_EVENT_MAP.put("GHAST_SHOOTS", 1016);
        WORLD_EVENT_MAP.put("CRAFTER_FAILS", 1050);
        WORLD_EVENT_MAP.put("BLAZE_SHOOTS", 1018);
        WORLD_EVENT_MAP.put("BLOCK_SCRAPED", 3005);
        WORLD_EVENT_MAP.put("LECTERN_BOOK_PAGE_TURNED", 1043);
        WORLD_EVENT_MAP.put("ANVIL_DESTROYED", 1029);
        WORLD_EVENT_MAP.put("VAULT_DEACTIVATES", 3016);
        WORLD_EVENT_MAP.put("BREWING_STAND_BREWS", 1035);
        WORLD_EVENT_MAP.put("ENDER_DRAGON_DIES", 1028);
        WORLD_EVENT_MAP.put("GRINDSTONE_USED", 1042);
        WORLD_EVENT_MAP.put("VAULT_EJECTS_ITEM", 3017);
        WORLD_EVENT_MAP.put("WITHER_BREAKS_BLOCK", 1022);
        WORLD_EVENT_MAP.put("SPAWNER_SPAWNS_MOB", 2004);
        WORLD_EVENT_MAP.put("BAT_TAKES_OFF", 1025);
        WORLD_EVENT_MAP.put("END_PORTAL_FRAME_FILLED", 1503);
        WORLD_EVENT_MAP.put("LAVA_EXTINGUISHED", 1501);
        WORLD_EVENT_MAP.put("ZOMBIE_INFECTS_VILLAGER", 1026);
        WORLD_EVENT_MAP.put("BLOCK_WAXED", 3003);
        WORLD_EVENT_MAP.put("POINTED_DRIPSTONE_DRIPS", 1504);
        WORLD_EVENT_MAP.put("SCULK_SHRIEKS", 3007);
        WORLD_EVENT_MAP.put("POINTED_DRIPSTONE_DRIPS_LAVA_INTO_CAULDRON", 1046);
        WORLD_EVENT_MAP.put("ZOMBIE_ATTACKS_IRON_DOOR", 1020);
        WORLD_EVENT_MAP.put("HUSK_CONVERTS_TO_ZOMBIE", 1041);
        WORLD_EVENT_MAP.put("TURTLE_EGG_PLACED", 2012);
        WORLD_EVENT_MAP.put("ELECTRICITY_SPARKS", 3002);
        WORLD_EVENT_MAP.put("ENDER_DRAGON_RESURRECTED", 3001);
        WORLD_EVENT_MAP.put("PHANTOM_BITES", 1039);
        WORLD_EVENT_MAP.put("REDSTONE_TORCH_BURNS_OUT", 1502);
        WORLD_EVENT_MAP.put("EYE_OF_ENDER_BREAKS", 2003);
        WORLD_EVENT_MAP.put("DISPENSER_ACTIVATED", 2000);
        WORLD_EVENT_MAP.put("FIREWORK_ROCKET_SHOOTS", 1004);
        WORLD_EVENT_MAP.put("TRIAL_SPAWNER_SPAWNS_MOB_AT_SPAWN_POS", 3012);
        WORLD_EVENT_MAP.put("ZOMBIE_ATTACKS_WOODEN_DOOR", 1019);
        WORLD_EVENT_MAP.put("TRIAL_SPAWNER_EJECTS_ITEM", 3014);
        WORLD_EVENT_MAP.put("DISPENSER_FAILS", 1001);
        WORLD_EVENT_MAP.put("SCULK_CHARGE", 3006);
        WORLD_EVENT_MAP.put("END_PORTAL_OPENED", 1038);
        WORLD_EVENT_MAP.put("ENDER_DRAGON_SHOOTS", 1017);
        WORLD_EVENT_MAP.put("ZOMBIE_VILLAGER_CURED", 1027);
        WORLD_EVENT_MAP.put("TRIAL_SPAWNER_DETECTS_PLAYER", 3013);
        WORLD_EVENT_MAP.put("DISPENSER_DISPENSES", 1000);
        WORLD_EVENT_MAP.put("DRAGON_BREATH_CLOUD_SPAWNS", 2006);
        WORLD_EVENT_MAP.put("COBWEB_WEAVED", 3018);
        WORLD_EVENT_MAP.put("END_GATEWAY_SPAWNS", 3000);
    }
    

    public static void saveMapToFile(Map<String, Integer> map) {
        Path modDir = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(mod -> mod.getPath("WorldEventMap"))
                .orElse(FabricLoader.getInstance().getGameDir().resolve("config"));

        Path filePath = modDir.resolve("world_events.txt");

        try {
            Files.createDirectories(modDir);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                writer.write("private static final Map<String, Integer> WORLD_EVENT_MAP = new HashMap<>(){");
                writer.newLine();

                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    writer.write(String.format("    WORLD_EVENT_MAP.put(\"%s\", %d);", entry.getKey(), entry.getValue()));
                    writer.newLine();
                }

                writer.write("};");
                writer.newLine();
                System.out.println("Map saved to " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
