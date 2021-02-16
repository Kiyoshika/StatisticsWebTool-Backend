package com.zweaver.statswebtool.statswebtool.data.sort;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SortData {

    private String[] columns;

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    private int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.length; ++i) {
            if (columns[i].equals(columnName)) {
                return i;
            }
        }

        return -1;
    }

    /*
    * sortConditions shape:
    * 0 - column name, 1 - ascending/descending, 2 - sort as number/text
     */
    public String[][] sortData(String[][] dataSet, String[][] sortConditions) {
        List<Comparator<String[]>> condList = new ArrayList<>();
        // for first condition, use list.add
        // for all other conditions, replace "add" with "set" to chain the conditions together

        //int columnIndex = getColumnIndex(sortConditions[0][0]);
        int columnIndex = Integer.parseInt(sortConditions[0][0]);
        
        if (sortConditions[0][2].equals("NUMBER")) {
                switch (sortConditions[0][1]) {
                    case "ASCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String b[]) {
                                return (int) (Double.parseDouble(a[columnIndex]) - Double.parseDouble(b[columnIndex]));
                            }
                        });
                        break;

                    case "DESCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String b[]) {
                                return (int) (Double.parseDouble(b[columnIndex]) - Double.parseDouble(a[columnIndex]));
                            }
                        });
                        break;
                }
            } else if (sortConditions[0][2].equals("TEXT")) {
                switch (sortConditions[0][1]) {
                    case "ASCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String[] b) {
                                return a[columnIndex].compareTo(b[columnIndex]); // descending
                            }
                        });
                        break;

                    case "DESCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String[] b) {
                                return b[columnIndex].compareTo(a[columnIndex]); // descending
                            }
                        });
                        break;
                }
            }

        // iterate over rest of sorts to chain together conditions
        for (int i = 1; i < sortConditions.length; ++i) {
            // must use final variables for inner classes
            //final int columnIndexFinal = getColumnIndex(sortConditions[i][0]);
            final int columnIndexFinal = Integer.parseInt(sortConditions[i][0]);
            
            if (sortConditions[i][2].equals("NUMBER")) {
                switch (sortConditions[i][1]) {
                    case "ASCENDING":
                        condList.set(0, condList.get(0).thenComparing(new Comparator<String[]>() {
                            public int compare(String[] a, String b[]) {
                                return (int) (Double.parseDouble(a[columnIndexFinal]) - Double.parseDouble(b[columnIndexFinal]));
                            }
                        }));
                        break;

                    case "DESCENDING":
                        condList.set(0, condList.get(0).thenComparing(new Comparator<String[]>() {
                            public int compare(String[] a, String b[]) {
                                return (int) (Double.parseDouble(b[columnIndexFinal]) - Double.parseDouble(a[columnIndexFinal]));
                            }
                        }));
                        break;
                }
            } else if (sortConditions[i][2].equals("TEXT")) {
                switch (sortConditions[i][1]) {
                    case "ASCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String[] b) {
                                return a[columnIndexFinal].compareTo(b[columnIndexFinal]); // descending
                            }
                        });
                        break;

                    case "DESCENDING":
                        condList.add(new Comparator<String[]>() {
                            public int compare(String[] a, String[] b) {
                                return b[columnIndexFinal].compareTo(a[columnIndexFinal]); // descending
                            }
                        });
                        break;
                }
            }
        }
        
        Arrays.sort(dataSet, condList.get(0));
        return dataSet;
    }
}
