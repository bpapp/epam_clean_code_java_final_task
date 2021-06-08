package com.epam.engx.cleancode.finaltask.task1;


import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;

import java.util.List;

public class Print implements Command {

    private static final String LEFT_UPPER_CORNER = "╔";
    private static final String RIGHT_UPPER_CORNER = "╗";
    private static final String NEW_LINE = "\n";
    private static final String LEFT_CORNER_SYMBOL = "╚";
    private static final String HORIZONTAL_LINE_BORDER_SYMBOL = "═";
    public static final String VERTICAL_LINE_BORDER_SYMBOL = "║";

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
        String[] command = input.split(" ");
        validateCommandLength(command);
        tableName = command[1];
        List<DataSet> data = manager.getTableData(tableName);
        view.write(getTableString(data));
    }

    private void validateCommandLength(String[] command) {
        if (command.length != 2) {
            throw new IllegalArgumentException("incorrect number of parameters. Expected 1, but is " + (command.length - 1));
        }
    }

    private String getTableString(List<DataSet> data) {
        int maxColumnSize = getMaxColumnSize(data);
        if (maxColumnSize == 0) {
            return getEmptyTable(tableName);
        } else {
            return getHeaderOfTheTable(data) + getStringTableData(data);
        }
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = "║ Table '" + tableName + "' is empty or does not exist ║";
        StringBuilder result = new StringBuilder(LEFT_UPPER_CORNER);
        for (int i = 0; i < textEmptyTable.length() - 2; i++) {
            result.append(HORIZONTAL_LINE_BORDER_SYMBOL);
        }
        result.append(RIGHT_UPPER_CORNER).append(NEW_LINE);
        result.append(textEmptyTable + NEW_LINE);
        result.append(LEFT_CORNER_SYMBOL);
        for (int i = 0; i < textEmptyTable.length() - 2; i++) {
            result.append(HORIZONTAL_LINE_BORDER_SYMBOL);
        }
        result.append("╝\n");
        return result.toString();
    }

    private int getMaxColumnSize(List<DataSet> dataSets) {
        int maxLength = 0;
        if (hasDataSets(dataSets)) {
            List<String> columnNames = dataSets.get(0).getColumnNames();
            for (String columnName : columnNames) {
                if (columnName.length() > maxLength) {
                    maxLength = columnName.length();
                }
            }
            for (DataSet dataSet : dataSets) {
                List<Object> values = dataSet.getValues();
                for (Object value : values) {
//                    if (value instanceof String)
                        if (String.valueOf(value).length() > maxLength) {
                            maxLength = String.valueOf(value).length();
                        }
                }
            }
        }
        return maxLength;
    }

    private String getStringTableData(List<DataSet> dataSets) {
        int rowsCount;
        rowsCount = dataSets.size();
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        if (maxColumnSize % 2 == 0) {
            maxColumnSize += 2;
        } else {
            maxColumnSize += 3;
        }
        int columnCount = getColumnCount(dataSets);
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            result += VERTICAL_LINE_BORDER_SYMBOL;
            for (int column = 0; column < columnCount; column++) {
                int valuesLength = String.valueOf(values.get(column)).length();
                if (valuesLength % 2 == 0) {
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        result += " ";
                    }
                    result += String.valueOf(values.get(column));
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        result += " ";
                    }
                    result += VERTICAL_LINE_BORDER_SYMBOL;
                } else {
                    for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
                        result += " ";
                    }
                    result += String.valueOf(values.get(column));
                    for (int j = 0; j <= (maxColumnSize - valuesLength) / 2; j++) {
                        result += " ";
                    }
                    result += VERTICAL_LINE_BORDER_SYMBOL;
                }
            }
            result += NEW_LINE;
            if (row < rowsCount - 1) {
                result += "╠";
                for (int j = 1; j < columnCount; j++) {
                    for (int i = 0; i < maxColumnSize; i++) {
                        result += HORIZONTAL_LINE_BORDER_SYMBOL;
                    }
                    result += "╬";
                }
                for (int i = 0; i < maxColumnSize; i++) {
                    result += HORIZONTAL_LINE_BORDER_SYMBOL;
                }
                result += "╣\n";
            }
        }
        result += LEFT_CORNER_SYMBOL;
        for (int j = 1; j < columnCount; j++) {
            for (int i = 0; i < maxColumnSize; i++) {
                result += HORIZONTAL_LINE_BORDER_SYMBOL;
            }
            result += "╩";
        }
        for (int i = 0; i < maxColumnSize; i++) {
            result += HORIZONTAL_LINE_BORDER_SYMBOL;
        }
        result += "╝\n";
        return result;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return hasDataSets(dataSets) ?
                dataSets.get(0).getColumnNames().size() : 0;
    }

    private String getHeaderOfTheTable(List<DataSet> dataSets) {
        int maxColumnSize = getMaxColumnSize(dataSets);
        String result = "";
        int columnCount = getColumnCount(dataSets);
        if (maxColumnSize % 2 == 0) {
            maxColumnSize += 2;
        } else {
            maxColumnSize += 3;
        }
        result += LEFT_UPPER_CORNER;
        for (int j = 1; j < columnCount; j++) {
            for (int i = 0; i < maxColumnSize; i++) {
                result += HORIZONTAL_LINE_BORDER_SYMBOL;
            }
            result += "╦";
        }
        for (int i = 0; i < maxColumnSize; i++) {
            result += HORIZONTAL_LINE_BORDER_SYMBOL;
        }
        result += RIGHT_UPPER_CORNER + NEW_LINE;
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result += VERTICAL_LINE_BORDER_SYMBOL;
            int columnNamesLength = columnNames.get(column).length();
            if (isColumnNamesLengthEven(columnNamesLength)) {
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result += " ";
                }
                result += columnNames.get(column);
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result += " ";
                }
            } else {
                for (int j = 0; j < (maxColumnSize - columnNamesLength) / 2; j++) {
                    result += " ";
                }
                result += columnNames.get(column);
                for (int j = 0; j <= (maxColumnSize - columnNamesLength) / 2; j++) {
                    result += " ";
                }
            }
        }
        result += "║\n";

        //last string of the header
        if (hasDataSets(dataSets)) {
            result += "╠";
            for (int j = 1; j < columnCount; j++) {
                for (int i = 0; i < maxColumnSize; i++) {
                    result += HORIZONTAL_LINE_BORDER_SYMBOL;
                }
                result += "╬";
            }
            for (int i = 0; i < maxColumnSize; i++) {
                result += HORIZONTAL_LINE_BORDER_SYMBOL;
            }
            result += "╣\n";
        } else {
            result += LEFT_CORNER_SYMBOL;
            for (int j = 1; j < columnCount; j++) {
                for (int i = 0; i < maxColumnSize; i++) {
                    result += HORIZONTAL_LINE_BORDER_SYMBOL;
                }
                result += "╩";
            }
            for (int i = 0; i < maxColumnSize; i++) {
                result += HORIZONTAL_LINE_BORDER_SYMBOL;
            }
            result += "╝\n";
        }
        return result;
    }

    private boolean isColumnNamesLengthEven(int columnNamesLength) {
        return columnNamesLength % 2 == 0;
    }

    private boolean hasDataSets(List<DataSet> dataSets) {
        return dataSets.size() > 0;
    }
}