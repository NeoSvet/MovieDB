package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.ActivityMainBinding
import ru.neosvet.moviedb.list.ListCallbacks
import ru.neosvet.moviedb.list.MovieItem
import ru.neosvet.moviedb.list.MoviesAdapter
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie
import java.util.*

class MainActivity : AppCompatActivity(), ListCallbacks, Observer<MovieState> {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MoviesAdapter
    private lateinit var model: MovieModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.getRoot())

        adapter = MoviesAdapter(this)
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

    private fun loadList() {
        model = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(MovieModel::class.java)
        model.loadList()
    }

    private fun showList(list: ArrayList<Movie>) {
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
    }

    private fun initViews() {
        binding.btnSearch.setOnClickListener {
            Toast.makeText(this, binding.etSearch.text, Toast.LENGTH_SHORT).show()
        }
        binding.rvMovies.layoutManager = LinearLayoutManager(this)
        binding.rvMovies.adapter = adapter;
    }

    override fun onItemClicked(id: Int) {

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
                    binding.rvMovies, getString(R.string.error)
                            + ": " + state.error.message,
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    }
}