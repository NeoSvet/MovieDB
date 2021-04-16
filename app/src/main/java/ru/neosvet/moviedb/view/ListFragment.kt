package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie
import java.util.*

private const val MAIN_STACK = "main"

class ListFragment : Fragment(), ListCallbacks, Observer<MovieState> {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val catalog = CatalogAdapter()
    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }
    private val finalId = 3
    private var lastId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    override fun onResume() {
        super.onResume()
        model.getState().observe(this, this)
    }

    override fun onPause() {
        super.onPause()
        model.getState().removeObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list, menu)
        val search = menu.findItem(R.id.search)
        val searchText = search.actionView as SearchView
        searchText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initList() {
        binding.rvCatalog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCatalog.adapter = catalog

        lastId = catalog.itemCount
        if (lastId < finalId)
            model.loadList(++lastId)
        else
            catalog.notifyDataSetChanged()
    }

    private fun showList(title: String, list: ArrayList<Movie>) {
        val adapter = MoviesAdapter(this)
        for (movie in list) {
            adapter.addItem(
                MovieItem(
                    id = movie.id, title = movie.title,
                    description = movie.date,
                    poster = movie.poster
                )
            )
        }
        catalog.addItem(title, adapter)
        catalog.notifyDataSetChanged()
    }

    override fun onItemClicked(id: Int) {
        activity?.supportFragmentManager?.let {
            it.beginTransaction()
                .replace(R.id.container, MovieFragment.newInstance(id))
                .addToBackStack(MAIN_STACK)
                .commit()
        }
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessList -> {
                binding.tvStatus.visibility = View.GONE
                showList(state.title, state.list)
                if (lastId < finalId)
                    model.loadList(++lastId)
                else
                    model.getState().value = MovieState.Finished
            }
            is MovieState.Loading -> {
                binding.tvStatus.visibility = View.VISIBLE
            }
            is MovieState.Error -> {
                binding.tvStatus.visibility = View.GONE
                binding.rvCatalog.showError(state.error.message)
            }
        }
    }
}