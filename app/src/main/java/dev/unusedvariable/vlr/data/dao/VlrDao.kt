package dev.unusedvariable.vlr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.unusedvariable.vlr.data.api.response.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VlrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMatches(matches: List<MatchPreviewInfo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllNews(news: List<NewsResponseItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTournamentInfo(tournaments: List<TournamentInfo.TournamentPreview>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentDetails(match: TournamentDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchInfo(match: MatchInfo)


    @Query("SELECT * from NewsResponseItem")
    fun getNews(): Flow<List<NewsResponseItem>>

    @Query("SELECT * from MatchPreviewInfo where status not like 'Completed%'")
    fun getUpcomingMatches(): Flow<List<MatchPreviewInfo>>

    @Query("SELECT * from MatchPreviewInfo where status like 'Completed%'")
    fun getCompletedMatches(): Flow<List<MatchPreviewInfo>>

    @Query("SELECT * from MatchPreviewInfo")
    fun getAllMatchesPreview(): Flow<List<MatchPreviewInfo>>

    @Query("SELECT * from TournamentPreview where status not like 'completed%'")
    fun getUpcomingTournament(): Flow<List<TournamentInfo.TournamentPreview>>

    @Query("SELECT * from TournamentPreview where status like 'completed%'")
    fun getCompletedTournament(): Flow<List<TournamentInfo.TournamentPreview>>

    @Query("SELECT * from TournamentPreview")
    fun getTournaments(): Flow<List<TournamentInfo.TournamentPreview>>

    @Query("SELECT * from MatchInfo where id = :id")
    fun getMatchById(id: String): Flow<MatchInfo>

    @Query("SELECT * from TournamentDetails where id = :id")
    fun getTournamentById(id: String): Flow<TournamentDetails>


}