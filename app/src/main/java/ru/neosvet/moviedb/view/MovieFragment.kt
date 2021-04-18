package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.PosterUtils
import ru.neosvet.moviedb.view.extension.OnBackFragment
import ru.neosvet.moviedb.view.extension.hideKeyboard
import ru.neosvet.moviedb.view.extension.showError
import ru.neosvet.moviedb.view.extension.showKeyboard

class MovieFragment : OnBackFragment(), Observer<MovieState> {
    companion object {
        private val ARG_ID = "movie_id"
        fun newInstance(movieId: Int) =
            MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, movieId)
                }
            }
    }

    private var movieId: Int? = null
    private var movie: MovieEntity? = null
    private var _binding: FragmentMovieBinding? = null
    private lateinit var itemEdit: MenuItem
    private lateinit var itemSave: MenuItem
    private lateinit var des: String
    private lateinit var note: String
    private val binding get() = _binding!!
    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            movieId = it.getInt(ARG_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDetails();
    }

    override fun onBackPressed(): Boolean {
        if (itemEdit.isVisible)
            return true
        closeEditNote()
        return false
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
        inflater.inflate(R.menu.movie, menu)
        itemEdit = menu.findItem(R.id.edit)
        itemSave = menu.findItem(R.id.save)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (movieId == null)
            return super.onOptionsItemSelected(item)
        if (item.itemId == R.id.message) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, ContactsFragment.newInstance(initMessage()))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
            return super.onOptionsItemSelected(item)
        }
        with(binding) {
            if (itemEdit.isVisible) {
                etNote.setText(note)
                tvDescription.visibility = View.GONE
                etNote.visibility = View.VISIBLE
                itemEdit.setVisible(false)
                itemSave.setVisible(true)
                etNote.requestFocus()
                etNote.setSelection(note.length)
                requireActivity().showKeyboard(etNote)
            } else {
                requireActivity().hideKeyboard(etNote)
                note = etNote.text.toString()
                movieId?.let {
                    model.addNote(it, note)
                }
                showDes()
                closeEditNote()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMessage(): String {
        movie?.let {
            return getString(R.string.recommend_movie) + it.title +
                    "\nhttps://www.themoviedb.org/movie/" + it.id
        }
        return ""
    }

    private fun closeEditNote() {
        binding.tvDescription.visibility = View.VISIBLE
        binding.etNote.visibility = View.GONE
        itemEdit.setVisible(true)
        itemSave.setVisible(false)
    }

    private fun loadDetails() {
        model.loadDetails(movieId)
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.Success -> {
                showItem(state.item)
            }
            is MovieState.Error -> {
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                binding.tvTitle.showError(message,
                    getString(R.string.repeat), { loadDetails() })
            }
        }
    }

    private fun showItem(item: MovieEntity) {
        movie = item
        with(binding) {
            PosterUtils.load(item.poster, ivPoster)
            tvTitle.text = item.title
            tvDate.text = getString(R.string.release_date) + item.date
            tvOriginal.text = item.original
            tvGenres.text = model.genresToString(item.genre_ids)
            des = item.description
            note = model.getNote(item.id)
            showDes()
            barVote.progress = (item.vote * 10).toInt()
            tvVote.text = "(${item.vote})"
        }
    }

    private fun showDes() {
        if (note.length == 0)
            binding.tvDescription.text = des
        else {
            val s = StringBuilder(des)
            s.appendLine()
            s.appendLine()
            s.appendLine(getString(R.string.my_note))
            s.append(note)
            binding.tvDescription.text = s.toString()
        }
    }
}