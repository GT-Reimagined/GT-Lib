package org.gtreimagined.gtlib.material.tags;

import org.gtreimagined.gtlib.material.Material;

public class NumberMaterialTag extends TypeMaterialTag<Integer> {
    public NumberMaterialTag(String id) {
        super(id);
    }

    public NumberMaterialTag(String id, boolean shared) {
        super(id, shared);
    }

    public int getInt(Material mat){
        return get(mat);
    }

}
