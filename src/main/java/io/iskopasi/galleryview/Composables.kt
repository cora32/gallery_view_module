package io.iskopasi.galleryview

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.Transition
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryComposable(model: GalleryModel) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val imageList = model.mediaFiles

    model.onRefresh {
        coroutineScope.launch {
            listState.animateScrollToItem(index = 0)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(75.dp)) {
        if (imageList.isEmpty()) Text(
            text = stringResource(R.string.no_data),
            color = Color.White,
            fontSize = 19.sp,
            modifier = Modifier.align(Alignment.Center)
        )
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
                    val elapsed =
                        DateUtils
                            .formatElapsedTime((System.currentTimeMillis() - item.file.lastModified()) / 1000)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .animateItemPlacement()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 5.dp)
                                .clickable(onClick = {
                                    model.onClick?.invoke(item.file)
                                })
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
                                        onClick = {
                                            model.remove(item.file)
                                        },
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
    }
}