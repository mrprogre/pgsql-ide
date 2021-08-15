import net.sf.jsqlparser.statement.select.SelectItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Pg {
    Common common = new Common();
    static Connection connection;
    static boolean isConnect = false;
    List<String> userTables = new ArrayList<>();
    List<String> userTableTypes = new ArrayList<>();
    List<String> userColumns = new ArrayList<>();
    List<String> types = new ArrayList<>();
    String[][] config = common.getConfig();
    String url = config[0][1].trim();
    String user = config[1][1].trim();
    String pwd = config[2][1].trim();
    int rowsLimitFromConfig = Integer.parseInt(config[3][1].trim());
    float guiOpacity = Float.parseFloat(config[4][1].trim());

    // Подключение к базе данных
    void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, pwd);
            if (connection != null) {
                isConnect = true;
                if (Gui.executeModel.getRowCount() > 0) Gui.executeModel.setRowCount(0);
                getUserTables(1);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Закрытие соединения
    void close() {
        try {
            if (isConnect) connection.close();
            isConnect = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Список всех таблиц, представлений текущего пользователя
    void getUserTables(int mode) {
        String[] favTables = common.getFavoriteFromFile();
        int count = 0;
        try {
            String sql = null;
            if (mode == 1) { // Tables + Views
                sql = "select table_name, table_type \n" +
                        "from information_schema.tables \n" +
                        "where table_schema='public' \n" +
                        "union all\n" +
                        "select matviewname, 'matview'\n" +
                        "from pg_matviews\n" +
                        "order by table_type, table_name";
            } else if (mode == 2) { // Tables
                sql = "select table_name, table_type\n" +
                        "from information_schema.tables\n" +
                        "where table_schema='public'\n" +
                        "and table_type = 'BASE TABLE'\n" +
                        "order by table_name";
            } else if (mode == 3) { // Views
                sql = "select table_name, table_type\n" +
                        "from information_schema.tables\n" +
                        "where table_schema='public'\n" +
                        "and table_type = 'VIEW'\n" +
                        "union all\n" +
                        "select matviewname, 'matview'\n" +
                        "from pg_matviews\n" +
                        "order by table_type\n";
            }
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String table = rs.getString("table_name");
                userTables.add(table);
                String type = rs.getString("table_type");
                userTableTypes.add(type);
                count++;

                // проверка на наличие таблицы в избранном
                boolean inFavorite = Arrays.asList(favTables).contains(table);

                // заполнение графы Info
                String info = null;
                for (String f : favTables) {
                    if (f.startsWith(table + ":info=")) {
                        info = f.substring(table.length() + 6);
                        break;
                    }
                }

                Object[] row = new Object[]{count, inFavorite, type.replace("BASE ", "").toLowerCase(Locale.ROOT), table, "","", info};
                Gui.executeModel.addRow(row);
            }
            rs.close();
            st.close();
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    // Список всех столбцов текущего пользователя
    void getUserColumns(String tableName) {
        if (userColumns.size() > 0) userColumns.clear(); //?
        if (types.size() > 0) types.clear(); //?
        try {
            String sql = "select column_name, data_type, udt_name from information_schema.columns \n" +
                    "where table_name= '" + tableName + "' \n" +
                    "order by ordinal_position";
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String column = rs.getString("column_name");
                userColumns.add(column);
                String udt_name = rs.getString("udt_name");
                types.add(udt_name);
            }
            rs.close();
            st.close();
        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    // Данные из выбранной таблицы
    void selectFromTable(String table, String object) {
        String sqlQuery = null;
        try {
            if (object.equals("matview")) {
                sqlQuery = Gui.definitionFromMatview + " LIMIT " + rowsLimitFromConfig;
            } else if (object.equals("table")|| object.equals("view")) {
                sqlQuery = "SELECT * FROM " + table + " LIMIT " + rowsLimitFromConfig;
            }
            PreparedStatement st = connection.prepareStatement(sqlQuery);

            ResultSet rs = st.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                Arrays.setAll(row, x -> {
                    try {
                        switch (types.get(x)) {
                            case "int2":
                            case "int4":
                            case "serial":
                            case "serial4":
                                return rs.getInt(x + 1);
                            case "int8":
                            case "serial8":
                                return rs.getLong(x + 1);
                            case "date":
                                return rs.getDate(x + 1);
                            case "timestamp":
                                return rs.getTimestamp(x + 1);
                            case "bool":
                                return rs.getBoolean(x + 1);
                            case "float4":
                            case "decimal":
                                return rs.getFloat(x + 1);
                            default:
                                return rs.getString(x + 1);
                        }
                    } catch (SQLException sql) {
                        sql.printStackTrace();
                    }
                    return null;
                });
                Gui.selectModel.addRow(row);
            }
            st.close();

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    // Количество строк в таблице
    int rowsInTable(String table) {
        int rowsCountInTable = 0;
        String rowsCountInTableQuery = "select count(*) from " + table;
        PreparedStatement stRowsCount;
        try {
            stRowsCount = connection.prepareStatement(rowsCountInTableQuery);
            ResultSet rsRowsCount = stRowsCount.executeQuery();
            if (rsRowsCount.next()) {
                rowsCountInTable = rsRowsCount.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsCountInTable;
    }

    // Выполнение любого запроса
    void select(String sql){
        try {
            if (Gui.selectModel.getRowCount() > 0) Gui.selectModel.setRowCount(0);
            PreparedStatement st = connection.prepareStatement(sql.replaceAll("\n",""));
            ResultSet rs = st.executeQuery();
            int columnCount;
            List <SelectItem> cols = common.getColumns(sql);
            if (cols.get(0).toString().equals("*")) {
                columnCount = rs.getMetaData().getColumnCount();
            } else {
                columnCount = cols.size();
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                Arrays.setAll(row, x -> {
                    try {
                        switch (types.get(x)) {
                            case "int2":
                            case "int4":
                            case "serial":
                            case "serial4":
                                return rs.getInt(x + 1);
                            case "int8":
                            case "serial8":
                                return rs.getLong(x + 1);
                            case "date":
                                return rs.getDate(x + 1);
                            case "timestamp":
                                return rs.getTimestamp(x + 1);
                            case "bool":
                                return rs.getBoolean(x + 1);
                            case "float4":
                            case "decimal":
                                return rs.getFloat(x + 1);
                            default:
                                return rs.getString(x + 1);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                Gui.selectModel.addRow(row);
            }
            st.close();

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
