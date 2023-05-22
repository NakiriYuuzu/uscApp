package tw.edu.finalproject.ui.event

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import tw.edu.finalproject.R
import tw.edu.finalproject.SecondaryActivity
import tw.edu.finalproject.databinding.FragmentEventBinding
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuShare


class EventFragment : Fragment(R.layout.fragment_event) {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var yuuzuShare: YuuzuShare

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventBinding.bind(view)

        initView()
    }

    private fun initView() {
        yuuzuShare = YuuzuShare(requireContext())
        Log.e("TAG", "initView: ${yuuzuShare.get<String>(YStore.YS_TOKEN)}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}