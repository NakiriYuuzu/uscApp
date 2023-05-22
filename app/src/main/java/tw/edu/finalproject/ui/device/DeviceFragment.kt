package tw.edu.finalproject.ui.device

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.MediaSource
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.FragmentDeviceBinding
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.yuuzu.YuuzuShare


class DeviceFragment : Fragment(R.layout.fragment_device) {

    // binding
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

    private lateinit var share: YuuzuShare

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeviceBinding.bind(view)

        initView()
        initButton()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    @SuppressLint("AuthLeak")
    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer

                val mediaSource: MediaSource = RtspMediaSource.Factory().setDebugLoggingEnabled(true).setForceUseRtpTcp(true)
                    .createMediaSource(MediaItem.fromUri(Constants.RTSP_URI))

                exoPlayer.setMediaSource(mediaSource)

                exoPlayer.playerError.apply {
                    Log.e("TAG", "initializePlayer: $this")
                    if (this?.message?.isNotEmpty() == true && this.message != "null") {
                        binding.totalOfflineDevice.text = "1"
                        // set color
                        binding.cameraStatusText.text = "離線"
                        binding.cameraStatusText.setTextColor(requireContext().getColor(R.color.md_theme_dark_error))
                        binding.cameraStatus.strokeColor = requireContext().getColor(R.color.md_theme_dark_error)
                    }
                }

                // exoPlayer.setMediaItem(MediaItem.fromUri("http://120.110.115.130:5000/mp4"))
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.prepare()
            }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initButton() {
        binding.btnTotalJetson.setOnClickListener {
            Toast.makeText(requireContext(), "JetsonNx設備: 1台", Toast.LENGTH_SHORT).show()
        }

        binding.btnTotalCamera.setOnClickListener {
            Toast.makeText(requireContext(), "攝影機設備: 1台", Toast.LENGTH_SHORT).show()
        }

        binding.btnTotalOffline.setOnClickListener {
            Toast.makeText(requireContext(), "離線設備: 0台", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        share = YuuzuShare(requireContext())

        binding.totalCamera.text = "1"
        binding.totalJetsonNx.text = "1"

        // set color
        binding.cameraStatusText.text = "正常"
        binding.cameraStatusText.setTextColor(requireContext().getColor(R.color.md_theme_dark_green))
        binding.cameraStatus.strokeColor = requireContext().getColor(R.color.md_theme_dark_green)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        // hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }
}