package com.rahul.symptoscan.presentation.onboarding.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahul.symptoscan.presentation.onboarding.component.*
import com.rahul.symptoscan.presentation.onboarding.event.OnboardingEvent
import com.rahul.symptoscan.presentation.onboarding.data.OnboardingData
import com.rahul.symptoscan.presentation.onboarding.viewmodel.OnboardingViewModel
import com.rahul.symptoscan.ui.components.PrimaryButton
import com.rahul.symptoscan.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { OnboardingData.pages.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(OnboardingEvent.PageChanged(pagerState.currentPage))
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { navigation ->
            when (navigation) {
                is OnboardingViewModel.OnboardingNavigation.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.PaddingMedium),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!state.isLastPage) {
                    SkipButton(onClick = { viewModel.onEvent(OnboardingEvent.SkipOnboarding) })
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = OnboardingData.pages[pageIndex]
                OnboardingPageContent(page = page)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.PaddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PaginationIndicator(
                    pageSize = OnboardingData.pages.size,
                    currentPage = pagerState.currentPage
                )

                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))

                PrimaryButton(
                    text = if (state.isLastPage) "Get Started" else "Next",
                    onClick = {
                        if (state.isLastPage) {
                            viewModel.onEvent(OnboardingEvent.NextPage)
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: com.rahul.symptoscan.presentation.onboarding.model.OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.PaddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IllustrationSection()
        
        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
        
        TitleSection(title = page.title)
        
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        
        DescriptionSection(description = page.description)
    }
}