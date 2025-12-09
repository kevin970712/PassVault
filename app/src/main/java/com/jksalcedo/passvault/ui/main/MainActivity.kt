package com.jksalcedo.passvault.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.adapter.PVAdapter
import com.jksalcedo.passvault.data.SortOption
import com.jksalcedo.passvault.databinding.ActivityMainBinding
import com.jksalcedo.passvault.ui.addedit.AddEditActivity
import com.jksalcedo.passvault.ui.addedit.PasswordDialogListener
import com.jksalcedo.passvault.ui.addedit.PasswordGenDialog
import com.jksalcedo.passvault.ui.base.BaseActivity
import com.jksalcedo.passvault.ui.category.ManageCategoriesDialog
import com.jksalcedo.passvault.ui.settings.SettingsActivity
import com.jksalcedo.passvault.ui.view.ViewEntryActivity
import com.jksalcedo.passvault.utils.Utility
import com.jksalcedo.passvault.viewmodel.CategoryViewModel
import com.jksalcedo.passvault.viewmodel.PasswordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The main activity of the app.
 */
class MainActivity : BaseActivity(), PasswordDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PVAdapter
    private lateinit var viewModel: PasswordViewModel
    private lateinit var categoryViewModel: CategoryViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = PVAdapter(this)
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter

        // View the entry
        adapter.onItemClick = { entry ->
            startActivity(ViewEntryActivity.createIntent(this, entry))
        }

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        viewModel.filteredEntries.observe(this) { list ->
            adapter.submitList(list)

            // Show message if empty
            binding.contentMain.tvMessage.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Add entry
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }

        // Dynamically load category chips from database
        categoryViewModel.allCategories.observe(this) { categories ->
            binding.chipGroupCategories.removeAllViews()

            // Add "All" chip
            val chipAll = Chip(this).apply {
                id = View.generateViewId()
                text = buildString {
                    append("All (")
                    append(viewModel.allEntries.value?.size ?: 0)
                    append(")")
                }
                isCheckable = true
                isChecked = true
                setChipBackgroundColorResource(R.color.chip_background)
            }
            binding.chipGroupCategories.addView(chipAll)

            // Add category chips
            categories.forEach { category ->
                val chip = Chip(this).apply {
                    id = View.generateViewId()
                    val count =
                        viewModel.allEntries.value?.count { it.category == category.name } ?: 0
                    text = buildString {
                        append(category.name)
                        append(" (")
                        append(count)
                        append(")")
                    }
                    isCheckable = true
                    setChipBackgroundColorResource(R.color.chip_background)
                    tag = category.name // Store category name in tag
                }
                binding.chipGroupCategories.addView(chip)
            }

            // Set up click listener for all chips
            binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

                val selectedChip = findViewById<Chip>(checkedIds[0])
                val categoryName = selectedChip?.tag as? String

                viewModel.filterByCategory(categoryName)
                updateCategoryCounts()
            }
        }

        // Update counts when entries change
        viewModel.allEntries.observe(this) {
            updateCategoryCounts()
        }
    }

    private fun updateCategoryCounts() {
        val entries = viewModel.allEntries.value ?: return

        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as? Chip ?: continue
            val categoryName = chip.tag as? String

            if (categoryName == null) {
                // "All" chip
                chip.text = buildString {
                    append("All (")
                    append(entries.size)
                    append(")")
                }
            } else {
                val count = entries.count { it.category == categoryName }
                chip.text = buildString {
                    append(categoryName)
                    append(" (")
                    append(count)
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

            R.id.action_manage_categories -> {
                val dialog = ManageCategoriesDialog()
                dialog.show(supportFragmentManager, "ManageCategoriesDialog")
                true
            }

            R.id.sort_name_asc -> {
                viewModel.setSortOption(SortOption.NAME_ASC)
                true
            }

            R.id.sort_name_desc -> {
                viewModel.setSortOption(SortOption.NAME_DESC)
                true
            }

            R.id.sort_date_created -> {
                viewModel.setSortOption(SortOption.DATE_CREATED_DESC)
                true
            }

            R.id.sort_date_modified -> {
                viewModel.setSortOption(SortOption.DATE_MODIFIED_DESC)
                true
            }

            R.id.sort_category -> {
                viewModel.setSortOption(SortOption.CATEGORY_ASC)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPasswordGenerated(password: String) {
        // No need to handle
    }

}