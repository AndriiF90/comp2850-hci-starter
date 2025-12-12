package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun validate(title: String): ValidationResult = when {
            title.isBlank() -> ValidationResult.Error("Title is required.")
            title.length < 3 -> ValidationResult.Error("Title must be at least 3 characters.")
            else -> ValidationResult.Success
        }
    }

    fun toPebbleContext(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "completed" to completed
    )
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
