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
        int maxRetries = 10;

        try {
            ResultSet medias;

            do {
                medias = executeQuery(generateStatementFrom(mf, total));
                mf = mf.loosenConstrains(); // Preventive loosening
            } while (!medias.next() && maxRetries-- > 0); // No results and retries left, retrying with a more lax query.
            if (maxRetries == 0) {
                return null; // Unable to find matching content.
            }
            results = new ArrayList<>();
            do {
                results.add(medias.getString("path"));
            } while (medias.next());
            medias.close();
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

    private String generateStatementFrom(WeightedMediaFilter mf, int count) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT path, title FROM audio WHERE ");
        if (!mf.getGenre().isEmpty()) {
            sb.append("(");
            for (int i = 0; i != mf.getGenre().size(); i++) {
                sb.append("genre ").append(mf.getGenre().get(i).getValue() < 0 ? "NOT" : "").append(" LIKE '%")
                        .append(mf.getGenre().get(i).getKey())
                        .append("%'")
                        // If not on last tag, add an AND clause, end of clauses group otherwise.
                        .append(i + 1 != mf.getGenre().size() ? " OR " : ") ");
            }
        }
        if (!mf.getFeel().isEmpty()) {
            sb.append(!mf.getGenre().isEmpty() ? "AND (" : "(");
            for (int i = 0; i != mf.getFeel().size(); i++) {
                sb.append("feel ").append(mf.getFeel().get(i).getValue() < 0 ? "NOT" : "").append(" LIKE '%")
                        .append(mf.getFeel().get(i).getKey())
                        .append("%'")
                        // If not on last tag, add an AND clause, end of group otherwise.
                        .append(i + 1 != mf.getFeel().size() ? " AND " : ") ");
            }
        }
        switch (mf.getBPM()) {
            case ENERGETIC:
                sb.append("AND high_bpm = 1 AND low_bpm = 0 ");
                sb.append("AND high_bpm = 1 AND low_bpm = 0 ");
                break;
            case NOT_CALM:
                sb.append("AND low_bpm = 0 ");
                break;
            case NEUTRAL:
                break;
            case NOT_ENERGETIC:
                sb.append("AND high_bpm = 0 ");
            case CALM:
                sb.append("AND high_bpm = 0 AND low_bpm = 1 ");
        }
        sb.append("ORDER BY RANDOM() LIMIT ").append(count); // TODO change random selection by 'Not in existing playlist'
        System.out.println(sb.toString());
        return sb.toString();
    }

    private ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();

        return stmt.executeQuery(query);
    }
}
