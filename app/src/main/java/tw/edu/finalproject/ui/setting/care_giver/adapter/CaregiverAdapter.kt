package tw.edu.finalproject.ui.setting.care_giver.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.finalproject.databinding.RecyclerUserBinding
import tw.edu.finalproject.ui.setting.care_giver.model.CaregiverDto

class CaregiverAdapter(
    private val currentAuth: String,
    private val onEditClickListener: OnEditClickListener,
    private val onDeleteClickListener: OnDeleteClickListener
): RecyclerView.Adapter<CaregiverAdapter.CaregiverViewHolder>() {

    class CaregiverViewHolder(
        val binding: RecyclerUserBinding,
        onEditClickListener: OnEditClickListener,
        onDeleteClickListener: OnDeleteClickListener
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.EditButton.setOnClickListener {
                onEditClickListener.onEditClick(bindingAdapterPosition)
            }

            binding.DeleteButton.setOnClickListener {
                onDeleteClickListener.onDeleteClick(bindingAdapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<CaregiverDto>() {
        override fun areItemsTheSame(oldItem: CaregiverDto, newItem: CaregiverDto): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CaregiverDto, newItem: CaregiverDto): Boolean {
            if (oldItem.user_group_id != newItem.user_group_id) return false
            if (oldItem.user_id != newItem.user_id) return false
            if (oldItem.user_name != newItem.user_name) return false
            if (oldItem.user_group_auth_name != newItem.user_group_auth_name) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaregiverViewHolder {
        return CaregiverViewHolder(
            RecyclerUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onEditClickListener,
            onDeleteClickListener
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CaregiverViewHolder, position: Int) {
        holder.binding.apply {
            val caregiver = differ.currentList[position]
            if (caregiver.user_name.length > 2)
                IconName.text = caregiver.user_name.substring(caregiver.user_name.length - 2)
            else
                IconName.text = caregiver.user_name

            if (caregiver.user_group_auth_name == "admin") {
                EditButton.visibility = View.GONE
                DeleteButton.visibility = View.GONE
            }

            if (currentAuth != "admin") {
                EditButton.visibility = View.GONE
                DeleteButton.visibility = View.GONE
            }

            UserName.text = "${caregiver.user_name}\n[${caregiver.user_group_auth_name}]"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface OnEditClickListener {
        fun onEditClick(position: Int)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }
}