package org.gtreimagined.gtlib.material.tags;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTag;

import java.util.Map;
import java.util.function.Function;

public class TypeMaterialTag<T> extends MaterialTag {

    protected final Map<Material, T> mapping = new Object2ObjectArrayMap<>();
    private Function<Material, T> defaultValue = null;
    public TypeMaterialTag(String id) {
        super(id);
    }

    public TypeMaterialTag(String id, boolean shared) {
        super(id, shared);
    }

    public TypeMaterialTag <T> setDefaultValue(Function<Material, T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public TypeMaterialTag <T> add(Material mat, T map) {
        if (!mat.enabled) return this;
        if (!mapping.containsKey(mat)){
            super.add(mat);
        }
        mapping.put(mat, map);
        return this;
    }

    public Map<Material, T> getAll() {
        return mapping;
    }

    public T get(Material mat){
        if (!mapping.containsKey(mat) && defaultValue != null) {
            return defaultValue.apply(mat);
        }
        return mapping.get(mat);
    }
}
