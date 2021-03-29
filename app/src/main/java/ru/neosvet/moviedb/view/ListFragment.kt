package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.MovieFragment
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

private const val MAIN_STACK = "main";

class ListFragment : Fragment(), ListCallbacks, Observer<MovieState> {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private var catalog = CatalogAdapter()
    private lateinit var model: MovieModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews();
        loadList();
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

    private fun loadList() {
        model = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(MovieModel::class.java)
        model.loadList()
    }

    private fun showList(list: ArrayList<Movie>) {
        val adapter = MoviesAdapter(this)
        for (movie in list) {
            adapter.addItem(
                MovieItem(
                    id = movie.id, title = movie.title,
                    description = movie.year.toString() + "\n" +
                            movie.country + "\n" + movie.genres,
                    poster = movie.poster
                )
            )
        }
        catalog.addItem("catalog title", adapter)
        catalog.notifyDataSetChanged()
    }

    private fun initViews() {
        binding.btnSearch.setOnClickListener {
            Toast.makeText(requireContext(), binding.etSearch.text, Toast.LENGTH_SHORT).show()
        }
        binding.rvCatalog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCatalog.adapter = catalog;
    }

    override fun onItemClicked(id: Int) {
        val manager = activity?.supportFragmentManager
        if (manager != null) {
            manager.beginTransaction()
                .replace(R.id.container, MovieFragment.newInstance(id))
                .addToBackStack(MAIN_STACK)
                .commit()
        }
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessList -> {
                binding.tvStatus.visibility = View.GONE
                showList(state.list)
            }
            is MovieState.Loading -> {
                binding.tvStatus.visibility = View.VISIBLE
            }
            is MovieState.Error -> {
                binding.tvStatus.visibility = View.GONE
                Snackbar.make(
                    binding.rvCatalog, getString(R.string.error)
                            + ": " + state.error.message,
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    }
}