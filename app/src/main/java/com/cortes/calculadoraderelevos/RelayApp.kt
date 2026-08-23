package com.cortes.calculadoraderelevos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay

/**
 * Punto de entrada visual de la aplicación.
 * Como cientificos de datos, vemos esto como el orquestador que decide qué "vista" del dataset mostrar
 * basándose en el estado actual del flujo de trabajo (Input -> Selection -> Result).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayApp(viewModel: RelayViewModel = viewModel()) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Fila compacta para maximizar el 'real estate' de la pantalla para los datos.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Calculadora de Relevos",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
                
                Box {
                    IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Introducción Manual") },
                            onClick = {
                                viewModel.navigateTo(RelayViewModel.Screen.Input)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cronómetro") },
                            onClick = {
                                viewModel.navigateTo(RelayViewModel.Screen.Stopwatch)
                                showMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Reiniciar Todo") },
                            onClick = {
                                viewModel.reset()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Firma de autoría con Insets para evitar colisión con la navegación del sistema.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "© 2026 - Desarrollado por Cortés / TheDataRoboteer",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            
            // Marca de agua: Escalamiento del logo para branding sutil.
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.6f)
                    .alpha(0.08f),
                contentScale = ContentScale.Fit
            )

            // Router de pantallas basado en el Enum de estado del ViewModel.
            when (viewModel.currentScreen) {
                RelayViewModel.Screen.Input -> AthleteInputScreen(viewModel)
                RelayViewModel.Screen.Selection -> DivisionSelectionScreen(viewModel)
                RelayViewModel.Screen.Result -> ResultScreen(viewModel)
                RelayViewModel.Screen.Stopwatch -> StopwatchScreen(viewModel)
            }
        }
    }
}

@Composable
fun HeaderLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo",
        modifier = Modifier
            .height(80.dp)
            .padding(bottom = 8.dp),
        contentScale = ContentScale.Fit
    )
}

/**
 * Pantalla de captura de datos manual.
 * Aquí realizamos la 'limpieza de datos' inicial (data cleaning) asegurando que los campos no estén vacíos
 * y que el tiempo sea un valor numérico válido (Double).
 */
