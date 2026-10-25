package com.example.fitnessrun

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnessrun.databinding.ActivityProgressHistoryBinding

class ProgressHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.historyRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.backButton.setOnClickListener { finish() }
        binding.retryButton.setOnClickListener { loadHistory() }
        loadHistory()
    }

    private fun loadHistory() {
        binding.historyContainer.removeAllViews()
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.retryButton.visibility = View.GONE
        val uid = DemoSession.cloudUid
        if (uid == null) {
            showEntry(CloudProgress.today(), DemoSession.progress.completedRoutines.size,
                DemoSession.progress.completedMeals.size, DemoSession.weekendRoutine, DemoSession.weekendFood)
            finishLoading(binding.historyContainer.childCount == 0)
            return
        }
        CloudProgress.history(uid).addOnSuccessListener { result ->
            if (isFinishing || isDestroyed || uid != DemoSession.cloudUid) return@addOnSuccessListener
            result.documents.forEach { document ->
                val routines = (document.get("completedRoutines") as? List<*>)?.size ?: 0
                val meals = (document.get("completedMeals") as? List<*>)?.size ?: 0
                showEntry(document.getString("date") ?: document.id, routines, meals,
                    document.getString("weekendRoutine") ?: "Sin seleccionar",
                    document.getString("weekendFood") ?: "Sin seleccionar")
            }
            finishLoading(result.isEmpty)
        }.addOnFailureListener { error ->
            if (isFinishing || isDestroyed || uid != DemoSession.cloudUid) return@addOnFailureListener
            finishLoading(false)
            binding.errorText.setText(CloudErrors.message(error))
            binding.errorText.visibility = View.VISIBLE
            binding.retryButton.visibility = View.VISIBLE
        }
    }

    private fun showEntry(date: String, routines: Int, meals: Int, weekendRoutine: String, weekendFood: String) {
        val view = layoutInflater.inflate(R.layout.item_progress_history, binding.historyContainer, false)
        view.findViewById<TextView>(R.id.dateText).text = date
        view.findViewById<TextView>(R.id.progressText).text = "$routines rutinas · $meals comidas registradas"
        view.findViewById<TextView>(R.id.weekendText).text = "Fin de semana: $weekendRoutine · $weekendFood"
        binding.historyContainer.addView(view)
    }

    private fun finishLoading(empty: Boolean) {
        binding.loadingIndicator.visibility = View.GONE
        binding.emptyText.visibility = if (empty) View.VISIBLE else View.GONE
    }
}
