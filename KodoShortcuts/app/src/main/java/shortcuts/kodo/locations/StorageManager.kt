package shortcuts.kodo.locations

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class LocationRule(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val actions: List<String>,
    val enabled: Boolean = true
)

object StorageManager {

    private const val PREFS = "kodo_shortcuts"
    private const val KEY = "locations"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getRules(context: Context): List<LocationRule> {
        return try {
            val array = JSONArray(
                prefs(context).getString(KEY, "[]")
            )

            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                val actions = obj.getJSONArray("actions")

                LocationRule(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    latitude = obj.getDouble("latitude"),
                    longitude = obj.getDouble("longitude"),
                    radius = obj.getDouble("radius").toFloat(),
                    actions = List(actions.length()) {
                        actions.getString(it)
                    },
                    enabled = obj.optBoolean("enabled", true)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun write(
        context: Context,
        rules: List<LocationRule>
    ) {
        val array = JSONArray()

        rules.forEach { rule ->
            val obj = JSONObject()

            obj.put("id", rule.id)
            obj.put("name", rule.name)
            obj.put("latitude", rule.latitude)
            obj.put("longitude", rule.longitude)
            obj.put("radius", rule.radius)
            obj.put("enabled", rule.enabled)
            obj.put("actions", JSONArray(rule.actions))

            array.put(obj)
        }

        prefs(context).edit()
            .putString(KEY, array.toString())
            .apply()
    }

    fun saveRule(context: Context, rule: LocationRule) {
        val rules = getRules(context).toMutableList()

        rules.removeAll { it.id == rule.id }
        rules.add(rule)

        write(context, rules)
    }

    fun deleteRule(context: Context, id: String) {
        write(
            context,
            getRules(context).filterNot { it.id == id }
        )
    }

    fun createRule(
        name: String,
        lat: Double,
        lng: Double,
        radius: Float,
        actions: List<String>,
        id: String = UUID.randomUUID().toString(),
        enabled: Boolean = true
    ) = LocationRule(
        id = id,
        name = name,
        latitude = lat,
        longitude = lng,
        radius = radius,
        actions = actions,
        enabled = enabled
    )
}