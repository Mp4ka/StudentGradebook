package ru.college.gradebook.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {

    @Insert
    suspend fun insert(grade: GradeEntity): Long

    @Update
    suspend fun update(grade: GradeEntity)

    @Delete
    suspend fun delete(grade: GradeEntity)

    @Query("DELETE FROM grade WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM grade ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grade WHERE disciplineId = :disciplineId ORDER BY date DESC, id DESC")
    fun observeByDiscipline(disciplineId: Long): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grade ORDER BY date DESC, id DESC")
    suspend fun getAll(): List<GradeEntity>

    @Query("SELECT * FROM grade WHERE disciplineId = :disciplineId ORDER BY date DESC, id DESC")
    suspend fun getByDiscipline(disciplineId: Long): List<GradeEntity>

    @Query("SELECT * FROM grade WHERE id = :id")
    suspend fun findById(id: Long): GradeEntity?

    @Query("SELECT COUNT(*) FROM grade")
    suspend fun count(): Int
}
