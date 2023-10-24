import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.FileReader
import java.net.InetSocketAddress


fun main(args: Array<String>) {
    println("### Make Simple WAS ###")

    val HOST = "localhost"
    val PORT = 8080

    val server = HttpServer.create(InetSocketAddress(HOST,PORT), 0)

    server.createContext("/") {
        val body = JSONObject()
        try {
            //method validation
            check(isValidMethod(it.requestMethod)) { "invalid method" }

            //make responseBody
            body["message"] = "default"
            sendResponse(body, it)
        } catch (e: Exception) {
            e.printStackTrace()
            body["message"] = e.message
            sendResponse(body, it, 500)
        }
    }

    server.createContext("/post-count") {
        val body = JSONObject()
        try {
            check(isValidMethod(it.requestMethod)) { "invalid method" }
            val path = "${System.getProperty("user.dir")}/data/user.json"
            val parser = JSONParser()
            val users: JSONArray = parser.parse(FileReader(path)) as JSONArray

            body["all_post_count"] = users.sumOf { user ->
                user as JSONObject
                user["post_count"].toString().toInt()
            }
            sendResponse(body, it)
        } catch (e: Exception) {
            e.printStackTrace()
            body["message"] = e.message
            sendResponse(body, it, 500)
        }
    }

    server.start()
}

fun isValidMethod(method: String) : Boolean = method=="GET"

fun sendResponse(body: JSONObject, ex: HttpExchange, code: Int = 200) {
    val responseBytes = body.toString().toByteArray()
    ex.responseHeaders.set("content-type", "application/json")
    ex.sendResponseHeaders(code, responseBytes.size.toLong())
    val os = ex.responseBody
    os.write(responseBytes)
    os.close()
}