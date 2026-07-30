package com.gymapp.sessions

enum class RestTimerStatus { IDLE, RUNNING, PAUSED, FINISHED }
data class RestTimerState(val exerciseName: String = "", val configuredSeconds: Int = 0, val remainingSeconds: Int = 0, val status: RestTimerStatus = RestTimerStatus.IDLE) {
    fun tick(seconds: Int) = if (status != RestTimerStatus.RUNNING) this else copy(remainingSeconds = (remainingSeconds - seconds).coerceAtLeast(0), status = if (remainingSeconds <= seconds) RestTimerStatus.FINISHED else RestTimerStatus.RUNNING)
    fun pause() = if (status == RestTimerStatus.RUNNING) copy(status = RestTimerStatus.PAUSED) else this
    fun resume() = if (status == RestTimerStatus.PAUSED) copy(status = RestTimerStatus.RUNNING) else this
    fun skip() = copy(remainingSeconds = 0, status = RestTimerStatus.IDLE)
    fun restart() = if (configuredSeconds > 0) copy(remainingSeconds = configuredSeconds, status = RestTimerStatus.RUNNING) else this
    companion object { fun start(exerciseName: String, seconds: Int) = if (seconds > 0) RestTimerState(exerciseName, seconds, seconds, RestTimerStatus.RUNNING) else RestTimerState() }
}
