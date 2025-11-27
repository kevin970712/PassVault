package com.jksalcedo.passvault.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.adapter.PVAdapter
import com.jksalcedo.passvault.databinding.ActivityMainBinding
import com.jksalcedo.passvault.ui.addedit.AddEditActivity
import com.jksalcedo.passvault.ui.settings.SettingsActivity
import com.jksalcedo.passvault.ui.view.ViewEntryActivity
import com.jksalcedo.passvault.viewmodel.PasswordViewModel

/**
 * The main activity of the app.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PVAdapter
    private lateinit var viewModel: PasswordViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = PVAdapter()
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter

        adapter.items.observe(this, Observer { items ->
            // Show message on first time
            binding.contentMain.tvMessage.visibility =
                if (items.isEmpty()) View.VISIBLE else View.GONE
        })

        // View the entry
        adapter.onItemClick = { entry ->
            startActivity(ViewEntryActivity.createIntent(this, entry))
        }

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        viewModel.allEntries.observe(this) { list ->
            adapter.submitList(list)
        }

        // Add entry
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}