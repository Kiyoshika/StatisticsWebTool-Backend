package com.zweaver.statswebtool.statswebtool.data.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterData {
    private String[] columns;

    private int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.length; ++i) {
            if (columns[i].equals(columnName)) {
                return i;
            }
        }

        return -1;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    // leeched this method from stack overflow:
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    private int evaluateExpression(final String str) {
        return (int) new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+'))
                        x += parseTerm(); // addition
                    else if (eat('-'))
                        x -= parseTerm(); // subtraction
                    else
                        return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*'))
                        x *= parseFactor(); // multiplication
                    else if (eat('/'))
                        x /= parseFactor(); // division
                    else
                        return x;
                }
            }

            double parseFactor() {
                if (eat('+'))
                    return parseFactor(); // unary plus
                if (eat('-'))
                    return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z')
                        nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt"))
                        x = Math.sqrt(x);
                    else if (func.equals("sin"))
                        x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos"))
                        x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan"))
                        x = Math.tan(Math.toRadians(x));
                    else
                        throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                    x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    /*
     * conditions: {0, ">5", "or"}, {2, "<=4", "and", {2, ">1", "none"}
     * 
     * The above expression will return results where column 0 > 5 OR (column 2 <= 4
     * AND column 2 > 1)
     */
    public String[][] filter(String[][] data, String[][] conditions) {
        // iterate over conditions and...apply
        String conditionText = "";
        boolean longCondition = true;
        String[] logicalExpressions = new String[data.length];
        int currentColumn = -1; // temporary

        for (int currentRow = 0; currentRow < data.length; ++currentRow) {
            logicalExpressions[currentRow] = ""; // start with blank expression per record

            for (int currentExpression = 0; currentExpression < conditions.length; ++currentExpression) {
                conditionText = conditions[currentExpression][1];
                // start with longer conditions
                switch (conditionText.substring(0, 2)) {
                    case ">=":
                        System.out.println(">=");
                        break;

                    case "<=":
                        System.out.println("<=");
                        break;

                    case "!=":
                        System.out.println("!=");
                        break;

                    case "<>": // contains (string)
                        System.out.println("contains");
                        break;

                    default:
                        longCondition = false;
                        break;
                }

                if (!longCondition) {
                    switch (conditionText.substring(0, 1)) {
                        case ">":
                            System.out.println(">");
                            break;

                        case "<":
                            System.out.println("<");
                            break;

                        case "=":
                            //currentColumn = getColumnIndex(conditions[currentExpression][0]);
                            currentColumn = Integer.parseInt(conditions[currentExpression][0]);
                            logicalExpressions[currentRow] += (data[currentRow][currentColumn]
                                    .equals(conditionText.substring(1))) ? "1" : "0";
                            // if logical is AND then multiply, else (OR) add
                            // Don't add any symbols at the end of an expression (or just ignore last
                            // expression)
                            if (currentExpression != conditions.length - 1 && currentRow != data.length) {
                                logicalExpressions[currentRow] += conditions[currentExpression][2].equals("AND") ? "*"
                                        : conditions[currentExpression][2].equals("OR") ? "+" : "";
                            }
                            break;

                        case "~": // like (SQL)
                            System.out.println("like");
                            break;

                        default:
                            if (conditions[currentExpression][1].equals("GROUP BEGIN")) {
                                logicalExpressions[currentRow] += "(";
                            } else if (conditions[currentExpression][1].equals("GROUP END")) {
                                logicalExpressions[currentRow] += ")";
                                logicalExpressions[currentRow] += conditions[currentExpression][2].equals("AND") ? "*"
                                        : conditions[currentExpression][2].equals("OR") ? "+" : "";
                            }
                            break;
                    }
                }
                // reset
                longCondition = true;
            }
        }

        // parse expressions to return results
        int result;
        List<String[]> filteredRows = new ArrayList<String[]>();
        for (int i = 0; i < logicalExpressions.length; ++i) {
            result = evaluateExpression(logicalExpressions[i]);
            if (result > 0) {
                filteredRows.add(data[i]); // keep row i
            } // otherwise, ignore row
        }

        String[][] filteredData = new String[filteredRows.size()][];
        filteredData = filteredRows.toArray(filteredData);
        return filteredData;
    }
}
