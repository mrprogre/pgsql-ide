import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    static String homeDir = System.getProperty("user.home") + File.separator + "pgsql-ide" + File.separator;
    static String configPath = homeDir + "config.txt";
    static String favoritePath = homeDir + "favorite.txt";

    static {
        File directory = new File(homeDir);
        File configFile = new File(configPath);
        File favoriteFile = new File(favoritePath);
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            if (!configFile.exists()) {
                Common common = new Common();
                common.copyFiles(Main.class.getResource("config.txt"), homeDir + "config.txt");
            }
            if (!favoriteFile.exists()) {
                favoriteFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final Gui gui = new Gui();
        Runnable runnable = () -> {
            FrameDragListener frameDragListener = new FrameDragListener(gui);
            gui.addMouseListener(frameDragListener);
            gui.addMouseMotionListener(frameDragListener);
        };
        SwingUtilities.invokeLater(runnable);
        //gui.connectionBtn.doClick();
    }
}


