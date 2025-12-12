import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.ktor.util.AttributeKey
import routes.taskRoutes
import storage.TaskStore
import java.io.StringWriter

fun main() {
    // starts the server and waits for it to finish
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/*
 * Configures the application module.
 * Sets up templating, storage, and routing logic.
 */
fun Application.module() {
    
    // sets up the engine for rendering HTML
    configureTemplating()

    // creates the task storage in memory
    // this instance is passed down to route
    val taskStore = TaskStore()

    routing {
        staticResources("/static", "static")

        taskRoutes(taskStore)
    }
}

// Helper functions kept for templating
/*
 * Configures the Pebble template engine.
 * Loads templates from the classpath and enables security features.
 */
fun Application.configureTemplating() {
    // builds the pebble engine with specific settings
    val engine = PebbleEngine.Builder()
        .loader(ClasspathLoader().apply { prefix = "templates/" }) // sets the root folder for templates
        .autoEscaping(true) // enables auto-escaping for XSS protection
        .build()
        
    // stores the engine in the application attributes for later retrieval
    attributes.put(AttributeKey<PebbleEngine>("pebble"), engine)
}

/*
 * Helper function to render templates easily in routes.
 * Takes a template path and data map, returns the rendered HTML string.
 */
suspend fun ApplicationCall.renderTemplate(templatePath: String, data: Map<String, Any>): String {
    // retrieves the pebble engine from application attributes
    val engine = application.attributes[AttributeKey<PebbleEngine>("pebble")]
    
    // creates a writer to capture the output
    val writer = StringWriter()
    
    // evaluates the template with the provided data
    engine.getTemplate(templatePath).evaluate(writer, data)
    
    // returns the resulting string
    return writer.toString()
}
