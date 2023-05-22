package tw.edu.finalproject.ui.setting.care_giver

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject
import tw.edu.finalproject.MainActivity
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.FragmentCaregiverBinding
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.ui.setting.care_giver.adapter.CaregiverAdapter
import tw.edu.finalproject.ui.setting.care_giver.model.AuthDto
import tw.edu.finalproject.ui.setting.care_giver.model.CaregiverDto
import tw.edu.finalproject.ui.setting.care_giver.view_model.CaregiverViewModel
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuApi
import tw.edu.finalproject.yuuzu.YuuzuShare

class CaregiverFragment : Fragment(R.layout.fragment_caregiver) {

    private var _binding: FragmentCaregiverBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CaregiverViewModel
    private lateinit var caregiverAdapter: CaregiverAdapter

    private lateinit var share: YuuzuShare
    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var dialogHelper: DialogHelper

    private lateinit var caregiverList: MutableList<CaregiverDto>
    private lateinit var authList: MutableList<AuthDto>
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var groupId = ""
    private var currentAuth = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCaregiverBinding.bind(view)
        viewModel = (activity as MainActivity).caregiverViewModel

        initView()
        initButton()
        setupRecyclerView()
        initData()
    }

    private fun setupRecyclerView() {
        caregiverAdapter = CaregiverAdapter(currentAuth, object : CaregiverAdapter.OnEditClickListener {
            override fun onEditClick(position: Int) {
                // change to edit fragment
                var selectIndex = 0
                var selected = ""

                dialogHelper.dropDownDialog("編輯權限", arrayAdapter, object :
                    DialogHelper.CustomPositiveListener {
                    override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                        if (selected.isNotBlank()) {
                            dialogHelper.alertDialog("確認要修改權限嗎？", "", 3, false, "確認", "取消", object :
                                DialogHelper.DialogPositiveListener, DialogHelper.DialogListener {
                                override fun onPositive(dialog: SweetAlertDialog) {
                                    yuuzuApi.api(Request.Method.PUT, Constants.API_USER_GROUP, object :
                                        YuuzuApi.YuuzuApiListener {
                                        override fun onSuccess(data: String) {
                                            viewModel.getCaregiverList()

                                            dialogHelper.alertDialog("修改成功", "", 2, false, "確認", object :
                                                DialogHelper.DialogPositiveListener {
                                                override fun onPositive(dialog: SweetAlertDialog) {
                                                    dialog.dismiss()
                                                    initData()
                                                }
                                            })
                                        }

                                        override fun onError(error: VolleyError) {
                                            try {
                                                val jsonObject = JSONObject(String(error.networkResponse.data))
                                                val message = jsonObject.getString("Message")
                                                dialogHelper.alertDialog("刪除照/被照護者", message, 1, true)
                                            } catch (e: Exception) {
                                                Log.e("TAG", "onError: ${e.message}")
                                            }
                                        }

                                        override val params: Map<String, String>
                                            get() = mapOf(
                                                "user_group_id" to caregiverList[position].user_group_id,
                                                "user_group_auth_id" to authList[selectIndex].user_group_auth_id
                                            )
                                    })

                                    dialog.dismiss()
                                    dropDownDialog.dismiss()
                                }

                                override fun onNegative(dialog: SweetAlertDialog) {
                                    dialog.dismiss()
                                }
                            })
                        } else {
                            dialogHelper.alertDialog("請選擇權限", "", 1, false)
                        }
                    }
                }, object : DialogHelper.OnItemClickListener {
                    override fun onItemClick(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        i: Int,
                        l: Long
                    ) {
                        selectIndex = i
                        selected = adapterView?.getItemAtPosition(i).toString()
                    }
                })

            }
        }, object : CaregiverAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                // delete it
                dialogHelper.alertDialog("刪除照/被照護者", "確定要刪除此照/被照護者嗎？", 3, false, "確認", "取消", object :
                    DialogHelper.DialogListener {
                    override fun onPositive(dialog: SweetAlertDialog) {
                        yuuzuApi.api(Request.Method.DELETE, "${Constants.API_USER_GROUP}?user_group_id=${caregiverList[position].user_group_id}", object :
                            YuuzuApi.YuuzuApiListener {
                            override fun onSuccess(data: String) {
                                viewModel.getCaregiverList()

                                // delete caregiverList[position]
                                caregiverList[position].let {
                                    caregiverList.remove(it)
                                }

                                dialogHelper.alertDialog("刪除照/被照護者", "刪除成功", 2, false, "確認", object :
                                    DialogHelper.DialogPositiveListener {
                                    override fun onPositive(dialog: SweetAlertDialog) {
                                        dialog.dismiss()
                                        initData()
                                    }
                                })
                            }

                            override fun onError(error: VolleyError) {
                                try {
                                    val jsonObject = JSONObject(String(error.networkResponse.data, Charsets.UTF_8))
                                    val message = jsonObject.getString("Message")
                                    dialogHelper.alertDialog("刪除照/被照護者", message, 1, true)
                                } catch (e: Exception) {
                                    Log.e("TAG", "onError: ${e.message}")
                                }
                            }

                            override val params: Map<String, String>
                                get() = mapOf()
                        })

                        dialog.dismissWithAnimation()
                    }

                    override fun onNegative(dialog: SweetAlertDialog) {
                        dialog.dismissWithAnimation()
                    }
                })
            }
        })

        binding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = caregiverAdapter
        }
    }

    private fun initData() {
        caregiverList = mutableListOf()
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is CaregiverViewModel.CaregiverState.Success -> {
                        Log.e("TAG", "initData: ${it.data}")
                        try {
                            val jsonObject = JSONObject(it.data)
                            val jsonArray = jsonObject.getJSONArray("group_member")
                            val authArray = jsonObject.getJSONArray("auth")

                            for (i in 0 until jsonArray.length()) {
                                val data = jsonArray.getJSONObject(i)
                                val caregiverDto = CaregiverDto(
                                    user_group_id = data.getString("user_group_id"),
                                    user_id = data.getString("user_id"),
                                    user_name = data.getString("user_name"),
                                    user_group_auth_name = data.getString("user_group_auth_name"),
                                    user_mail = data.getString("user_mail"),
                                )

                                if (caregiverDto in caregiverList) continue
                                else caregiverList.add(caregiverDto)

                                caregiverAdapter.differ.submitList(caregiverList)
                            }

                            val authNameList = mutableListOf<String>()
                            for (i in 0 until authArray.length()) {
                                val auth = authArray.getJSONObject(i)
                                val authDto = AuthDto(
                                    user_group_auth_id = auth.getString("user_group_auth_id"),
                                    user_group_auth_name = auth.getString("user_group_auth_name"),
                                )

                                authList.add(authDto)
                                authNameList.add(auth.getString("user_group_auth_name"))
                            }

                            // save authList
                            arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, authNameList)

                        } catch (e: Exception) {
                            dialogHelper.alertDialog("錯誤", e.message.toString(), 1, true)
                        }
                    }

                    is CaregiverViewModel.CaregiverState.Error -> {
                        Log.e("TAG", "initData: ${it.error}")
                        dialogHelper.alertDialog("錯誤", it.error, 1, true)
                    }

                    is CaregiverViewModel.CaregiverState.Loading -> {
                        Log.e("TAG", "initData: Loading")
                    }
                }
            }
        }
    }

    private fun initButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.addButton.setOnClickListener {
            var selectIndex = 0
            var selected = ""

            dialogHelper.dropDownDialog("邀請加入群組", "請輸入使用者的郵件", arrayAdapter, object :
                DialogHelper.CustomDropInputListener {
                override fun onPositiveClick(
                    var1: View,
                    dropDownDialog: AlertDialog,
                    inputText: TextInputEditText
                ) {
                    if (selected.isNotBlank() && selected != "") {
                        val email = inputText.text.toString()
                        val authId = authList[selectIndex].user_group_auth_id

                        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Log.e("TAG", "onPositiveClick: $email $selected $selectIndex")
                            yuuzuApi.api(Request.Method.POST, Constants.API_USER_GROUP, object :
                                YuuzuApi.YuuzuApiListener {
                                override fun onSuccess(data: String) {
                                    Log.e("TAG", "onSuccess: $data")
                                    viewModel.getCaregiverList()

                                    dialogHelper.alertDialog("邀請成功", "", 2, false, "確認", object :
                                        DialogHelper.DialogPositiveListener {
                                        override fun onPositive(dialog: SweetAlertDialog) {
                                            dialog.dismissWithAnimation()
                                            initData()
                                        }
                                    })
                                }

                                override fun onError(error: VolleyError) {
                                    try {
                                        val jsonObject = JSONObject(String(error.networkResponse.data, Charsets.UTF_8))
                                        val message = jsonObject.getString("Message")
                                        dialogHelper.alertDialog("錯誤", message, 1, true)

                                    } catch (e: Exception) {
                                        dialogHelper.alertDialog("錯誤", e.message.toString(), 1, true)
                                    }
                                }

                                override val params: Map<String, String>
                                    get() = mapOf(
                                        "user_mail" to email,
                                        "user_group_auth_id" to authId,
                                        "group_id" to groupId
                                    )
                            })

                            dropDownDialog.dismiss()

                        } else {
                            dialogHelper.alertDialog("錯誤", "請輸入正確的郵件格式", 1, true)
                        }

                    } else {
                        dialogHelper.alertDialog("錯誤", "請選擇權限", 1, true)
                    }
                }
            }, object :
                DialogHelper.OnItemClickListener {
                override fun onItemClick(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    selectIndex = i
                    selected = adapterView?.getItemAtPosition(i).toString()
                }
            })
        }
    }

    private fun initView() {
        viewModel.getCaregiverList()
        share = YuuzuShare(requireContext())
        yuuzuApi = YuuzuApi(requireContext())
        dialogHelper = DialogHelper(requireActivity())

        groupId = share.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.group_id.toString()
        currentAuth = share.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.user_group_auth_name.toString()
        caregiverList = mutableListOf()
        authList = mutableListOf()

        if (currentAuth != "admin") binding.addButton.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}