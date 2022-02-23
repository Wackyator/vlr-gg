package dev.unusedvariable.vlr.data.api.response


import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@Entity
data class TournamentPreview(
    @SerialName("dates")
    val dates: String = "", // Feb 13—14
    @SerialName("id")
    @PrimaryKey
    val id: String = "", // 890
    @SerialName("img")
    val img: String = "", // https://owcdn.net/img/62099d2b41b45.png
    @SerialName("location")
    val location: String = "", // ca
    @SerialName("prize")
    val prize: String = "", // $1,572
    @SerialName("status")
    val status: String = "", // completed
    @SerialName("title")
    val title: String = "" // Toronto VALORANT: Viper's Pit $2K Online Qualifier
)