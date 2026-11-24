package ru.college.gradebook.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {

    @Insert
    suspend fun insert(discipline: DisciplineEntity): Long

    @Update
    suspend fun update(discipline: DisciplineEntity)

    @Delete
    suspend fun delete(discipline: DisciplineEntity)

    @Query("DELETE FROM discipline WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM discipline ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM discipline ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<DisciplineEntity>

    @Query("SELECT * FROM discipline WHERE id = :id")
    suspend fun findById(id: Long): DisciplineEntity?

    @Query("SELECT COUNT(*) FROM discipline")
    suspend fun count(): Int
}