@Composable
fun AthleteInputScreen(viewModel: RelayViewModel) {
    var name by remember { mutableStateOf("") }
    var timeStr by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()

        Text(
            text = if (editingIndex == null) "Añadir Atleta" else "Editar Atleta",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                showError = false
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && name.isBlank(),
            singleLine = true,
            trailingIcon = {
                if (editingIndex != null) {
                    IconButton(onClick = {
                        editingIndex = null
                        name = ""
                        timeStr = ""
                        showError = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar edición")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = timeStr,
            onValueChange = { 
                timeStr = it
                showError = false
            },
            label = { Text("Tiempo (segundos)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showError && timeStr.toDoubleOrNull() == null,
            singleLine = true
        )

        if (showError) {
            Text(
                text = "Por favor, rellena ambos campos correctamente.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fila de acciones optimizada para evitar overlap en pantallas pequeñas.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val time = timeStr.toDoubleOrNull()
                    if (name.isNotBlank() && time != null) {
                        if (editingIndex == null) {
                            viewModel.addAthlete(name, time)
                        } else {
                            viewModel.updateAthlete(editingIndex!!, name, time)
                            editingIndex = null
                        }
                        name = ""
                        timeStr = ""
                        showError = false
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.weight(0.35f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                if (editingIndex == null) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Añadir", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Act.", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (editingIndex == null) {
                Button(
                    onClick = {
                        val time = timeStr.toDoubleOrNull()
                        if (name.isNotBlank() && time != null) {
                            viewModel.addAthlete(name, time)
                            viewModel.finishAdding()
                        } else if (name.isBlank() && timeStr.isBlank() && viewModel.athletes.isNotEmpty()) {
                            viewModel.finishAdding()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.weight(0.65f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Guardar y Calcular", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Atletas añadidos: ${viewModel.athletes.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // El listado es una visualización tabular de nuestro dataset actual.
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(viewModel.athletes.withIndex().toList()) { (index, athlete) ->
                ListItem(
                    headlineContent = { Text(athlete.name) },
                    supportingContent = { Text("${athlete.time}s") },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editingIndex = index
                                name = athlete.name
                                timeStr = athlete.time.toString()
                                showError = false
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { 
                                if (editingIndex == index) {
                                    editingIndex = null
                                    name = ""
                                    timeStr = ""
                                }
                                viewModel.removeAthlete(index) 
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/**
 * Cronómetro para captura de datos en tiempo real.
 * Como analistas, aquí generamos datos de series temporales (timestamps) 
 * y calculamos la diferencia delta para obtener el tiempo de ejecución.
 */
@Composable
fun StopwatchScreen(viewModel: RelayViewModel) {
    var name by remember { mutableStateOf("") }
    var timeMillis by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var lastTick by remember { mutableStateOf(0L) }
    var showError by remember { mutableStateOf(false) }

    // Bucle de muestreo (Sampling loop) que actualiza el contador cada 10ms.
    LaunchedEffect(isRunning) {
        if (isRunning) {
            lastTick = System.currentTimeMillis()
            while (isRunning) {
                delay(10)
                val current = System.currentTimeMillis()
                timeMillis += current - lastTick
                lastTick = current
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()

        Text(
            text = "Cronómetro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                showError = false
            },
            label = { Text("Nombre del Atleta") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError && name.isBlank(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Formateo de los datos crudos (ms) a una cadena legible.
        val seconds = (timeMillis / 1000) % 60
        val minutes = (timeMillis / 60000) % 60
        val millis = (timeMillis % 1000) / 10
        val timeFormatted = "%02d:%02d.%02d".format(minutes, seconds, millis)

        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Icon(if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (isRunning) "Parar" else "Iniciar")
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    timeMillis = 0
                },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Reiniciar")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && timeMillis > 0) {
                    val timeSeconds = timeMillis / 1000.0
                    viewModel.addAthlete(name, timeSeconds)
                    name = ""
                    timeMillis = 0
                    isRunning = false
                    showError = false
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            Text("Guardar y Añadir")
        }

        if (showError && name.isBlank()) {
            Text(
                text = "Pon un nombre antes de guardar.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Atletas: ${viewModel.athletes.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            TextButton(onClick = { viewModel.finishAdding() }) {
                Text("Ir a Calcular")
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(viewModel.athletes.withIndex().toList().reversed()) { (index, athlete) ->
                ListItem(
                    headlineContent = { Text(athlete.name) },
                    supportingContent = { Text("${athlete.time}s") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeAthlete(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/**
 * Pantalla de selección de parámetros.
 * Aquí definimos el hiperparámetro 'k' (número de equipos) para nuestra partición.
 * Aplicamos una restricción de validación: N >= k*2 para asegurar una distribución mínima.
 */
@Composable
fun DivisionSelectionScreen(viewModel: RelayViewModel) {
    val numAthletes = viewModel.athletes.size
    val options = (2..8).filter { numAthletes >= it * 2 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeaderLogo()
        Text(
            text = "Dividir en equipos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Total de atletas: $numAthletes", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (options.isEmpty()) {
            Text(
                text = "No hay suficientes atletas para dividir en al menos 2 equipos (necesitas al menos 4 atletas).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.reset() }) {
                Text("Volver a empezar")
            }
        } else {
            options.forEach { n ->
                Button(
                    onClick = { viewModel.divideIntoTeams(n) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Dividir en $n equipos")
                }
            }
        }
    }
}

/**
 * Pantalla de salida de datos (Output).
 * Visualizamos los clusters resultantes y el valor agregado (suma de tiempos) de cada uno.
 */
@Composable
fun ResultScreen(viewModel: RelayViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderLogo()
        Text(
            text = "Equipos calculados",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.teams.withIndex().toList()) { (index, team) ->
                val totalTime = team.sumOf { it.time }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Equipo ${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tiempo esperado: ${"%.2f".format(totalTime)}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        team.forEach { athlete ->
                            Text("${athlete.name}: ${athlete.time}s", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.reset() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Nueva calculadora")
        }
    }
}
