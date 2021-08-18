import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    static String directoryPath = "C:\\Users\\Public\\Documents\\Pides\\";
    static String configPath = directoryPath + "config.txt";
    static String favoritePath = directoryPath + "favorite.txt";
    // linux
    static String linuxPath = "./Public/pides/";
    static String linuxConfigPath = linuxPath + "config.txt";
    static String linuxFavoritePath = linuxPath + "favorite.txt";
    static String OS = OSValidator.getOS();

    static {

        if (OS.contains("win")) {
            File directory = new File(directoryPath);
            File configFile = new File(configPath);
            File favoriteFile = new File(favoritePath);
            try {
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (!configFile.exists()) {
                    Common common = new Common();
                    common.copyFiles(Main.class.getResource("config.txt"), directoryPath + "config.txt");
                }
                if (!favoriteFile.exists()) {
                    favoriteFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (OS.contains("uni")) {
            File directory = new File(linuxPath);
            File configFile = new File(linuxConfigPath);
            File favoriteFile = new File(linuxFavoritePath);
            try {
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (!configFile.exists()) {
                    Common common = new Common();
                    common.copyFiles(Main.class.getResource("config.txt"), linuxPath + "config.txt");
                }
                if (!favoriteFile.exists()) {
                    favoriteFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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


