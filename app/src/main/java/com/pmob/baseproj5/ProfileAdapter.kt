package com.pmob.baseproj5

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pmob.baseproj5.databinding.ItemProfileBinding

class ProfileAdapter(private val barangList: List<Barang>) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(private val binding: ItemProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(barang: Barang) {
            val context = itemView.context


            val resId = context.resources.getIdentifier(
                "profil_${barang.id % 5 + 1}",
                "drawable",
                context.packageName
            )
            binding.ivProfile.setImageResource(if (resId != 0) resId else R.drawable.ic_launcher_background)


            binding.tvUsername.text = barang.nama
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val barang = barangList[position]
        holder.bind(barang)
    }

    override fun getItemCount(): Int = barangList.size
}

