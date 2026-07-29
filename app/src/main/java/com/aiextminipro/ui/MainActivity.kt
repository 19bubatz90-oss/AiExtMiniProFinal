package com.aiextminipro.ui
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiextminipro.R
import com.aiextminipro.repository.ChatRepository
import com.aiextminipro.viewmodel.ChatViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter
    private lateinit var inputEdit: EditText
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage(); applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val repo = ChatRepository(this)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return ChatViewModel(repo) as T
            }
        }).get(ChatViewModel::class.java)
        initViews(); setupRecyclerView(); observeViewModel(); setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        inputEdit = findViewById(R.id.inputEdit)
        sendButton = findViewById(R.id.sendButton)
        progressBar = ProgressBar(this).apply { visibility = View.GONE }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) {
            adapter.submitList(it)
            recyclerView.scrollToPosition(it.size - 1)
        }
        viewModel.isLoading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) {
            it?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun setupListeners() {
        sendButton.setOnClickListener { sendMessage() }
        inputEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage(); true
            } else false
        }
    }

    private fun sendMessage() {
        val text = inputEdit.text.toString().trim()
        if (text.isNotEmpty()) { inputEdit.text.clear(); viewModel.sendMessage(text) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change_key -> showApiKeyDialog()
            R.id.menu_clear_history -> { viewModel.clearAllData(); Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show() }
            R.id.menu_toggle_language -> toggleLanguage()
            R.id.menu_toggle_darkmode -> toggleDarkMode()
            R.id.menu_toggle_nologging -> {
                viewModel.setNonLogging(!viewModel.isNonLogging)
                Toast.makeText(this, if (viewModel.isNonLogging) R.string.non_logging_active else R.string.logging_active, Toast.LENGTH_SHORT).show()
            }
            R.id.menu_toggle_history -> {
                viewModel.setSaveHistory(!viewModel.saveHistoryEnabled)
                Toast.makeText(this, if (viewModel.saveHistoryEnabled) R.string.logging_active else R.string.history_cleared, Toast.LENGTH_SHORT).show()
            }
            R.id.menu_export -> {
                Toast.makeText(this, "Export: ${viewModel.messages.value?.size ?: 0} Nachrichten", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun showApiKeyDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle(R.string.api_key_dialog_title)
            .setMessage(R.string.api_key_dialog_message)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.setApiKey(input.text.toString().trim())
                Toast.makeText(this, R.string.api_key_saved, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toggleLanguage() {
        val new = if (Locale.getDefault().language == "de") "en" else "de"
        Locale.setDefault(Locale(new))
        val config = resources.configuration
        config.setLocale(Locale(new))
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun toggleDarkMode() {
        val mode = AppCompatDelegate.getDefaultNightMode()
        AppCompatDelegate.setDefaultNightMode(
            if (mode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        recreate()
    }

    private fun applyLanguage() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "de") ?: "de"
        Locale.setDefault(Locale(lang))
        val config = resources.configuration
        config.setLocale(Locale(lang))
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getBoolean("dark", false)) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
