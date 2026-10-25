package app.topgun.android.features.blog

import androidx.compose.ui.res.stringResource
import app.topgun.android.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.topgun.android.data.Load
import app.topgun.android.data.containerViewModel
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAccountRadius
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import app.topgun.android.designsystem.TGText
import app.topgun.android.designsystem.components.AccountTopBar
import app.topgun.android.designsystem.components.EmptyState
import app.topgun.android.designsystem.components.ErrorState
import app.topgun.android.designsystem.components.TGImage
import app.topgun.android.designsystem.detailTitle
import app.topgun.core.model.Blog

/** iOS `BlogView`: posts with image, source logo + name and "Read More"; tapping opens `BlogDetailView`. */
@Composable
fun BlogScreen(onBack: () -> Unit) {
    val vm = containerViewModel { BlogViewModel(it.api) }
    val state by vm.state.collectAsStateWithLifecycle()
    // Blog id, not the object, so the selection survives rotation.
    var openId by rememberSaveable { mutableStateOf<Int?>(null) }
    val blogs = (state.blogs as? Load.Loaded)?.value.orEmpty()
    val open = blogs.firstOrNull { it.id == openId }

    BackHandler(enabled = open != null) { openId = null }

    Column(Modifier.fillMaxSize().background(TG.colors.background)) {
        if (open != null) {
            AccountTopBar(open.subHeader?.takeIf { it.isNotBlank() } ?: open.title, onBack = { openId = null })
            BlogDetail(open)
        } else {
            AccountTopBar(stringResource(R.string.blog_title), onBack = onBack)
            when (val s = state.blogs) {
                Load.Loading -> BlogSkeleton()
                is Load.Failed -> ErrorState(s.error.userMessage, onRetry = vm::reload)
                is Load.Loaded -> if (s.value.isEmpty()) {
                    EmptyState(stringResource(R.string.blog_empty))
                } else {
                    BlogList(state, vm, onOpen = { openId = it.id })
                }
            }
        }
    }
}

@Composable
private fun BlogList(state: BlogListState, vm: BlogViewModel, onOpen: (Blog) -> Unit) {
    val blogs = (state.blogs as Load.Loaded).value
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = TGSpace.l, vertical = TGSpace.s),
        verticalArrangement = Arrangement.spacedBy(TGSpace.l),
    ) {
        items(blogs, key = { it.id }) { blog -> BlogCard(blog, onOpen = { onOpen(blog) }) }
        item(key = "footer") {
            // Reaching the footer asks for the next page.
            LaunchedEffect(blogs.size, state.hasMore) { if (state.hasMore && state.moreError == null) vm.loadMore() }
            Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = TGSpace.l), contentAlignment = Alignment.Center) {
                when {
                    state.loadingMore -> CircularProgressIndicator(color = TG.colors.accent)
                    state.moreError != null -> ErrorState(state.moreError, onRetry = vm::loadMore)
                }
            }
        }
    }
}

@Composable
private fun BlogCard(blog: Blog, onOpen: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen).padding(vertical = TGSpace.s),
        verticalArrangement = Arrangement.spacedBy(TGSpace.s + TGSpace.xs / 2),
    ) {
        BlogImage(blog, TGAccountRadius.blogListImage)
        Text(blog.title, style = TGText.sailecBold(17), color = TG.colors.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            SourceLogo(blog, TGAccountSize.blogLogoList)
            Text(blog.source, style = TGText.sailecRegular(15), color = TG.colors.textSecondary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.heightIn(min = TGSize.tapMin), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.blog_read_more), style = TGText.sailecRegular(12), color = TGBrand.blue)
            }
        }
    }
}

@Composable
private fun BlogImage(blog: Blog, shape: androidx.compose.ui.graphics.Shape) {
    Box(Modifier.fillMaxWidth().height(TGAccountSize.blogImageHeight).clip(shape)) {
        TGImage(blog.image, contentDescription = stringResource(R.string.blog_image, blog.title), modifier = Modifier.fillMaxSize())
        if (blog.image.isNullOrBlank()) {
            // iOS placeholder `photo.fill`.
            Icon(Icons.Filled.Newspaper, contentDescription = null, tint = TGBrand.gray, modifier = Modifier.align(Alignment.Center).size(TGSize.tapMin))
        }
    }
}

@Composable
private fun SourceLogo(blog: Blog, size: androidx.compose.ui.unit.Dp) {
    if (blog.sourceLogo.isNullOrBlank()) {
        Icon(Icons.Filled.Newspaper, contentDescription = null, tint = TGBrand.gray, modifier = Modifier.size(size))
    } else {
        TGImage(blog.sourceLogo, contentDescription = stringResource(R.string.blog_logo, blog.source), modifier = Modifier.size(size).clip(CircleShape))
    }
}

/** iOS `BlogDetailView`: image, source logo + name, sub header as the headline, then the description. */
@Composable
private fun BlogDetail(blog: Blog) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(TGSpace.l).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(TGSpace.l - TGSpace.xs / 4),
    ) {
        BlogImage(blog, TGRadius.image)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            SourceLogo(blog, TGAccountSize.blogLogoDetail)
            Text(blog.source, style = TGText.sailecRegular(15), color = TG.colors.textSecondary)
        }
        Text(blog.subHeader?.takeIf { it.isNotBlank() } ?: blog.title, style = TGText.detailTitle, color = TG.colors.textPrimary)
        Text(blog.description, style = TGText.sailecRegular(17), color = TG.colors.textSecondary)
    }
}

/** Loading placeholders (iOS `skeletonBlogCard`, without the shimmer): three gray cards. */
@Composable
private fun BlogSkeleton() {
    Column(Modifier.fillMaxSize().padding(TGSpace.l), verticalArrangement = Arrangement.spacedBy(TGSpace.l)) {
        repeat(3) {
            Column(verticalArrangement = Arrangement.spacedBy(TGSpace.s + TGSpace.xs / 2)) {
                Box(Modifier.fillMaxWidth().height(TGAccountSize.blogImageHeight).clip(TGAccountRadius.blogListImage).background(TGBrand.gray.copy(alpha = 0.3f)))
                Box(Modifier.fillMaxWidth().height(TGSpace.xl - TGSpace.xs).clip(TGRadius.chip).background(TGBrand.gray.copy(alpha = 0.3f)))
                Box(Modifier.fillMaxWidth(0.4f).height(TGSpace.l).clip(TGRadius.chip).background(TGBrand.gray.copy(alpha = 0.3f)))
            }
        }
    }
}
