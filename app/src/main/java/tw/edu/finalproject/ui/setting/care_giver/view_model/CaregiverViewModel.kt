package tw.edu.finalproject.ui.setting.care_giver.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.VolleyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class CaregiverViewModel(
    app: Application
) : ViewModel() {

    private val _state = MutableStateFlow<CaregiverState>(CaregiverState.Loading)
    val state = _state.asStateFlow()

    private val yuuzuShare = YuuzuShare(app)
    private val yuuzuApi = YuuzuApi(app)

    init {
        getCaregiverList()
    }

    fun getCaregiverList() {
        viewModelScope.launch {
            if (yuuzuShare.isValueExist<LoginDto>(YStore.YS_CURRENT_GROUP)) {
                val currentGroup = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)

                yuuzuApi.api(Request.Method.GET, "${Constants.API_USER_GROUP}?group_id=${currentGroup?.group_id}", object :
                    YuuzuApi.YuuzuApiListener {
                    override fun onSuccess(data: String) {
                        Log.e("TAG", "initViewModel: $data")
                        _state.value = CaregiverState.Success(data)
                    }

                    override fun onError(error: VolleyError) {
                        try {
                            val errorBody = String(error.networkResponse.data, Charsets.UTF_8)
                            val jsonObject = JSONObject(errorBody)
                            _state.value = CaregiverState.Error(jsonObject.getString("Status"))

                        } catch (e: Exception) {
                            _state.value = CaregiverState.Error(e.message.toString())
                        }
                    }

                    override val params: Map<String, String>
                        get() = mapOf()
                })
            }
        }
    }

    sealed class CaregiverState {
        object Loading : CaregiverState()
        data class Success(val data: String) : CaregiverState()
        data class Error(val error: String) : CaregiverState()
    }
}