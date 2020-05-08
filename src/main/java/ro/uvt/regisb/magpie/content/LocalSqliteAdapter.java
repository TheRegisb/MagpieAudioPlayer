package ro.uvt.regisb.magpie.content;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class LocalSqliteAdapter implements MediaRetriever {
    private Connection conn = null;

    @Override
    public boolean connect(String address) {
        if (conn != null) {
            return false;
        }
        try {
            conn = DriverManager.getConnection(address);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<String> download(int total, Object filter) {
        /*
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM audio WHERE high_bpm = 0 AND genre LIKE '%" + filter + "%' LIMIT " + total);

            while (rs.next()) {
                System.out.println(String.format("Title: %s, Path: %s", rs.getString("title"), rs.getString("path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        */
        // TODO generate statement from filter
        // TODO return list of paths or song objects
        return null;
    }

    @Override
    public void disconnect() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
