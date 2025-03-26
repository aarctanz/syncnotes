package Client;

import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;

import javax.swing.*;

public class ThemeManager {
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkPurpleIJTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
