package storage

import model.Task

class TaskStore {
    private val tasks = mutableListOf<Task>()

    fun getAll(): List<Task> = tasks.toList()
    fun getById(id: String): Task? = tasks.find { it.id == id }
    fun add(task: Task) { tasks.add(task) }
    fun update(task: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            return true
        }
        return false
    }
    fun delete(id: String): Boolean = tasks.removeIf { it.id == id }
}
