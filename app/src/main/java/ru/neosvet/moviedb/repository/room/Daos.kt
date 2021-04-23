package ru.neosvet.moviedb.repository.room

import androidx.room.*

@Dao
interface MovieDao {
    @Query("SELECT * FROM MovieEntity WHERE id=:id")
    fun get(id: Int): MovieEntity?

    @Query("SELECT * FROM MovieEntity WHERE id IN (:ids) AND adult=0")
    fun getList(ids: List<Int>): List<MovieEntity>

    @Query("SELECT * FROM MovieEntity WHERE id IN (:ids)")
    fun getListWithAdult(ids: List<Int>): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: MovieEntity)

    @Update
    fun update(entity: MovieEntity)

    @Delete
    fun delete(entity: MovieEntity)
}

@Dao
interface DetailsDao {
    @Query("SELECT * FROM DetailsEntity WHERE id=:id")
    fun get(id: Int): DetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: DetailsEntity)

    @Delete
    fun delete(entity: DetailsEntity)
}

@Dao
interface GenreDao {
    @Query("SELECT * FROM GenreEntity WHERE id=:id")
    fun get(id: Int): GenreEntity?

    @Query("SELECT * FROM GenreEntity WHERE id IN (:ids)")
    fun getList(ids: List<Int>): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: GenreEntity)

    @Delete
    fun delete(entity: GenreEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM NoteEntity WHERE id=:id")
    fun get(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: NoteEntity)

    @Delete
    fun delete(entity: NoteEntity)
}

@Dao
interface CatalogDao {
    @Query("SELECT * FROM CatalogEntity WHERE name=:name")
    fun get(name: String): CatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: CatalogEntity)

    @Query("DELETE FROM CatalogEntity WHERE name=:name")
    fun delete(name: String)
}

@Dao
interface PeopleDao {
    @Query("SELECT * FROM PersonEntity WHERE id=:id")
    fun get(id: Int): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(entity: PersonEntity)

    @Delete
    fun delete(entity: PersonEntity)
}