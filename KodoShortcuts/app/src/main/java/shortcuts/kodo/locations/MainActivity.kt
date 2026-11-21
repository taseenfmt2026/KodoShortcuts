
package shortcuts.kodo.locations

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6558D3),
                    secondary = Color(0xFF8B80E8),
                    background = Color(0xFFF7F6FC),
                    surface = Color.White
                )
            ) {
                KodoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KodoApp() {

    val context = LocalContext.current

    var rules by remember {
        mutableStateOf(StorageManager.getRules(context))
    }

    var editingRule by remember {
        mutableStateOf<LocationRule?>(null)
    }

    var showEditor by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (LocationManager.hasLocationPermission(context)) {
                message = "Location permission granted."
            } else {
                message = "Location permission is required."
            }
        }

    fun reload() {
        rules = StorageManager.getRules(context)
    }

    fun save(rule: LocationRule) {
        StorageManager.saveRule(context, rule)
        LocationManager.refresh(context)
        reload()
        message = "Location saved."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Kodo Shortcuts",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Location automation",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            reload()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRule = null
                    showEditor = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add location"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFECE9FF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Your automation",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "${rules.size} saved locations",
                            color = Color.DarkGray
                        )

                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Grant location permission")
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts(
                                        "package",
                                        context.packageName,
                                        null
                                    )
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Background location settings")
                        }

                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.DoNotDisturbOn,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Grant DND access")
                        }
                    }
                }
            }

            item {
                Text(
                    "Saved locations",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (rules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocationSearching,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "No locations yet",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Add your first location to get started.",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            items(rules, key = { it.id }) { rule ->

                LocationCard(
                    rule = rule,
                    onEdit = {
                        editingRule = rule
                        showEditor = true
                    },
                    onDelete = {
                        StorageManager.deleteRule(
                            context,
                            rule.id
                        )
                        LocationManager.refresh(context)
                        reload()
                        message = "Location deleted."
                    },
                    onToggle = { enabled ->
                        save(rule.copy(enabled = enabled))
                    }
                )
            }

            item {
                Spacer(Modifier.height(70.dp))
            }
        }
    }

    if (showEditor) {
        LocationEditor(
            initial = editingRule,
            onDismiss = {
                showEditor = false
            },
            onSave = {
                save(it)
                showEditor = false
            }
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = {
                message = null
            },
            title = {
                Text("Kodo Shortcuts")
            },
            text = {
                Text(it)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        message = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationCard(
    rule: LocationRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    menuExpanded = true
                }
            )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        rule.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        "${rule.radius} feet radius",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = rule.enabled,
                    onCheckedChange = onToggle
                )
            }

            HorizontalDivider()

            Text(
                "Coordinates",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )

            Text(
                "${rule.latitude}, ${rule.longitude}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                "Activities",
                fontWeight = FontWeight.Medium
            )

            if (rule.actions.isEmpty()) {
                Text(
                    "No activities added",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            rule.actions.forEach { action ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (action) {
                            AutomationManager.SILENT ->
                                Icons.Default.VolumeOff

                            AutomationManager.DND ->
                                Icons.Default.DoNotDisturbOn

                            else -> Icons.Default.Bolt
                        },
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        when (action) {
                            AutomationManager.SILENT -> "Silent Mode"
                            AutomationManager.DND -> "Do Not Disturb"
                            else -> action
                        },
                        fontSize = 14.sp
                    )
                }
            }

            Box {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit location") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete location") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = Color.Red
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }

                Text(
                    "Hold location for options",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationEditor(
    initial: LocationRule?,
    onDismiss: () -> Unit,
    onSave: (LocationRule) -> Unit
) {

    val context = LocalContext.current

    var name by remember {
        mutableStateOf(initial?.name ?: "")
    }

    var latitude by remember {
        mutableStateOf(initial?.latitude?.toString() ?: "")
    }

    var longitude by remember {
        mutableStateOf(initial?.longitude?.toString() ?: "")
    }

    var radius by remember {
        mutableStateOf(initial?.radius?.toString() ?: "500")
    }

    var silent by remember {
        mutableStateOf(
            initial?.actions?.contains(AutomationManager.SILENT) ?: false
        )
    }

    var dnd by remember {
        mutableStateOf(
            initial?.actions?.contains(AutomationManager.DND) ?: false
        )
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "Add location"
                else "Edit location"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        if (!LocationManager.hasLocationPermission(context)) {
                            error = "Grant location permission first."
                        } else {
                            try {
                                locationClient.getCurrentLocation(
                                    Priority.PRIORITY_HIGH_ACCURACY,
                                    null
                                ).addOnSuccessListener { location ->
                                    if (location != null) {
                                        latitude = location.latitude.toString()
                                        longitude = location.longitude.toString()
                                    } else {
                                        error = "Could not detect location."
                                    }
                                }.addOnFailureListener {
                                    error = it.message ?: "Location error."
                                }
                            } catch (e: SecurityException) {
                                error = "Location permission required."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use current GPS location")
                }

                OutlinedTextField(
                    value = radius,
                    onValueChange = {
                        radius = it.filter { c ->
                            c.isDigit() || c == '.'
                        }
                    },
                    label = { Text("Radius (feet)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Text(
                    "Activities",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = silent,
                        onCheckedChange = { silent = it }
                    )
                    Text("Silent Mode")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dnd,
                        onCheckedChange = { dnd = it }
                    )
                    Text("Do Not Disturb")
                }

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {

                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()
                    val r = radius.toFloatOrNull()

                    if (name.isBlank()) {
                        error = "Enter a location name."
                        return@TextButton
                    }

                    if (lat == null || lat !in -90.0..90.0 ||
                        lng == null || lng !in -180.0..180.0
                    ) {
                        error = "Enter valid coordinates."
                        return@TextButton
                    }

                    if (r == null || !r.isFinite() || r <= 0f) {
                        error = "Enter a valid radius."
                        return@TextButton
                    }

                    val actions = buildList {
                        if (silent) add(AutomationManager.SILENT)
                        if (dnd) add(AutomationManager.DND)
                    }

                    val rule = StorageManager.createRule(
                        name = name.trim(),
                        lat = lat,
                        lng = lng,
                        radius = r,
                        actions = actions,
                        id = initial?.id
                            ?: java.util.UUID.randomUUID().toString(),
                        enabled = initial?.enabled ?: true
                    )

                    onSave(rule)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
