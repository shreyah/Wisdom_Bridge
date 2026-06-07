package com.example

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "experts")
data class DbExpert(
    @PrimaryKey val id: String,
    val name: String,
    val topic: String,
    val bio: String,
    val experience_years: Int,
    val languages: String, // Semi-colon separated list e.g. "English; Hindi"
    val rate_per_30min: Int,
    val availability: String,
    val rating: Double,
    val session_count: Int,
    val avatar_seed: String
)

@Entity(tableName = "bookings")
data class DbBooking(
    @PrimaryKey val id: String,
    val expert_id: String,
    val learner_id: String,
    val slot_time: String, // format "MON • 10:00 AM - 10:30 AM" or similar
    val status: String,    // 'confirmed' or 'cancelled' etc.
    val duration_minutes: Int = 30,
    val is_voice: Boolean = false,
    val is_video: Boolean = false,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "slots")
data class DbSlot(
    @PrimaryKey val id: String, // expert_id + day + time
    val expert_id: String,
    val day: String,   // "MON", "TUE", etc.
    val time: String,  // "10:00 AM", etc.
    val status: String, // "free" or "booked"
    val booked_by: String? = null // learner name or ID
)

@Entity(tableName = "requests")
data class DbRequest(
    @PrimaryKey val id: String,
    val learner_id: String,
    val topic: String,
    val message: String,
    val status: String, // "open", "completed" etc.
    val created_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "responses")
data class DbResponse(
    @PrimaryKey val id: String,
    val request_id: String,
    val expert_id: String,
    val message: String,
    val created_at: Long = System.currentTimeMillis()
)

// --- DAOS ---

@Dao
interface ExpertDao {
    @Query("SELECT * FROM experts")
    fun getAllExpertsFlow(): Flow<List<DbExpert>>

    @Query("SELECT * FROM experts")
    suspend fun getAllExperts(): List<DbExpert>

    @Query("SELECT * FROM experts WHERE id = :expertId")
    suspend fun getExpertById(expertId: String): DbExpert?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperts(experts: List<DbExpert>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpert(expert: DbExpert)

    @Query("DELETE FROM experts")
    suspend fun clearAllExperts()
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY created_at DESC")
    fun getAllBookingsFlow(): Flow<List<DbBooking>>

    @Query("SELECT * FROM bookings WHERE learner_id = :learnerId ORDER BY created_at DESC")
    fun getBookingsForLearnerFlow(learnerId: String): Flow<List<DbBooking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: DbBooking)

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteBooking(bookingId: String)
}

@Dao
interface SlotDao {
    @Query("SELECT * FROM slots WHERE expert_id = :expertId")
    fun getSlotsForExpertFlow(expertId: String): Flow<List<DbSlot>>

    @Query("SELECT * FROM slots WHERE expert_id = :expertId")
    suspend fun getSlotsForExpert(expertId: String): List<DbSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<DbSlot>)

    @Query("UPDATE slots SET status = :status, booked_by = :bookedBy WHERE id = :slotId")
    suspend fun updateSlotStatus(slotId: String, status: String, bookedBy: String?)
}

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests ORDER BY created_at DESC")
    fun getAllRequestsFlow(): Flow<List<DbRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: DbRequest)
}

@Dao
interface ResponseDao {
    @Query("SELECT * FROM responses ORDER BY created_at DESC")
    fun getAllResponsesFlow(): Flow<List<DbResponse>>

    @Query("SELECT * FROM responses WHERE request_id = :requestId ORDER BY created_at DESC")
    fun getResponsesForRequestFlow(requestId: String): Flow<List<DbResponse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: DbResponse)
}

// --- DATABASE HOLDER ---

@Database(
    entities = [
        DbExpert::class,
        DbBooking::class,
        DbSlot::class,
        DbRequest::class,
        DbResponse::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AgeNoBarDatabase : RoomDatabase() {

    abstract fun expertDao(): ExpertDao
    abstract fun bookingDao(): BookingDao
    abstract fun slotDao(): SlotDao
    abstract fun requestDao(): RequestDao
    abstract fun responseDao(): ResponseDao

    companion object {
        @Volatile
        private var INSTANCE: AgeNoBarDatabase? = null

        fun getDatabase(context: Context): AgeNoBarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgeNoBarDatabase::class.java,
                    "age_no_bar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
