package tw.edu.finalproject

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import tw.edu.finalproject.databinding.ActivityMainBinding
import tw.edu.finalproject.ui.notification.view_model.AddNotificationViewModel
import tw.edu.finalproject.ui.notification.view_model.NotificationViewModel
import tw.edu.finalproject.ui.setting.SettingViewModel
import tw.edu.finalproject.ui.setting.care_giver.view_model.CaregiverViewModel
import tw.edu.finalproject.yuuzu.PermissionHelper
import tw.edu.finalproject.yuuzu.websocket.WebSocketService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: FragmentContainerView

    lateinit var addNotificationViewModel: AddNotificationViewModel
    lateinit var notificationViewModel: NotificationViewModel
    lateinit var settingViewModel: SettingViewModel
    lateinit var caregiverViewModel: CaregiverViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Intent(this, WebSocketService::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(this)
            else startService(this)
        }

        PermissionHelper(this).requestPermission()

        navHostFragment = findViewById(R.id.fragment)
        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        notificationViewModel = NotificationViewModel(application)

        settingViewModel = SettingViewModel(application)

        caregiverViewModel = CaregiverViewModel(application)
    }
}