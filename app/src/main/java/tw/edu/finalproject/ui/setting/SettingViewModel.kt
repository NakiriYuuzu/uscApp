package tw.edu.finalproject.ui.setting

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.VolleyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.ui.setting.care_giver.model.CaregiverDto
import tw.edu.finalproject.ui.setting.user_setting.model.UserDto
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class SettingViewModel(
    app: Application
) : ViewModel() {
    private val _settingState = MutableStateFlow<SettingState>(SettingState.Waiting)
    val settingState = _settingState.asStateFlow()

    private val yuuzuShare = YuuzuShare(app)
    private val yuuzuApi = YuuzuApi(app)

    init {
        getGroupList()
        getUserDetail()
        getUserGroupList()
    }

    fun getGroupList() {
        yuuzuApi.api(Request.Method.GET, Constants.API_GROUP_LIST, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val jsonArray = jsonObject.getJSONArray("groups")
                    val list = mutableListOf<LoginDto>()

                    for (i in 0 until jsonArray.length()) {
                        val group = jsonArray.getJSONObject(i)
                        list.add(
                            LoginDto(
                            user_group_id = group.getString("user_group_id"),
                            group_id = group.getString("group_id"),
                            group_name = group.getString("group_name"),
                            group_number = group.getString("group_number"),
                            user_name = group.getString("user_name"),
                            user_group_auth_id = group.getString("user_group_auth_id"),
                            user_group_auth_name = group.getString("user_group_auth_name")
                        ))
                    }

                    _settingState.value = SettingState.Success(list)

                } catch (e: Exception) {
                    _settingState.value = SettingState.Error(e.message.toString())
                }
            }

            override fun onError(error: VolleyError) {
                try {
                    val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                    _settingState.value = SettingState.Error(jsonObject.getString("Message"))
                } catch (e: Exception) {
                    _settingState.value = SettingState.Error(e.message.toString())
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    sealed class SettingState {
        data class Success(val data: MutableList<LoginDto>) : SettingState()
        data class Error(val message: String) : SettingState()
        object Waiting : SettingState()
    }

    private val _userState = MutableStateFlow<UserState>(UserState.Waiting)
    val userState = _userState.asStateFlow()

    fun getUserDetail() {
        viewModelScope.launch {
            yuuzuApi.api(Request.Method.GET, Constants.API_USER_DETAIL, object : YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    try {
                        val jsonObject = JSONObject(data)
                        val userData = UserDto(
                            user_name = jsonObject.getString("user_name"),
                            user_mail = jsonObject.getString("user_mail"),
                            user_phone = jsonObject.getString("user_phone"),
                            user_gender = jsonObject.getString("user_gender"),
                            user_password = jsonObject.getString("user_password"),
                        )

                        _userState.value = UserState.Success(userData)

                    } catch (e: Exception) {
                        _userState.value = UserState.Error(e.message.toString())
                    }
                }

                override fun onError(error: VolleyError) {
                    try {
                        val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                        _userState.value = UserState.Error(jsonObject.getString("Message"))
                    } catch (e: Exception) {
                        _userState.value = UserState.Error(e.message.toString())
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })
        }
    }

    fun updateUserDetail(
        user_name: String,
        user_mail: String,
        user_phone: String,
        user_gender: String,
        user_password: String,
    ): String {
        var result = ""
        viewModelScope.launch {
            yuuzuApi.api(Request.Method.PUT, Constants.API_USER_DETAIL, object :
                YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    result = "Success"
                }

                override fun onError(error: VolleyError) {
                    result = try {
                        val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                        jsonObject.getString("Message")
                    } catch (e: Exception) {
                        e.message.toString()
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf(
                        "user_name" to user_name,
                        "user_mail" to user_mail,
                        "user_phone" to user_phone,
                        "user_gender" to user_gender,
                        "user_password" to user_password,
                    )
            })
        }

        return result
    }

    sealed class UserState {
        data class Success(val data: UserDto) : UserState()
        data class Error(val message: String) : UserState()
        object Waiting : UserState()
    }

    private val _caregiverState = MutableStateFlow<CaregiverState>(CaregiverState.Waiting)
    val caregiverState = _caregiverState.asStateFlow()

    fun getUserGroupList() {
        viewModelScope.launch {
            val groupId = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.group_id
            yuuzuApi.api(Request.Method.GET, "${Constants.API_USER_GROUP}?group_id=$groupId", object :
                YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    val groupList = mutableListOf<CaregiverDto>()

                    try {
                        val jsonObject = JSONObject(data)
                        val jsonArray = jsonObject.getJSONArray("group_member")
                        for (i in 0 until jsonArray.length()) {
                            val group = jsonArray.getJSONObject(i)
                            val caregiverDto = CaregiverDto(
                                user_group_id = group.getString("user_group_id"),
                                user_id = group.getString("user_id"),
                                user_name = group.getString("user_name"),
                                user_group_auth_name = group.getString("user_group_auth_name"),
                                user_mail = group.getString("user_mail"),
                            )

                            groupList.add(caregiverDto)
                        }

                        _caregiverState.value = CaregiverState.Success(groupList)

                    } catch (e: Exception) {
                        _caregiverState.value = CaregiverState.Error(e.message.toString())
                    }
                }

                override fun onError(error: VolleyError) {
                    try {
                        val errorBody = String(error.networkResponse.data, Charsets.UTF_8)
                        val jsonObject = JSONObject(errorBody)
                        _caregiverState.value = CaregiverState.Error(jsonObject.getString("Status"))

                    } catch (e: Exception) {
                        _caregiverState.value = CaregiverState.Error(e.message.toString())
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf()
            })
        }
    }

    fun sendNotify(id: String, mail: String, msg: String): String {
        var result = ""
        yuuzuApi.api(Request.Method.POST, Constants.API_SEND_REMIND, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                result = "Success"
            }

            override fun onError(error: VolleyError) {
                result = try {
                    val errorBody = String(error.networkResponse.data, Charsets.UTF_8)
                    val jsonObject = JSONObject(errorBody)
                    jsonObject.getString("Status")

                } catch (e: Exception) {
                    e.message.toString()
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    "user_mail" to mail,
                    "group_id" to id,
                    "message" to msg
                )
        })

    return result
    }

    sealed class CaregiverState {
        data class Success(val data: MutableList<CaregiverDto>) : CaregiverState()
        data class Error(val message: String) : CaregiverState()
        object Waiting : CaregiverState()
    }
}