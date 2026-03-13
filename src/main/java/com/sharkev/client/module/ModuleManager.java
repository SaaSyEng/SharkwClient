package com.sharkev.client.module;

import com.sharkev.client.module.modules.combat.*;
import com.sharkev.client.module.modules.misc.*;
import com.sharkev.client.module.modules.movement.*;
import com.sharkev.client.module.modules.visual.*;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Set<Integer> pressedKeys = new HashSet<>();

    public ModuleManager() {
        // Movement
        register(new Speed());
        register(new Fly());
        register(new Bhop());
        register(new NoFall());
        register(new Sprint());
        register(new Scaffold());
        register(new Jesus());
        register(new Phase());
        register(new Step());
        register(new NoSlow());
        register(new AirJump());

        // Combat
        register(new KillAura());
        register(new AimAssist());
        register(new AutoClicker());
        register(new Criticals());
        register(new Velocity());
        register(new Reach());
        register(new WTap());
        register(new Strafe());
        register(new AutoArmor());
        register(new Backtrack());

        // Visual
        register(new ESP());
        register(new Tracers());
        register(new Fullbright());
        register(new TargetHUD());
        register(new KeyStrokes());
        register(new HUD());
        register(new XRay());

        // Misc
        register(new Timer());
        register(new Blink());
        register(new AntiAFK());
        register(new AutoEat());
        register(new AutoRespawn());
        register(new FreeCam());
        register(new FlagDetector());
        register(new FastBreak());
        register(new FastPlace());
    }

    private void register(Module m) { modules.add(m); }

    public void onTick() {
        for (Module m : modules) {
            int key = m.getKeybind();
            if (key == 0) continue;

            if (Keyboard.isKeyDown(key)) {
                if (!pressedKeys.contains(key)) {
                    pressedKeys.add(key);
                    m.toggle();
                }
            } else {
                pressedKeys.remove(key);
            }
        }
    }

    public List<Module> getModules() { return modules; }

    public List<Module> getByCategory(Category c) {
        List<Module> r = new ArrayList<>();
        for (Module m : modules) if (m.getCategory() == c) r.add(m);
        return r;
    }

    public Module getByName(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }
}
