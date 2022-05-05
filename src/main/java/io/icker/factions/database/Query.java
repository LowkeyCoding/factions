package io.icker.factions.database;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Query {
    private String query;
    private Object[] args;

    public Query(String query, Object ...args) {
        this.query = query;
        this.args = args;
    }

    public void update() {
        try {
            PreparedStatement statement = Database.connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {}
    }

	public <T> T first(Class<T> clazz) {
        List<T> items = get(clazz);
        return (items == null || items.isEmpty()) ? null : items.get(0);
    }

	public <T> List<T> get(Class<T> clazz) {
        String table = getTableName(clazz);

        HashMap<String, Field> columns = new HashMap<String, Field>();
        for (Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                columns.put(field.getAnnotation(Column.class).value(), field);
            }
        }

        try {
            PreparedStatement statement = Database.connection.prepareStatement(query);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i, args[i]);
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData meta = result.getMetaData();
			int colCount = meta.getColumnCount();

            ArrayList<T> items = new ArrayList<T>();

            while (result.next()) {
                T row = clazz.getDeclaredConstructor().newInstance();
    
                for (int i = 1; i <= colCount; i++) {
                    String name = meta.getColumnLabel(i);
                    Object value = result.getObject(i);
    
                    columns.get(name).set(row, value);
                }
                items.add(row);
            }

            return items;
        } catch (SQLException | ReflectiveOperationException e) {
            return null;
        } 
    }

    private <T> String getTableName(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return table == null ? clazz.getSimpleName() : table.value();
    }
}