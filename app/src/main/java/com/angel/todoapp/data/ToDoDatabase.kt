package com.angel.todoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Configuracion principal de la base de datos local Room sobre SQLite.
 * Implementa el patron Singleton seguro para hilos (thread-safe) a fin de evitar
 * multiples instancias abiertas simultaneamente en la aplicacion.
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ToDoDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        private const val DATABASE_NAME = "todo_app_database.db"

        @Volatile
        private var INSTANCE: ToDoDatabase? = null

        /**
         * Obtiene la instancia unica de la base de datos o la construye si aun no existe.
         */
        fun getInstance(context: Context): ToDoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
