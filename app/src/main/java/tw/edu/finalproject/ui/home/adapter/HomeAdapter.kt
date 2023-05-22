package tw.edu.finalproject.ui.home.adapter

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.finalproject.R
import tw.edu.finalproject.databinding.RecyclerNotificationBinding
import tw.edu.finalproject.ui.home.model.RemindData
import tw.edu.finalproject.ui.notification.adapter.NotificationAdapter
import java.util.*

class HomeAdapter(
    private val onItemClickListener: NotificationAdapter.OnItemClickListener
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    class HomeViewHolder(
        val binding: RecyclerNotificationBinding,
        onItemClickListener: NotificationAdapter.OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.notificationItem.setOnClickListener {
                onItemClickListener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    private val differCallback = object: DiffUtil.ItemCallback<RemindData>() {
        override fun areItemsTheSame(oldItem: RemindData, newItem: RemindData): Boolean {
            if (oldItem.remind_name != newItem.remind_name) return false
            if (oldItem.remind_time != newItem.remind_time) return false
            if (oldItem.status != newItem.status) return false
            return true
        }

        override fun areContentsTheSame(oldItem: RemindData, newItem: RemindData): Boolean {
           return oldItem == newItem
        }
    }

    val differ = androidx.recyclerview.widget.AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            RecyclerNotificationBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.binding.apply {
            val notification = differ.currentList[position]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val timeFormat = SimpleDateFormat("HH:mm")
            val parsedDate: Date = inputFormat.parse(notification.remind_time)
            val currentDate = dateFormat.format(parsedDate)
            val currentTime = timeFormat.format(parsedDate)

            date.text = currentDate
            time.text = currentTime
            message.text = notification.remind_name

            val isDarkMode = holder.itemView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            if (notification.status != "0") {
                if (isDarkMode) notificationAlert.setColorFilter(holder.itemView.context.getColor(R.color.md_theme_dark_green))
                else notificationAlert.setColorFilter(holder.itemView.context.getColor(R.color.md_theme_light_green))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}