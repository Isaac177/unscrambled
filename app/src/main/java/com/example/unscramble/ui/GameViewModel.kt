package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord: String

    private var usedWords: MutableSet<String> = mutableSetOf()

    var userGuess by mutableStateOf("")
        private set

    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (String(tempWord).equals(word, false)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(
            currentScrambledWord = pickRandomWordAndShuffle()
        )
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)){
            val updateScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updateScore)
        }
        else {_uiState.update {  currentState ->
            currentState.copy(
                isGuessedWordWrong = true,
                currentWordCount = currentState.currentWordCount.inc()
            )
        }}
        updateUserGuess("")
    }

    fun skipWord() {
        _uiState.update { currentState ->
            currentState.copy(
                currentWordCount = currentState.currentWordCount.inc(),
                currentScrambledWord = pickRandomWordAndShuffle()
            )
        }
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    score = updatedScore,
                    isGuessedWordWrong = false,
                    currentWordCount = currentState.currentWordCount.inc(),
                    currentScrambledWord = pickRandomWordAndShuffle()
                )
            }
        }
    }

    init {
        resetGame()
    }
}