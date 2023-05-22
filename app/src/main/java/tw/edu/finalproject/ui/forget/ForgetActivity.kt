package tw.edu.finalproject.ui.forget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.ActivityForgetBinding

class ForgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {

    }
}