package com.ucsc.mcs.impl.datamanage;

import com.ucsc.mcs.impl.connector.SqlConnector;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        File file = new File("ANNOUNCEMENT Dump170614-2.csv");
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

                if (line.contains("?")) {
                    count ++;
                    arabicCount ++;
                    continue;
                }

                this.insertAnnData(line);
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

    private void insertAnnData(String line) {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc.announcements (ann_id, exchange, ann_date, symbol, heading, body, language_id) values (?,?,?,?,?,?,?)";
            String[] params = line.split(",");
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setInt(1, Integer.parseInt(params[0]));
                statement.setString(2, params[1]);
                statement.setString(3, params[2]);
                statement.setString(4, params[3]);
                statement.setString(5, params[4]);
                statement.setString(6, params[5]);
                statement.setString(7, params[6]);
                statement.execute();

                System.out.println("Announcement added ! " + params[0]);

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error Line : " + line);
            } finally {
            }
        }
    }


    public void cleanAndInsertInitialNewsData() {
        int count = 0;
        int arabicCount = 0;
        File file = new File("NEWS Dump170614-10.csv");
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

                this.insertNewsData(line);
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

    private void insertNewsData(String line) {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc.news (news_id, exchange, symbol, date, heading, body) values (?,?,?,?,?,?)";
            String[] params = line.split(",");
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setInt(1, Integer.parseInt(params[0]));
                statement.setString(2, params[1]);
                statement.setString(3, params[2]);
                statement.setString(4, params[3]);
                statement.setString(5, params[4]);
                statement.setString(6, params[5]);
                statement.execute();

                System.out.println("Announcement added ! " + params[0]);

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

            String createTableSQL = "insert into msc.history (exchange, symbol, date, open, close) values (?,?,?,?,?)";
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
}
