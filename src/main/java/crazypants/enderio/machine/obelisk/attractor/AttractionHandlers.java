package crazypants.enderio.machine.obelisk.attractor;

import java.util.ArrayList;
import java.util.List;

public class AttractionHandlers {

  public static final AttractionHandlers instance = new AttractionHandlers();

  private final List<IMobAttractionHandler> registry = new ArrayList<IMobAttractionHandler>();

  private AttractionHandlers() {
    // TODO: Squid
    // TODO: Bat
    // TODO: Wither
    // TODO: Ghast
    // Immobile: Shulker
    // Not SoulVialable: Ender Dragon
    registry.add(new EndermanAttractionHandler());
    registry.add(new SilverfishAttractorHandler());
    registry.add(new SlimeAttractionHandler());
    registry.add(new TargetAttractionHandler());
    registry.add(new AIAttractionHandler());
  }

  public static void register(IMobAttractionHandler handler) {
    instance.registry.add(0, handler);
  }

  public List<IMobAttractionHandler> getRegistry() {
    return registry;
  }

}
