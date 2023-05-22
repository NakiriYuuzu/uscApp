package tw.edu.finalproject.ui.notification.view_model

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
import tw.edu.finalproject.ui.notification.model.CareReceiver
import tw.edu.finalproject.ui.notification.model.NotificationDto
import tw.edu.finalproject.ui.notification.model.RemindModel
import tw.edu.finalproject.ui.notification.model.RemindType
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class NotificationViewModel(
    app: Application,
): ViewModel() {

    val notificationData = MutableStateFlow<List<NotificationDto>>(emptyList())

    private val _state = MutableStateFlow<NotificationState>(NotificationState.Waiting)
    val state = _state.asStateFlow()

    private val yuuzuApi = YuuzuApi(app)
    private val yuuzuShare = YuuzuShare(app)
    private val userGroupId = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.user_group_id
    private val groupId = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.group_id

    init {
        viewModelScope.launch {
            getAllNotification() // FIXME: 之後需要改成今天
        }
    }

    fun getTodayNotification() {
        yuuzuApi.api(
            Request.Method.GET,
            "${Constants.API_REMIND}?user_group_id=${userGroupId}&filter=today",
            object : YuuzuApi.YuuzuApiListener {

                override fun onSuccess(data: String) {
                    val list = mutableListOf<NotificationDto>()
                    try {
                        val jsonObject = JSONObject(data)
                        val jsonArray = jsonObject.getJSONArray("remind_data")

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            list.add(
                                NotificationDto(
                                    remind_name = item.getString("remind_name"),
                                    remind_time = item.getString("remind_time"),
                                    status = item.getInt("status"),
                                )
                            )
                        }

                        _state.value = NotificationState.Success(list)

                        notificationData.value = list

                    } catch (e: Exception) {
                        _state.value = NotificationState.Error(e.message.toString())
                    }
                }

                override fun onError(error: VolleyError) {
                    try {
                        val jsonObject =
                            JSONObject(String(error.networkResponse.data, Charsets.UTF_8))
                        val message = jsonObject.getString("Message")
                        _state.value = NotificationState.Error(message)
                    } catch (e: Exception) {
                        _state.value = NotificationState.Error(e.message.toString())
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })

        Log.e("TAG", "getTodayNotification: ")
    }

    fun getAllNotification() {
        yuuzuApi.api(
            Request.Method.GET,
            "${Constants.API_REMIND}?user_group_id=${userGroupId}&filter=all",
            object : YuuzuApi.YuuzuApiListener {

                override fun onSuccess(data: String) {
                    val list = mutableListOf<NotificationDto>()
                    try {
                        val jsonObject = JSONObject(data)
                        val jsonArray = jsonObject.getJSONArray("remind_data")

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            list.add(
                                NotificationDto(
                                    remind_name = item.getString("remind_name"),
                                    remind_time = item.getString("remind_time"),
                                    status = item.getInt("status"),
                                )
                            )
                        }

                        _state.value = NotificationState.Success(list)
                        notificationData.value = list

                    } catch (e: Exception) {
                        _state.value = NotificationState.Error(e.message.toString())
                    }
                }

                override fun onError(error: VolleyError) {
                    try {
                        val jsonObject =
                            JSONObject(String(error.networkResponse.data, Charsets.UTF_8))
                        val message = jsonObject.getString("Message")
                        _state.value = NotificationState.Error(message)
                    } catch (e: Exception) {
                        _state.value = NotificationState.Error(e.message.toString())
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })

        Log.e("TAG", "getAllNotification: ")
    }

    private val _remindModel = MutableStateFlow<RemindState>(RemindState.Waiting)
    val remindModel = _remindModel.asStateFlow()

    fun getRemindModel(remindId: String) = viewModelScope.launch {
        yuuzuApi.api(Request.Method.POST, Constants.API_REMIND_FORMAT, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val jsonArray = jsonObject.getJSONArray("被照護者")
                    val userList = ArrayList<CareReceiver>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        userList.add(
                            CareReceiver(
                                user_id = item.getString("user_id"),
                                user_name = item.getString("user_name"),
                            )
                        )
                    }

                    val remindFormat = jsonObject.getJSONObject("remind_format")
                    val remindFormatList = remindFormat.keys().asSequence().toList()
                    // get string from between "
                    _remindModel.value = RemindState.Success(RemindModel(userList, remindFormatList))
                    Log.e("TAG", "onSuccess: $remindFormat | $remindFormatList")


                } catch (e: Exception) {
                    _remindModel.value = RemindState.Error(e.message.toString())
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    _remindModel.value = RemindState.Error(error.networkResponse.data.toString())
                } else {
                    _remindModel.value = RemindState.Error("No network")
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    "group_id" to groupId!!,
                    "remind_id" to remindId
                )
        })
    }

    private val _remindType = MutableStateFlow<RemindTypeState>(RemindTypeState.Waiting)
    val remindType = _remindType.asStateFlow()

    fun getRemindType() {
        viewModelScope.launch {
            yuuzuApi.api(Request.Method.GET, Constants.API_REMIND_FORMAT, object :
                YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    try {
                        val jsonObject = JSONObject(data)
                        val jsonArray = jsonObject.getJSONArray("remind_type")
                        val remindTypeList = ArrayList<RemindType>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            remindTypeList.add(
                                RemindType(
                                    remind_id = item.getString("remind_id"),
                                    remind_name = item.getString("remind_name"),
                                )
                            )
                        }
                        _remindType.value = RemindTypeState.Success(remindTypeList)
                    } catch (e: Exception) {
                        _remindType.value = RemindTypeState.Error(e.message.toString())
                    }
                }

                override fun onError(error: VolleyError) {
                    if (error.networkResponse != null) {
                        _remindType.value = RemindTypeState.Error(error.networkResponse.data.toString())
                    } else {
                        _remindType.value = RemindTypeState.Error("No network")
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })
        }
    }

    sealed class NotificationState {
        data class Success(val data: MutableList<NotificationDto>) : NotificationState()
        data class Error(val message: String) : NotificationState()
        object Waiting : NotificationState()
    }

    sealed class RemindState {
        data class Success(val data: RemindModel): RemindState()
        data class Error(val message: String): RemindState()
        object Waiting: RemindState()
    }

    sealed class RemindTypeState {
        data class Success(val data: List<RemindType>): RemindTypeState()
        data class Error(val message: String): RemindTypeState()
        object Waiting: RemindTypeState()
    }
}