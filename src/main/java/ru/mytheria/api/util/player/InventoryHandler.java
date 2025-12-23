package ru.mytheria.api.util.player;


import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import ru.mytheria.api.clientannotation.QuickImport;


public class InventoryHandler implements QuickImport {
    public static boolean isSlotFull(int pSlot) {
        return !mc.player.getInventory().getStack(pSlot).isEmpty();
    }

    public static int findItemSlot(Item pItem) {
        return findItemSlot(pItem, true);
    }

    public static int findItemSlot(Item pItem, boolean pArmor) {
        if (pArmor) {
            for (ItemStack pStack : mc.player.getInventory().armor) {
                if (pStack.getItem() == pItem) {
                    return -2;
                }
            }
        }

        int pSlot = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack pStack = mc.player.getInventory().getStack(i);
            if (pStack.getItem() == pItem) {
                pSlot = i;
                break;
            }
        }

        if (pSlot < 9 && pSlot != -1) {
            pSlot = pSlot + 36;
        }

        return pSlot;
    }

    public static int findEmptySlot(boolean pInHotBar) {
        int pStart = pInHotBar ? 0 : 9;
        int pEnd = pInHotBar ? 9 : 36;

        for (int i = pStart; i < pEnd; ++i) {
            if (!mc.player.getInventory().getStack(i).isEmpty()) {
                continue;
            }
            return i;
        }

        return -1;
    }

    public static int getEmptySlot(boolean pHotBar) {
        for (int i = pHotBar ? 0 : 9; i < (pHotBar ? 9 : 45); ++i) {
            if (!mc.player.getInventory().getStack(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static void moveItem(int pFromSlot, int pToSlot, boolean pIsAir) {
        if (pFromSlot == pToSlot) return;

        pickupItem(pFromSlot, 0);
        pickupItem(pToSlot, 0);

        if (pIsAir) {
            pickupItem(pFromSlot, 0);
        }
    }

    public static void moveItem(int pFromSlot, int pToSlot) {
        if (pFromSlot == pToSlot) return;

        pickupItem(pFromSlot, 0);
        pickupItem(pToSlot, 0);
        pickupItem(pFromSlot, 0);
    }

    public static int getSlotInInventoryOrHotBar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static void pickupItem(int slot, int button) {
        mc.interactionManager.clickSlot(mc.player.getInventory().selectedSlot, slot, button, SlotActionType.PICKUP, mc.player);
    }

    public static int getAxeInInventory(boolean pInHotBar) {
        int pFirstSlot = pInHotBar ? 0 : 9;
        int pLastSlot = pInHotBar ? 9 : 36;

        for (int i = pFirstSlot; i < pLastSlot; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public static int findBestSlotInHotBar() {
        int pEmptySlot = findEmptySlot(true);

        if (pEmptySlot != -1) {
            return pEmptySlot;
        } else {
            return findNonSwordSlot();
        }
    }

    private int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty() && mc.player.getInventory().selectedSlot != i) {
                return i;
            }
        }
        return -1;
    }

    private static int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            Item pItem = mc.player.getInventory().getStack(i).getItem();
            Identifier id = Registries.ITEM.getId(pItem);
            if (!(pItem instanceof SwordItem) && !id.getPath().equals("elytra") && mc.player.getInventory().selectedSlot != i) {
                return i;
            }
        }
        return -1;
    }

    public static int getSlotInInventory(Item pItem) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == pItem) {
                finalSlot = i;
            }
        }
        return finalSlot;
    }

    public int getSlotInInventoryOrHotbar(Item pItem, boolean pInHotbar) {
        int firstSlot = pInHotbar ? 0 : 9;
        int lastSlot = pInHotbar ? 9 : 36;
        int finalSlot = -1;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == pItem) {
                finalSlot = i;
            }
        }
        return finalSlot;
    }

    public static boolean doesHotbarHaveItem(Item pItem) {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.getInventory().getStack(i).getItem() == pItem) {
                return true;
            }
        }
        return false;
    }

    public static class InteractionHandPlayer {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

       /* public void onPacket(EventPacket event) {
            if (!event.isReceive()) {
                return;
            }
            if (event.getPacket() instanceof SetCursorItemS2CPacket) {
                this.isChangingItem = true;
            }
        }*/

        public void setOriginalSlot(int originalSlot) {
            this.originalSlot = originalSlot;
        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                mc.player.getInventory().selectedSlot = this.originalSlot;
                if (resetItem) {
                    this.isChangingItem = false;
                    this.originalSlot = -1;
                    isEnabled = false;
                }
            }
        }
    }
    public static void findAndThrowItem(Item item, String name, float yaw, float pitch) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            boolean isCorrectItem = stack.getItem() == item;
            boolean isCustomNamed = !stack.getName().getString().equals(stack.getItem().getName().getString());
            boolean nameMatches = stack.getName().getString().equals(name);

            if (isCorrectItem && isCustomNamed && nameMatches) {
                int prevSlot = mc.player.getInventory().selectedSlot;
                int hotbarSlot = i < 9 ? i : i - 36;

                mc.player.getInventory().selectedSlot = hotbarSlot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prevSlot;
                return;
            }
        }
    }
    public static void findAndThrowItem(Item item, float yaw, float pitch) {
        int slot = findItemSlot(item);
        if (slot == -1) return;

        int hotbarSlot = slot < 9 ? slot : slot - 36;
        int prevSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = hotbarSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

}
