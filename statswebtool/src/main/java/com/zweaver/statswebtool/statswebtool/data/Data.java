package com.zweaver.statswebtool.statswebtool.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Data {
    public String[] columns;
    public String[][] data;
    
    private int rowCount = 0;
    private int colCount = 0;
    
    // get dimensions of data set to properly size array
    public void getDims(BufferedReader br) {
        int currentRow = 0;
        String[] splitRow;
        
        // count rows and colunns to size array first
        try {           
            String currentLine;
            while ((currentLine = br.readLine()) != null) {

                rowCount += 1;
                if (currentRow == 0) {
                    columns = currentLine.split(",");
                    colCount = columns.length;
                    currentRow += 1;
                }
            }
            br.close();
            columns = new String[colCount];
            data = new String[rowCount-1][colCount]; //-1 for the column headers
        } catch (Exception e) {
            System.out.println("Couldn't read file!");
        }
    }
        
    // naive parser to populate data (uses split() method which is not a great way to parse, but good for now)
    public void parseCSV(BufferedReader br) {
        int currentRow = 0;
        String[] splitRow;

        try {           
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
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
            br.close();
        } catch (Exception e) {
            System.out.println("Couldn't read file to populate data!");
        }
    }
}