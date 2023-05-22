package tw.edu.finalproject.yuuzu

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class YuuzuApi(ctx: Context) {
    private val requestQueue: RequestQueue
    private val yuuzuShare = YuuzuShare(ctx)

    init {
        requestQueue = Volley.newRequestQueue(ctx)
    }

    fun api(method: Int, url: String, yuuzuApiListener: YuuzuApiListener) {
        val request: StringRequest = object : StringRequest(method, url, { response ->

            val result = String(response.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)

            yuuzuApiListener.onSuccess(result)

        }, { error ->
            yuuzuApiListener.onError(error)
        }) {
            override fun getParams(): Map<String, String> {
                return yuuzuApiListener.params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                if (yuuzuShare.get<String>("Token") != null) headers["Token"] = "${yuuzuShare.get<String>("Token")}"
                return headers
            }
        }

        requestQueue.add(request)
    }

    suspend fun suspendApi(method: Int, url: String, yuuzuApiListener: ViewModelYuuzuApiListener) {
        val request: StringRequest = object : StringRequest(method, url, { response ->
            val result = String(response.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
            suspend {
                yuuzuApiListener.onSuccess(result)
            }
        }, { error ->
            suspend {
                yuuzuApiListener.onError(error)
            }
        }) {
            override fun getParams(): Map<String, String> {
                return yuuzuApiListener.params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }
        }

        requestQueue.add(request)
    }

    fun flushAPI() {
        requestQueue.cancelAll { true }
        requestQueue.stop()
    }

    interface YuuzuApiListener {
        fun onSuccess(data: String)
        fun onError(error: VolleyError)
        val params: Map<String, String>
    }

    interface ViewModelYuuzuApiListener {
        suspend fun onSuccess(data: String)
        suspend fun onError(error: VolleyError)
        val params: Map<String, String>
    }
}