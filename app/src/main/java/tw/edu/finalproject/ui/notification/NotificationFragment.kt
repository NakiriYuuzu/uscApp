package tw.edu.finalproject.ui.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import tw.edu.finalproject.MainActivity
import tw.edu.finalproject.R
import tw.edu.finalproject.SecondaryActivity
import tw.edu.finalproject.databinding.FragmentNotificationBinding
import tw.edu.finalproject.ui.notification.adapter.NotificationAdapter
import tw.edu.finalproject.ui.notification.model.RemindModel
import tw.edu.finalproject.ui.notification.model.RemindType
import tw.edu.finalproject.ui.notification.view_model.NotificationViewModel
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.yuuzu.YuuzuShare

class NotificationFragment : Fragment(R.layout.fragment_notification) {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationViewModel

    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var yuuzuShare: YuuzuShare
    private lateinit var dialogHelper: DialogHelper

    private lateinit var remindTypeList: List<RemindType>
    private lateinit var remindModel: RemindModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationBinding.bind(view)

        viewModel = (activity as MainActivity).notificationViewModel


        initView()
        initData()
        initButton()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(object : NotificationAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(requireContext(), "$position", Toast.LENGTH_SHORT).show()
            }
        })

        binding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = notificationAdapter
        }
    }

    private fun initData() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                when (it) {
                    is NotificationViewModel.NotificationState.Success -> {
                        // get data without duplicate
                        notificationAdapter.differ.submitList(it.data.toList())
                    }

                    is NotificationViewModel.NotificationState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }

                    is NotificationViewModel.NotificationState.Waiting -> {
                        Log.e("TAG", "initData: ")
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.remindModel.collect {
                when (it) {
                    is NotificationViewModel.RemindState.Success -> {
                        remindModel = it.data
                    }

                    is NotificationViewModel.RemindState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.remindType.collect {
                when (it) {
                    is NotificationViewModel.RemindTypeState.Success -> {
                        remindTypeList = it.data
                    }

                    is NotificationViewModel.RemindTypeState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun selectUserAndNavigate() {
        val userList = remindModel.careReceiverList?.map { it.user_name }
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, userList!!)
        var selected = ""
        var position = 0
        dialogHelper.dropDownDialog("請選擇提醒對象", arrayAdapter, object: DialogHelper.CustomPositiveListener {
            override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                if (selected.isNotBlank()) {
                    val currentSelect = remindModel.careReceiverList?.get(position)
                    val newUserList = remindModel.careReceiverList?.filter { it.user_id == currentSelect?.user_id }
                    yuuzuShare.put(RemindModel(newUserList, remindModel.remindFormat), YStore.YS_REMIND_MODEL)
                    findNavController().navigate(R.id.action_notificationFragment_to_addNotificationFragment)
                    dropDownDialog.dismiss()
                } else {
                    dialogHelper.alertDialog("錯誤", "請選擇提醒對象", 1, true)
                }
            }

        }, object : DialogHelper.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                selected = adapterView?.getItemAtPosition(i).toString()
                position = i
            }
        })
    }

    private fun initButton() {
        binding.filterDate.setOnClickListener {
            var selected = ""
            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, listOf("今天", "所有"))
            dialogHelper.dropDownDialog("顯示今天/所有通知", arrayAdapter, object :
                DialogHelper.CustomPositiveListener {
                override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                    if (selected.isNotBlank()) {
                        when (selected) {
                            "今天" -> {
                                viewModel.getTodayNotification()
                                initData()
                            }
                            "所有" -> {
                                viewModel.getAllNotification()
                                initData()
                            }
                        }

                        dropDownDialog.dismiss()

                    } else {
                        dialogHelper.alertDialog("錯誤", "請選擇一個選項", 1, true)
                    }
                }
            }, object : DialogHelper.OnItemClickListener {
                override fun onItemClick(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    selected = adapterView?.getItemAtPosition(i).toString()
                }
            })
        }

        binding.addButton.setOnClickListener {
            viewModel.getRemindType()
            val selectorList = remindTypeList.map { it.remind_name }
            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dialog_dropdown_item, selectorList)
            Log.e("TAG", "initButton: $remindTypeList")
            var selected = ""
            var position = 0

            dialogHelper.dropDownDialog("選擇提醒類型", arrayAdapter, object : DialogHelper.CustomPositiveListener {
                override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                    if (selected.isNotBlank()) {
                        val remindId = remindTypeList[position].remind_id
                        yuuzuShare.put(remindId, YStore.YS_REMIND_ID)
                        yuuzuShare.put(remindTypeList[position].remind_name, YStore.YS_REMIND_NAME)
                        selectUserAndNavigate()
                        dropDownDialog.dismiss()

                    } else {
                        dialogHelper.alertDialog("錯誤", "請選擇一個選項", 1, true)
                    }
                }
            }, object : DialogHelper.OnItemClickListener {
                override fun onItemClick(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    selected = adapterView?.getItemAtPosition(i).toString()
                    position = i
                    viewModel.getRemindModel(remindTypeList[position].remind_id)
                }
            })
        }
    }

    private fun initView() {
        setupRecyclerView()

        yuuzuShare = YuuzuShare(requireContext())
        dialogHelper = DialogHelper(requireActivity())

        remindTypeList = emptyList()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllNotification()
        viewModel.getRemindType()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}