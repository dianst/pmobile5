package com.pmob.baseproj5

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pmob.baseproj5.databinding.ItemBarangBinding

class BarangAdapter(
    private val barangList: List<Barang>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BarangViewHolder(binding, mainActivity)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        holder.bind(barang, position)
    }

    override fun getItemCount(): Int = barangList.size

    class BarangViewHolder(
        private val binding: ItemBarangBinding,
        private val mainActivity: MainActivity
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(barang: Barang, position: Int) {
            binding.apply {

                ivMore.setOnClickListener {
                    mainActivity.showEditDialog(barang)
                }

                binding.ivDelete.setOnClickListener {
                    mainActivity.showDeleteDialog(barang)
                }

                tvNama.text = barang.nama
                tvJenis.text = barang.jenis

                val context = itemView.context
                val totalGambar = 5
                val indexGambar = (position % totalGambar) + 1


                if (!barang.imageUri.isNullOrEmpty()) {
                    try {
                        ivGambar.setImageURI(Uri.parse(barang.imageUri))
                    } catch (e: Exception) {
                        ivGambar.setImageResource(R.drawable.ic_launcher_background)
                    }
                } else {
                    val resId = context.resources.getIdentifier(
                        "gambar_$indexGambar",
                        "drawable",
                        context.packageName
                    )
                    ivGambar.setImageResource(if (resId != 0) resId else R.drawable.ic_launcher_background)
                }


                val resIdProfile = context.resources.getIdentifier(
                    "profil_$indexGambar",
                    "drawable",
                    context.packageName
                )
                ivProfile.setImageResource(if (resIdProfile != 0) resIdProfile else R.drawable.ic_launcher_background)


                itemView.setOnClickListener {
                    val intent = Intent(it.context, DetailActivity::class.java)
                    intent.putExtra("barang_id", barang.id)
                    it.context.startActivity(intent)
                }


            }
        }
    }
}
