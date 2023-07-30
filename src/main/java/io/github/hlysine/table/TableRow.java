package io.github.hlysine.table;

public class TableRow {
    public final String[] columns;

    public TableRow(String[] columns) {
        this.columns = columns;
    }

    public TableRow(int columns) {
        this.columns = new String[columns];
    }

    public void setFloat(int column, float value) {
        this.columns[column] = Float.toString(value);
    }

    public float getFloat(int column) {
        return Float.parseFloat(this.columns[column]);
    }

    public void setInt(int column, int value) {
        this.columns[column] = Integer.toString(value);
    }

    public int getInt(int column) {
        return Integer.parseInt(this.columns[column]);
    }

    public void setString(int column, String value) {
        this.columns[column] = value;
    }

    public String getString(int column) {
        return this.columns[column];
    }
}
