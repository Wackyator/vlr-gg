package dev.staticvar.vlr.ui.events

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateStandardPaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import dev.staticvar.vlr.R
import dev.staticvar.vlr.data.api.response.TournamentPreview
import dev.staticvar.vlr.ui.Local16DPPadding
import dev.staticvar.vlr.ui.Local4DPPadding
import dev.staticvar.vlr.ui.Local8DPPadding
import dev.staticvar.vlr.ui.VlrViewModel
import dev.staticvar.vlr.ui.common.ErrorUi
import dev.staticvar.vlr.ui.common.ScrollHelper
import dev.staticvar.vlr.ui.common.VlrHorizontalViewPager
import dev.staticvar.vlr.ui.common.VlrSegmentedButtons
import dev.staticvar.vlr.ui.common.VlrTabRowForViewPager
import dev.staticvar.vlr.ui.helper.CardView
import dev.staticvar.vlr.ui.helper.cardAlpha
import dev.staticvar.vlr.ui.scrim.StatusBarSpacer
import dev.staticvar.vlr.ui.scrim.StatusBarType
import dev.staticvar.vlr.ui.theme.VLRTheme
import dev.staticvar.vlr.utils.StableHolder
import dev.staticvar.vlr.utils.Waiting
import dev.staticvar.vlr.utils.onFail
import dev.staticvar.vlr.utils.onPass
import dev.staticvar.vlr.utils.onWaiting
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun EventOverviewAdaptive(modifier: Modifier = Modifier, viewModel: VlrViewModel, hideNav: (Boolean) -> Unit) {
  var selectedItem: String? by rememberSaveable {
    mutableStateOf(null)
  }
  val paneScaffoldDirective = calculateStandardPaneScaffoldDirective(currentWindowAdaptiveInfo())
  val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
    scaffoldDirective = PaneScaffoldDirective(
      contentPadding = PaddingValues(0.dp),
      maxHorizontalPartitions = paneScaffoldDirective.maxHorizontalPartitions,
      horizontalPartitionSpacerSize = paneScaffoldDirective.horizontalPartitionSpacerSize,
      maxVerticalPartitions = paneScaffoldDirective.maxVerticalPartitions,
      verticalPartitionSpacerSize = paneScaffoldDirective.verticalPartitionSpacerSize,
      excludedBounds = paneScaffoldDirective.excludedBounds
    )
  )

  LaunchedEffect(navigator.currentDestination) {
    if (navigator.currentDestination?.pane == ThreePaneScaffoldRole.Secondary) {
      hideNav(false)
    } else hideNav(true)
  }

  val tabs =
    listOf(
      stringResource(id = R.string.ongoing),
      stringResource(id = R.string.upcoming),
      stringResource(id = R.string.completed)
    )
  val pagerState = rememberPagerState(pageCount = { tabs.size })


  BackHandler(navigator.canNavigateBack()) {
    selectedItem = null
    navigator.navigateBack()
  }

  ListDetailPaneScaffold(
    listPane = {
      AnimatedPane(modifier = modifier) {
        EventScreen(
          viewModel = viewModel,
          pagerState = pagerState,
          selectedItem = selectedItem ?: " ",
          action = {
            selectedItem = it
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
          })
      }
    },
    detailPane = {
      selectedItem?.let {
        AnimatedPane(modifier = modifier) {
          EventDetails(viewModel = viewModel, id = it)
        }
      }
    },
    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    modifier = modifier
  )
}

