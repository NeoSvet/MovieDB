package ru.neosvet.moviedb.view

import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.ActivityMainBinding
import ru.neosvet.moviedb.utils.ConnectUtils
import ru.neosvet.moviedb.view.extension.OnBackFragment

class MainActivity : AppCompatActivity() {
    companion object {
        val MAIN_STACK = "main"
    }

    private val TAG_LIST = "list"
    private lateinit var binding: ActivityMainBinding
    private val recConnect = ConnectUtils()
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.getRoot())

        registerReceiver(recConnect, IntentFilter(CONNECTIVITY_ACTION))
        if (savedInstanceState == null) {
            openList(false)
        }
        initButtons()
        binding.ivBigPoster.setOnClickListener {
            binding.ivBigPoster.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (binding.ivBigPoster.visibility == View.VISIBLE) {
            binding.ivBigPoster.visibility = View.GONE
            return
        }
        if (snackbar != null) {
            hideError()
            return
        }
        if (binding.pStatus.visibility == View.VISIBLE)
            return
        supportFragmentManager.fragments.forEach {
            if (it.isVisible && it is OnBackFragment) {
                if (!it.onBackPressed())
                    return
            }
        }
        super.onBackPressed()
    }

    fun getBitPoster() = binding.ivBigPoster

    private fun initButtons() {
        binding.btnMain.setOnClickListener {
            val frag = supportFragmentManager.findFragmentByTag(TAG_LIST)
            if (frag == null) {
                clearStack()
                openList(false)
            } else {
                if (frag.isRemoving)
                    clearStack()
                val list = frag as ListFragment
                list.closeSearch()
            }
        }
        binding.btnSearch.setOnClickListener {
            val frag = supportFragmentManager.findFragmentByTag(TAG_LIST)
            if (frag == null) {
                clearStack()
                openList(true)
            } else {
                if (frag.isRemoving)
                    clearStack()
                val list = frag as ListFragment
                list.openSearch()
            }
        }
        binding.btnSettings.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(MAIN_STACK).commit()
        }
    }

    private fun clearStack() {
        supportFragmentManager.popBackStack(
            MAIN_STACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun openList(withSearch: Boolean) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ListFragment.newInstance(withSearch), TAG_LIST)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(recConnect)
    }

    fun startLoad() {
        binding.pStatus.visibility = View.VISIBLE
        hideError()
    }

    fun finishLoad() {
        binding.pStatus.visibility = View.GONE
    }

    fun showError(
        message: String?,
        titleAction: String?,
        eventAction: View.OnClickListener?
    ) {
        val msg = message?.let {
            ": " + it
        } ?: ""
        snackbar = Snackbar.make(
            binding.panelButtons,
            getString(R.string.error) + msg,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(titleAction, eventAction)
        snackbar?.show()
    }

    fun hideError() {
        snackbar?.dismiss()
        snackbar = null
    }
}