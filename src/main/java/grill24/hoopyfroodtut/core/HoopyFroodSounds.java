package grill24.hoopyfroodtut.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, HoopyFroodTut.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SEP_FIELD_ACTIVATE = SOUND_EVENTS.register(
            "sep_field.activate",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "sep_field.activate")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SEP_FIELD_DEACTIVATE = SOUND_EVENTS.register(
            "sep_field.deactivate",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "sep_field.deactivate")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BIS_COLLECT = SOUND_EVENTS.register(
            "bis.collect",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "bis.collect")));
}
