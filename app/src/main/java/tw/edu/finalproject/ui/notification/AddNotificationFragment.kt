package tw.edu.finalproject.ui.notification

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.FragmentAddNotificationBinding
import tw.edu.finalproject.ui.notification.model.RemindModel
import tw.edu.finalproject.ui.notification.view_model.AddNotificationViewModel
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.DialogHelper
import tw.edu.finalproject.yuuzu.ViewHelper
import tw.edu.finalproject.yuuzu.YuuzuShare
import java.util.Calendar

class AddNotificationFragment : Fragment(R.layout.fragment_add_notification) {

    private var _binding: FragmentAddNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var yuuzuShare: YuuzuShare
    private lateinit var dialogHelper: DialogHelper
    private lateinit var viewModel: AddNotificationViewModel

    private lateinit var remindModel: RemindModel

    private var totalOfLines = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddNotificationBinding.bind(view)

        initView()
        initButton()
    }

    private fun getEditTextResult(): String {
        var finalResult = "'{"

        for (i in 0 until totalOfLines) {
            val editText = binding.root.findViewById<TextInputEditText>(i)
            val text = editText.text.toString()
            val result = "\"${remindModel.remindFormat[i]}\":[\"$text\"],"
            Log.e("TAG", "getEditTextResult: $result")

            finalResult += result
        }

        finalResult = finalResult.substring(0, finalResult.length - 1)
        finalResult += "}"
        Log.e("TAG", "getEditTextResult: $finalResult")

        return finalResult
    }

    private fun addEditText(hint: String, id: Int) {
        val textInputEditText = TextInputEditText(requireContext())
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        textInputEditText.id = id
        textInputEditText.setPadding(16, 16, 16, 16)

        val textInputLayout = TextInputLayout(requireContext(), null, R.style.customTextLayout)
        textInputLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        textInputLayout.hint = hint
        textInputLayout.setPadding(16, 16, 16, 16)
        textInputLayout.addView(textInputEditText)

        binding.linearLayoutEditText.addView(textInputLayout)
    }

    private fun initButton() {
        binding.submitButton.setOnClickListener {
            // get today
            val calendar = Calendar.getInstance()
            val years = calendar.get(Calendar.YEAR)
            val months = calendar.get(Calendar.MONTH)
            val days = calendar.get(Calendar.DAY_OF_MONTH)


            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val realMonth = month + 1
                val date = "$year-${if (realMonth < 10) "0$realMonth" else "$realMonth"}-${if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"}"
                Log.e("TAG", "initButton: $date" )

                TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                    val realMinute = minute + 1
                    val time = "$hourOfDay:${if (realMinute < 10) "0$realMinute" else "$realMinute"}:00"
                    Log.e("TAG", "initButton: $time" )

                    if (date.isNotBlank() && time.isNotBlank()) {
                        val timeResult = "$date $time"
                        getEditTextResult()
                        viewModel.addRemind(timeResult, getEditTextResult())
                        findNavController().popBackStack()
                    }

                }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true).show()

            }, years, months, days).show()


            // viewModel.addRemind()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        ViewHelper(requireActivity()).setupUI(binding.root)
        yuuzuShare = YuuzuShare(requireContext())
        dialogHelper = DialogHelper(requireActivity())
        viewModel = AddNotificationViewModel(requireActivity().application)
        remindModel = yuuzuShare.get<RemindModel>(YStore.YS_REMIND_MODEL)!!
        totalOfLines = remindModel.remindFormat.count()

        binding.titleText.text = "新增${yuuzuShare.get<String>(YStore.YS_REMIND_NAME)}"

        for (i in 0 until totalOfLines) {
            addEditText(remindModel.remindFormat[i], i)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}