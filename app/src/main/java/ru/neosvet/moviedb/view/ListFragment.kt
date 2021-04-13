package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentListBinding
import ru.neosvet.moviedb.list.CatalogAdapter
import ru.neosvet.moviedb.list.ListCallbacks
import ru.neosvet.moviedb.list.MovieItem
import ru.neosvet.moviedb.list.MoviesAdapter
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.SettingsUtils
import java.util.*

class ListFragment : Fragment(), ListCallbacks, Observer<MovieState> {
    companion object {
        private val ARG_SEARCH = "search"
        fun newInstance(withSearch: Boolean) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SEARCH, withSearch)
                }
            }
    }

    private val COUNT_LIST = 6
    private lateinit var searcher: SearchView
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private var snackbar: Snackbar? = null
    private val catalog by lazy {
        CatalogAdapter(requireContext())
    }
    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }
    private val settings: SettingsUtils by lazy {
        SettingsUtils(requireContext())
    }
    private var query: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCatalog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCatalog.adapter = catalog
        savedInstanceState?.let {
            query = it.getString(ARG_SEARCH)
        }
    }

    override fun onResume() {
        super.onResume()
        model.getState().observe(this, this)
        if (catalog.itemCount < COUNT_LIST)
            loadNextList()
    }

    override fun onPause() {
        super.onPause()
        model.getState().removeObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_SEARCH, query)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list, menu)
        val search = menu.findItem(R.id.search)
        searcher = search.actionView as SearchView
        searcher.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                startSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        searcher.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                query = null
                return false
            }
        })
        query?.let {
            searcher.setQuery(it, false)
        }
        arguments?.let {
            if (it.getBoolean(ARG_SEARCH))
                openSearch()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun startSearch(query: String) {
        this.query = query
        catalog.clear()
        loadNextList()
    }

    private fun loadNextList() {
        if (query == null) {
            when (catalog.itemCount) {
                0 -> model.loadUpcoming(settings.getAdult())
                2 -> model.loadPopular(settings.getAdult())
                4 -> model.loadTopRated(settings.getAdult())
            }
            return
        }
        query?.let {
            model.search(it, catalog.itemCount / 2 + 1, settings.getAdult())
        }
    }

    private fun showList(title: String, list: ArrayList<Movie>) {
        val adapter = MoviesAdapter(this)
        for (movie in list) {
            adapter.addItem(
                MovieItem(
                    id = movie.id, title = movie.title,
                    description = movie.date,
                    poster = movie.poster,
                    vote = (movie.vote * 10).toInt()
                )
            )
        }
        catalog.addItem(getTranslate(title), adapter)
        catalog.notifyDataSetChanged()
    }

    private fun getTranslate(title: String): String {
        return when (title) {
            MovieModel.UPCOMING -> getString(R.string.upcoming)
            MovieModel.POPULAR -> getString(R.string.popular)
            MovieModel.TOP_RATED -> getString(R.string.top_rated)
            MovieModel.SEARCH -> getString(R.string.search_result)
            else -> title
        }
    }

    override fun onItemClicked(id: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MovieFragment.newInstance(id))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessList -> {
                showList(state.title, state.list)
                if (catalog.itemCount == COUNT_LIST) {
                    binding.tvStatus.visibility = View.GONE
                    model.getState().value = MovieState.Finished
                } else
                    loadNextList()
            }
            is MovieState.Loading -> {
                binding.tvStatus.visibility = View.VISIBLE
                snackbar?.dismiss()
            }
            is MovieState.Error -> {
                binding.tvStatus.visibility = View.GONE
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                snackbar = binding.rvCatalog.showError(message,
                    getString(R.string.repeat), {
                        catalog.clear()
                        loadNextList()
                    })
            }
        }
    }

    fun openSearch() {
        arguments?.putBoolean(ARG_SEARCH, true)
        searcher.setIconified(false)
        searcher.clearFocus()
    }

    fun closeSearch() {
        if (query != null) {
            query = null
            searcher.setQuery(query, false)
            searcher.clearFocus()
            catalog.clear()
            loadNextList()
        }
        arguments?.putBoolean(ARG_SEARCH, false)
        searcher.setIconified(true)
    }
}