package Client;

import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;

import javax.swing.*;

public class ThemeManager {
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(new FlatArcOrangeIJTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
