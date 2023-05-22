package tw.edu.finalproject.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import tw.edu.finalproject.databinding.ActivityRegisterBinding
import tw.edu.finalproject.ui.login.LoginActivity
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.yuuzu.ViewHelper
import tw.edu.finalproject.yuuzu.YuuzuApi

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var dialogHelper: DialogHelper
    private lateinit var yuuzuApi: YuuzuApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initButton()
    }

    private fun register() {
        dialogHelper.showLoadingDialog("註冊中...", false)

        yuuzuApi.api(Request.Method.POST, Constants.API_REGISTER, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                dialogHelper.hideLoadingDialog()
            }

            override fun onError(error: VolleyError) {
                dialogHelper.hideLoadingDialog()
            }

            override val params: Map<String, String>
                get() = mapOf(

                )
        })
    }

    private fun verifyForm(): String {
        if (binding.inputEmail.text.toString().isEmpty()) return "請輸入電子郵件"

        if (binding.inputPassword.text.toString().isEmpty()) return "請輸入密碼"

        if (binding.retypePassword.text.toString().isEmpty()) return "請再次輸入密碼"

        if (binding.inputPassword.text.toString() != binding.retypePassword.text.toString()) return "密碼不一致"

        return ""
    }

    private fun initButton() {
        binding.registerHere.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        binding.registerButton.setOnClickListener {
            if (verifyForm().isEmpty()) register()
            else dialogHelper.alertDialog("輸入錯誤！", verifyForm(), SweetAlertDialog.ERROR_TYPE, true)
        }
    }

    private fun initView() {
        ViewHelper(this).setupUI(binding.root)
        yuuzuApi = YuuzuApi(this)
        dialogHelper = DialogHelper(this)
    }
}