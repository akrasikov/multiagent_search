/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Anton
 */
public class ResultsModel extends AbstractTableModel {

    private List<SearchResult> results;
    private String[] columnNames = {"Имя файла", "Ресурс", "Тип", "Совпадений"};

    public ResultsModel() {
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public int getRowCount() {
        return results.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        SearchResult result = results.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return result.getPath();
            case 1:
                return result.getResource();
            case 2:
                return result.getType();
            case 3:
                return result.getHits();
            default:
                return null;
        }
    }
}
