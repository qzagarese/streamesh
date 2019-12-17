package io.scicast.streamesh.shell.util;

import org.springframework.shell.table.*;

public class TableFactory {


    public static Table createTable(String[] headerData, String[][] content) {
       String newContent[][] = new String[(content.length) + 1][headerData.length];
       for(int i = 0; i < headerData.length; i++) {
           newContent[0][i] = headerData[i];
       }
       for(int i = 0; i < content.length; i ++) {
           for(int j = 0; j < headerData.length; j++) {
               newContent[i + 1][j] = content[i][j];
           }
       }
       return createTable(newContent);
    }

    public static Table createTable(String[][] content) {
        TableModel model = new ArrayTableModel(content);
        TableBuilder tableBuilder = new TableBuilder(model);
        for(int i = 0; i < content.length; i++) {
            for(int j = 0; j < content[i].length; j++) {
                content[i][j] = content[i][j] == null ? "" : content[i][j];
                tableBuilder.on(at(i, j)).addAligner(SimpleHorizontalAligner.center);
                tableBuilder.on(at(i, j)).addAligner(getVerticalAligner(content.length, i));
            }
        }
        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
    }

    private static SimpleVerticalAligner getVerticalAligner(int length, int i) {
        if(i == 0) {
            return SimpleVerticalAligner.top;
        } else if (i == length - 1) {
            return  SimpleVerticalAligner.bottom;
        } else {
            return SimpleVerticalAligner.middle;
        }
    }

    public static CellMatcher at(final int theRow, final int col) {
        return new CellMatcher() {
            @Override
            public boolean matches(int row, int column, TableModel model) {
                return row == theRow && column == col;
            }
        };
    }
}