@Composable
@NonSkippableComposable
fun EventScreen(
  viewModel: VlrViewModel,
  pagerState: PagerState,
  selectedItem: String,
  action: (String) -> Unit,
) {

  val allTournaments by
  remember(viewModel) { viewModel.getEvents() }
    .collectAsStateWithLifecycle(initialValue = Waiting())
  var triggerRefresh by remember(viewModel) { mutableStateOf(true) }
  val updateState by
  remember(triggerRefresh) { viewModel.refreshEvents() }
    .collectAsStateWithLifecycle(initialValue = Ok(false))

  val swipeRefresh =
    rememberPullRefreshState(updateState.get() ?: false, { triggerRefresh = triggerRefresh.not() })

  val resetScroll by
  remember { viewModel.resetScroll }.collectAsStateWithLifecycle(initialValue = false)

  val modifier: Modifier = Modifier

  Column(
    modifier = modifier
      .fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    StatusBarSpacer(statusBarType = StatusBarType.TRANSPARENT)
    allTournaments
      .onPass {
        data?.let { list ->
          TournamentPreviewContainer(
            modifier = Modifier,
            list = StableHolder(list),
            pagerState = pagerState,
            swipeRefresh = swipeRefresh,
            updateState = updateState,
            resetScroll = resetScroll,
            selectedItem = selectedItem,
            action = action,
            postResetScroll = { viewModel.postResetScroll() }
          )
        }
      }
      .onWaiting { LinearProgressIndicator(modifier.animateContentSize()) }
      .onFail { Text(text = message()) }
  }
}

@Composable
fun TournamentPreviewContainer(
  modifier: Modifier = Modifier,
  list: StableHolder<List<TournamentPreview>>,
  swipeRefresh: PullRefreshState,
  pagerState: PagerState,
  updateState: Result<Boolean, Throwable?>,
  resetScroll: Boolean,
  selectedItem: String,
  action: (String) -> Unit,
  postResetScroll: () -> Unit,
) {

  val tabs =
    listOf(
      stringResource(id = R.string.ongoing),
      stringResource(id = R.string.upcoming),
      stringResource(id = R.string.completed)
    )
  val mapByStatus by remember(list) { mutableStateOf(list.item.groupBy { it.status }) }


  val (ongoing, upcoming, completed) =
    remember(list) {
      mapByStatus.let {
        Triple(
          it[tabs[0].lowercase()].orEmpty(),
          it[tabs[1].lowercase()].orEmpty(),
          it[tabs[2].lowercase()].orEmpty()
        )
      }
    }

  Box {
    Column(
      modifier = modifier
        .fillMaxSize()
        .animateContentSize()
        .pullRefresh(swipeRefresh),
      verticalArrangement = Arrangement.Top
    ) {
      AnimatedVisibility(
        visible = updateState.get() == true || swipeRefresh.progress != 0f,
      ) {
        LinearProgressIndicator(
          modifier
            .fillMaxWidth()
            .padding(Local16DPPadding.current)
            .animateContentSize()
            .testTag("common:loader")
        )
      }
      updateState.getError()?.let {
        ErrorUi(modifier = modifier, exceptionMessage = it.stackTraceToString())
      }
      //    VlrTabRowForViewPager(modifier = modifier, pagerState = pagerState, tabs = tabs)

      VlrHorizontalViewPager(
        modifier = modifier,
        pagerState = pagerState,
        {
          if (ongoing.isEmpty()) {
            NoEventUI(modifier = modifier)
          } else {
            val lazyListState = rememberLazyListState()
            lazyListState.ScrollHelper(resetScroll = resetScroll, postResetScroll)
            LazyColumn(
              modifier
                .fillMaxSize()
                .testTag("eventOverview:live"),
              verticalArrangement = Arrangement.Top,
              state = lazyListState
            ) {
              items(ongoing, key = { item -> item.id }) {
                TournamentPreview(
                  modifier = modifier,
                  tournamentPreview = it,
                  selectedItem = selectedItem,
                  action
                )
              }
            }
          }
        },
        {
          if (upcoming.isEmpty()) {
            NoEventUI(modifier = modifier)
          } else {
            val lazyListState = rememberLazyListState()
            lazyListState.ScrollHelper(resetScroll = resetScroll, postResetScroll)
            LazyColumn(
              modifier
                .fillMaxSize()
                .testTag("eventOverview:upcoming"),
              verticalArrangement = Arrangement.Top,
              state = lazyListState
            ) {
              items(upcoming, key = { item -> item.id }) {
                TournamentPreview(
                  modifier = modifier,
                  tournamentPreview = it,
                  selectedItem = selectedItem,
                  action
                )
              }
            }
          }
        },
        {
          if (completed.isEmpty()) {
            NoEventUI(modifier = modifier)
          } else {
            val lazyListState = rememberLazyListState()
            lazyListState.ScrollHelper(resetScroll = resetScroll, postResetScroll)
            LazyColumn(
              modifier
                .fillMaxSize()
                .testTag("eventOverview:result"),
              verticalArrangement = Arrangement.Top,
              state = lazyListState
            ) {
              items(completed, key = { item -> item.id }) {
                TournamentPreview(
                  modifier = modifier,
                  tournamentPreview = it,
                  selectedItem = selectedItem,
                  action
                )
              }
            }
          }
        }
      )
    }

    val scope = rememberCoroutineScope()
    VlrSegmentedButtons(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(8.dp),
      highlighted = pagerState.currentPage,
      items = tabs,
    ) { _, index ->
      scope.launch {
        pagerState.animateScrollToPage(index)
      }
    }
  }
}

