@file:Suppress("LongMethod", "LongParameterList")

package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import kotlinx.coroutines.launch

private val ScreenPadding = 24.dp
private val ImageCornerRadius = 24.dp
private val IndicatorDotSize = 10.dp
private const val IMAGE_ASPECT_RATIO = 1.35f

data class GuidePage(
    @param:StringRes val titleResId: Int,
    @param:StringRes val messageResId: Int,
    @param:DrawableRes val imageResId: Int,
)

@Composable
fun GuidePagerScreen(
    pages: List<GuidePage>,
    @StringRes nonFinalLeadingActionLabelRes: Int,
    @StringRes finalTrailingActionLabelRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes finalLeadingActionLabelRes: Int = R.string.action_back,
    @StringRes imageContentDescriptionRes: Int = R.string.onboarding_image_placeholder,
    @StringRes pageIndicatorDescriptionRes: Int = R.string.onboarding_page_indicator,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = ScreenPadding),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Spacer(modifier = Modifier.height(48.dp))
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                GuidePageContent(
                    page = pages[pageIndex],
                    imageContentDescriptionRes = imageContentDescriptionRes,
                )
            }
            GuidePageIndicator(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                pageIndicatorDescriptionRes = pageIndicatorDescriptionRes,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 20.dp),
            )
            GuideActions(
                isLastPage = isLastPage,
                nonFinalLeadingActionLabelRes = nonFinalLeadingActionLabelRes,
                finalLeadingActionLabelRes = finalLeadingActionLabelRes,
                finalTrailingActionLabelRes = finalTrailingActionLabelRes,
                onDismiss = onDismiss,
                onBack = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
            )
        }
    }
}

@Composable
private fun GuideActions(
    isLastPage: Boolean,
    @StringRes nonFinalLeadingActionLabelRes: Int,
    @StringRes finalLeadingActionLabelRes: Int,
    @StringRes finalTrailingActionLabelRes: Int,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = if (isLastPage) onBack else onDismiss) {
            Text(
                text =
                    stringResource(
                        if (isLastPage) {
                            finalLeadingActionLabelRes
                        } else {
                            nonFinalLeadingActionLabelRes
                        },
                    ),
            )
        }
        Button(onClick = if (isLastPage) onDismiss else onNext) {
            Text(
                text =
                    stringResource(
                        if (isLastPage) {
                            finalTrailingActionLabelRes
                        } else {
                            R.string.action_next
                        },
                    ),
            )
        }
    }
}

@Composable
private fun GuidePageContent(
    page: GuidePage,
    @StringRes imageContentDescriptionRes: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(page.titleResId),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Image(
            painter = painterResource(page.imageResId),
            contentDescription = stringResource(imageContentDescriptionRes),
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(IMAGE_ASPECT_RATIO)
                    .clip(RoundedCornerShape(ImageCornerRadius)),
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(page.messageResId),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuidePageIndicator(
    currentPage: Int,
    pageCount: Int,
    @StringRes pageIndicatorDescriptionRes: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(pageIndicatorDescriptionRes, currentPage + 1, pageCount)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .size(IndicatorDotSize)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
            )
        }
    }
}
