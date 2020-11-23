package biz.wolschon.wag.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.LiveData
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_entry.view.*
import biz.wolschon.wag.databinding.DeviceListEntryBinding

class SingleDeviceListAdapter(
    liveList: LiveData<List<SingleDeviceViewModel>>,
    private val viewLifecycleOwner: LifecycleOwner,
    /**
     * An initial list can be supplied.
     * It will be superseeded if this dapter is used to Observe
     * a LiveData.
     */
    private var values: List<SingleDeviceViewModel> = ArrayList()
) :
    RecyclerView.Adapter<SingleDeviceListAdapter.ViewHolder>(),
     Observer<List<SingleDeviceViewModel>> {

init {
            liveList.observe(viewLifecycleOwner, this)
    }

    var sorting: Comparator<SingleDeviceViewModel>? = null
        set(value) {
            field = value
            sorting?.let { values = values.sortedWith(it) }
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = values.size

    @Throws(ArrayIndexOutOfBoundsException::class)
    operator fun get(adapterPosition: Int): SingleDeviceViewModel {
        return values[adapterPosition]
    }

    fun getOrNull(adapterPosition: Int): SingleDeviceViewModel? =
        try {
            values[adapterPosition]
        } catch (x: ArrayIndexOutOfBoundsException) {
            null
        }

    /**
     * Observe the livedata that backs this lista.
     */
    override fun onChanged(newList: List<SingleDeviceViewModel>) {
        values = sorting?.let { newList.sortedWith(it) } ?: newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DeviceListEntryBinding.inflate(inflater, parent, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }

    inner class ViewHolder(
            val binding: DeviceListEntryBinding) :
            RecyclerView.ViewHolder(binding.root) {

        private var singleDevice: SingleDeviceViewModel? = null

        fun bind(singleDevice: SingleDeviceViewModel) {
            binding.singleDevice = singleDevice
            this.singleDevice = singleDevice
        }
    }
}
