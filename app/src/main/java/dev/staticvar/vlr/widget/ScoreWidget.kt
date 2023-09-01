package dev.staticvar.vlr.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.staticvar.vlr.MainActivity
import dev.staticvar.vlr.data.VlrRepository
import dev.staticvar.vlr.data.api.response.MatchPreviewInfo
import dev.staticvar.vlr.ui.Destination
import dev.staticvar.vlr.ui.theme.VLRTheme
import dev.staticvar.vlr.ui.theme.WidgetTheme
import dev.staticvar.vlr.utils.Constants
import dev.staticvar.vlr.utils.readableDateAndTimeWithZone
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScoreWidget(private val repository: VlrRepository) : GlanceAppWidget() {

  @Composable
  fun Content() {
    var list by remember { mutableStateOf(listOf<MatchPreviewInfo>()) }
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(Unit) {
      coroutine.launch(Dispatchers.IO) { list = repository.upcomingMatches().subList(0, 10) }
    }
    val context = LocalContext.current

    WidgetTheme(context = context, darkTheme = context.isDarkThemeOn()) {
      if (list.isEmpty())
        Column(
          modifier =
            GlanceModifier.fillMaxSize()
              .padding(8.dp)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .cornerRadius(16.dp),
          verticalAlignment = Alignment.Vertical.CenterVertically,
          horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
          Text(
            text = "Unable to find matches, open the app to fetch data.",
            style =
              TextStyle(
                color = ColorProvider(VLRTheme.colorScheme.onPrimaryContainer),
                textAlign = TextAlign.Center
              ),
          )
        }
      else {

        LazyColumn(
          GlanceModifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .cornerRadius(16.dp)
        ) {
          item {
            Text(
              text = "Upcoming / ongoing matches",
              style =
                TextStyle(
                  textAlign = TextAlign.Start,
                  color = ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                  fontSize = 16.sp
                ),
              modifier = GlanceModifier.fillMaxWidth().padding(8.dp)
            )
          }
          item {
            Text(
              text =
                "Last updated at ${
                LocalTime
                  .now()
                  .atOffset(ZoneOffset.UTC)
                  .format(DateTimeFormatter.ofPattern("HH:mm a"))
              }",
              style =
                TextStyle(
                  textAlign = TextAlign.Start,
                  color = ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                  fontSize = 10.sp
                ),
              modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
          }
          items(list) {
            Column(
              modifier =
                GlanceModifier.fillMaxWidth()
                  .cornerRadius(16.dp)
                  .padding(8.dp)
                  .clickable(
                    actionStartActivity(
                      Intent(
                        Intent.ACTION_VIEW,
                        "${Constants.DEEP_LINK_BASEURL}${Destination.Match.Args.ID}=${it.id}".toUri(),
                        context,
                        MainActivity::class.java
                      )
                    )
                  ),
            ) {
              Column(
                modifier =
                  GlanceModifier.fillMaxWidth()
                    .cornerRadius(16.dp)
                    .padding(4.dp)
                    .background(VLRTheme.colorScheme.inversePrimary),
              ) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                  Text(
                    text =
                      if (it.status.equals("LIVE", true)) "LIVE"
                      else it.time?.readableDateAndTimeWithZone ?: "",
                    modifier = GlanceModifier.fillMaxWidth(),
                    style =
                      TextStyle(
                        textAlign = TextAlign.Center,
                        color = ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                        fontSize = 12.sp
                      )
                  )
                }
                Row(
                  GlanceModifier.fillMaxWidth(),
                  verticalAlignment = Alignment.Vertical.CenterVertically,
                  horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                  Text(
                    text = it.team1.name,
                    style =
                      TextStyle(
                        ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                      ),
                    modifier = GlanceModifier.defaultWeight().padding(2.dp),
                    maxLines = 1
                  )
                  Text(
                    text = it.team2.name,
                    style =
                      TextStyle(
                        ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                      ),
                    modifier = GlanceModifier.defaultWeight().padding(2.dp)
                  )
                }
                Row(
                  GlanceModifier.fillMaxWidth(),
                  verticalAlignment = Alignment.Vertical.CenterVertically,
                  horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                  Text(
                    text = it.team1.score?.toString() ?: "-",
                    style =
                      TextStyle(
                        ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                        textAlign = TextAlign.Center
                      ),
                    modifier = GlanceModifier.defaultWeight().padding(2.dp)
                  )
                  Text(
                    text = it.team2.score?.toString() ?: "-",
                    style =
                      TextStyle(
                        ColorProvider(MaterialTheme.colorScheme.onPrimaryContainer),
                        textAlign = TextAlign.Center
                      ),
                    modifier = GlanceModifier.defaultWeight().padding(2.dp),
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent { Content() }
  }
}

fun Context.isDarkThemeOn(): Boolean {
  return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}
