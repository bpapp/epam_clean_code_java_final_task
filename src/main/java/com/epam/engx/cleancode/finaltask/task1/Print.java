package com.epam.engx.cleancode.finaltask.task1;


import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;

import java.util.List;
import java.util.OptionalInt;

public class Print implements Command {

    private static final String UPPER_LEFT_CORNER_SYMBOL = "╔";
    private static final String UPPER_RIGHT_CORNER_SYMBOL = "╗";
    private static final String NEW_LINE = "\n";
    private static final String LOWER_LEFT_CORNER_SYMBOL = "╚";
    private static final String LOWER_RIGHT_CORNER_SYMBOL = "╝";
    private static final String LOWER_COLUMN_SEPARATOR_SYMBOL = "╩";
    private static final String HORIZONTAL_LINE_BORDER_SYMBOL = "═";
    private static final String VERTICAL_LINE_BORDER_SYMBOL = "║";
    private static final String COLUMN_SEPARATOR = "╦";
    private static final String BOX_DRAWING_DOUBLE_VERTICAL_AND_LEFT_LINE_SEGMENT = "╣";
    private static final String BOX_DRAWING_DOUBLE_VERTICAL_AND_RIGHT_LINE_SEGMENT = "╠";
    private static final String BOX_DRAWING_DOUBLE_VERTICAL_AND_HORIZONTAL_LINE_SEGMENT = "╬";
    private static final int TABLE_NAME_INDEX = 1;

    private View view;
    private DatabaseManager manager;
    private String tableName;

    public Print(View view, DatabaseManager manager) {
        this.view = view;
        this.manager = manager;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith("print ");
    }

    @Override
    public void process(String input) {
        String[] command = splitInputBySpace(input);
        validateCommandLength(command);
        tableName = command[TABLE_NAME_INDEX];
        List<DataSet> data = manager.getTableData(tableName);
        view.write(getTableString(data));
    }

    private String[] splitInputBySpace(String input) {
        return input.split(" ");
    }

    private void validateCommandLength(String[] command) {
        if (command.length != 2) {
            throw new IllegalArgumentException("incorrect number of parameters. Expected 1, but is " + (command.length - 1));
        }
    }

    private String getTableString(List<DataSet> data) {
        return hasNoColumnSize(getMaxColumnSize(data)) ?
                getEmptyTable(tableName) :
                getHeaderOfTheTable(data) + getStringTableData(data);
    }

    private boolean hasNoColumnSize(int maxColumnSize) {
        return maxColumnSize == 0;
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = "║ Table '" + tableName + "' is empty or does not exist ║";
        StringBuilder result = new StringBuilder();
        getEmptyTableHeader(textEmptyTable, result);
        getEmptyTableBody(textEmptyTable, result);
        getEmptyTableFooter(textEmptyTable, result);
        return result.toString();
    }

