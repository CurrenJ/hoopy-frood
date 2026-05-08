package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.command.WobblyWaterDebugCommand;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import java.util.*;

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
        HoopyFroodEntities.ENTITY_TYPES.register(modEventBus);
        HoopyFroodCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        HoopyFroodDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        HoopyFroodRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        HoopyFroodSounds.SOUND_EVENTS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        // SEP Field — server-side AI suppression events
        NeoForge.EVENT_BUS.addListener(this::onLivingChangeTarget);
        NeoForge.EVENT_BUS.addListener(this::onEntityTickPost);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);

        // Debug commands
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Scaffolding conversion: right-click with scaffolding to upgrade redstone components
        NeoForge.EVENT_BUS.addListener(ScaffoldingConversionEvents::onRightClickBlock);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                HoopyFroodBlockEntityTypes.BEGGING_ITEM_SCRABBLER.get(),
                (be, direction) -> VanillaContainerWrapper.of(be)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                HoopyFroodBlockEntityTypes.BANISHING_BIN.get(),
                (be, direction) -> VanillaContainerWrapper.of(be)
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                HoopyFroodBlockEntityTypes.EJECTOR.get(),
                (be, direction) -> VanillaContainerWrapper.of(be)
        );
        // ProximitySensor has no inventory, but register for completeness/future use.
        // PulseLatch and RedstoneClock have no inventory either.
    }

    // -------------------------------------------------------------------------
    // SEP Field — mob AI suppression
    // -------------------------------------------------------------------------

    /**
     * Cancel mob target acquisition when the incoming target is inside a SEP field.
     * Handles the case where a mob first spots a player/entity in the zone.
     */
    private void onLivingChangeTarget(LivingChangeTargetEvent event) {
        var target = event.getNewAboutToBeSetTarget();
        if (target != null
                && !event.getEntity().level().isClientSide()
                && SepFieldManager.isInField(event.getEntity().level(), target.position())) {
            event.setCanceled(true);
        }
    }

    /**
     * Drop a mob's current target (and halt its navigation) whenever the target
     * moves into a SEP field mid-chase. Fires after each entity tick so the
     * suppression applies even to targets that were acquired before the field existed.
     */
    private void onEntityTickPost(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide()) return;

        var target = mob.getTarget();
        if (target != null && SepFieldManager.isInField(mob.level(), target.position())) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    /**
     * Clear the SEP field registry for a dimension when that level unloads so
     * block entity positions from the old session don't leak into the next.
     */
    private void onLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            SepFieldManager.clearDimension(serverLevel.dimension());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        WobblyWaterDebugCommand.register(event.getDispatcher());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }
}
