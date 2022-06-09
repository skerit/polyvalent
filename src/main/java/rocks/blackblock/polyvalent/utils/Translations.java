package rocks.blackblock.polyvalent.utils;

import net.minecraft.util.Language;

public class Translations {

    public static String get(String key) {
        return Language.getInstance().get(key);
    }

    public static String getIfTranslated(String key) {
        var language = Language.getInstance();

        if (language.hasTranslation(key)) {
            return language.get(key);
        }

        return null;
    }

}
