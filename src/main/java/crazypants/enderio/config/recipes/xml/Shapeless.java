package crazypants.enderio.config.recipes.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import crazypants.enderio.config.recipes.InvalidRecipeConfigException;
import crazypants.enderio.config.recipes.RecipeConfigElement;
import crazypants.enderio.config.recipes.StaxFactory;

public class Shapeless implements RecipeConfigElement {

  private List<Item> items;

  private transient boolean valid;

  @Override
  public Object readResolve() throws InvalidRecipeConfigException {
    try {
      if (items == null || items.isEmpty()) {
        throw new InvalidRecipeConfigException("Not enough items");
      }
      if (items.size() > 9) {
        throw new InvalidRecipeConfigException("Too many items");
      }
      // Sorry mezz, no "Just enough items" exception
      valid = true;
      for (Item item : items) {
        valid = valid && item.isValid();
      }
    } catch (InvalidRecipeConfigException e) {
      throw new InvalidRecipeConfigException(e, "in <Shapeless>");
    }
    return this;
  }

  @Override
  public void enforceValidity() throws InvalidRecipeConfigException {
    for (Item item : items) {
      item.enforceValidity();
    }
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  public Object[] getElements() {
    List<Object> elements = new ArrayList<Object>();

    for (Item item : items) {
      elements.add(item.getRecipeObject());
    }

    return elements.toArray();
  }

  @Override
  public boolean setAttribute(StaxFactory factory, String name, String value) throws InvalidRecipeConfigException, XMLStreamException {
    return false;
  }

  @Override
  public boolean setElement(StaxFactory factory, String name, StartElement startElement) throws InvalidRecipeConfigException, XMLStreamException {
    if ("item".equals(name)) {
      if (items == null) {
        items = new ArrayList<Item>();
      }
      items.add(factory.read(new Item(), startElement));
      return true;
    }

    return false;
  }

}