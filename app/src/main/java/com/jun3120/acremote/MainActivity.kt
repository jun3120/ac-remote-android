package com.jun3120.acremote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jun3120.acremote.data.ir.IrTransmitter
import com.jun3120.acremote.data.local.RemotePreferences
import com.jun3120.acremote.data.local.SavedRemote
import com.jun3120.acremote.ui.brand.BrandPickerActivity
import com.jun3120.acremote.ui.brand.SimpleListAdapter
import com.jun3120.acremote.ui.remote.RemoteControlActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddRemote: View
    private lateinit var tvNoRemotes: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvIrStatus: TextView

    private var savedRemotes: List<SavedRemote> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddRemote = findViewById(R.id.btn_add_remote)
        tvNoRemotes = findViewById(R.id.tv_no_remotes)
        recyclerView = findViewById(R.id.rv_saved_remotes)
        tvIrStatus = findViewById(R.id.tv_ir_status)

        recyclerView.layoutManager = LinearLayoutManager(this)

        if (!IrTransmitter.hasIrEmitter(this)) {
            tvIrStatus.text = "⚠ 此设备不支持红外发射"
            tvIrStatus.visibility = View.VISIBLE
        }

        btnAddRemote.setOnClickListener {
            startActivityForResult(
                Intent(this, BrandPickerActivity::class.java),
                REQUEST_BRAND_PICKER
            )
        }

        loadSavedRemotes()
    }

    override fun onResume() {
        super.onResume()
        loadSavedRemotes()
    }

    private fun loadSavedRemotes() {
        savedRemotes = RemotePreferences.getSavedRemotes(this)
        if (savedRemotes.isEmpty()) {
            tvNoRemotes.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoRemotes.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            val items = savedRemotes.map {
                it to "${it.brandName}"
            }
            recyclerView.adapter = SimpleListAdapter(items) { any ->
                val remote = any as SavedRemote
                openRemote(remote)
            }
        }
    }

    private fun openRemote(remote: SavedRemote) {
        val intent = Intent(this, RemoteControlActivity::class.java).apply {
            putExtra(RemoteControlActivity.EXTRA_CODE_PATH, remote.codePath)
            putExtra(RemoteControlActivity.EXTRA_CATEGORY_ID, remote.categoryId)
            putExtra(RemoteControlActivity.EXTRA_SUB_CATEGORY, remote.subCategory)
            putExtra(RemoteControlActivity.EXTRA_BRAND_NAME, remote.brandName)
        }
        startActivity(intent)
    }

    @Deprecated("Use registerForActivityResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAND_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val codePath = data.getStringExtra(BrandPickerActivity.EXTRA_CODE_PATH) ?: return
            val categoryId = data.getIntExtra(BrandPickerActivity.EXTRA_CATEGORY_ID, 1)
            val subCategory = data.getIntExtra(BrandPickerActivity.EXTRA_SUB_CATEGORY, 0)
            val brandName = data.getStringExtra(BrandPickerActivity.EXTRA_BRAND_NAME) ?: "空调"

            openRemote(SavedRemote(codePath, categoryId, subCategory, brandName))
        }
    }

    companion object {
        private const val REQUEST_BRAND_PICKER = 100
    }
}
