package com.example.geoglow.ui.screens

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import com.example.geoglow.ColorViewModel
import com.example.geoglow.CustomGalleryContract
import com.example.geoglow.Friend
import com.example.geoglow.IDGenerator
import com.example.geoglow.MqttClient
import com.example.geoglow.PermissionHandler
import com.example.geoglow.R
import com.example.geoglow.SharedPreferencesHelper
import com.example.geoglow.createImageFile
import com.example.geoglow.paletteToRgbList
import kotlinx.coroutines.delay
import java.util.Objects


@Composable
fun MainScreen(navController: NavController, viewModel: ColorViewModel, mqttClient: MqttClient) {
    val context = LocalContext.current
    val user: Friend? = SharedPreferencesHelper.getUser(context)
    var expandInfo: Boolean by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(true) }
    val permissionHandler = PermissionHandler(context)
    val file = context.createImageFile()
    val imageUri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider",
        file
    )

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = CustomGalleryContract()) { uri ->
        uri?.let(viewModel::setColorState)
        uri?.let(viewModel::updateColorList)
        navController.navigate(Screen.ImageScreen.route)
    }
    
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.setColorState(imageUri, true)
            viewModel.updateColorList(imageUri)
            navController.navigate(Screen.ImageScreen.route)
        } else {
            Log.e("Composables", "couldn't take picture")
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { success ->
        permissionHandler.onPermissionResult(Manifest.permission.CAMERA, success)
        if (success) cameraLauncher.launch(imageUri)
    }

    if (user == null && showPopup) {
        WelcomePopup (
            mqttClient,
            onSave = { showPopup = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End
        ) {
            if (expandInfo) {
                InfoCard(
                    user = user,
                    onClose = { expandInfo = !expandInfo }
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(onClick = { expandInfo = !expandInfo }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_info_outline_24),
                    contentDescription = "Info",
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 35.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.geoglow_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GeoGlow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (permissionHandler.hasPermission(Manifest.permission.CAMERA)) {
                            cameraLauncher.launch(imageUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_photo_camera_24),
                        contentDescription = "camera"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Take photo",
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = {
                        galleryLauncher.launch()
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_image_24),
                        contentDescription = "image"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose image",
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(navController: NavController, viewModel: ColorViewModel, mqttClient: MqttClient) {
    val context = LocalContext.current
    val colorState: ColorViewModel.ColorState by viewModel.colorState.collectAsStateWithLifecycle(
        lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    )
    val imageBitmap = colorState.imageBitmap
    val palette = colorState.androidPalette
    val colorList = colorState.colorList
    val colorPalette = palette?.let { paletteToRgbList(it) } ?: emptyList()
    val user: Friend? = SharedPreferencesHelper.getUser(context)
    val friendList = SharedPreferencesHelper.getFriendList(context)
    var showPopup by remember { mutableStateOf(false) }
    val tabs = listOf("Android Palette", "Color Thief")
    var tabIndex by remember { mutableStateOf(0) }

    BackHandler {
        navController.navigate(Screen.MainScreen.route)
        viewModel.resetColorState()
    }

    TopAppBar(
        title = { Text(text = "") },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.navigate(Screen.MainScreen.route)
                    viewModel.resetColorState()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "back"
                )
            }
        }
    )

    if (showPopup && friendList.isNotEmpty()) {
        FriendSelectionPopup(
            navController,
            viewModel,
            colorList ?: colorPalette, //colorPalette,
            friendList,
            mqttClient,
            onDismiss = { showPopup = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 35.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "chosen image",
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 6.dp)
                    .size(290.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.0.dp,
                        palette?.mutedSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "default image",
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 6.dp)
                    .size(290.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.0.dp,
                        MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            0 -> AndroidPalette(palette)
            1 -> if (colorList?.isNotEmpty() == true) ColorThiefPalette(colorList) else LoadingAnimation()
        }

        FloatingActionButton(
            onClick = {
                mqttClient.subscribe(user?.friendId ?: "-1")
                mqttClient.publish(user?.friendId ?: "-1", null)
                showPopup = true
            },
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = 10.dp)
                .size(50.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_24),
                contentDescription = "done"
            )
        }
    }
}

@Composable
fun WelcomePopup(mqttClient: MqttClient, onSave: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Welcome!", style = MaterialTheme.typography.headlineSmall)
                Text("Please enter your name:")
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(enabled = name.isNotBlank(), onClick = {
                        val user = Friend (
                            name = name,
                            friendId = IDGenerator.generateUniqueID(),
                            devices = mutableListOf()
                        )
                        SharedPreferencesHelper.setUser(context, user)
                        mqttClient.publish(user.friendId ?: "-1", user.name)
                        onSave()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun FriendSelectionPopup(
    navController: NavController,
    viewModel: ColorViewModel,
    colorPalette: List<Array<Int>>,
    friends: List<Friend>,
    mqttClient: MqttClient,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedFriends = remember { mutableStateListOf<Friend>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Friends", style = MaterialTheme.typography.headlineSmall) },
        text = {
            LazyColumn {
                items(friends.size) { index ->
                    val friend = friends[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedFriends.contains(friend)) {
                                    selectedFriends.remove(friend)
                                } else {
                                    selectedFriends.add(friend)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedFriends.contains(friend),
                            onCheckedChange = {
                                if (it) {
                                    selectedFriends.add(friend)
                                } else {
                                    selectedFriends.remove(friend)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.Medium
                            )
                            if (friend.devices.isNotEmpty()) {
                                Text(text = friend.devices.first())
                            } else {
                                Text(text = "no devices", fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = selectedFriends.isNotEmpty(), onClick = {
                selectedFriends.forEach {
                    if (it.devices.isNotEmpty()) {
                        mqttClient.publish(it.friendId ?: "-1", it.devices.first(), colorPalette)
                    } else {
                        Log.i("Mqtt","Can't publish colors, as no devices are listed.")
                    }
                }
                navController.navigate(Screen.MainScreen.route)
                viewModel.resetColorState()
                Toast.makeText(context, "Color palette was sent", Toast.LENGTH_LONG).show()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun IconText(iconId: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "icon",
            modifier = Modifier
                .padding(end = 10.dp)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )

        Text(
            text = text,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun InfoCard(user: Friend?, onClose: () -> Unit) {
    Box {
        Card (
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable { onClose() }
        ) {
            Column (
                modifier = Modifier
                    //.fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                IconText(iconId = R.drawable.baseline_tag_24, text = user?.friendId ?: "-1")
                IconText(iconId = R.drawable.baseline_person_24, text = user?.name ?: "No name")
                if (user?.devices?.isNotEmpty() == true) {
                    IconText(iconId = R.drawable.baseline_list_alt_24, text = user.devices.first())
                }
            }
        }
    }
}

@Composable
fun AndroidPalette(palette: Palette?) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = palette?.vibrantSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Vibrant",
                    color = palette?.vibrantSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = palette?.darkVibrantSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Dark Vibrant",
                    color = palette?.darkVibrantSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = palette?.lightVibrantSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Light Vibrant",
                    color = palette?.lightVibrantSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = palette?.mutedSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Muted",
                    color = palette?.mutedSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = palette?.darkMutedSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Dark Muted",
                    color = palette?.darkMutedSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = palette?.lightMutedSwatch?.rgb?.let(::Color)
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(20)
                    )
                    .weight(1f)
            ) {
                Text(
                    text = "Light Muted",
                    color = palette?.lightMutedSwatch?.bodyTextColor?.let(::Color)
                        ?: MaterialTheme.colorScheme.onSecondary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun ColorThiefPalette(colorList: List<Array<Int>>) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until 5) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (col in 0 until 2) {
                    val index = row * 2 + col
                    if (index < colorList.size) {
                        val colorArray = colorList[index]
                        val color = Color(colorArray[0], colorArray[1], colorArray[2])

                        Box(
                            modifier = Modifier
                                .background(
                                    color = color,
                                    shape = RoundedCornerShape(20)
                                )
                                .weight(1f)
                        ) {
                            Text(
                                text = "(${colorArray[0]}, ${colorArray[1]}, ${colorArray[2]})",
                                color = if (color.luminance() > 0.5) Color.Black else Color.White,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val circleSize = 16.dp
    val circleColor = MaterialTheme.colorScheme.secondary
    val spaceBetween = 6.dp
    val travelDistance = 14.dp
    val distance = with(LocalDensity.current) { travelDistance.toPx() }

    val circles = listOf(
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) }
    )
    val circleValues = circles.map { it.value }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 using LinearOutSlowInEasing
                        1.0f at 300 using LinearOutSlowInEasing
                        0.0f at 600 using LinearOutSlowInEasing
                        0.0f at 1200 using LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        circleValues.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .graphicsLayer { translationY = -value * distance }
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
