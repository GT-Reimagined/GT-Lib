package org.gtreimagined.gtlib.item;

import lombok.Getter;
import net.minecraft.world.item.CreativeModeTab;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBasic<T extends ItemBasic<T>> extends Item implements IGTObject, ITextureProvider, IModelProvider, ICreativeTabProvider {

    @Getter
    protected String domain, id, tooltip = "", subDir = "";
    protected boolean enabled = true;
    protected CreativeModeTab tab = Ref.TAB_ITEMS;

    public ItemBasic(String domain, String id, String subDir, Properties properties) {
        super(properties);
        this.domain = domain;
        this.id = id;
        this.subDir = subDir;
        GTAPI.register(getClass(), this);
        if (getClass() != ItemBasic.class){
            GTAPI.register(ItemBasic.class, this);
        }
    }

    public ItemBasic(String domain, String id, Properties properties) {
        this(domain, id, "", properties);
    }

    public ItemBasic(String domain, String id, Class clazz, Properties properties) {
        super(properties);
        this.domain = domain;
        this.id = id;
        GTAPI.register(clazz, this);
    }

    public ItemBasic(String domain, String id) {
        this(domain, id, "", new Properties());
    }

    public ItemBasic(String domain, String id, String subDir) {
        this(domain, id, subDir, new Properties());
    }

    @Override
    public boolean allowedIn(CreativeModeTab tab) {
        return tab == this.tab;
    }

    public T tab(CreativeModeTab tab) {
        this.tab = tab;
        return (T) this;
    }

    public T tip(String tooltip) {
        this.tooltip = tooltip;
        return (T) this;
    }

    @Override
    public String getDomain() {
        return this instanceof ISharedGTObject ? Ref.SHARED_ID : domain;
    }

    @Override
    public String getId() {
        return id;
    }

    public ItemStack get(int count) {
        return new ItemStack(this, count);
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[]{new Texture(domain, "item/basic/" + subDir + getId())};
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (!tooltip.isEmpty()){
            tooltipComponents.add(Utils.translatable("tooltip." + getDomain() + "." + getId().replace("/", ".")));
        }
    }
}
