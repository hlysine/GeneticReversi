package io.github.hlysine.table;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private int columns = 0;
    private final List<String> headerRow = new ArrayList<>();
    private final List<TableRow> rows = new ArrayList<>();

    public void addColumn() {
        this.headerRow.add("");
        this.columns++;
    }

    public void addColumn(String header) {
        this.headerRow.add(header);
        this.columns++;
    }

    public TableRow addRow() {
        TableRow row = new TableRow(this.columns);
        this.rows.add(row);
        return row;
    }

    public TableRow getRow(int i) {
        return this.rows.get(i);
    }

    public void saveToFile(String path) throws IOException {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(path)).build()) {
            boolean headerEmpty = true;
            for (String header : this.headerRow) {
                if (header.length() > 0) {
                    headerEmpty = false;
                    break;
                }
            }
            if (!headerEmpty) {
                writer.writeNext(this.headerRow.toArray(new String[0]));
            }
            for (TableRow row : this.rows) {
                writer.writeNext(row.columns);
            }
        }
    }

    public static Table loadFromFile(String path) throws IOException, CsvException {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(path)).build()) {
            List<String[]> rows = reader.readAll();
            Table table = new Table();
            table.columns = rows.get(0).length;
            for (String[] strings : rows) {
                TableRow row = new TableRow(strings);
                table.rows.add(row);
            }
            return table;
        }
    }
}
