package ru.neosvet.moviedb.view

import android.animation.ValueAnimator
import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.ActivityMainBinding
import ru.neosvet.moviedb.utils.ConnectUtils
import ru.neosvet.moviedb.utils.ImageUtils
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
        binding.pBigImage.setOnClickListener {
            binding.pBigImage.visibility = View.GONE
        }
        binding.pStatus.setOnClickListener {
            //for block buttons
        }
    }

    override fun onBackPressed() {
        if (binding.pBigImage.visibility == View.VISIBLE) {
            binding.pBigImage.visibility = View.GONE
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

    fun loadBigImage(url: String) {
        with(binding.ivBigImage) {
            ImageUtils.loadBig(url, this)
            getLayoutParams().width = 100
            getLayoutParams().height = 100
            requestLayout()
            binding.pBigImage.visibility = View.VISIBLE

            val parent = binding.root as View
            val toValue: Int
            val isWidth: Boolean
            if (parent.width < parent.height) {
                toValue = parent.width
                isWidth = true
            } else {
                toValue = parent.height
                isWidth = false
            }

            val animator = ValueAnimator.ofInt(100, toValue)
            animator.duration = 1200
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { animation ->
                if (isWidth) {
                    getLayoutParams().width = animation.animatedValue as Int
                    getLayoutParams().height =
                        (getLayoutParams().width * 1.33f).toInt()
                } else {
                    getLayoutParams().height = animation.animatedValue as Int
                    getLayoutParams().width =
                        (getLayoutParams().height * 0.66f).toInt()
                }
                requestLayout()
            }
            animator.start()
        }
    }
}