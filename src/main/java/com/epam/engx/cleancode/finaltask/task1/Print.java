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
    private static final String SPACE = " ";
    private static final String PRINT_COMMAND = "print ";
    private static final int CORRECT_NUMBER_OF_PARAMETERS = 2;

    private final View view;
    private final DatabaseManager manager;

    private enum LevelBoundary {
        UPPER("╔", "╦", "╗"),
        MIDDLE("╠", "╬", "╣"),
        BOTTOM("╚", "╩", "╝");

        private final String leftBoundary;
        private final String middleBoundary;
        private final String rightBoundary;

        LevelBoundary(String leftBoundary, String middleBoundary, String rightBoundary) {
            this.leftBoundary = leftBoundary;
            this.middleBoundary = middleBoundary;
            this.rightBoundary = rightBoundary;
        }
    }
    
    public Print(View view, DatabaseManager manager) {
        this.view = view;
        this.manager = manager;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith(PRINT_COMMAND);
    }

    @Override
    public void process(String input) {
        String[] commands = splitInputBySpace(input);
        validateCommandLength(commands.length);
        String tableName = commands[TABLE_NAME_INDEX];
        List<DataSet> data = manager.getTableData(tableName);
        view.write(getTableString(data, tableName));
    }

    private String[] splitInputBySpace(String input) {
        return input.split(SPACE);
    }

    private void validateCommandLength(int length) {
        if (length != CORRECT_NUMBER_OF_PARAMETERS) {
            throw new IllegalArgumentException("incorrect number of parameters. Expected 1, but is " + (length - 1));
        }
    }

    private String getTableString(List<DataSet> data, String tableName) {
        return hasNoColumnSize(getMaxColumnSize(data)) ?
                getEmptyTable(tableName) :
                getHeaderOfTheTable(data) + getBodyAndFooterOfTheTable(data);
    }

    private boolean hasNoColumnSize(int maxColumnSize) {
        return maxColumnSize == 0;
    }

    private String getEmptyTable(String tableName) {
        String textEmptyTable = String.format("║ Table '%s' is empty or does not exist ║", tableName);
        StringBuilder result = new StringBuilder();
        composeEmptyTable(textEmptyTable, result);
        return result.toString();
    }

    private void composeEmptyTable(String textEmptyTable, StringBuilder result) {
        getEmptyTableHeader(textEmptyTable, result);
        getEmptyTableBody(textEmptyTable, result);
        getEmptyTableFooter(textEmptyTable, result);
    }

    private void getEmptyTableHeader(String textEmptyTable, StringBuilder result) {
        result.append(UPPER_LEFT_CORNER_SYMBOL);
        result.append(drawHorizontalLine(textEmptyTable));
        result.append(UPPER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private void getEmptyTableBody(String textEmptyTable, StringBuilder result) {
        result.append(textEmptyTable).append(NEW_LINE);
    }

    private void getEmptyTableFooter(String textEmptyTable, StringBuilder result) {
        result.append(LOWER_LEFT_CORNER_SYMBOL);
        result.append(drawHorizontalLine(textEmptyTable));
        result.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private String drawHorizontalLine(String textEmptyTable) {
        return duplicateSymbol(HORIZONTAL_LINE_BORDER_SYMBOL, textEmptyTable.length() - 2);
    }

    private String duplicateSymbol(String symbol, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(symbol);
        }
        return builder.toString();
    }

//    private String buildHorizontalBoundaryLine(LevelBoundary levelBoundary other parameters) {
//        LevelBoundary.BOTTOM.rightBoundary;
//
//    }


    private int getMaxColumnSize(List<DataSet> dataSets) {
        OptionalInt maxLength = OptionalInt.of(0);
        OptionalInt maxLengthForDataValues = OptionalInt.of(0);
        if (hasDataSets(dataSets)) {
            maxLength = getMaxLengthForColumNames(dataSets);
            maxLengthForDataValues = getMaxLengthForDataValues(dataSets);
        }
        return calculateMaximumSize(maxLength, maxLengthForDataValues);
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
                .flatMap(List::stream)
                //   .filter(sc -> sc instanceof String)
                .map(String::valueOf)
                .mapToInt(String::length)
                .max();
    }

    private int calculateMaximumSize(OptionalInt maxLength, OptionalInt maxLengthForDataValues) {
        return Math.max(maxLength.orElse(0), maxLengthForDataValues.orElse(0));
    }

    private String getBodyAndFooterOfTheTable(List<DataSet> dataSets) {
        StringBuilder tableDataContent = new StringBuilder();
        int maxColumnSize = incrementMaxColumnSize(getMaxColumnSize(dataSets));
        int columnCount = getColumnCount(dataSets);
        buildTableBody(dataSets, tableDataContent);
        buildTableFooter(maxColumnSize, tableDataContent, columnCount);
        return tableDataContent.toString();
    }

    private void buildTableBody(List<DataSet> dataSets, StringBuilder tableDataContent) {
        int rowsCount = dataSets.size();
        int maxColumnSize = incrementMaxColumnSize(getMaxColumnSize(dataSets));
        int columnCount = getColumnCount(dataSets);
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
            for (int column = 0; column < columnCount; column++) {
                int valuesLength = String.valueOf(values.get(column)).length();
                if (valuesLength % 2 == 0) {
                    appendSpaceToTableContent(maxColumnSize, tableDataContent, valuesLength);
                    tableDataContent.append(values.get(column));
                    appendSpaceToTableContent(maxColumnSize, tableDataContent, valuesLength);
                    tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
                } else {
                    appendSpaceToTableContent(maxColumnSize, tableDataContent, valuesLength);
                    tableDataContent.append(values.get(column));
                    appendOneMoreSpaceToTableContent(maxColumnSize, tableDataContent, valuesLength);
                    tableDataContent.append(VERTICAL_LINE_BORDER_SYMBOL);
                }
            }
            tableDataContent.append(NEW_LINE);
            if (row < rowsCount - 1) {
                drawCellLowerPart(maxColumnSize, tableDataContent, columnCount);
            }
        }
    }

    private void drawCellLowerPart(int maxColumnSize, StringBuilder tableDataContent, int columnCount) {
        tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_RIGHT_LINE_SEGMENT);
        for (int j = 1; j < columnCount; j++) {
            composeHorizontalLine(maxColumnSize, tableDataContent);
            tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_HORIZONTAL_LINE_SEGMENT);
        }
        composeHorizontalLine(maxColumnSize, tableDataContent);
        tableDataContent.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_LEFT_LINE_SEGMENT).append(NEW_LINE);
    }

    private void buildTableFooter(int maxColumnSize, StringBuilder tableDataContent, int columnCount) {
        tableDataContent.append(LOWER_LEFT_CORNER_SYMBOL);
        for (int j = 1; j < columnCount; j++) {
            composeHorizontalLine(maxColumnSize, tableDataContent);
            tableDataContent.append(LOWER_COLUMN_SEPARATOR_SYMBOL);
        }
        composeHorizontalLine(maxColumnSize, tableDataContent);
        tableDataContent.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private void appendOneMoreSpaceToTableContent(int maxColumnSize, StringBuilder tableDataContent, int valuesLength) {
        for (int j = 0; j <= (maxColumnSize - valuesLength) / 2; j++) {
            tableDataContent.append(SPACE);
        }
    }

    private void appendSpaceToTableContent(int maxColumnSize, StringBuilder tableDataContent, int valuesLength) {
        for (int j = 0; j < (maxColumnSize - valuesLength) / 2; j++) {
            tableDataContent.append(SPACE);
        }
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
        StringBuilder result = new StringBuilder();
        int columnCount = getColumnCount(dataSets);
        int maxColumnSize = incrementMaxColumnSize(getMaxColumnSize(dataSets));
        drawUpperPartOfHeader(result, columnCount, maxColumnSize);
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result.append(VERTICAL_LINE_BORDER_SYMBOL);
            int columnNamesLength = columnNames.get(column).length();
            appendSpaceToTableContent(maxColumnSize, result, columnNamesLength);
            result.append(columnNames.get(column));
            if (isColumnNamesLengthEven(columnNamesLength)) {
                appendSpaceToTableContent(maxColumnSize, result, columnNamesLength);
            } else {
                appendOneMoreSpaceToTableContent(maxColumnSize, result, columnNamesLength);
            }
        }
        result.append(VERTICAL_LINE_BORDER_SYMBOL).append(NEW_LINE);

        //last string of the header
        if (hasDataSets(dataSets)) {
            drawCellLowerPart(maxColumnSize, result, columnCount);
        } else {
            buildTableFooter(maxColumnSize, result, columnCount);
        }
        return result.toString();
    }

    private void drawUpperPartOfHeader(StringBuilder result, int columnCount, int maxColumnSize) {
        result.append(UPPER_LEFT_CORNER_SYMBOL);
        for (int j = 1; j < columnCount; j++) {
            composeHorizontalLine(maxColumnSize, result);
            result.append(COLUMN_SEPARATOR);
        }
        composeHorizontalLine(maxColumnSize, result);
        result.append(UPPER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
    }

    private boolean isColumnNamesLengthEven(int columnNamesLength) {
        return columnNamesLength % 2 == 0;
    }

    private boolean hasDataSets(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }
}
