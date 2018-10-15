package server.lang;

import java.util.HashMap;
import java.util.Map;

public class Languages {
    public static final Language en = new Language("EN") {
        @Override
        void fillDictionary() {
            addText(Text.S_ENTER_NAME, "Enter your name: \n");
            addText(Text.S_HELLO, "Hello, %s\n");
            addText(Text.S_HAS_JOINED, "%s joined.\n");
            addText(Text.S_LEFT, "%s left.\n");
            addText(Text.S_STATUS_AVAILABLE, "%s - available.\n");
            addText(Text.S_STATUS_UNAVAILABLE, "%s - unavailable.\n");
            addText(Text.S_INVALID_COMMAND, "Invalid command: \"%s\"\n");
            addText(Text.S_PM_TARGET_UNAVAILABLE, "%s is unavailable. Wait until %s becomes available.\n");
            addText(Text.S_PM_SELF_UNAVAILABLE, "You are unavailable. Change your status to available.\n");
            addText(Text.S_CURRENTLY_MUTED, "You are muted, you cannot do that.\n");
            addText(Text.S_PM_FROM, "<PM from %s>%s\n");
            addText(Text.S_IS_NOW_ADMIN, "%s is now an admin.\n");
            addText(Text.S_IS_NO_LONGER_ADMIN, "%s is no longer an admin.\n");
            addText(Text.S_KICKED, "You have been kicked from the room.\n");
            addText(Text.S_MUTED, "You have been muted.\n");
            addText(Text.S_UNMUTED, "You are no longer muted.\n");
            addText(Text.S_USER_NOT_FOUND, "%s was not found.\n");
            addText(Text.S_NO_PERMISSION, "You don't have permission to do that.\n");
            addText(Text.S_COLOR_CHOOSE, "Choose your color: ");
            addText(Text.S_COLOR_INVALID, "Invalid color\n");
            addText(Text.S_LANG_INVALID, "Language unavailable\n");
            addText(Text.S_LANG_CHANGED, "Language changed to %s\n");
            addColor(Color.C_BLACK, "black");
            addColor(Color.C_RED, "red");
            addColor(Color.C_GREEN, "green");
            addColor(Color.C_YELLOW, "yellow");
            addColor(Color.C_BLUE, "blue");
            addColor(Color.C_PURPLE, "purple");
            addColor(Color.C_CYAN, "cyan");
            addColor(Color.C_WHITE, "white");
        }
    };
    public static final Language ro = new Language("RO") {
        @Override
        void fillDictionary() {
            addText(Text.S_ENTER_NAME, "Introduceti numele: \n");
            addText(Text.S_HELLO, "Buna, %s\n");
            addText(Text.S_HAS_JOINED, "%s a intrat.\n");
            addText(Text.S_LEFT, "%s a iesit\n.");
            addText(Text.S_STATUS_AVAILABLE, "%s - disponibil.\n");
            addText(Text.S_STATUS_UNAVAILABLE, "%s - indisponibil.\n");
            addText(Text.S_INVALID_COMMAND, "Comanda invalida: \"%s\"\n");
            addText(Text.S_PM_TARGET_UNAVAILABLE, "%s e indisponibil. Asteptati pana %s devine disponibil.\n");
            addText(Text.S_PM_SELF_UNAVAILABLE, "Sunteti indisponibil. Schimbati statusul in disponibil.\n");
            addText(Text.S_CURRENTLY_MUTED, "Sunteti dezactivat, nu puteti face asta.\n");
            addText(Text.S_PM_FROM, "<PM de la %s>%s\n");
            addText(Text.S_IS_NOW_ADMIN, "%s este acum admin.\n");
            addText(Text.S_IS_NO_LONGER_ADMIN, "%s nu mai este admin.\n");
            addText(Text.S_KICKED, "Ati fost dat afara din camera\n.");
            addText(Text.S_MUTED, "Ati fost dezactivat.\n");
            addText(Text.S_UNMUTED, "Nu mai sunteti dezactivat.\n");
            addText(Text.S_USER_NOT_FOUND, "%s nu a fost gasit.\n");
            addText(Text.S_NO_PERMISSION, "Nu aveti permisiune pentru asta.\n");
            addText(Text.S_COLOR_CHOOSE, "Alegeti culoarea: ");
            addText(Text.S_COLOR_INVALID, "Culoare indisponibila\n");
            addText(Text.S_LANG_INVALID, "Limba indisponibila\n");
            addText(Text.S_LANG_CHANGED, "Limba s-a schimbat in %s\n");
            addColor(Color.C_BLACK, "negru");
            addColor(Color.C_RED, "rosu");
            addColor(Color.C_GREEN, "verde");
            addColor(Color.C_YELLOW, "galben");
            addColor(Color.C_BLUE, "albastru");
            addColor(Color.C_PURPLE, "mov");
            addColor(Color.C_CYAN, "turcoaz");
            addColor(Color.C_WHITE, "alb");
        }
    };
    public static final Language it = new Language("IT") {
        @Override
        void fillDictionary() {
            addText(Text.S_ENTER_NAME, "Inserisci il tuo nome: \n");
            addText(Text.S_HELLO, "Ciao, %s\n");
            addText(Text.S_HAS_JOINED, "%s si è unito.\n");
            addText(Text.S_LEFT, "%s se n'è andato.\n");
            addText(Text.S_STATUS_AVAILABLE, "%s - disponibile.\n");
            addText(Text.S_STATUS_UNAVAILABLE, "%s - non disponibile.\n");
            addText(Text.S_INVALID_COMMAND, "Comando non valido: \"%s\"\n");
            addText(Text.S_PM_TARGET_UNAVAILABLE, "%s non è disponibile. aspetta che Alex diventi disponibile.\n");
            addText(Text.S_PM_SELF_UNAVAILABLE, "Non sei disponibile. Cambia il tuo stato a disponibile.\n");
            addText(Text.S_CURRENTLY_MUTED, "Sei disattivato, non puoi farlo.\n");
            addText(Text.S_PM_FROM, "<PM di %s>%s\n");
            addText(Text.S_IS_NOW_ADMIN, "%s è ora un amministratore.\n");
            addText(Text.S_IS_NO_LONGER_ADMIN, "%s non è più un amministratore.\n");
            addText(Text.S_KICKED, "Sei stato preso a calci dalla stanza.\n");
            addText(Text.S_MUTED, "Sei stato disattivato.\n");
            addText(Text.S_UNMUTED, "Non sei più disattivato.\n");
            addText(Text.S_USER_NOT_FOUND, "%s non è stato trovato.\n");
            addText(Text.S_NO_PERMISSION, "Non hai il permesso di farlo.\n");
            addText(Text.S_COLOR_CHOOSE, "Scegli il tuo colore: ");
            addText(Text.S_COLOR_INVALID, "Colore non valido\n");
            addText(Text.S_LANG_INVALID, "Lingua non disponibile\n");
            addText(Text.S_LANG_CHANGED, "Lingua cambiata in %s\n");
            addColor(Color.C_BLACK, "nero");
            addColor(Color.C_RED, "rosso");
            addColor(Color.C_GREEN, "verde");
            addColor(Color.C_YELLOW, "giallo");
            addColor(Color.C_BLUE, "blu");
            addColor(Color.C_PURPLE, "viola");
            addColor(Color.C_CYAN, "ciano");
            addColor(Color.C_WHITE, "bianca");
        }
    };
    public enum Text {
        S_ENTER_NAME,
        S_HELLO,
        S_HAS_JOINED,
        S_LEFT,
        S_STATUS_AVAILABLE,
        S_STATUS_UNAVAILABLE,
        S_INVALID_COMMAND,
        S_PM_TARGET_UNAVAILABLE,
        S_PM_SELF_UNAVAILABLE,
        S_CURRENTLY_MUTED,
        S_PM_FROM,
        S_IS_NOW_ADMIN,
        S_IS_NO_LONGER_ADMIN,
        S_KICKED,
        S_MUTED,
        S_UNMUTED,
        S_USER_NOT_FOUND,
        S_NO_PERMISSION,
        S_COLOR_CHOOSE,
        S_COLOR_INVALID,
        S_LANG_INVALID,
        S_LANG_CHANGED

    }
    public enum Color {
        C_BLACK,
        C_RED,
        C_GREEN,
        C_YELLOW,
        C_BLUE,
        C_PURPLE,
        C_CYAN,
        C_WHITE
    }
    public static abstract class Language {
        public final String name;
        private Map<Text, String> textMap;
        private Map<Color, String> colorMap;
        private Map<String, String> colorValuesMap;
        private Language(String name){
            this.name = name;
            textMap = new HashMap<>();
            colorMap = new HashMap<>();
            colorValuesMap = new HashMap<>();
            fillDictionary();
            addColorValues();
        }
        public String text(Text key){
            String text = textMap.get(key);
            if (text != null && !text.equals(""))
                return text;
            else
                return en.text(key);
        }
        public String color(Color key){
            return colorMap.get(key);
        }
        public String colorValue(Color key){
            return colorValue(color(key));
        }
        public String colorValue(String key){
            return colorValuesMap.get(key);
        }

        abstract void fillDictionary();

        void addText(Text key, String value){
            textMap.put(key, value);
        }
        void addColor(Color key, String value){
            colorMap.put(key, value);
        }

        private void addColorValues(){
            colorValuesMap.put(color(Color.C_BLACK), "\u001B[30m");
            colorValuesMap.put(color(Color.C_RED), "\u001B[31m");
            colorValuesMap.put(color(Color.C_GREEN), "\u001B[32m");
            colorValuesMap.put(color(Color.C_YELLOW), "\u001B[33m");
            colorValuesMap.put(color(Color.C_BLUE), "\u001B[34m");
            colorValuesMap.put(color(Color.C_PURPLE), "\u001B[35m");
            colorValuesMap.put(color(Color.C_CYAN), "\u001B[36m");
            colorValuesMap.put(color(Color.C_WHITE), "\u001B[37m");
        }
    }
}
