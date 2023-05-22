package tw.edu.finalproject.ui.home.view_model

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
import tw.edu.finalproject.ui.home.model.RemindData
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class HomeViewModel(
    app: Application
): ViewModel() {
    private val yuuzuApi = YuuzuApi(app)
    private val yuuzuShare = YuuzuShare(app)
    private val group = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)!!

    private val _state = MutableStateFlow<HomeState>(HomeState.Empty)
    val state = _state.asStateFlow()

    init {
        getRemindData()
    }

    fun getRemindData() {
        viewModelScope.launch {
            yuuzuApi.api(Request.Method.GET, Constants.API_REMIND_ONE + "?user_group_id=${group.user_group_id}&filter=all", object :
                YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    try {
                        val jsonObject = JSONObject(data)
                        val remindDataList = mutableListOf<RemindData>()
                        val status = jsonObject.getString("Status")
                        Log.e("TAG", "onSuccess: $status")
                        if (status == "成功") {
                            val jsonArray = jsonObject.getJSONArray("remind_data")
                            Log.e("TAG", "onSuccess: ${jsonObject.getJSONArray("remind_data")}")
                            for (i in 0 until jsonArray.length()) {
                                val remindDatas = jsonArray.getJSONObject(i)
                                val remindData = RemindData(
                                    remind_name = remindDatas.getString("remind_name"),
                                    remind_time = remindDatas.getString("remind_time"),
                                    status = remindDatas.getString("status"),
                                    remind_record_file_path = remindDatas.getString("remind_record_file_path")
                                )
                                remindDataList.add(remindData)
                            }
                        }
                        Log.e("TAG", "onSuccess: $remindDataList")
                        _state.value = HomeState.Success(remindDataList)

                    } catch (e: Exception) {
                        _state.value = HomeState.Error(e.message!!)
                    }
                }

                override fun onError(error: VolleyError) {
                    if (error.networkResponse != null) {
                        _state.value = HomeState.Error(error.networkResponse.data.toString())
                    } else {
                        _state.value = HomeState.Error("Unknown Error")
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })
        }
    }

    sealed class HomeState {
        object Empty: HomeState()
        data class Success(val data: MutableList<RemindData>): HomeState()
        data class Error(val message: String): HomeState()
    }
}