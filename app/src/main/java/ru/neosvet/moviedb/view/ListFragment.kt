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
import ru.neosvet.moviedb.list.CatalogCallbacks
import ru.neosvet.moviedb.list.MovieItem
import ru.neosvet.moviedb.list.MoviesAdapter
import ru.neosvet.moviedb.model.ListModel
import ru.neosvet.moviedb.model.ListState
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.SettingsUtils

class ListFragment : Fragment(), CatalogCallbacks, Observer<ListState> {
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
    private val catalogAdapter = CatalogAdapter()
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
        binding.rvCatalog.adapter = catalogAdapter
        savedInstanceState?.let {
            query = it.getString(ARG_SEARCH)
        }
        model.adult = settings.getAdult()
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
        else if (catalogAdapter.itemCount < COUNT_LIST)
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
            catalogAdapter.clear()
            catalogAdapter.notifyDataSetChanged()
            loadNextList()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSearch(query: String) {
        isLastSearch = false
        this.query = query
        catalogAdapter.clear()
        loadNextList()
    }

    private fun loadNextList() {
        if (query == null) {
            when (catalogAdapter.itemCount) {
                0 -> model.loadUpcoming(isRefresh, 1)
                1 -> model.loadPopular(isRefresh, 1)
                2 -> model.loadTopRated(isRefresh, 1)
            }
            return
        }
        if (catalogAdapter.itemCount > 0) {
            finishLoad()
            return
        }
        query?.let {
            model.search(it, 1, !isLastSearch)
        }
    }

    private fun showList(index: Int, catalog: CatalogEntity, list: List<MovieEntity>) {
        val adapter = MoviesAdapter(catalog, this)

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
        if (index >= catalogAdapter.itemCount) //is new item
            catalogAdapter.addItem(getTranslate(catalog.name), adapter)
        else
            catalogAdapter.replaceItem(index, adapter)
    }

    private fun getTranslate(title: String): String {
        return when (title) {
            ListModel.UPCOMING -> getString(R.string.upcoming)
            ListModel.POPULAR -> getString(R.string.popular)
            ListModel.TOP_RATED -> getString(R.string.top_rated)
            else -> if (title.contains(ListModel.SEARCH))
                getString(R.string.search_result)
            else
                title
        }
    }

    override fun onItemClicked(id: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MovieFragment.newInstance(id))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
    }

    override fun onPageClicked(page: Int, adapter: MoviesAdapter) {
        when (adapter.getName()) {
            ListModel.UPCOMING -> model.loadUpcoming(isRefresh, page)
            ListModel.POPULAR -> model.loadPopular(isRefresh, page)
            ListModel.TOP_RATED -> model.loadTopRated(isRefresh, page)
            else -> query?.let {
                model.search(it, page, !isLastSearch)
            }
        }
    }

    override fun onChanged(state: ListState) {
        when (state) {
            is ListState.Success -> {
                showList(state.index, state.catalog, state.list)
                if (catalogAdapter.itemCount == COUNT_LIST)
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
                catalogAdapter.clear()
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
            catalogAdapter.clear()
            catalogAdapter.notifyDataSetChanged()
            isLastSearch = true
            loadNextList()
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
            catalogAdapter.clear()
            catalogAdapter.notifyDataSetChanged()
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