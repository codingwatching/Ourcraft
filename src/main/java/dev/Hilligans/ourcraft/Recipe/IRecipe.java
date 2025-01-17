package dev.Hilligans.ourcraft.Recipe;

import dev.Hilligans.ourcraft.Container.Container;

public interface IRecipe<T extends Container> {

    RecipeElement[] getOutput();
    RecipeElement[] getInput();



    class RecipeElement {
        public IRecipeComponent component;
        public int slot;
        public int count;

        public RecipeElement(IRecipeComponent component, int slot, int count) {
            this.component = component;
            this.slot = slot;
            this.count = count;
        }
    }



}
