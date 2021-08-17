import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

public class Common {

    //Получение списка таблиц в sql-запросе
    List <String> getTableName(String sqlQuery) {
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sqlQuery);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        Select selectStatement = (Select) statement;
        TablesNamesFinder tnf = new TablesNamesFinder();
        return tnf.getTableList(selectStatement);
    }

    // Получение столбцов bp sql-запроса
    List <SelectItem> getColumns (String sqlQuery) {
        Select stmt = null;
        try {
            stmt = (Select) CCJSqlParserUtil.parse(sqlQuery);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        assert stmt != null;
        return ((PlainSelect) stmt.getSelectBody()).getSelectItems();
    }

    // Копирование файлов из jar
    void copyFiles(URL p_file, String copy_to) {
        File copied = new File(copy_to);
        try (InputStream in = p_file.openStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(copied))) {
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Подсчет количества строк в файле
    int countLines(String path) {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(path));
            int cnt;
            while (true) {
                if (reader.readLine() == null) break;
            }
            cnt = reader.getLineNumber();
            reader.close();
            return cnt;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Считывание строк из файла в двумерный массив строк
    String[][] getConfig() {
        String path = null;
        if (Main.OS.contains("win")) {
            path = Main.configPath;
        } else if (Main.OS.contains("uni")) {
            path = Main.linuxPath;
        }

        int rowsCount = countLines(path);
        String[][] lines = new String[rowsCount][];

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null && i < rowsCount) {
                lines[i++] = line.split(";");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(Arrays.deepToString(lines));
        return lines;
    }

    // Определение типа столбца для корректной сортировки любой таблицы
    Class[] typeClass(List<String> p_type_list) {
        Class[] column_types = new Class[p_type_list.size()];
        try {
            for (int i = 0; i < p_type_list.size(); i++) {
                switch (p_type_list.get(i)) {
                    case "int2":
                    case "int4":
                    case "serial":
                    case "serial4":
                        column_types[i] = Integer.class;
                        break;
                    case "int8":
                    case "serial8":
                        column_types[i] = Long.class;
                        break;
                    case "date":
                        column_types[i] = Date.class;
                        break;
                    case "timestamp":
                        column_types[i] = Timestamp.class;
                        break;
                    case "bool":
                        column_types[i] = Boolean.class;
                        break;
                    case "float4":
                    case "decimal":
                        column_types[i] = Float.class;
                        break;
                    default:
                        column_types[i] = String.class;
                        break;
                }
            }
        } catch (Exception r) {
            r.printStackTrace();
        }
        return column_types;
    }

    // Запись избранных таблиц
    void writeToConfig(String type, String text) {
        String path = null;
        if (Main.OS.contains("win")) {
            path = Main.favoritePath;
        } else if (Main.OS.contains("uni")) {
            path = Main.linuxFavoritePath;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8)) {
            writer.write(type + "=" + text + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Считывание избранных таблиц из файла
    String[] getFavoriteFromFile() {
        String path = null;
        if (Main.OS.contains("win")) {
            path = Main.favoritePath;
        } else if (Main.OS.contains("uni")) {
            path = Main.linuxFavoritePath;
        }

        int linesAmount = countLines(path);
        String[] lines = new String[linesAmount];

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            int i = 0;

            while ((line = reader.readLine()) != null && i < linesAmount) {
//                if (line.startsWith("favorite_table=")) {
//                    lines[i++] = line.replaceAll("favorite_table=", "");
//                }
                lines[i++] = line.replaceAll("favorite_table=", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("168 " + Arrays.toString(lines));
        return lines;
    }

    // Удаление таблицы из списка избранных таблиц
    void deleteFromFavorites(String type, String objectName) {
        String path = null;
        if (Main.OS.contains("win")) {
            path = Main.favoritePath;
        } else if (Main.OS.contains("uni")) {
            path = Main.linuxFavoritePath;
        }

        try {
            Path input = Paths.get(path);
            Path temp = Files.createTempFile("temp", ".txt");
            Stream<String> lines = Files.lines(input);
            try (BufferedWriter writer = Files.newBufferedWriter(temp)) {
                lines.filter(line -> {
                            assert objectName != null;
                            return !line.startsWith(type + "=" + objectName);
                        })
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            Files.move(temp, input, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Удаление информации из таблицы
    void deleteFromFavorites(String objectName) {
        String path = null;
        if (Main.OS.contains("win")) {
            path = Main.favoritePath;
        } else if (Main.OS.contains("uni")) {
            path = Main.linuxFavoritePath;
        }
        try {
            Path input = Paths.get(path);
            Path temp = Files.createTempFile("temp", ".txt");
            Stream<String> lines = Files.lines(input);
            try (BufferedWriter writer = Files.newBufferedWriter(temp)) {
                lines.filter(line -> {
                            assert objectName != null;
                            return !line.startsWith(objectName);
                        })
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            Files.move(temp, input, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
