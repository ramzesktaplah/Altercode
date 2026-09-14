package com.ai.altercode.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

/**
 * Encrypted on-device SQLite storage for snippet history.
 *
 * Uses SQLCipher for full-database encryption at rest. The database
 * passphrase is generated once, encrypted with a Keystore-backed AES key,
 * and persisted via [CryptoKeyManager]. No plaintext data ever touches disk.
 */
class SnippetDatabase private constructor(
    context: Context,
    passphrase: ByteArray
) : SQLiteOpenHelper(
    context.applicationContext,
    DB_NAME,
    passphrase,
    null,
    DB_VERSION,
    0,
    null,
    null,
    false
) {

    companion object {
        private const val DB_NAME = "altercode_history.db"
        private const val DB_VERSION = 1
        private const val TABLE = "snippets"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_ACTION = "action"
        private const val COL_SOURCE_LANG = "source_lang"
        private const val COL_TARGET_LANG = "target_lang"
        private const val COL_SOURCE_CODE = "source_code"
        private const val COL_RESULT = "result"
        private const val COL_SUMMARY = "summary"
        private const val COL_FAVORITE = "favorite"
        private const val COL_CREATED_AT = "created_at"

        fun create(context: Context): SnippetDatabase {
            val passphrase = CryptoKeyManager.getDatabasePassphrase(context.applicationContext)
            return SnippetDatabase(context.applicationContext, passphrase)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_ACTION TEXT NOT NULL,
                $COL_SOURCE_LANG TEXT NOT NULL,
                $COL_TARGET_LANG TEXT,
                $COL_SOURCE_CODE TEXT NOT NULL,
                $COL_RESULT TEXT NOT NULL,
                $COL_SUMMARY TEXT NOT NULL DEFAULT '',
                $COL_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_snippets_created ON $TABLE($COL_CREATED_AT DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    fun insert(draft: NewSnippet, createdAt: Long): Long {
        val values = ContentValues().apply {
            put(COL_TITLE, draft.title)
            put(COL_ACTION, draft.action.id)
            put(COL_SOURCE_LANG, draft.sourceLanguage.id)
            put(COL_TARGET_LANG, draft.targetLanguage?.id)
            put(COL_SOURCE_CODE, draft.sourceCode)
            put(COL_RESULT, draft.result)
            put(COL_SUMMARY, draft.summary)
            put(COL_FAVORITE, 0)
            put(COL_CREATED_AT, createdAt)
        }
        return writableDatabase.insert(TABLE, null, values)
    }

    fun queryAll(): List<Snippet> {
        val out = mutableListOf<Snippet>()
        readableDatabase.query(
            TABLE, null, null, null, null, null,
            "$COL_FAVORITE DESC, $COL_CREATED_AT DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) out += cursor.toSnippet()
        }
        return out
    }

    fun setFavorite(id: Long, favorite: Boolean) {
        val values = ContentValues().apply { put(COL_FAVORITE, if (favorite) 1 else 0) }
        writableDatabase.update(TABLE, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        writableDatabase.delete(TABLE, null, null)
    }

    private fun Cursor.toSnippet(): Snippet {
        val targetId = getString(getColumnIndexOrThrow(COL_TARGET_LANG))
        return Snippet(
            id = getLong(getColumnIndexOrThrow(COL_ID)),
            title = getString(getColumnIndexOrThrow(COL_TITLE)),
            action = CodeAction.fromId(getString(getColumnIndexOrThrow(COL_ACTION))),
            sourceLanguage = CodeLanguage.fromId(getString(getColumnIndexOrThrow(COL_SOURCE_LANG)))
                ?: CodeLanguage.AUTO,
            targetLanguage = CodeLanguage.fromId(targetId),
            sourceCode = getString(getColumnIndexOrThrow(COL_SOURCE_CODE)),
            result = getString(getColumnIndexOrThrow(COL_RESULT)),
            summary = getString(getColumnIndexOrThrow(COL_SUMMARY)) ?: "",
            isFavorite = getInt(getColumnIndexOrThrow(COL_FAVORITE)) == 1,
            createdAt = getLong(getColumnIndexOrThrow(COL_CREATED_AT))
        )
    }
}