    private void getEmptyTableHeader(String textEmptyTable, StringBuilder result) {
        result.append(UPPER_LEFT_CORNER_SYMBOL);
        for (int i = 0; i < textEmptyTable.length() - 2; i++) {
            result.append(HORIZONTAL_LINE_BORDER_SYMBOL);
        }
        result.append(UPPER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private void getEmptyTableBody(String textEmptyTable, StringBuilder result) {
        result.append(textEmptyTable + NEW_LINE);
    }

    private void getEmptyTableFooter(String textEmptyTable, StringBuilder result) {
        result.append(LOWER_LEFT_CORNER_SYMBOL);
        for (int i = 0; i < textEmptyTable.length() - 2; i++) {
            result.append(HORIZONTAL_LINE_BORDER_SYMBOL);
        }
        result.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private int getMaxColumnSize(List<DataSet> dataSets) {
        OptionalInt maxLength = OptionalInt.of(0);
        OptionalInt maxLengthForDataValues = OptionalInt.of(0);
        if (hasDataSets(dataSets)) {
            maxLength = getMaxLengthForColumNames(dataSets);
            maxLengthForDataValues = getMaxLengthForDataValues(dataSets);
        }
        return countMaximumSize(maxLength, maxLengthForDataValues);
    }

    private OptionalInt getMaxLengthForColumNames(List<DataSet> dataSets) {
        OptionalInt maxLength;
        maxLength = dataSets.get(0).getColumnNames().stream()
                .mapToInt(String::length)
                .max();
        return maxLength;
    }

    private OptionalInt getMaxLengthForDataValues(List<DataSet> dataSets) {
        return dataSets.stream()
                .map(DataSet::getValues)
                .flatMap(items -> items.stream())
                //   .filter(sc -> sc instanceof String)
                .map(String::valueOf)
                .mapToInt(String::length)
                .max();
    }

    private int countMaximumSize(OptionalInt maxLength, OptionalInt maxLengthForDataValues) {
        return Math.max(maxLength.orElse(0), maxLengthForDataValues.orElse(0));
    }

    private String getStringTableData(List<DataSet> dataSets) {
        int rowsCount = dataSets.size();
        int maxColumnSize = getMaxColumnSize(dataSets);
        StringBuilder tableDataContent = new StringBuilder();
        maxColumnSize = incrementMaxColumnSize(maxColumnSize);
        int columnCount = getColumnCount(dataSets);
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
            for (int column = 0; column < columnCount; column++) {
                int valuesLength = String.valueOf(values.get(column)).length();
                if (valuesLength % 2 == 0) {
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        tableDataContent.append(" ");
                    }
                    tableDataContent.append(values.get(column));
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        tableDataContent.append(" ");
                    }
                    tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
                } else {
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        tableDataContent.append(" ");
                    }
                    tableDataContent.append(values.get(column));
                    for (int j = 0; j <= (maxColumnSize - valuesLength) / 2; j++) {
                        tableDataContent.append(" ");
                    }
                    tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
                }
            }
            tableDataContent.append(NEW_LINE);
            if (row < rowsCount - 1) {
                tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_RIGHT_LINE_SEGMENT);
                for (int j = 1; j < columnCount; j++) {
                    composeHorizontalLine(maxColumnSize, tableDataContent);
                    tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_HORIZONTAL_LINE_SEGMENT);
                }
                composeHorizontalLine(maxColumnSize, tableDataContent);
                tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_LEFT_LINE_SEGMENT).append(NEW_LINE);
            }
        }
        tableDataContent.append(LOWER_LEFT_CORNER_SYMBOL);
        for (int j = 1; j < columnCount; j++) {
            composeHorizontalLine(maxColumnSize, tableDataContent);
            tableDataContent.append(LOWER_COLUMN_SEPARATOR_SYMBOL);
        }
        composeHorizontalLine(maxColumnSize, tableDataContent);
        tableDataContent.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
        return tableDataContent.toString();
    }

    private int incrementMaxColumnSize(int maxColumnSize) {
        return (maxColumnSize % 2 == 0) ? (maxColumnSize + 2) : (maxColumnSize + 3);
    }

    private void composeHorizontalLine(int maxColumnSize, StringBuilder result) {
        for (int i = 0; i < maxColumnSize; i++) {
            result.append(HORIZONTAL_LINE_BORDER_SYMBOL);
        }
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return hasDataSets(dataSets) ?
                dataSets.get(0).getColumnNames().size() : 0;
    }

    private String getHeaderOfTheTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        StringBuilder result = new StringBuilder();
        int columnCount = getColumnCount(dataSets);
        maxColumnSize = incrementMaxColumnSize(maxColumnSize);
        result.append(UPPER_LEFT_CORNER_SYMBOL);
        for (int j = 1; j < columnCount; j++) {
            composeHorizontalLine(maxColumnSize, result);
            result.append(COLUMN_SEPARATOR);
        }
        composeHorizontalLine(maxColumnSize, result);
        result.append(UPPER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result.append(VERTICAL_LINE_BORDER_SYMBOL);
            int columnNamesLength = columnNames.get(column).length();
            if (isColumnNamesLengthEven(columnNamesLength)) {
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result.append(" ");
                }
                result.append(columnNames.get(column));
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result.append(" ");
                }
            } else {
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result.append(" ");
                }
                result.append(columnNames.get(column));
                for (int j = 0; j <= (maxColumnSize - columnNamesLength) / 2; j++) {
                    result.append(" ");
                }
            }
        }
        result.append(VERTICAL_LINE_BORDER_SYMBOL).append(NEW_LINE);

        //last string of the header
        if (hasDataSets(dataSets)) {
            result.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_RIGHT_LINE_SEGMENT);
            for (int j = 1; j < columnCount; j++) {
                composeHorizontalLine(maxColumnSize, result);
                result.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_HORIZONTAL_LINE_SEGMENT);
            }
            composeHorizontalLine(maxColumnSize, result);
            result.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_LEFT_LINE_SEGMENT).append(NEW_LINE);
        } else {
            result.append(LOWER_LEFT_CORNER_SYMBOL);
            for (int j = 1; j < columnCount; j++) {
                composeHorizontalLine(maxColumnSize, result);
                result.append(LOWER_COLUMN_SEPARATOR_SYMBOL);
            }
            composeHorizontalLine(maxColumnSize, result);
            result.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
        }
        return result.toString();
    }

    private boolean isColumnNamesLengthEven(int columnNamesLength) {
        return columnNamesLength % 2 == 0;
    }

    private boolean hasDataSets(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }
}