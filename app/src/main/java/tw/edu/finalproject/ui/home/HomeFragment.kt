package tw.edu.finalproject.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.koushikdutta.async.http.AsyncHttpResponse
import com.koushikdutta.async.http.body.MultipartFormDataBody
import tw.edu.finalproject.R
import tw.edu.finalproject.SecondaryActivity
import tw.edu.finalproject.databinding.FragmentHomeBinding
import tw.edu.finalproject.ui.home.adapter.HomeAdapter
import tw.edu.finalproject.ui.home.model.RemindData
import tw.edu.finalproject.ui.home.view_model.HomeViewModel
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.ui.notification.adapter.NotificationAdapter
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.yuuzu.YuuzuShare
import tw.edu.finalproject.yuuzu.YuuzuV2Api
import java.io.File


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var yuuzuShare: YuuzuShare
    private lateinit var dialogHelper: DialogHelper
    private lateinit var homeAdapter: HomeAdapter

    private lateinit var viewModel: HomeViewModel

    private lateinit var currentSelected: RemindData
    private lateinit var remindData: List<RemindData>

    private var videoPath: String? = null

    private val takeVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val myData: Intent? = result.data
                if (myData != null) {
                    val videoUri = myData.data
                    val group = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)
                    Log.e("Tag", "$group")
                    if (videoUri != null) {
                        videoPath = getRealPathFromURI(videoUri)
                        Log.e("RealPath", "$videoPath")
                        dialogHelper.showLoadingDialog("上傳中", false)

                        YuuzuV2Api().api(Constants.JETSON_UPLOAD, object : YuuzuV2Api.GetPostListener {
                            override val body: MultipartFormDataBody
                                get() = MultipartFormDataBody().apply {
                                    addFilePart("file", File(videoPath!!))
                                    addStringPart("user_name", group?.group_name)
                                    addStringPart("recognition", currentSelected.remind_name)
                                    addStringPart("reminder", "1")
                                }

                            override fun response(
                                exception: Exception?,
                                source: AsyncHttpResponse?,
                                result: String?
                            ) {
                                if (exception != null) {
                                    dialogHelper.hideLoadingDialog()
                                    dialogHelper.alertDialog("錯誤", exception.message!!, 1, false)
                                    Log.e("Exception", "${exception.message}")
                                }
                                if (result != null) {
                                    dialogHelper.hideLoadingDialog()
                                    dialogHelper.alertDialog("成功", "上傳成功", 2, false)
                                    Log.e("Result", result)
                                }
                            }
                        })

                    } else {
                        Log.e("videoUri", "null")
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        initView()
        initRecyclerView()
        Handler(Looper.getMainLooper()).postDelayed({
            initViewModel()
        }, 1000)
    }

    private fun initRecyclerView() {
        homeAdapter = HomeAdapter(object : NotificationAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent()
                currentSelected = remindData[position]
                intent.action = MediaStore.ACTION_VIDEO_CAPTURE
                takeVideo.launch(intent)
            }
        })

        binding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = homeAdapter
        }
    }

    private fun initViewModel() {
        viewModel = (activity as SecondaryActivity).homeViewModel
        viewModel.getRemindData()
        lifecycleScope.launchWhenCreated {
            viewModel.state.collect {
                when (it) {
                    is HomeViewModel.HomeState.Success -> {
                        remindData = emptyList()
                        remindData = it.data
                        homeAdapter.differ.submitList(it.data.toList())
                    }
                    is HomeViewModel.HomeState.Error -> {
                        dialogHelper.alertDialog("錯誤", it.message, 1, true)
                    }
                    else -> Unit
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        remindData = emptyList()
        yuuzuShare = YuuzuShare(requireContext())
        dialogHelper = DialogHelper(requireActivity())

        val group = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)
        binding.groupName.text = group?.group_name
        binding.userName.text = "  ${group?.user_name}"
    }

    private fun getRealPathFromURI(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentUri?.let { context?.contentResolver?.query(it, proj, null, null, null) }
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            columnIndex?.let { cursor?.getString(it) }
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}