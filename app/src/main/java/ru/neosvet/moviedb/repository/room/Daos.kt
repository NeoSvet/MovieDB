package ru.neosvet.moviedb.repository.room

import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM MovieEntity WHERE id=:id")
    fun get(id: Int): MovieEntity?

    @Query("SELECT * FROM MovieEntity WHERE id IN (:ids)")
    fun getList(ids: List<Int>): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(entity: MovieEntity)

    @Update
    fun update(entity: MovieEntity)

    @Delete
    fun delete(entity: MovieEntity)
}

@Dao
interface GenreDao {
    @Query("SELECT * FROM GenreEntity WHERE id=:id")
    fun get(id: Int): GenreEntity?

    @Query("SELECT * FROM GenreEntity WHERE id IN (:ids)")
    fun getList(ids: List<Int>): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(entity: GenreEntity)

    @Update
    fun update(entity: GenreEntity)

    @Delete
    fun delete(entity: GenreEntity)
}

@Dao
interface CatalogDao {
    @Query("SELECT * FROM CatalogEntity WHERE name=:name")
    fun get(name: String): CatalogEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(entity: CatalogEntity)

    @Update
    fun update(entity: CatalogEntity)

    @Query("DELETE FROM CatalogEntity WHERE name=:name")
    fun delete(name: String)
}