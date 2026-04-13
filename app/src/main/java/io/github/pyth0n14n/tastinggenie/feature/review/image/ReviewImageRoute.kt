package io.github.pyth0n14n.tastinggenie.feature.review.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

@Composable
fun ReviewImageRoute(
    onBack: () -> Unit,
    viewModel: ReviewImageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewImageScreen(
        state = state,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewImageScreen(
    state: ReviewImageUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_image_viewer)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> MessageContent(text = stringResource(state.error.messageResId))
            state.imageUris.isEmpty() -> MessageContent(text = stringResource(R.string.message_no_image))
            else ->
                ReviewImagePager(
                    imageUris = state.imageUris,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                )
        }
    }
}

@Composable
private fun ReviewImagePager(
    imageUris: List<String>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { imageUris.size }
    Box(modifier = modifier) {
        HorizontalPager(state = pagerState) { page ->
            AsyncImage(
                model = imageUris[page],
                contentDescription = stringResource(R.string.content_review_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
