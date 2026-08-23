package com.cortes.calculadoraderelevos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * El ViewModel actúa como nuestro motor de procesamiento de datos.
 * Maneja el estado global de la aplicación y encapsula la lógica algorítmica.
 */
class RelayViewModel : ViewModel() {
    // Dataset principal de atletas. Usamos mutableStateListOf para reactividad en la UI.
    var athletes = mutableStateListOf<Athlete>()
        private set

    // Estado de la navegación (State machine básica).
    var currentScreen by mutableStateOf(Screen.Input)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    // Resultado del procesamiento: Una lista de clusters (equipos).
    var teams by mutableStateOf<List<List<Athlete>>>(emptyList())
        private set

    // Funciones CRUD para la manipulación del dataset.
    fun addAthlete(name: String, time: Double) {
        athletes.add(Athlete(name, time))
    }

    fun removeAthlete(index: Int) {
        if (index in athletes.indices) {
            athletes.removeAt(index)
        }
    }

    fun updateAthlete(index: Int, name: String, time: Double) {
        if (index in athletes.indices) {
            athletes[index] = Athlete(name, time)
        }
    }

    fun finishAdding() {
        currentScreen = Screen.Selection
    }

    /**
     * ALGORITMO DE PARTICIÓN (Equilibrado de Equipos)
     * Este es un problema de optimización combinatoria. Para resolverlo de forma eficiente,
     * aplicamos una heurística codiciosa (Greedy Algorithm):
     * 1. Ordenamos el dataset por tiempo descendente (para manejar primero los valores más pesados).
     * 2. Asignamos cada elemento al cluster que tenga la menor suma acumulada en ese momento.
     * Objetivo: Minimizar la varianza del tiempo total entre los equipos.
     */
    fun divideIntoTeams(numTeams: Int) {
        val sortedAthletes = athletes.sortedByDescending { it.time }
        val resultTeams = List(numTeams) { mutableListOf<Athlete>() }
        
        for (athlete in sortedAthletes) {
            // Buscamos el equipo con la menor carga actual (Minimización local).
            val targetTeam = resultTeams.minByOrNull { team -> team.sumOf { it.time } }
            targetTeam?.add(athlete)
        }
        
        teams = resultTeams
        currentScreen = Screen.Result
    }

    fun reset() {
        athletes.clear()
        teams = emptyList()
        currentScreen = Screen.Input
    }

    // Definición del espacio de estados de la aplicación.
    enum class Screen {
        Input, Selection, Result, Stopwatch
    }
}
