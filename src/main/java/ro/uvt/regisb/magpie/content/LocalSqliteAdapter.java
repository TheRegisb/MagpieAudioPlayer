package ro.uvt.regisb.magpie.content;

import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.sql.*;
import java.util.ArrayList;
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
        WeightedMediaFilter mf = (WeightedMediaFilter) filter;
        List<String> results = null;
        
        // TODO generate proper statement from filter (assuming WeightedMediaFilter)
        try (Statement stmt = conn.createStatement()) {
            // TODO replace ORDER BY RANDOM() by "Not already in playlist"
            ResultSet rs = stmt.executeQuery(String.format("SELECT title, path FROM audio WHERE feel LIKE '%%%s%%' ORDER BY RANDOM() LIMIT %d", mf.getFeel().get(0).getKey(), total));

            if (!rs.next()) {
                // TODO loosen statement and retry
            } else {
                results = new ArrayList<>();
                do {
                    results.add(rs.getString("path"));
                } while (rs.next());
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
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
