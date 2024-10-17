package com.example.appgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.appgame.ui.theme.AppGameTheme
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val respostes = mutableStateListOf<Int>()
        val quiz = QuizViewModel()
        var time = 0

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppGameTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController, respostes, quiz) }
                    composable("game") { GameScreen(navController, respostes,quiz, time) }
                    composable("end") { FinalScreen(navController, quiz,  respostes) }

                }
            }
        }
    }


    @Composable
    fun HomeScreen(
        navController: NavController,
        respostes: MutableList<Int>,
        quiz: QuizViewModel,

    ) {

        if (respostes.size > 0) {
            respostes.clear()
        }

            LaunchedEffect(quiz.isQuizLoaded.value) {
                if (quiz.isQuizLoaded.value) {
                    quiz.resetTimer()
                    startGame(navController)
                }
            }



        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Game Quiz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledButtonStartGame {
                quiz.viewModelScope.launch {
                    quiz.loadQuiz("")

                }



            }
        }
    }

    fun startGame(navController: NavController) {
        navController.navigate("game")
    }

    @Composable
    fun FilledButtonStartGame(onClick: () -> Unit) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.BottomCenter)
        ) {
            Button(onClick = { onClick() }) {
                Text("Començar el joc")
            }
        }
    }

    @Composable
    fun GameScreen(
        navController: NavController,
        respostes: MutableList<Int>,
        quiz: QuizViewModel,
        initialTime: Int
    ) {
        if (!quiz.timeStarted.value) {
            println("Iniciando temporizador")
            quiz.startTimer()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Agregar padding general
        ) {
            if (respostes.size < 10) {
                // Mostrar el tiempo
                Text(
                    text = "${quiz.time.value} seg.",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary // Color dinámico
                )

                // Contenedor para la pregunta y la imagen
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 150.dp), // Ajustar el espaciado superior
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quiz.quiz?.preguntes?.get(respostes.size)?.enunciat
                            ?: "No hay preguntas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp), // Añadir un poco de padding al texto
                        color = MaterialTheme.colorScheme.onSurface // Color dinámico
                    )

                    // Mostrar imagen
                    AsyncImage(
                        model = quiz.quiz?.preguntes?.get(respostes.size)?.imatge,
                        contentDescription = "Imagen de la pregunta",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(12.dp)) // Bordes redondeados
                            .background(MaterialTheme.colorScheme.surface) // Fondo para la imagen
                            .padding(8.dp)
                    )
                }

                // Contenedor para los botones de respuesta
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.BottomCenter)
                        .align(Alignment.BottomCenter) // Cambiar de BottomEnd a BottomCenter
                        .padding(top = 25.dp) // Espaciado superior para separar de la imagen
                ) {
                    Column {
                        if (respostes.size < 10) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AnswerButton(
                                    0, respostes, navController,
                                    quiz.quiz!!.preguntes[respostes.size],
                                    quiz
                                )
                                AnswerButton(
                                    1,
                                    respostes,
                                    navController,
                                    quiz.quiz!!.preguntes[respostes.size],
                                    quiz
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AnswerButton(
                                    2,
                                    respostes,
                                    navController,
                                    quiz.quiz!!.preguntes[respostes.size],
                                    quiz
                                )
                                AnswerButton(
                                    3,
                                    respostes,
                                    navController,
                                    quiz.quiz!!.preguntes[respostes.size],
                                    quiz
                                )
                            }
                        }
                    }
                }
            }
        }
    }



    @Composable
    fun FinalScreen(
        navController: NavController,
        quiz: QuizViewModel,
        respostes: MutableList<Int>,

    ) {

        var encertades by remember { mutableStateOf(-1) }
        var done by remember { mutableStateOf(false) } // remember prevents reset on recomposition

        if (!done) {
            quiz.getCorrectes(quiz.quiz!!.uid, respostes)
            done = true
        }

        LaunchedEffect(quiz.quiz) {
            if (quiz.quiz!!.encertades != -2) {
                encertades = quiz.quiz!!.encertades
            }
        }

        quiz.isQuizLoaded.value = false

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (encertades != -1) {
                Text(
                    text = "Partida finalitzada",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Encertades: $encertades/10"
                )
                Text(
                    text = "Temps: ${quiz.time.value} segons"
                )

                Spacer(modifier = Modifier.height(16.dp))
                FilledButtonTornarInici(quiz) { navController.navigate("home") }
            } else {
                Text(
                    text = "Carregant...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    @Composable
    fun AnswerButton(index: Int, respostes: MutableList<Int>, navController: NavController, pregunta: QuizViewModel.Pregunta, quiz: QuizViewModel) {
        Box {
            Button(
                onClick = { addResponse(index, respostes, navController, quiz) },
                modifier = Modifier
                    .padding(10.dp)
                    .size(150.dp, 60.dp)
            ) {
                Text(pregunta.respostes[index])
            }
        }
    }

    @Composable
    fun FilledButtonTornarInici(quiz: QuizViewModel, onClick: () -> Unit) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.BottomCenter)
        ) {
            Button(onClick = {
                onClick() }) {
                Text("Tornar a l'inici")
            }
        }
    }

    fun addResponse(index: Int, respostes: MutableList<Int>, navController: NavController, quiz: QuizViewModel) {
        respostes.add(index)

        if (respostes.size == 10) {
            quiz.stopTimer()
            println("Navegando a end")
            navController.navigate("end")
        } else {
            navController.navigate("game")
        }
    }

    class QuizViewModel : ViewModel() {
        var quiz: Quiz? = null
        var isQuizLoaded = mutableStateOf(false)
        var time = mutableStateOf(0)
        var timeStarted= mutableStateOf(false)
        fun loadQuiz(uid: String) {
            val currentUid = quiz?.uid ?: uid
            viewModelScope.launch(Dispatchers.IO) {

                isQuizLoaded.value = false
                try {

                    val url = URL("http://quizeric.dam.inspedralbes.cat:24269/preguntesPartida")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    val requestBody = """{"uid": "$currentUid"}"""
                    val outputStream = OutputStreamWriter(connection.outputStream)
                    outputStream.write(requestBody)
                    outputStream.flush()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val gson = Gson()
                        quiz = gson.fromJson(response, Quiz::class.java)
                        println("Quiz loaded: $quiz")
                        isQuizLoaded.value = true

                    } else {
                        println("Error: ${connection.responseCode}")
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        fun startTimer() {
            viewModelScope.launch(Dispatchers.IO) {
                quiz?.let { println(it.encertades) }
                    timeStarted.value = true // Cambiamos el estado a true al iniciar el temporizador
                    while (quiz?.encertades == -2) {
                        delay(1000)
                        time.value += 1
                        println("Tiempo: ${timeStarted.value}") // Para depurar y ver el progreso del tiempo
                    }

            }
        }
        fun stopTimer() {
            quiz?.encertades  = -3
            timeStarted.value = false
        }
        fun resetTimer() {
            time.value = 0
        }
        fun getCorrectes(uid: String, respostes: MutableList<Int>) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val url = URL("http://quizeric.dam.inspedralbes.cat:24269/respostesPartida")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    val gson = Gson()
                    val respostesJson = gson.toJson(respostes)
                    val requestBody =
                        """{"uid": "$uid", "respostes": ${respostesJson}}"""
                    val outputStream = OutputStreamWriter(connection.outputStream)
                    outputStream.write(requestBody)
                    outputStream.flush()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }


                        // Obtener el campo "encertades" de la respuesta JSON
                        quiz = gson.fromJson(response, Quiz::class.java)

                    } else {
                        println("Error: ${connection.responseCode}")
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                println("FunctionDone")
            }
        }

        data class Pregunta(
            val enunciat: String,
            val respostes: List<String>,
            val imatge: String
        )

        data class Quiz(
            val uid: String,
            val preguntes: List<Pregunta>,
            var encertades: Int = -2
        )
    }
}