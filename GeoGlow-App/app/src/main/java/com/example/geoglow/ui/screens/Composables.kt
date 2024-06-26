package com.example.geoglow.ui.screens

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import java.util.Objects


@Composable
fun MainScreen(navController: NavController, viewModel: ColorViewModel, mqttClient: MqttClient) {
    //val viewModel: ColorViewModel = viewModel()
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
        navController.navigate(Screen.ImageScreen.route)
    }
    
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri.let(viewModel::setColorState)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_info_outline_24),
                contentDescription = "icon",
                modifier = Modifier
                    .size(25.dp)
                    .clickable { expandInfo = !expandInfo },
                tint = MaterialTheme.colorScheme.secondary
            )

            if (expandInfo) {
                Box (
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                ) {
                    Card (
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clickable { expandInfo = !expandInfo }
                    ) {
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            IconText(iconId = R.drawable.baseline_tag_24, text = user?.id ?: "-1")
                            IconText(iconId = R.drawable.baseline_person_24, text = user?.name ?: "No name")
                            if (user?.devices?.isNotEmpty() == true) {
                                IconText(iconId = R.drawable.baseline_list_alt_24, text = user.devices.first())
                            }
                        }
                    }
                }
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
    //val viewModel: ColorViewModel = viewModel()
    val context = LocalContext.current
    val colorState: ColorViewModel.ColorState by viewModel.colorState.collectAsStateWithLifecycle(
        lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    )
    val imageBitmap: ImageBitmap? = colorState.imageBitmap
    val palette: Palette? = colorState.palette
    val colorPalette = palette?.let { paletteToRgbList(it) } ?: emptyList()
    val user: Friend? = SharedPreferencesHelper.getUser(context)
    var showPopup by remember { mutableStateOf(false) }


    TopAppBar(
        title = { Text(text = "") },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.navigate(Screen.MainScreen.route)
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "back"
                )
            }
        }
    )

    if (showPopup) {
        FriendSelectionPopup(
            navController,
            colorPalette,
            mqttClient,
            onDismiss = { showPopup = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "chosen image",
                modifier = Modifier
                    .padding(10.dp)
                    .size(320.dp)
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
                    .padding(10.dp)
                    .size(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.0.dp,
                        MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(16.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row (
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

            Row (
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

            Row (
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

        FloatingActionButton(
            onClick = {
                //TODO: do it somewhere else
                mqttClient.subscribe(user?.id ?: "-1")
                mqttClient.publish(user?.id ?: "-1", null)
                showPopup = true
            },
            modifier = Modifier
                .align(alignment = Alignment.End)
                .padding(end = 10.dp)
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
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            val user = Friend (
                                name = name,
                                id = IDGenerator.generateUniqueID(),
                                devices = mutableListOf()
                            )
                            SharedPreferencesHelper.setUser(context, user)
                            mqttClient.publish(user.id ?: "-1", user.name)
                            onSave()
                        }
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
    colorPalette: List<Array<Int>>,
    mqttClient: MqttClient,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedFriends = remember { mutableStateListOf<Friend>() }
    val friendList = SharedPreferencesHelper.getFriendList(context)
    val friends = friendList.ifEmpty { listOf(
        Friend("Anna", "47fh39cv", mutableListOf("366452")),
        Friend("Hans", "kj876fnt", mutableListOf("767854")),
        Friend("Peter", "0j6gd8nb", mutableListOf("364765"))
    ) }

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
                            Text(text = friend.devices.first())
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                selectedFriends.forEach {
                    mqttClient.publish(it.id ?: "-1", it.devices.first(), colorPalette)
                }
                navController.navigate(Screen.MainScreen.route)
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
