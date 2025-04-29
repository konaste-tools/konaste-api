package dev.bauxe.konaste.repository.persistence.history.migrations

import java.sql.Connection

/**
 * All score history migrations must:
 * - Update previous versioned entries to the migration version
 * - Repopulate new fields (where possible) using raw_data
 */
interface ScoreHistoryMigration {
  fun applyMigration(connection: Connection)
}
