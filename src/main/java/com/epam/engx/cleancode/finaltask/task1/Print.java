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
    private static final int ONE_MORE_SPACE_VALUE = 1;

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
        result.append(composeEmptyTable(textEmptyTable));
        return result.toString();
    }

    private String composeEmptyTable(String textEmptyTable) {
        StringBuilder builder = new StringBuilder();
        builder.append(getEmptyTableHeader(textEmptyTable));
        builder.append(getEmptyTableBody(textEmptyTable));
        builder.append(getEmptyTableFooter(textEmptyTable));
        return builder.toString();
    }

    private String getEmptyTableHeader(String textEmptyTable) {
        StringBuilder builder = new StringBuilder();
        builder.append(UPPER_LEFT_CORNER_SYMBOL);
        builder.append(drawHorizontalLine(textEmptyTable));
        builder.append(UPPER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
        return builder.toString();
    }

    private String getEmptyTableBody(String textEmptyTable) {
        return textEmptyTable.concat(NEW_LINE);
    }

    private String getEmptyTableFooter(String textEmptyTable) {
        StringBuilder builder = new StringBuilder();
        builder.append(LOWER_LEFT_CORNER_SYMBOL);
        builder.append(drawHorizontalLine(textEmptyTable));
        builder.append(LOWER_RIGHT_CORNER_SYMBOL).append(NEW_LINE);
        return builder.toString();
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

    private String buildHorizontalBoundaryLine(LevelBoundary levelBoundary, int columnCount, int maxColumnSize) {
        StringBuilder builder = new StringBuilder();
        builder.append(levelBoundary.leftBoundary);
        for (int j = 1; j < columnCount; j++) {
            builder.append(composeHorizontalLine(maxColumnSize));
            builder.append(levelBoundary.middleBoundary);
        }
        builder.append(composeHorizontalLine(maxColumnSize));
        builder.append(levelBoundary.rightBoundary);
        return builder.toString();
    }


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
        tableDataContent.append(buildTableBody(dataSets));
        tableDataContent.append(buildTableFooter(maxColumnSize, columnCount));
        return tableDataContent.toString();
    }

    private String buildTableBody(List<DataSet> dataSets) {
        StringBuilder tableBody = new StringBuilder();
        int rowsCount = dataSets.size();
        int maxColumnSize = incrementMaxColumnSize(getMaxColumnSize(dataSets));
        int columnCount = getColumnCount(dataSets);
        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            tableBody.append(VERTICAL_LINE_BORDER_SYMBOL);
            for (int column = 0; column < columnCount; column++) {
                int valuesLength = String.valueOf(values.get(column)).length();
                if (valuesLength % 2 == 0) {
                    tableBody.append(appendSpaceToTableContent(maxColumnSize, valuesLength));
                    tableBody.append(values.get(column));
                    tableBody.append(appendSpaceToTableContent(maxColumnSize, valuesLength));
                    tableBody.append(VERTICAL_LINE_BORDER_SYMBOL);
                } else {
                    tableBody.append(appendSpaceToTableContent(maxColumnSize, valuesLength));
                    tableBody.append(values.get(column));
                    tableBody.append(appendOneMoreSpaceToTableContent(maxColumnSize, valuesLength));
                    tableBody.append(VERTICAL_LINE_BORDER_SYMBOL);
                }
            }
            tableBody.append(NEW_LINE);
            if (row < rowsCount - 1) {
                tableBody.append(drawCellLowerPart(maxColumnSize, columnCount));
            }
        }
        return tableBody.toString();
    }

    private String drawCellLowerPart(int maxColumnSize, int columnCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_RIGHT_LINE_SEGMENT);
        for (int j = 1; j < columnCount; j++) {
            builder.append(composeHorizontalLine(maxColumnSize));
            builder.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_HORIZONTAL_LINE_SEGMENT);
        }
        builder.append(composeHorizontalLine(maxColumnSize));
        builder.append(BOX_DRAWING_DOUBLE_VERTICAL_AND_LEFT_LINE_SEGMENT).append(NEW_LINE);
        return builder.toString();
    }

    private String buildTableFooter(int maxColumnSize, int columnCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                buildHorizontalBoundaryLine(LevelBoundary.BOTTOM, columnCount, maxColumnSize))
                .append(NEW_LINE);
        return builder.toString();
    }

    private String appendOneMoreSpaceToTableContent(int maxColumnSize, int valuesLength) {
        return duplicateSymbol(SPACE, ((maxColumnSize - valuesLength) / 2) + ONE_MORE_SPACE_VALUE);
    }

    private String appendSpaceToTableContent(int maxColumnSize, int valuesLength) {
        return duplicateSymbol(SPACE, ((maxColumnSize - valuesLength) / 2));
    }

    private int incrementMaxColumnSize(int maxColumnSize) {
        return (maxColumnSize % 2 == 0) ? (maxColumnSize + 2) : (maxColumnSize + 3);
    }

    private String composeHorizontalLine(int maxColumnSize) {
        return duplicateSymbol(HORIZONTAL_LINE_BORDER_SYMBOL, maxColumnSize);
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return hasDataSets(dataSets) ?
                dataSets.get(0).getColumnNames().size() : 0;
    }

    private String getHeaderOfTheTable(List<DataSet> dataSets) {
        StringBuilder result = new StringBuilder();
        int columnCount = getColumnCount(dataSets);
        int maxColumnSize = incrementMaxColumnSize(getMaxColumnSize(dataSets));
        result.append(drawUpperPartOfHeader(columnCount, maxColumnSize));
        List<String> columnNames = dataSets.get(0).getColumnNames();
        for (int column = 0; column < columnCount; column++) {
            result.append(VERTICAL_LINE_BORDER_SYMBOL);
            int columnNamesLength = columnNames.get(column).length();
            result.append(appendSpaceToTableContent(maxColumnSize, columnNamesLength));
            result.append(columnNames.get(column));
            if (isColumnNamesLengthEven(columnNamesLength)) {
                result.append(appendSpaceToTableContent(maxColumnSize, columnNamesLength));
            } else {
                result.append(appendOneMoreSpaceToTableContent(maxColumnSize, columnNamesLength));
            }
        }
        result.append(VERTICAL_LINE_BORDER_SYMBOL).append(NEW_LINE);

        //last string of the header
        if (hasDataSets(dataSets)) {
            result.append(drawCellLowerPart(maxColumnSize, columnCount));
        } else {
            result.append(buildTableFooter(maxColumnSize, columnCount));
        }
        return result.toString();
    }

    private String drawUpperPartOfHeader(int columnCount, int maxColumnSize) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                buildHorizontalBoundaryLine(LevelBoundary.UPPER, columnCount, maxColumnSize))
                .append(NEW_LINE);
        return builder.toString();
    }

    private boolean isColumnNamesLengthEven(int columnNamesLength) {
        return columnNamesLength % 2 == 0;
    }

    private boolean hasDataSets(List<DataSet> dataSets) {
        return !dataSets.isEmpty();
    }
}
