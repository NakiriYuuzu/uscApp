package tw.edu.finalproject.ui.notification.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.VolleyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.ui.notification.model.RemindModel
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class AddNotificationViewModel(
    app: Application,
) : ViewModel() {
    private val yuuzuApi = YuuzuApi(app)
    private val yuuzuShare = YuuzuShare(app)

    private val group = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)!!
    private val remindModel = yuuzuShare.get<RemindModel>(YStore.YS_REMIND_MODEL)!!
    private val userId = remindModel.careReceiverList!![0].user_id
    private val remindId = yuuzuShare.get<String>(YStore.YS_REMIND_ID)!!

    private val _state = MutableStateFlow<AddRemindState>(AddRemindState.Empty)

    fun addRemind(time: String, data: String) {
        viewModelScope.launch {
            val datas = mapOf(
                "group_id" to group.group_id,
                "user_id" to userId,
                "remind_id" to remindId,
                "remind_time" to time,
                "remind_format" to "$data"
            )
            Log.e("TAG", "addRemind: $datas")
            _state.value = AddRemindState.Loading
            yuuzuApi.api(Request.Method.POST, Constants.API_REMIND, object : YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    _state.value = AddRemindState.Success
                    Log.e("TAG", "onSuccess: $data")
                }

                override fun onError(error: VolleyError) {
                    if (error.networkResponse != null) {
                        _state.value = AddRemindState.Error(error.networkResponse.data.toString())
                    } else {
                        _state.value = AddRemindState.Error("Unknown Error")
                    }
                }

                override val params: Map<String, String>
                    get() = datas
            })
        }
    }

    sealed class AddRemindState {
        object Loading : AddRemindState()
        object Success : AddRemindState()
        data class Error(val message: String) : AddRemindState()
        object Empty : AddRemindState()
    }
}