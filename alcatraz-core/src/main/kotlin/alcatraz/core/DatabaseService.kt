package alcatraz.core

import java.sql.Connection
import java.sql.ResultSet

interface DatabaseService {

    /**
     * Get a database connection
     */
    fun getConnection(): Connection

    /**
     * Initialize the database with default schema and data
     */
    fun initialize()

    /**
     * Reset database to initial state
     */
    fun reset()

    /**
     * Get the database file path
     */
    fun getDatabasePath(): String

    /**
     * Check if database exists
     */
    fun exists(): Boolean

    /**
     * Close and cleanup resources
     */
    fun close()

    /**
     * Execute a query and return results
     */
    fun <T> query(sql: String, params: List<Any> = emptyList(), mapper: (ResultSet) -> T): List<T>

    /**
     * Execute an update/insert/delete statement
     */
    fun execute(sql: String, params: List<Any> = emptyList()): Int

    /**
     * Execute multiple statements in a transaction
     */
    fun transaction(block: (Connection) -> Unit)
}