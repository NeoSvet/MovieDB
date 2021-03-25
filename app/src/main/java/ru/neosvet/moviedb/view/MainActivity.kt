package ru.neosvet.moviedb.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.ListCallbacks
import ru.neosvet.moviedb.list.MovieItem
import ru.neosvet.moviedb.list.MoviesAdapter
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.Movie
import java.util.ArrayList

class MainActivity : AppCompatActivity(), ListCallbacks, Observer<MovieState> {
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var tvStatus: MaterialTextView
    private lateinit var rvMovies: RecyclerView
    private lateinit var adapter: MoviesAdapter
    private lateinit var model: MovieModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        tvStatus = findViewById(R.id.tvStatus);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener {
            Toast.makeText(this, etSearch.text, Toast.LENGTH_SHORT).show()
        }
        rvMovies = findViewById(R.id.rvMovies);
        rvMovies.layoutManager = LinearLayoutManager(this)
        rvMovies.adapter = adapter;
    }

    override fun onItemClicked(id: Int) {

    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessList -> {
                tvStatus.visibility = View.GONE
                showList(state.list)
            }
            is MovieState.Loading -> {
                tvStatus.visibility = View.VISIBLE
            }
            is MovieState.Error -> {
                tvStatus.visibility = View.GONE
                val msg = state.error.message
                Snackbar.make(
                    rvMovies, getString(R.string.error)
                            + ": " + msg,
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    }
}