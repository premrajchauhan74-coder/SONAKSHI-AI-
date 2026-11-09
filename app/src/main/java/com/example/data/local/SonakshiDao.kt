package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SonakshiDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET wasActionExecuted = 1 WHERE id = :messageId")
    suspend fun markActionExecuted(messageId: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT * FROM automation_macros ORDER BY id ASC")
    fun getAllMacros(): Flow<List<AutomationMacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: AutomationMacroEntity): Long

    @Update
    suspend fun updateMacro(macro: AutomationMacroEntity)

    @Query("DELETE FROM automation_macros WHERE id = :macroId")
    suspend fun deleteMacroById(macroId: Int)

    @Query("SELECT COUNT(*) FROM automation_macros")
    suspend fun getMacroCount(): Int
}
