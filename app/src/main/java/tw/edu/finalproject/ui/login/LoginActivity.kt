package tw.edu.finalproject.ui.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import tw.edu.finalproject.MainActivity
import tw.edu.finalproject.R
import tw.edu.finalproject.SecondaryActivity
import tw.edu.finalproject.databinding.ActivityLoginBinding
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var dialogHelper: DialogHelper
    private lateinit var yuuzuShare: YuuzuShare
    private lateinit var yuuzuApi: YuuzuApi

    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        // startActivity(Intent(this, MainActivity::class.java))
        initButton()
    }

    private fun joinGroup() {
        dialogHelper.inputDialog("加入群組", "請輸入群組代號", object : DialogHelper.CustomTextListener {
            override fun onPositiveTextClick(
                var1: View,
                dialog: AlertDialog,
                inputText: TextInputEditText
            ) {
                dialog.dismiss()
                // TODO: 加入群組 POST TO BACKEND
            }

            override fun onNegativeTextClick(var1: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
    }

    private fun getGroupList() {
        yuuzuApi.api(Request.Method.GET, Constants.API_GROUP_LIST, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val jsonArray = jsonObject.getJSONArray("groups")
                    val groupList = mutableListOf<LoginDto>()
                    val list = mutableListOf<String>()
                    var selected = 0
                    var onSelected = ""

                    for (i in 0 until jsonArray.length()) {
                        val group = jsonArray.getJSONObject(i)
                        groupList.add(
                            LoginDto(
                                user_group_id = group.getString("user_group_id"),
                                group_id = group.getString("group_id"),
                                group_name = group.getString("group_name"),
                                group_number = group.getString("group_number"),
                                user_name = group.getString("user_name"),
                                user_group_auth_id = group.getString("user_group_auth_id"),
                                user_group_auth_name = group.getString("user_group_auth_name")
                            )
                        )

                        list.add(group.getString("group_name"))
                    }

                    arrayAdapter = ArrayAdapter(this@LoginActivity, R.layout.dialog_dropdown_item, list)
                    dialogHelper.dropDownDialog("請選擇要進入的群組", arrayAdapter, object:
                        DialogHelper.CustomPositiveListener {
                        override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                            val group = groupList[selected]
                            if (onSelected.isNotBlank() && onSelected != "") {

                                if (group.user_group_auth_name == "被照護者") {
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            SecondaryActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }

                                // Save group info
                                yuuzuShare.put(group, YStore.YS_CURRENT_GROUP)
                                dialogHelper.hideLoadingDialog()
                                dropDownDialog.cancel()
                            } else {
                                dialogHelper.alertDialog("請選擇要進入的群組", "", 1, true)
                            }
                        }
                    }, object : DialogHelper.OnItemClickListener {
                        override fun onItemClick(
                            adapterView: AdapterView<*>?,
                            view: View?,
                            i: Int,
                            l: Long
                        ) {
                            selected = i
                            onSelected = adapterView?.getItemAtPosition(i).toString()
                        }
                    })

                } catch (e: Exception) {
                    dialogHelper.hideLoadingDialog()
                    dialogHelper.alertDialog("錯誤", e.message.toString(), 1, true)
                }
            }

            override fun onError(error: VolleyError) {
                try {
                    val responseBody = String(error.networkResponse.data, Charsets.UTF_8)
                    val jsonObject = JSONObject(responseBody)
                    dialogHelper.alertDialog("錯誤", jsonObject.getString("Message"), 1, true)
                } catch (e: Exception) {
                    dialogHelper.alertDialog("錯誤", e.message.toString(), 1, true)
                    dialogHelper.hideLoadingDialog()
                }

                dialogHelper.hideLoadingDialog()
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun login() {
        if (verifyLoginForm().isNotEmpty()) {
            dialogHelper.hideLoadingDialog()
            dialogHelper.alertDialog("輸入錯誤！", verifyLoginForm(), SweetAlertDialog.ERROR_TYPE, true)
            return
        }

        yuuzuApi.api(Request.Method.POST, Constants.API_LOGIN, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.getString("Status")

                    if (status == "成功") {
                        val token = jsonObject.getString(YStore.YS_TOKEN)
                        yuuzuShare.put(token, YStore.YS_TOKEN)
                        yuuzuShare.put(binding.inputAccount.text.toString(), YStore.YS_ACCOUNT)
                        yuuzuShare.put(binding.inputPassword.text.toString(), YStore.YS_PASSWORD)
                        getGroupList()
                    } else {
                        when (val message = jsonObject.getString("Message")) {
                            "尚未加入群組" -> {
                                // TODO: 讓使用者輸入群組代號
                                joinGroup()

                            }
                            "帳號或密碼錯誤" -> {
                                dialogHelper.hideLoadingDialog()
                                dialogHelper.alertDialog("錯誤", message, 1, true)
                                yuuzuShare.remove(YStore.YS_ACCOUNT)
                                yuuzuShare.remove(YStore.YS_PASSWORD)

                            }
                            else -> {
                                dialogHelper.alertDialog("錯誤", message, 1, false, "前往", "取消", object :
                                    DialogHelper.DialogListener {
                                    override fun onPositive(dialog: SweetAlertDialog) {
                                        dialog.dismiss()
                                        // TODO: 跳轉到網頁的註冊頁面
                                        // direct to website
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://120.110.115.146:5000/register"))
                                        startActivity(browserIntent)
                                    }

                                    override fun onNegative(dialog: SweetAlertDialog) {
                                        dialog.dismiss()
                                    }
                                })
                            }
                        }
                    }

                } catch (e: Exception) {
                    dialogHelper.alertDialog("錯誤!", e.message.toString(), SweetAlertDialog.ERROR_TYPE, true)
                    dialogHelper.hideLoadingDialog()
                }

                dialogHelper.hideLoadingDialog()
            }

            override fun onError(error: VolleyError) {
                try {
                    val responseBody = String(error.networkResponse.data, Charsets.UTF_8)
                    val jsonObject = JSONObject(responseBody)
                    dialogHelper.alertDialog(getString(R.string.loginFail), jsonObject.getString("Message"), SweetAlertDialog.ERROR_TYPE, true)

                } catch (e: Exception) {
                    dialogHelper.alertDialog(getString(R.string.loginFail), e.message.toString(), SweetAlertDialog.ERROR_TYPE, true)
                    dialogHelper.hideLoadingDialog()
                }
                dialogHelper.hideLoadingDialog()
            }

            override val params: Map<String, String>
                get() = mapOf(
                    YStore.YS_ACCOUNT to binding.inputAccount.text.toString(),
                    YStore.YS_PASSWORD to binding.inputPassword.text.toString()
                )
        })
    }

    private fun verifyLoginForm(): String {
        var result = ""
        val acc = binding.inputAccount.text.toString()
        val pwd = binding.inputPassword.text.toString()

        if (acc.isEmpty()) result += getString(R.string.nullAccount)
        if (pwd.isEmpty()) result += getString(R.string.nullPassword)

        return result
    }

    private fun initData() {
        if (yuuzuShare.get<String>(YStore.YS_ACCOUNT).isNullOrEmpty()) return
        if (yuuzuShare.get<String>(YStore.YS_PASSWORD).isNullOrEmpty()) return
        binding.inputAccount.setText("${yuuzuShare.get<String>(YStore.YS_ACCOUNT)}")
        binding.inputPassword.setText("${yuuzuShare.get<String>(YStore.YS_PASSWORD)}")
    }

    private fun initButton() {
        binding.loginButton.setOnClickListener {
             dialogHelper.showLoadingDialog("登入中...", false)
             login()
        }
    }

    private fun initView() {
        splashScreen()
        ViewHelper(this).setupUI(binding.root)
        dialogHelper = DialogHelper(this)
        yuuzuShare = YuuzuShare(this)
        yuuzuApi = YuuzuApi(this)

        initData()
    }

    private fun splashScreen() {
        binding.splashLogo.visibility = View.VISIBLE
        binding.splashLogo.fadeOut(1000L, 0L)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashLogo.visibility = View.GONE
            binding.logo.visibility = View.VISIBLE
            binding.loginPage.visibility = View.VISIBLE
            binding.logo.fadeIn(1000L, 0L)
            binding.loginPage.fadeIn(1000L, 0L)
        }, 1000)
    }
}