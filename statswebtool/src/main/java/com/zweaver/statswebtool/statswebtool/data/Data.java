package com.zweaver.statswebtool.statswebtool.data;

import java.io.File;
import java.util.Scanner;

public class Data {
    public String[] columns;
    public String[][] data;
    
    private int rowCount = 0;
    private int colCount = 0;
    
    // naive parser, assumes it contains column names and no commas inside strings
    public void parseCSV(String filePath) {
        int currentRow = 0;
        String[] splitRow;
        
        // count rows and colunns to size array first
        try {
            File csvFile = new File(filePath);
            Scanner fileReader = new Scanner(csvFile);
            
            while (fileReader.hasNextLine()) {
                String currentLine = fileReader.nextLine();
                rowCount += 1;
                if (currentRow == 0) {
                    columns = currentLine.split(",");
                    colCount = columns.length;
                    currentRow += 1;
                }
            }
            fileReader.close();
        } catch (Exception e) {
            System.out.println("Couldn't read file!");
        }
        
        // reset row counter for second iteration of file
        currentRow = 0;
        columns = new String[colCount];
        data = new String[rowCount-1][colCount]; //-1 for the column headers
        
        // populate data
        try {
            File csvFile = new File(filePath);
            Scanner fileReader = new Scanner(csvFile);
            
            while (fileReader.hasNextLine()) {
                String currentLine = fileReader.nextLine();
                if (currentRow == 0) {
                    columns = currentLine.split(",");
                    currentRow += 1;
                } else {
                    splitRow = currentLine.split(",");
                    for (int i = 0; i < splitRow.length; ++i) {
                        data[currentRow-1][i] = splitRow[i];
                    }
                    currentRow += 1;
                }
            }
            fileReader.close();
        } catch (Exception e) {
            System.out.println("Couldn't read file!");
        }
    }
}