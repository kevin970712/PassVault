package com.jksalcedo.passvault.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.adapter.PVAdapter
import com.jksalcedo.passvault.data.enums.SortOption
import com.jksalcedo.passvault.databinding.ActivityMainBinding
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.ui.addedit.AddEditActivity
import com.jksalcedo.passvault.ui.addedit.PasswordDialogListener
import com.jksalcedo.passvault.ui.addedit.PasswordGenDialog
import com.jksalcedo.passvault.ui.base.BaseActivity
import com.jksalcedo.passvault.ui.category.ManageCategoriesDialog
import com.jksalcedo.passvault.ui.settings.SettingsActivity
import com.jksalcedo.passvault.ui.view.ViewEntryActivity
import com.jksalcedo.passvault.viewmodel.CategoryViewModel
import com.jksalcedo.passvault.viewmodel.PasswordViewModel

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

        val prefsRepository = PreferenceRepository(this)
        val useBottomAppBar = prefsRepository.getUseBottomAppBar()

        if (useBottomAppBar) {
            binding.toolbar.visibility = View.GONE
            binding.bottomAppBar.visibility = View.VISIBLE
            setSupportActionBar(binding.bottomAppBar)

            // Ensure FAB is anchored to BottomAppBar
            val params =
                binding.fabAdd.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
            params.anchorId = binding.bottomAppBar.id
            params.setMargins(0, 0, 0, 0)
            binding.fabAdd.layoutParams = params

            // Add top padding to chips to avoid status bar overlap
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                val statusBarHeight = resources.getDimensionPixelSize(resourceId)
                binding.chipScrollView.setPadding(
                    binding.chipScrollView.paddingLeft,
                    statusBarHeight + 16, // extra padding
                    binding.chipScrollView.paddingRight,
                    binding.chipScrollView.paddingBottom
                )
            }
        } else {
            binding.toolbar.visibility = View.VISIBLE
            binding.bottomAppBar.visibility = View.GONE
            setSupportActionBar(binding.toolbar)

            // Remove FAB anchor
            val params =
                binding.fabAdd.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
            params.anchorId = View.NO_ID
            params.gravity = Gravity.BOTTOM or Gravity.END
            val margin = (16 * resources.displayMetrics.density).toInt()
            params.setMargins(0, 0, margin, margin)
            binding.fabAdd.layoutParams = params
        }

        adapter = PVAdapter(this)
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter

        // View the entry
        adapter.onItemClick = { entry ->
            startActivity(ViewEntryActivity.createIntent(this, entry))
        }

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        var pendingEmptyStateRunnable: Runnable? = null
        viewModel.filteredEntries.observe(this) { list ->
            adapter.submitList(list)

            // Cancel any pending empty state show
            pendingEmptyStateRunnable?.let { binding.root.removeCallbacks(it) }

//            val showEmpty = list.isEmpty() && !viewModel.isSearching()
//            val showNotFound = list.isEmpty() && viewModel.isSearching()

            if (list.isNotEmpty()) {
                binding.contentMain.tvMessage.visibility = View.GONE
                binding.contentMain.ivIllustration.visibility = View.GONE
            } else {
                // Delay showing empty state to avoid flash on initial load
                pendingEmptyStateRunnable = Runnable {
                    if (viewModel.filteredEntries.value?.isEmpty() == true) {
                        binding.contentMain.tvMessage.visibility = View.VISIBLE
                        binding.contentMain.ivIllustration.visibility = View.VISIBLE
                        if (viewModel.isSearching()) {
                            binding.contentMain.ivIllustration.setImageResource(R.drawable.il_not_found)
                            binding.contentMain.tvMessage.text =
                                getString(R.string.no_results_found)
                        } else {
                            binding.contentMain.ivIllustration.setImageResource(R.drawable.il_no_entries)
                            binding.contentMain.tvMessage.text =
                                getString(R.string.click_the_button_to_add_your_first_password)
                        }
                    }
                }
                binding.root.postDelayed(pendingEmptyStateRunnable, 150)
            }
        }

        // Add entry
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
        // Delay showing FAB until icon is loaded to prevent flash on older Android
        binding.fabAdd.post { binding.fabAdd.visibility = View.VISIBLE }

        // Dynamically load category chips from database
        categoryViewModel.allCategories.observe(this) { categories ->
            binding.chipGroupCategories.removeAllViews()

            // Build category color map and pass to adapter
            val categoryColors = categories.associate { it.name to it.colorHex }
            adapter.setCategoryColors(categoryColors)

            // Add "All" chip
            val chipAll =
                layoutInflater.inflate(R.layout.chip, binding.chipGroupCategories, false) as Chip
            chipAll.apply {
                id = View.generateViewId()
                text = buildString {
                    append("All (")
                    append(viewModel.allEntries.value?.size ?: 0)
                    append(")")
                }
                isCheckable = true
                isChecked = true
                setChipBackgroundColorResource(R.color.chip_background_selector)
            }
            binding.chipGroupCategories.addView(chipAll)

            // Add category chips
            categories.forEach { category ->
                val chip = layoutInflater.inflate(
                    R.layout.chip,
                    binding.chipGroupCategories,
                    false
                ) as Chip
                chip.apply {
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
                    setChipBackgroundColorResource(R.color.chip_background_selector)
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
        viewModel.setSearchQuery(query ?: "")
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnSearchClickListener {
            binding.fabAdd.visibility = View.INVISIBLE
        }
        searchView.setOnCloseListener {
            binding.fabAdd.visibility = View.VISIBLE
            false
        }
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