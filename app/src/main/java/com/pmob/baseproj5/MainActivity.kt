package com.pmob.baseproj5

import android.app.Activity
import com.pmob.baseproj5.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.pmob.baseproj5.databinding.ActivityMainBinding
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbBarang: DatabaseBarang
    private lateinit var barangDao:BarangDao

    private final var GALLERY_REQ_CODE = 1000
    private lateinit var appExecutors: AppExecutor
    private var ivPreview: ImageView? = null
    private var selectedImageUri: Uri? = null



    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data
                ivPreview?.setImageURI(selectedImageUri)
            }
        }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val filename = "IMG_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, filename)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            file.absolutePath // path permanen untuk disimpan di DB
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appExecutors = AppExecutor()
        dbBarang = DatabaseBarang.getDatabase(applicationContext)
        barangDao = dbBarang.barangDao()
        binding.apply {
            barangDao.getAllBarang().observe(this@MainActivity) { list ->

                val adapter = BarangAdapter(list, this@MainActivity)
                binding.lvRoomDb.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.lvRoomDb.adapter = adapter


                val profileAdapter = ProfileAdapter(list) // langsung list Barang
                binding.rvProfiles.layoutManager =
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                binding.rvProfiles.adapter = profileAdapter
            }


            fabAdd.setOnClickListener {
                val dialogView = layoutInflater.inflate(com.pmob.baseproj5.R.layout.pop_up_tambah, null)

                val etUsername = dialogView.findViewById<EditText>(com.pmob.baseproj5.R.id.etUsername)
                val etCaption = dialogView.findViewById<EditText>(com.pmob.baseproj5.R.id.etCaption)
                val btnSimpan = dialogView.findViewById<Button>(com.pmob.baseproj5.R.id.btnSimpan)
                val btnPilihGambar = dialogView.findViewById<Button>(com.pmob.baseproj5.R.id.btnPilihGambar)
                ivPreview = dialogView.findViewById(R.id.ivPreview)

                val dialog = AlertDialog.Builder(this@MainActivity)
                    .setView(dialogView)
                    .create()

                dialog.show()

                btnPilihGambar.setOnClickListener {
                    val iGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    iGallery.type = "image/*"
                    galleryLauncher.launch(iGallery)
                }


//                btnPilihGambar.setOnClickListener {
//                    val intent = Intent(Intent.ACTION_PICK)
//                    intent.type = "image/*"
//                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
//                }


                btnSimpan.setOnClickListener {
                    val username = etUsername.text.toString()
                    val caption = etCaption.text.toString()

                    if (username.isNotEmpty() && caption.isNotEmpty()) {
                        val uri: Uri? = selectedImageUri
                        val imagePath: String? = uri?.let { saveImageToInternalStorage(it) }

                        appExecutors.diskIO.execute {

                            val newBarang = Barang(0, username, caption, 0, imagePath)
                            barangDao.insert(newBarang)
                        }
                        dialog.dismiss()

                    } else {
                        etUsername.error = "Wajib diisi"
                        etCaption.error = "Wajib diisi"
                    }
                    Toast.makeText(this@MainActivity, "Simpan berhasil", Toast.LENGTH_LONG).show();
                }




                appExecutors.diskIO.execute {
//                    val barangTitles = listOf("Meja", "Semen", "Triplek", "Pasir")
//                    val jenisBarang = listOf("Perabotan", "Material", "Material", "Material")
//                    val hargaBarang = listOf(50000,48000,15000,68000)
//                    for(i in 1..4){
//                        val newBarang = Barang(i, barangTitles[i-1], jenisBarang[i-1], hargaBarang[i-1])
//                        barangDao.insert(newBarang)
//                    }



                }
            }





            val barangList: LiveData<List<Barang>> = barangDao.getAllBarang()
            barangList.observe(this@MainActivity, Observer { list ->
                val layoutManager = LinearLayoutManager(this@MainActivity)
                lvRoomDb.layoutManager = layoutManager
                val adapter = BarangAdapter(list, this@MainActivity)
                lvRoomDb.adapter = adapter
            })


        }



    }

    fun showDeleteDialog(barang: Barang) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Hapus Postingan")
            .setMessage("Apakah kamu yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                appExecutors.diskIO.execute {
                    barangDao.delete(barang)
                }
                Toast.makeText(this, "Hapus berhasil", Toast.LENGTH_LONG).show();
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

    fun showEditDialog(barang: Barang) {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_tambah, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etCaption = dialogView.findViewById<EditText>(R.id.etCaption)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnPilihGambar = dialogView.findViewById<Button>(R.id.btnPilihGambar)
        ivPreview = dialogView.findViewById(R.id.ivPreview)


        etUsername.setText(barang.nama)
        etCaption.setText(barang.jenis)
        barang.imageUri?.let {
            val file = File(it)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ivPreview?.setImageBitmap(bitmap)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.show()

        btnPilihGambar.setOnClickListener {
            val iGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            iGallery.type = "image/*"
            galleryLauncher.launch(iGallery)
        }

        btnSimpan.setOnClickListener {
            val username = etUsername.text.toString()
            val caption = etCaption.text.toString()

            if (username.isNotEmpty() && caption.isNotEmpty()) {
                val imagePath: String? = selectedImageUri?.let { saveImageToInternalStorage(it) } ?: barang.imageUri

                appExecutors.diskIO.execute {
                    val updatedBarang = Barang(barang.id, username, caption, 0, imagePath)
                    barangDao.update(updatedBarang)
                }

                dialog.dismiss()
                Toast.makeText(this, "Edit Berhasil", Toast.LENGTH_LONG).show();

            } else {
                etUsername.error = "Wajib diisi"
                etCaption.error = "Wajib diisi"
            }
        }
    }




}