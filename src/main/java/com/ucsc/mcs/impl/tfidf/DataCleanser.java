package com.ucsc.mcs.impl.tfidf;

import com.monitorjbl.xlsx.StreamingReader;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * Created by JagathA on 8/11/2017.
 */
public class DataCleanser {

    private SqlConnector sqlConnector = null;

    public DataCleanser(SqlConnector connector) {
        this.sqlConnector = connector;
    }

    public void cleanAndInsertInitialAnnData() {
        int count = 0;
        int arabicCount = 0;
        int errors = 0;

        try {
            InputStream is = new FileInputStream(new File("ANNOUNCEMENT Dump170614.xlsx"));
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(4096)
                    .open(is);
            ArrayList<String[]> rows = new ArrayList();
            Sheet sheet = workbook.getSheet("Export Worksheet");
                System.out.println("Started writing Ann data " + sheet.getSheetName());
                for (Row r : sheet) {
                    String[] row = new String[7];
                    try {
                        row[0] = r.getCell(0).getStringCellValue();
                        row[1] = r.getCell(1).getStringCellValue();
                        row[2] = r.getCell(2).getStringCellValue();
                        row[3] = r.getCell(3).getStringCellValue();
                        row[4] = r.getCell(4).getStringCellValue();
                        row[5] = r.getCell(5).getStringCellValue();
                        row[6] = r.getCell(6).getStringCellValue();
                    } catch (Exception e) {
                        errors ++;
                        e.printStackTrace();
                    }

                    if(count == 0) {
                        count ++;
                        continue;
                    }

                    if(isProbablyArabic(row[4])){
                        arabicCount ++;
                        continue;
                    }

                    rows.add(row);
//                    System.out.println("+++++ " + Arrays.asList(row));
                    this.insertAnnData(row);
                    count ++;
                }
            System.out.println("Ann Completed.... " + rows.size() + " " + count + " " + arabicCount + " " + errors);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void insertAnnData(String[] line) {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc2.announcements (ann_id, exchange, ann_date, symbol, heading, body, language_id) values (?,?,?,?,?,?,?)";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setInt(1, Integer.parseInt(line[0]));
                statement.setString(2, line[1]);
                statement.setString(3, line[2]);
                statement.setString(4, line[3]);
                statement.setString(5, (Jsoup.parse(line[4]).text()).replaceAll("[^a-zA-Z0-9]", " "));
                statement.setString(6, (Jsoup.parse(line[5]).text()).replaceAll("[^a-zA-Z0-9]", " "));
                statement.setString(7, line[6]);
                statement.execute();

//                System.out.println("Announcement added ! " + line[0]);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Line : " + line);
            } finally {
            }
        }
    }


    public void cleanAndInsertInitialNewsData() {
        int count = 0;
        int arabicCount = 0;
        int errors = 0;

        try {
            InputStream is = new FileInputStream(new File("NEWS Dump170614.xlsx"));
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(4096)
                    .open(is);
            ArrayList<String[]> rows = new ArrayList();


            Sheet sheet = workbook.getSheet("Export Worksheet");
            System.out.println("Started writing News data " + sheet.getSheetName());
                for (Row r : sheet) {
                    String[] row = new String[6];
                    try {
                        row[0] = r.getCell(0).getStringCellValue();
                        row[1] = r.getCell(1).getStringCellValue();
                        row[2] = r.getCell(2).getStringCellValue();
                        row[3] = r.getCell(3).getStringCellValue();
                        row[4] = r.getCell(4).getStringCellValue();
                        row[5] = r.getCell(5).getStringCellValue();
                    } catch (Exception e) {
                        errors ++;
                        e.printStackTrace();
                    }

                    if(count == 0) {
                        count ++;
                        continue;
                    }


                    if(isProbablyArabic(row[4])){
                        arabicCount ++;
                        continue;
                    }
                    rows.add(row);
//                    System.out.println("+++++ " + Arrays.asList(row));
                    this.insertNewsData(row);
                    count ++;
                }


            System.out.println("News Completed... " + rows.size() + " " + count + " " + arabicCount + " " + errors);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertNewsData(String[] line) {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc2.news (news_id, exchange, symbol, news_date, heading, body) " +
                    "values (?,?,?,?,?,?)";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setInt(1, Integer.parseInt(line[0]));
                statement.setString(2, line[1]);
                statement.setString(3, line[2]);
                statement.setString(4, line[3].substring(0,10));
                statement.setString(5, (Jsoup.parse(line[4]).text()).replaceAll("[^a-zA-Z0-9]", " "));
                statement.setString(6, (Jsoup.parse(line[5]).text()).replaceAll("[^a-zA-Z0-9]", " "));
                statement.execute();

//                System.out.println("News added ! " + line[0]);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Line : " + line);
            } finally {
            }
        }
    }

    public void cleanAndInsertInitialHistoryData() {
        int count = 0;
        int arabicCount = 0;
        File file = new File("HIST_OPEN_CLOSE-csv.csv");
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(file), "ASCII"));
            for (String line; (line = br.readLine()) != null; ) {
                System.out.println(">> " + count + " - " + line);
                if (count == 0){
                    count ++;
                    continue;
                }

                if (line.contains("???")) {
                    count ++;
                    arabicCount ++;
                    continue;
                }

                this.insertHistoryData(line);
                count++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Completed.... " + count + " " + arabicCount);

    }

    private void insertHistoryData(String line) {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc2.history (exchange, symbol, date, open, close) values (?,?,?,?,?)";
            String[] params = line.split(",");
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setString(1, params[0]);
                statement.setString(2, params[1]);
                statement.setString(3, params[2]);
                statement.setString(4, params[3]);
                statement.setString(5, params[4]);
                statement.execute();

                System.out.println("History added ! " + params[0]);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error Line : " + line);
            } finally {
            }
        }
    }

    private boolean isProbablyArabic(String s) {
        try {
            for (int i = 0; i < s.length();) {
                int c = s.codePointAt(i);
                if (c >= 0x0600 && c <= 0x06E0)
                    return true;
                i += Character.charCount(c);
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }
}
