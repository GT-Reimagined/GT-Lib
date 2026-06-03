package org.gtreimagined.gtlib.mui;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.ThemeBuilder;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.mui.GTGuiTextures.IDs;

public class GTGuiThemes {
    public static final String STANDARD_THEME_ID = Ref.SHARED_ID + ":standard";
    public static final ThemeBuilder<?> STANDARD_THEME = new ThemeBuilder<>(STANDARD_THEME_ID)
            .background(IThemeApi.BUTTON, IDs.STANDARD_BUTTON)
            .background(IThemeApi.CLOSE_BUTTON, IDs.STANDARD_BUTTON)
            .widgetTheme(IThemeApi.TOGGLE_BUTTON, new SelectableTheme.Builder<>().background(IDs.STANDARD_BUTTON).selectedBackground(IDs.STANDARD_BUTTON_PRESSED));

    public static void registerThemes(){
        IThemeApi.get().registerTheme(STANDARD_THEME);
    }
}
