package tw.edu.finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import tw.edu.finalproject.databinding.ActivitySecondaryBinding
import tw.edu.finalproject.ui.home.view_model.HomeViewModel
import tw.edu.finalproject.ui.notification.view_model.NotificationViewModel
import tw.edu.finalproject.ui.setting.SettingViewModel
import tw.edu.finalproject.yuuzu.PermissionHelper

class SecondaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondaryBinding
    private lateinit var navHostFragment: FragmentContainerView

    lateinit var settingViewModel: SettingViewModel
    lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeViewModel = HomeViewModel(application)

        settingViewModel = SettingViewModel(application)
        settingViewModel.getGroupList()
        settingViewModel.getUserDetail()

        navHostFragment = findViewById(R.id.fragmentSecondary)
        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        PermissionHelper(this).requestPermission()
    }
}