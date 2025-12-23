package ru.mytheria.api.util.enviorement;

import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import ru.mytheria.Mytheria;
import ru.mytheria.mixin.IClientPlayerInteractionManager;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class InventoryHelper {

    @AllArgsConstructor
    public enum Switch implements Nameable {
        Normal(Text.of("Нормальный")),
        Silent(Text.of("Сайлент")),
        Alternative(Text.of("Альтернативный")),
        None(Text.of("Никакой"));

        private final Text name;

        @Override
        public Text getName() {
            return name;
        }
    }

    public enum Swap {
        Pickup,
        Swap
    }

    public static int findBestAxe( int start, int end ) {
        int netheriteSlot = -1;
        int diamondSlot = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int stoneSlot = -1;
        int woodenSlot = -1;

        for (int i = end; i >= start; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.NETHERITE_AXE) netheriteSlot = i;
            else if (stack.getItem() == Items.DIAMOND_AXE) diamondSlot = i;
            else if (stack.getItem() == Items.IRON_AXE) ironSlot = i;
            else if (stack.getItem() == Items.GOLDEN_AXE) goldenSlot = i;
            else if (stack.getItem() == Items.STONE_AXE) stoneSlot = i;
            else if (stack.getItem() == Items.WOODEN_AXE) woodenSlot = i;
        }

        if (netheriteSlot != -1) return netheriteSlot;
        if (diamondSlot != -1) return diamondSlot;
        if (ironSlot != -1) return ironSlot;
        if (goldenSlot != -1) return goldenSlot;
        if (stoneSlot != -1) return stoneSlot;

        return woodenSlot;
    }

    @AllArgsConstructor
    public enum Swing implements Nameable {
        MainHand(Text.of("Основная рука")),
        OffHand(Text.of("Вторая рука")),
        Packet(Text.of("Пакет")),
        None(Text.of("Никакой"));

        private final Text name;

        @Override
        public Text getName() {
            return name;
        }
    }

    public static int indexToSlot( int index ) {
        if (index >= 0 && index <= 8) return 36 + index;
        return index;
    }


    public static void swap( Swap mode, int slot, int targetSlot ) {
        if (slot == -1 || targetSlot == -1) return;
        switch (mode) {
            case Pickup -> {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(targetSlot), 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, indexToSlot(slot), 0, SlotActionType.PICKUP, mc.player);
            }
            case Swap -> mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, targetSlot, SlotActionType.SWAP, mc.player);
        }
    }

    public static void switchSlot( Switch mode, int slot, int previousSlot ) {
        if (slot == -1 || previousSlot == -1 || slot == Mytheria.getInstance().getServerManager().getServerSlot()) return;

        switch (mode) {
            case Normal -> {
                mc.player.getInventory().selectedSlot = slot;
                ((IClientPlayerInteractionManager) mc.interactionManager).syncSelectedSlot$drug();
            }
            case Silent -> NetworkWrapper.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            case Alternative -> swap(Swap.Swap, slot, previousSlot);
        }
    }

    public static void switchBack( Switch mode, int slot, int previousSlot ) {
        if (slot == -1 || previousSlot == -1 || slot == Mytheria.getInstance().getServerManager().getServerSlot()) return;

        switch (mode) {
            case Normal -> {
                mc.player.getInventory().selectedSlot = previousSlot;
                ((IClientPlayerInteractionManager) mc.interactionManager).syncSelectedSlot$drug();
            }
            case Silent -> NetworkWrapper.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            case Alternative -> swap(Swap.Swap, slot, previousSlot);
        }
    }

    public static void swing( Swing mode ) {
        switch (mode) {
            case MainHand -> mc.player.swingHand(Hand.MAIN_HAND);
            case OffHand -> mc.player.swingHand(Hand.OFF_HAND);
            case Packet -> NetworkWrapper.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

}
