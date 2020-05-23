/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie.content;

import ro.uvt.regisb.magpie.utils.WeightedMediaFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite3 adapter.
 * Connect to a local, project-compliant SQLite3 database
 * to query and return its media paths.
 */
public class LocalSqliteAdapter implements MediaRetriever {
    private Connection conn = null;

    /**
     * Connect to a database file.
     *
     * @param address Local SQL file path.
     * @return Success of the connection.
     */
    @Override
    public boolean connect(String address) {
        if (conn != null) {
            return false;
        }
        try {
            conn = DriverManager.getConnection(address);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieve media paths from the connected source.
     *
     * @param total  Media to download.
     * @param filter Media filter, expected to be a WeightedMediaFilter.
     * @return List of local paths.
     * @see WeightedMediaFilter
     */
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

    /**
     * Generate a SQLite3 statement from a filter.
     * @param mf Media filter.
     * @param count Media to download.
     * @return SQL statement literal.
     */
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
        if (sb.toString().endsWith(") ")) {
            sb.append("AND ");
        }
        switch (mf.getBPM()) {
            case ENERGETIC:
                sb.append("high_bpm = 1 AND low_bpm = 0 ");
                break;
            case NOT_CALM:
                sb.append("low_bpm = 0 ");
                break;
            case NEUTRAL:
                sb.append("TRUE ");
                break;
            case NOT_ENERGETIC:
                sb.append("high_bpm = 0 ");
                break;
            case CALM:
                sb.append("high_bpm = 0 AND low_bpm = 1 ");
                break;
        }
        sb.append("ORDER BY RANDOM() LIMIT ").append(count); // TODO change random selection by 'Not in existing playlist'
        return sb.toString();
    }

    /**
     * Execute SQL query literal to the connected database.
     * @param query SQL query literal.
     * @return Result of the query.
     * @throws SQLException On statement execution failure.
     */
    private ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();

        return stmt.executeQuery(query);
    }
}
