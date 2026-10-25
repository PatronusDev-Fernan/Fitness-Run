package com.example.fitnessrun

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.fitnessrun.databinding.ActivityFoodPreferencesBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FoodPreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodPreferencesBinding
    private val selectedVegetables = linkedSetOf<String>()
    private val selectedFruits = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSystemBars()
        configureDropdowns()
        configureActions()
        selectedVegetables.addAll(savedInstanceState?.getStringArrayList("selectedVegetables").orEmpty())
        selectedFruits.addAll(savedInstanceState?.getStringArrayList("selectedFruits").orEmpty())
        renderChips(selectedVegetables, binding.vegetableChipGroup)
        renderChips(selectedFruits, binding.fruitChipGroup)
        binding.dislikesEditText.doAfterTextChanged { refreshChoices() }
        binding.restrictionsEditText.doAfterTextChanged { refreshChoices() }
        binding.dietTypeDropdown.doAfterTextChanged { refreshChoices() }
        if (savedInstanceState == null) refreshChoices()
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.foodRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun configureDropdowns() {
        configureDropdown(binding.dietTypeDropdown, R.array.diet_types)
        configureDropdown(binding.foodGoalDropdown, R.array.food_goals)
    }

    private fun configureDropdown(dropdown: android.widget.AutoCompleteTextView, arrayId: Int) {
        val options = resources.getStringArray(arrayId)
        dropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options))
        dropdown.setText(options.first(), false)
    }

    private fun configureActions() {
        binding.selectVegetablesButton.setOnClickListener {
            showFoodSelector(R.string.food_vegetables, FoodCatalog.Group.VEGETABLES, selectedVegetables, binding.vegetableChipGroup)
        }
        binding.selectFruitsButton.setOnClickListener {
            showFoodSelector(R.string.food_fruits, FoodCatalog.Group.FRUITS, selectedFruits, binding.fruitChipGroup)
        }
        binding.selectOtherFoodsButton.setOnClickListener { showOtherFoods() }
        binding.backButton.setOnClickListener { finish() }
        binding.finishButton.setOnClickListener {
            refreshChoices()
            val summary = Intent(this, RegistrationSummaryActivity::class.java).apply {
                intent.extras?.let { putExtras(it) }
                putExtra(RegistrationKeys.VEGETABLES, selectedVegetables.joinToString())
                putExtra(RegistrationKeys.FRUITS, selectedFruits.joinToString())
                putExtra(RegistrationKeys.FAVORITES, binding.favoritesEditText.text?.toString()?.trim().orEmpty())
                putExtra(RegistrationKeys.DISLIKES, binding.dislikesEditText.text?.toString()?.trim().orEmpty())
                putExtra(RegistrationKeys.RESTRICTIONS, binding.restrictionsEditText.text?.toString()?.trim().orEmpty())
                putExtra(RegistrationKeys.DIET_TYPE, binding.dietTypeDropdown.text.toString())
                putExtra(RegistrationKeys.FOOD_GOAL, binding.foodGoalDropdown.text.toString())
            }
            startActivity(summary)
        }
    }

    private fun showFoodSelector(
        titleId: Int,
        group: FoodCatalog.Group,
        selectedItems: LinkedHashSet<String>,
        chipGroup: ChipGroup
    ) {
        refreshChoices()
        val options = FoodCatalog.filter(currentProfile()).available.filter { it.group == group }.map { it.name }.toTypedArray()
        if (options.isEmpty()) {
            MaterialAlertDialogBuilder(this).setMessage(R.string.food_no_options).setPositiveButton(R.string.dialog_accept, null).show()
            return
        }
        val checkedItems = BooleanArray(options.size) { options[it] in selectedItems }

        MaterialAlertDialogBuilder(this)
            .setTitle(titleId)
            .setMultiChoiceItems(options, checkedItems) { _, index, checked ->
                checkedItems[index] = checked
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_accept) { _, _ ->
                selectedItems.clear()
                options.forEachIndexed { index, option ->
                    if (checkedItems[index]) selectedItems.add(option)
                }
                renderChips(selectedItems, chipGroup)
            }
            .show()
    }

    private fun currentProfile(): Map<String, String> = mapOf(
        RegistrationKeys.ALLERGY to intent.getStringExtra(RegistrationKeys.ALLERGY).orEmpty(),
        RegistrationKeys.RESTRICTIONS to binding.restrictionsEditText.text.toString(),
        RegistrationKeys.DISLIKES to binding.dislikesEditText.text.toString(),
        RegistrationKeys.DIET_TYPE to binding.dietTypeDropdown.text.toString()
    )

    private fun refreshChoices() {
        val result = FoodCatalog.filter(currentProfile())
        val names = result.available.map { it.name }.toSet()
        selectedVegetables.retainAll(names)
        selectedFruits.retainAll(names)
        renderChips(selectedVegetables, binding.vegetableChipGroup)
        renderChips(selectedFruits, binding.fruitChipGroup)
        binding.foodFilterNotice.text = if (result.unresolved.isEmpty())
            getString(R.string.food_filter_count, result.available.size, result.excluded.size)
        else getString(R.string.food_filter_review, result.unresolved.joinToString())
    }

    private fun showOtherFoods() {
        val options = FoodCatalog.filter(currentProfile()).available
            .filter { it.group !in setOf(FoodCatalog.Group.FRUITS, FoodCatalog.Group.VEGETABLES) }
        val previous = binding.favoritesEditText.text.toString().split(',').map { it.trim() }.filter { it.isNotBlank() }
        val selected = options.map { it.name in previous }.toBooleanArray()
        MaterialAlertDialogBuilder(this).setTitle(R.string.food_other_title)
            .setMultiChoiceItems(options.map { "${it.group.label} · ${it.name}" }.toTypedArray(), selected) { _, index, checked -> selected[index] = checked }
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_accept) { _, _ ->
                val catalogNames = FoodCatalog.foods.map { it.name }.toSet()
                val custom = previous.filter { it !in catalogNames }
                binding.favoritesEditText.setText((custom + options.filterIndexed { index, _ -> selected[index] }.map { it.name }).joinToString())
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("selectedVegetables", ArrayList(selectedVegetables))
        outState.putStringArrayList("selectedFruits", ArrayList(selectedFruits))
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        refreshChoices()
    }

    private fun renderChips(items: LinkedHashSet<String>, group: ChipGroup) {
        group.removeAllViews()
        items.toList().forEach { item ->
            val chip = Chip(this).apply {
                text = item
                isCloseIconVisible = true
                isCheckable = false
                setOnCloseIconClickListener {
                    items.remove(item)
                    group.removeView(this)
                }
            }
            group.addView(chip)
        }
    }
}
