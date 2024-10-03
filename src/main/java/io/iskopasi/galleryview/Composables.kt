package io.iskopasi.galleryview

import android.text.format.DateUtils
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

@Composable
fun BoxScope.NoDataText() {
    Text(
        text = stringResource(R.string.no_data),
        color = Color.White,
        fontSize = 19.sp,
        modifier = Modifier.align(Alignment.Center)
    )
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun HorizontalGalleryView(model: GalleryModel, height: Dp) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val imageList = model.mediaFiles

    model.onRefresh {
        coroutineScope.launch {
            listState.animateScrollToItem(index = 0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (imageList.isEmpty()) NoDataText()
        else
            LazyRow(
                state = listState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            ) {
                items(items = imageList,
                    key = {
                        it.file.name
                    }) { item ->
                    val length = DateUtils
                        .formatElapsedTime(item.length / 1000)
                    val elapsed = item.file.lastModified().toElapsed()

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .animateItemPlacement()
                    ) {
                        VideoItem(
                            item,
                            length,
                            elapsed,
                            {
                                model.onClick?.invoke(item.file)
                            },
                            {
                                model.remove(item.file)
                            },
                        )
                    }
                }
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridGalleryView(model: GalleryModel) {
    val imageList = model.mediaFiles

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (imageList.isEmpty()) NoDataText()
        else Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.recent),
                    fontSize = 23.sp,
                    color = Color.White,
                )
                ClearButtonBig(model)
            }
            Box(modifier = Modifier.height(32.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = imageList,
                    key = {
                        it.file.name
                    }) { item ->
                    val length = DateUtils
                        .formatElapsedTime(item.length / 1000)
                    val elapsed = item.file.lastModified().toElapsed()

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .animateItemPlacement()
                    ) {
                        VideoItem(
                            item, length, elapsed,
                            {
                                model.onClick?.invoke(item.file)
                            },
                            {
                                model.remove(item.file)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BoxScope.VideoItem(
    item: GalleryData,
    length: String,
    elapsed: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 5.dp)
                    .clickable(onClick = onClick)
                    .size(70.dp)
            ) {
                GlideImage(
                    model = item.file, contentDescription = item.file.name,
                    contentScale = ContentScale.Fit,
                    transition = CrossFade(
                        TweenSpec(durationMillis = 1000)
                    ),
                    modifier = Modifier
                        .border(width = 1.dp, Color.Black)
                        .align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(15.dp)
                        .clickable(
                            onClick = onRemove,
                            enabled = true,
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(
                                bounded = false,
                                radius = 20.dp
                            )
                        )
                        .border(
                            1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clip(
                            RoundedCornerShape(32.dp)
                        )
                        .background(color = Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Remove",
                        tint = Color.Black
                    )
                }
                Text(
                    text = length,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Text(
                text = stringResource(id = R.string.ago, elapsed),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ClearButton(galleryModel: GalleryModel) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .width(64.dp)
            .border(
                1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(32.dp)
            )
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(Color.White.copy(alpha = 0.7f))
            .clickable(
                onClick = galleryModel::clear,
                enabled = true,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = 64.dp,
                    color = Color.Black
                )
            )
    ) {
        Text(
            stringResource(R.string.clear),
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ClearButtonBig(galleryModel: GalleryModel) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .width(96.dp)
            .border(
                1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(32.dp)
            )
            .clip(
                RoundedCornerShape(32.dp)
            )
            .background(Color.White.copy(alpha = 0.7f))
            .clickable(
                onClick = galleryModel::clear,
                enabled = true,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = 96.dp,
                    color = Color.Black
                )
            )
    ) {
        Text(
            stringResource(R.string.clear_all),
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .align(Alignment.Center)
        )
    }
}