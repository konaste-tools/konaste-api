package dev.bauxe.konaste.repository.persistence.history.migrations

import java.sql.Connection

class Migration1 : ScoreHistoryMigration {
  companion object {
    private const val MIGRATION_ENTITY_VERSION = 1
    private const val MIGRATION =
        """
CREATE TABLE score_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_at TEXT DEFAULT (datetime('now')),
    song_id INTEGER,
    score INTEGER,
    best_score INTEGER,
    combo INTEGER,
    criticals INTEGER,
    nears INTEGER,
    misses INTEGER,
    health INTEGER,
    level INTEGER,
    difficulty INTEGER,
    grade INTEGER,
    best_grade INTEGER,
    clear_mark INTEGER,
    best_clear_mark INTEGER,
    ex INTEGER,
    best_ex INTEGER,
    error_early INTEGER,
    near_early INTEGER,
    critical_early INTEGER,
    s_critical INTEGER,
    critical_late INTEGER,
    near_late INTEGER,
    error_late INTEGER,
    s_critical_chip INTEGER,
    critical_chip INTEGER,
    near_chip INTEGER,
    error_chip INTEGER,
    s_critical_long INTEGER,
    miss_long INTEGER,
    s_critical_tsumami INTEGER,
    miss_tsumami INTEGER,
    raw_data BLOB,
    entity_version INTEGER
);"""
  }

  override fun applyMigration(connection: Connection) {
    connection.createStatement().use { statement ->
      statement.executeUpdate(MIGRATION)
      statement.executeUpdate("PRAGMA user_version = 1;")
    }
    connection.commit()
  }
}