@Composable
fun NoEventUI(modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    Spacer(modifier = modifier.weight(1f))
    Text(
      text = stringResource(R.string.no_ongoing_event),
      modifier = modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      style = VLRTheme.typography.bodyLarge,
      color = VLRTheme.colorScheme.primary
    )
    Spacer(modifier = modifier.weight(1f))
  }
}

@Composable
fun TournamentPreview(
  modifier: Modifier = Modifier,
  tournamentPreview: TournamentPreview,
  selectedItem: String,
  action: (String) -> Unit,
) {
  CardView(
    modifier = modifier.clickable { action(tournamentPreview.id) },
    colors = if (tournamentPreview.id == selectedItem) {
      CardDefaults.cardColors(
        containerColor = VLRTheme.colorScheme.secondaryContainer.copy(cardAlpha),
        contentColor = VLRTheme.colorScheme.onSecondaryContainer
      )
    } else {
      CardDefaults.cardColors(
        containerColor = VLRTheme.colorScheme.primaryContainer.copy(cardAlpha),
        contentColor = VLRTheme.colorScheme.onPrimaryContainer
      )
    }
  ) {
    Column(modifier = modifier.padding(Local8DPPadding.current)) {
      Text(
        text = tournamentPreview.title,
        style = VLRTheme.typography.titleMedium,
        modifier = modifier.padding(Local4DPPadding.current),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = VLRTheme.colorScheme.primary,
      )

      Row(
        modifier = modifier.padding(Local4DPPadding.current),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val annotatedLocationString = buildAnnotatedString {
          appendInlineContent(id = "location")
          append(tournamentPreview.location.uppercase())
        }
        val inlineLocationContentMap =
          mapOf(
            "location" to
                InlineTextContent(Placeholder(16.sp, 16.sp, PlaceholderVerticalAlign.TextCenter)) {
                  Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    modifier = modifier.size(16.dp),
                    contentDescription = ""
                  )
                }
          )
        val annotatedDateString = buildAnnotatedString {
          appendInlineContent(id = "date")
          append(" ")
          append(tournamentPreview.dates)
        }
        val inlineDateContentMap =
          mapOf(
            "date" to
                InlineTextContent(Placeholder(16.sp, 16.sp, PlaceholderVerticalAlign.TextCenter)) {
                  Icon(
                    imageVector = Icons.Outlined.DateRange,
                    modifier = modifier.size(16.dp),
                    contentDescription = ""
                  )
                }
          )
        Text(
          annotatedLocationString,
          style = VLRTheme.typography.bodyMedium,
          inlineContent = inlineLocationContentMap,
          modifier = modifier
            .padding(Local4DPPadding.current)
            .weight(1f),
          textAlign = TextAlign.Start,
        )
        Text(
          text = tournamentPreview.prize,
          modifier = modifier.padding(Local4DPPadding.current),
          textAlign = TextAlign.Start,
          style = VLRTheme.typography.bodyMedium
        )
        Text(
          annotatedDateString,
          style = VLRTheme.typography.bodyMedium,
          inlineContent = inlineDateContentMap,
          modifier = modifier
            .padding(Local4DPPadding.current)
            .weight(1f),
          textAlign = TextAlign.End,
        )
      }
    }
  }
}
