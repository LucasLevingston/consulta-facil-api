package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class V45__rename_legacy_columns extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();

        if (columnExists(conn, "features", "key")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE features RENAME COLUMN \"key\" TO feature_key");
            }
        }

        if (columnExists(conn, "plan_features", "value")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE plan_features RENAME COLUMN \"value\" TO feature_value");
            }
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        String sql = "SELECT 1 FROM information_schema.columns " +
                "WHERE LOWER(table_name) = '" + table + "' AND LOWER(column_name) = '" + column + "'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }
}
