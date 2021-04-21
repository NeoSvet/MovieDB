package ru.neosvet.moviedb.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentListBinding
import ru.neosvet.moviedb.list.CatalogAdapter
import ru.neosvet.moviedb.list.ListCallbacks
import ru.neosvet.moviedb.list.MovieItem
import ru.neosvet.moviedb.list.MoviesAdapter
import ru.neosvet.moviedb.model.ListModel
import ru.neosvet.moviedb.model.ListState
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.SettingsUtils

class ListFragment : Fragment(), ListCallbacks, Observer<ListState> {
    companion object {
        private val ARG_SEARCH = "search"
        fun newInstance(withSearch: Boolean) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SEARCH, withSearch)
                }
            }
    }

    private val COUNT_LIST = 3
    private var searcher: SearchView? = null
    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val catalog = CatalogAdapter()
    private val model: ListModel by lazy {
        ViewModelProvider(this).get(ListModel::class.java)
    }
    private val settings: SettingsUtils by lazy {
        SettingsUtils(requireContext())
    }
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences(
            ListFragment::class.java.simpleName,
            Context.MODE_PRIVATE
        )
    }
    private var query: String? = null
    private var isLastSearch = false
    private var isRefresh = false

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

        var isSearch = false
        arguments?.let {
            if (it.getBoolean(ARG_SEARCH))
                isSearch = true
        }
        if (isSearch)
            openSearch()
        else if (catalog.itemCount < COUNT_LIST)
            loadNextList()
    }

    override fun onPause() {
        super.onPause()
        model.getState().removeObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        saveQuery()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_SEARCH, query)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list, menu)
        val search = menu.findItem(R.id.search)
        searcher = search.actionView as SearchView
        searcher?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                startSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        searcher?.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                query = null
                return false
            }
        })
        query?.let {
            searcher?.setQuery(it, false)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh) {
            isRefresh = true
            catalog.clear()
            catalog.notifyDataSetChanged()
            loadNextList()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSearch(query: String) {
        isLastSearch = false
        this.query = query
        catalog.clear()
        loadNextList()
    }

    private fun loadNextList() {
        if (query == null) {
            when (catalog.itemCount) {
                0 -> model.loadUpcoming(isRefresh, settings.getAdult())
                1 -> model.loadPopular(isRefresh, settings.getAdult())
                2 -> model.loadTopRated(isRefresh, settings.getAdult())
            }
            return
        }
        query?.let {
            if (isLastSearch)
                model.lastSearch(catalog.itemCount + 1)
            else
                model.search(it, catalog.itemCount + 1, settings.getAdult())
        }
    }

    private fun showList(title: String, list: List<MovieEntity>) {
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
            ListModel.UPCOMING -> getString(R.string.upcoming)
            ListModel.POPULAR -> getString(R.string.popular)
            ListModel.TOP_RATED -> getString(R.string.top_rated)
            else -> if (title.contains(ListModel.SEARCH))
                getResultSearchTitle(title)
            else
                title
        }
    }

    private fun getResultSearchTitle(title: String): String {
        return getString(R.string.search_result) +
                title.substring(
                    title.indexOf(ListModel.SEARCH) +
                            ListModel.SEARCH.length
                )
    }

    override fun onItemClicked(id: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MovieFragment.newInstance(id))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
    }

    override fun onChanged(state: ListState) {
        when (state) {
            is ListState.Success -> {
                showList(state.title, state.list)
                if (catalog.itemCount == COUNT_LIST)
                    finishLoad()
                else
                    loadNextList()
            }
            is ListState.Loading -> {
                main.startLoad()
            }
            is ListState.Error -> {
                finishLoad()
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                showError(message)
            }
        }
    }

    private fun showError(message: String?) {
        main.showError(message,
            getString(R.string.repeat), {
                catalog.clear()
                loadNextList()
            })
    }

    private fun finishLoad() {
        main.finishLoad()
        model.getState().value = ListState.Finished
        isRefresh = false
    }

    fun openSearch() {
        if (query == null)
            query = pref.getString(ARG_SEARCH, null)
        if (query != null) {
            catalog.clear()
            catalog.notifyDataSetChanged()
            isLastSearch = true
            model.lastSearch(1)
            searcher?.setQuery(query, false)
        }
        searcher?.setIconified(false)
    }

    fun closeSearch() {
        if (query != null) {
            saveQuery()
            query = null
            searcher?.setQuery(query, false)
            searcher?.clearFocus()
            catalog.clear()
            catalog.notifyDataSetChanged()
            loadNextList()
        }
        arguments?.putBoolean(ARG_SEARCH, false)
        searcher?.setIconified(true)
    }

    private fun saveQuery() {
        if (query != null)
            pref.edit().putString(ARG_SEARCH, query).apply()
    }
}