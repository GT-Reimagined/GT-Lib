package org.gtreimagined.gtlib.recipe.loader;

public interface IRecipeRegistrate {

    void add(String domain, String id, IRecipeLoader load);

    interface IRecipeLoader {
        void init();
    }

}

