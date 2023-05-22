package tw.edu.finalproject.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import tw.edu.finalproject.MainActivity
import tw.edu.finalproject.R
import tw.edu.finalproject.SecondaryActivity
import tw.edu.finalproject.databinding.FragmentSettingBinding
import tw.edu.finalproject.ui.login.LoginActivity
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuShare

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingViewModel

    private lateinit var dialogHelper: DialogHelper
    private lateinit var yuuzuShare: YuuzuShare

    private var settingList = mutableListOf<LoginDto>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingBinding.bind(view)
        viewModel = if (activity is MainActivity) (activity as MainActivity).settingViewModel
                    else (activity as SecondaryActivity).settingViewModel

        viewModel.getUserDetail()

        initView()
        initData()
        initButton()
    }

    private fun initButton() {
        // 照護者設定
        binding.editCaregiver.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_caregiverFragment)
            // requireActivity().startActivity(Intent(requireContext(), CaregiverActivity::class.java))
        }

        // notify send
        binding.sendNotification.setOnClickListener {
            viewModel.getUserGroupList()
            lifecycleScope.launch {
                viewModel.caregiverState.collect { collects ->
                    when (collects) {
                        is SettingViewModel.CaregiverState.Success -> {
                            val caregiverDataList = collects.data.filter { it.user_group_auth_name == "被照護者" }
                            val caregiverList = caregiverDataList.map { it.user_name }
                            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, caregiverList)

                            var selected = ""
                            var selectIndex = 0

                            dialogHelper.dropDownDialog("選擇通知被照護者", "請輸入通知内容", arrayAdapter, object :
                                DialogHelper.CustomDropInputListener {
                                override fun onPositiveClick(
                                    var1: View,
                                    dropDownDialog: AlertDialog,
                                    inputText: TextInputEditText
                                ) {
                                    val input = inputText.text.toString()
                                    if (selected.isNotBlank() && input.isNotBlank()) {
                                        val selectedUser = caregiverDataList[selectIndex]
                                        val groupId = selectedUser.user_id
                                        val email = selectedUser.user_mail
                                        val result = viewModel.sendNotify(groupId, email, input)

                                        if (result == "") dialogHelper.alertDialog("通知成功", "已成功通知被照護者", 2, true)
                                        else dialogHelper.alertDialog("通知失敗", result, 1, true)

                                    } else {
                                        dialogHelper.alertDialog("請輸入完整資料", "", 1, false)
                                    }

                                    dropDownDialog.dismiss()
                                }
                            }, object : DialogHelper.OnItemClickListener {
                                override fun onItemClick(
                                    adapterView: AdapterView<*>?,
                                    view: View?,
                                    i: Int,
                                    l: Long
                                ) {
                                    selected = adapterView?.getItemAtPosition(i).toString()
                                    selectIndex = i
                                }
                            })
                        }

                        is SettingViewModel.CaregiverState.Error -> {
                            dialogHelper.alertDialog("錯誤", collects.message, 1, false)
                        }

                        is SettingViewModel.CaregiverState.Waiting -> {
                            dialogHelper.showLoadingDialog("加載中", true)
                        }
                    }
                }
            }
        }

        // 個人資料
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_userFragment)
        }

        // 切換照護者
        binding.switchGroup.setOnClickListener {
            val groupList = settingList
            val list = settingList.map { it.group_name }

            var selected = 0
            var onSelected = ""

            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, list)
            dialogHelper.dropDownDialog("切換照護者", arrayAdapter, object : DialogHelper.CustomPositiveListener {
                override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                    if (onSelected.isNotBlank()) {
                        // save data
                        val group = groupList[selected]
                        yuuzuShare.put(group, YStore.YS_CURRENT_GROUP)

                        if (group.user_group_auth_name == "被照護者") {
                            requireActivity().finish()
                            requireActivity().startActivity(Intent(requireContext(), SecondaryActivity::class.java))
                        } else {
                            requireActivity().finish()
                            requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
                        }

                        // dismiss
                        dropDownDialog.dismiss()

                    } else {
                        dialogHelper.alertDialog("錯誤", "請選擇照護者!", 1, true)
                    }
                }
            }, object :
                DialogHelper.OnItemClickListener {
                override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                    selected = i
                    onSelected = adapterView?.getItemAtPosition(i).toString()
                }
            })
        }

        // 登出
        binding.logoutButton.setOnClickListener {
            dialogHelper.alertDialog("確認要登出嗎？", "", 3, false, "確認", "取消", object :
                DialogHelper.DialogListener {
                override fun onPositive(dialog: SweetAlertDialog) {
                    dialog.dismiss()
                    yuuzuShare.clear()
                    requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }

                override fun onNegative(dialog: SweetAlertDialog) {
                    dialog.dismiss()
                }
            })
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            viewModel.settingState.collect {
                when (it) {
                    is SettingViewModel.SettingState.Success -> {
                        settingList = it.data
                    }
                    is SettingViewModel.SettingState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }
                    is SettingViewModel.SettingState.Waiting -> {
                        dialogHelper.showLoadingDialog("請稍後...", true)
                    }
                }
            }
        }
    }

    private fun initView() {
        yuuzuShare = YuuzuShare(requireContext())
        dialogHelper = DialogHelper(requireActivity())

        settingList = mutableListOf()

        if (yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP) != null) {
            val currentGroup = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)
            if (currentGroup?.user_group_auth_name == "被照護者") {
                binding.careGiverView.visibility = View.GONE
            }
        }

        val userName = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.user_name
        if (userName?.length!! > 2) {
            binding.settingUserIconName.text = userName.substring(userName.length - 2)
        } else {
            binding.settingUserIconName.text = userName
        }

        binding.settingUserName.text = userName
        binding.settingUserAuth.text = "${yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.user_group_auth_name}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}