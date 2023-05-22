package tw.edu.finalproject.ui.setting.user_setting

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.launch
import tw.edu.finalproject.MainActivity
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.FragmentUserBinding
import tw.edu.finalproject.ui.setting.SettingViewModel
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.yuuzu.ViewHelper

class UserFragment : Fragment(R.layout.fragment_user) {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingViewModel

    private lateinit var dialogHelper: DialogHelper

    private var selected = ""
    private val genderList = listOf("男", "女")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserBinding.bind(view)
        viewModel = (activity as MainActivity).settingViewModel

        initView()
        initData()
        initButton()
    }

    private fun initButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.submitButton.setOnClickListener {
            val name = binding.settingUserNameInput.text.toString()
            val email = binding.settingUserEmail.text.toString()
            val phone = binding.settingUserPhone.text.toString()
            val gender = binding.settingUserGender.text.toString()
            val password = binding.settingUserPassword.text.toString()
            val passwordConfirm = binding.settingUserPasswordConfirm.text.toString()

            val result = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && passwordConfirm.isNotBlank()
            if (!result) {
                dialogHelper.alertDialog("錯誤", "請輸入完整資料!", 1, true)
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                dialogHelper.alertDialog("錯誤", "密碼不一致!", 1, true)
                return@setOnClickListener
            }

            selected = if (gender == "男") "M" else "F"

            // send Data
            lifecycleScope.launch {
                val isSuccess = viewModel.updateUserDetail(name, email, phone, selected, password)
                viewModel.getUserDetail()

                if (isSuccess.isNotBlank()) {
                    dialogHelper.alertDialog("錯誤", "修改失敗!", 1, false, "確定", object :
                        DialogHelper.DialogPositiveListener {
                        override fun onPositive(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                        }
                    })

                } else {
                    dialogHelper.alertDialog("成功", "", 2, false, "確定", object :
                        DialogHelper.DialogPositiveListener {
                        override fun onPositive(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                            findNavController().popBackStack()
                        }
                    })
                }
            }
        }

        binding.settingUserGender.setOnItemClickListener { parent, _, position, _ ->
            selected = parent.getItemAtPosition(position).toString()
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            viewModel.userState.collect {
                when (it) {
                    is SettingViewModel.UserState.Success -> {
                        binding.settingUserNameInput.setText(it.data.user_name)
                        binding.settingUserEmail.setText(it.data.user_mail)
                        binding.settingUserPhone.setText(it.data.user_phone)

                        if (it.data.user_gender == "F") binding.settingUserGender.setText(genderList[1], false)
                        else binding.settingUserGender.setText(genderList[0], false)

                        selected = binding.settingUserGender.text.toString()
                    }
                    is SettingViewModel.UserState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }
                    is SettingViewModel.UserState.Waiting -> {
                        Log.e("TAG", "initData: Waiting")
                    }
                }
            }
        }
    }

    private fun initView() {
        ViewHelper(requireActivity()).setupUI(binding.root)
        dialogHelper = DialogHelper(requireActivity())

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, genderList)
        binding.settingUserGender.setAdapter(arrayAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}