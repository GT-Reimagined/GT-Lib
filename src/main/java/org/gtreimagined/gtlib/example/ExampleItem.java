package org.gtreimagined.gtlib.example;

import org.gtreimagined.gtlib.item.ItemBasic;
import org.gtreimagined.gtlib.registration.IAntimatterObject;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;

public class ExampleItem extends ItemBasic<ExampleItem> implements IAntimatterObject, ITextureProvider, IModelProvider {

    public ExampleItem(String domain, String id, Properties properties) {
        super(domain, id, "", properties);
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[]{new Texture(getDomain(), "item/" + getId())};
    }
}
