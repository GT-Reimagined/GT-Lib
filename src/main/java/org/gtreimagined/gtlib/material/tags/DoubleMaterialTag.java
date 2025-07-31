package org.gtreimagined.gtlib.material.tags;

import org.gtreimagined.gtlib.material.Material;

public class DoubleMaterialTag extends TypeMaterialTag<Material> {

    public DoubleMaterialTag(String id) {
        super(id);
    }
    
    public Material getMapping(Material mat) {
        return mapping.get(mat);
    }
}
