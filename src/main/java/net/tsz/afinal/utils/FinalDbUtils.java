package net.tsz.afinal.utils;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import net.tsz.afinal.db.sqlite.SqlBuilder;
import net.tsz.afinal.db.table.Id;
import net.tsz.afinal.db.table.Property;
import net.tsz.afinal.db.table.TableInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 数据库工具类(不单独提供直接创建表方法，只允许对现有的表进行更改)
 * 1、插入数据库表的普通列，注意不支持插入主键列
 * 2、重命名数据库表名
 * 3、复制整张数据库表的数据
 * 4、更新表结构，会根据参数TableInfo对应的表名查询数据库中是否存在。存在则更新参数TableInfo对应的表结构，一般用于数据库版本更新、
 * 5、判断表是否存在
 * 6、查询数据库表是否存在某个列
 * 7、获取某一个表的全部列名
 * 8、 查询数据库表的列的数据类型是否为指定数据类型
 *
 * @author  huangbiao
 */

public class FinalDbUtils {

    private static void debugSql(String sql) {
            android.util.Log.d("FinalDbUtils SQL", ">>>>>>  " + sql);
    }

    /**
     * 插入数据库表的普通列，注意不支持插入主键列
     */
    public static void alterAndAddCloumnTable(TableInfo table , String cloumnName, Class<?> dataType, SQLiteDatabase db){
        if(table == null) return;
        try {
            String tableName = table.getTableName();
            String sql = SqlBuilder.getAlterSql("ADD", tableName, cloumnName, dataType);
            debugSql(sql);
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重命名数据库表名
     */
    public static void alterAndRenameTable(String oldTableName, String newTableName, SQLiteDatabase db){
        try {
            String sql = "ALTER TABLE ";
            sql = sql+oldTableName+" RENAME TO " + newTableName+";";
            debugSql(sql);
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制整张数据库表的数据
     * @param columns  需要复制的列（必须保证两张表所复制的列的列名和数据类型存在并相等）
     */
    public static void copyTableData(String oldTableName, String newTableName, ArrayList<String> columns, SQLiteDatabase db){
        try {
            String copyTableDataSql = SqlBuilder.getCopyTableDataSql(oldTableName, newTableName, columns);
            debugSql(copyTableDataSql);
            db.execSQL(copyTableDataSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *  更新表结构，会根据参数TableInfo对应的表名查询数据库中是否存在。存在则更新参数TableInfo对应的表结构
     *  一般用于数据库版本更新
     *  @param tableInfo  新的表结构
     */
    public static void updateTableInfo(TableInfo tableInfo, SQLiteDatabase db){
        if(tableIsExist(tableInfo,db)){
            boolean isDrop = false;
            Id id = tableInfo.getId();
            ArrayList<String> copyColumns = new ArrayList<String>();
            ArrayList<String> tableIAllColumn = getTableIAllColumn(tableInfo.getTableName(), db);
            if(tableIAllColumn.contains(id.getColumn()) && tableIsColumnType(tableInfo,id.getColumn(),id.getDataType(),db)){
                copyColumns.add(id.getColumn());
                tableIAllColumn.remove(id.getColumn());
            }else {
                isDrop = true;
            }
            Collection<Property> propertys = tableInfo.propertyMap.values();
            for (Property property : propertys) {
                if(tableIAllColumn.contains(property.getColumn()) && tableIsColumnType(tableInfo,property.getColumn(),property.getDataType(),db)){
                    copyColumns.add(property.getColumn());
                    tableIAllColumn.remove(property.getColumn());
                }else {
                    isDrop = true;
                }
            }
            if(isDrop || tableIAllColumn.size() > 0 ){
                String oldTableName = tableInfo.getTableName();
                String newTableName = oldTableName + "_temp";
                tableInfo.setTableName(newTableName);
                if(tableIsExist(tableInfo,db)){
                    dropTable(tableInfo,db);
                }
                creatTable(tableInfo,db);
                if(copyColumns.size() > 0){
                    copyTableData(oldTableName,newTableName,copyColumns,db);
                }
                tableInfo.setTableName(oldTableName);
                dropTable(tableInfo,db);
                alterAndRenameTable(newTableName,oldTableName,db);
            }
        }
    }

    /**
     * 判断表是否存在
     */
    public static boolean tableIsExist(TableInfo table, SQLiteDatabase db) {
        if (table.isCheckDatabese())
            return true;

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.getTableName() + "' ";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabese(true);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return false;
    }

    /***
     * 查询数据库表是否存在某个列
     */
    public static boolean tableIsColumnExist(TableInfo table, String columnName, SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.getTableName() + "' AND sql like '%"+columnName+"%'";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabese(true);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return false;
    }

    /***
     * 获取某一个表的全部列名
     */
    public static ArrayList<String> getTableIAllColumn(String tableName, SQLiteDatabase db) {
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            String sql = "PRAGMA table_info (" +
                    tableName +
                    ");";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                int name = cursor.getColumnIndex("name");
                while (cursor.moveToNext()){
                    list.add(cursor.getString(name));
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return list;
    }

    /***
     * 查询数据库表的列的数据类型是否为指定数据类型
     */
    public static boolean tableIsColumnType(TableInfo table, String columnName, Class<?> dataType, SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            String dataTypeSql = SqlBuilder.getDataTypeSql(dataType);
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.getTableName() + "' AND sql like '%"+columnName;
            if(!TextUtils.isEmpty(dataTypeSql)){
                sql = sql+" "+dataTypeSql;
            }
            sql = sql+"%'";
            debugSql(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.setCheckDatabese(true);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return false;
    }

    private static void creatTable(TableInfo tableInfo, SQLiteDatabase db){
        String sql = SqlBuilder.getCreatTableSQL(tableInfo);
        debugSql(sql);
        db.execSQL(sql);
    }


    private static void dropTable(TableInfo table, SQLiteDatabase db) {
        String sql = "DROP TABLE " + table.getTableName();
        debugSql(sql);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        table.setCheckDatabese(false);
    }

}
