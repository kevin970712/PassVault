package com.jksalcedo.passvault.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.adapter.PVAdapter
import com.jksalcedo.passvault.databinding.ActivityMainBinding
import com.jksalcedo.passvault.ui.addedit.AddEditActivity
import com.jksalcedo.passvault.ui.addedit.PasswordDialogListener
import com.jksalcedo.passvault.ui.addedit.PasswordGenDialog
import com.jksalcedo.passvault.ui.settings.SettingsActivity
import com.jksalcedo.passvault.ui.view.ViewEntryActivity
import com.jksalcedo.passvault.viewmodel.PasswordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The main activity of the app.
 */
class MainActivity : AppCompatActivity(), PasswordDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PVAdapter
    private lateinit var viewModel: PasswordViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = PVAdapter(this)
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
        viewModel.filteredEntries.observe(this) { list ->
            adapter.submitList(list)
        }

        // Add entry
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }

        // Filter Category
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val category = when (checkedIds[0]) {
                R.id.chipAll -> null
                R.id.chipGeneral -> "General"
                R.id.chipSocial -> "Social"
                R.id.chipWork -> "Work"
                R.id.chipPersonal -> "Personal"
                R.id.chipFinance -> "Finance"
                R.id.chipEntertainment -> "Entertainment"
                else -> null
            }

            viewModel.filterByCategory(category)

            // Update counts
            viewModel.allEntries.value?.let { entries ->
                binding.chipAll.text = buildString {
                    append("All (")
                    append(entries.size)
                    append(")")
                }
                binding.chipGeneral.text = buildString {
                    append("General (")
                    append(entries.count { it.category == "General" })
                    append(")")
                }
                binding.chipSocial.text = buildString {
                    append("Social (")
                    append(entries.count { it.category == "Social" })
                    append(")")
                }
                binding.chipWork.text = buildString {
                    append("Work (")
                    append(entries.count { it.category == "Work" })
                    append(")")
                }
                binding.chipPersonal.text = buildString {
                    append("Personal (")
                    append(entries.count { it.category == "Personal" })
                    append(")")
                }
                binding.chipFinance.text = buildString {
                    append("Finance (")
                    append(entries.count { it.category == "Finance" })
                    append(")")
                }
                binding.chipEntertainment.text = buildString {
                    append("Entertainment (")
                    append(entries.count { it.category == "Entertainment" })
                    append(")")
                }
            }
        }
    }

    private fun performSearch(query: String?) {
        lifecycleScope.launch {
            if (!query.isNullOrEmpty()) {
                val result = withContext(Dispatchers.IO) {
                    viewModel.search(query)
                }
                adapter.submitList(result)
            } else {
                viewModel.filteredEntries.value?.let { adapter.submitList(it) }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchPlateId =
            searchView.context.resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchView.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            R.id.action_generator -> {
                val dialog = PasswordGenDialog()
                dialog.isCancelable = false
                dialog.show(supportFragmentManager, null)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPasswordGenerated(password: String) {
        // No need to handle
    }

}