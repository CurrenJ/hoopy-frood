package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.Config;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(HoopyFroodTut.MODID)
public class HoopyFroodTut {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hoopyfroodtut";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public HoopyFroodTut(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterCapabilities);

        HoopyFroodTutBlocks.BLOCKS.register(modEventBus);
        HoopyFroodItems.ITEMS.register(modEventBus);
        HoopyFroodBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        HoopyFroodCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        HoopyFroodDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        HoopyFroodRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                HoopyFroodBlockEntityTypes.BEGGING_ITEM_SCRABBLER.get(),
                (be, direction) -> VanillaContainerWrapper.of(be)
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }
}
