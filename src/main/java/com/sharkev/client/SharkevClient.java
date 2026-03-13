package com.sharkev.client;

import com.sharkev.client.gui.ClickGui;
import com.sharkev.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "sharkevclient", name = "SharkevClient", version = "1.0")
public class SharkevClient {

    public static SharkevClient INSTANCE;
    public static ModuleManager moduleManager;
    public static ClickGui clickGui;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean guiKeyDown = false;
    private static boolean initialized = false;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        doInit();
    }

    public static void doInit() {
        if (initialized) return;
        initialized = true;
        SharkevClient inst = new SharkevClient();
        INSTANCE = inst;
        moduleManager = new ModuleManager();
        clickGui = new ClickGui();
        MinecraftForge.EVENT_BUS.register(inst);
        System.out.println("[SharkevClient] Loaded - RIGHT SHIFT to open menu");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Lazy init in case @Mod didn't fire
        if (!initialized) doInit();

        // Toggle ClickGUI with RIGHT SHIFT
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (keyDown && !guiKeyDown) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(clickGui);
            } else if (mc.currentScreen instanceof ClickGui) {
                mc.displayGuiScreen(null);
            }
        }
        guiKeyDown = keyDown;

        // Check keybinds for each module
        moduleManager.onTick();
    }
}
