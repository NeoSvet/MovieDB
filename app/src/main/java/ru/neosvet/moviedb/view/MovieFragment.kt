package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMovieBinding
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.model.MovieState
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.MyException
import ru.neosvet.moviedb.utils.ImageUtils
import ru.neosvet.moviedb.view.extension.OnBackFragment
import ru.neosvet.moviedb.view.extension.hideKeyboard
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
    private val binding get() = _binding!!
    private lateinit var itemEdit: MenuItem
    private lateinit var itemSave: MenuItem
    private lateinit var des: String
    private lateinit var note: String
    private lateinit var cast_ids: List<String>
    private lateinit var cast: List<String>
    private lateinit var crew_ids: List<String>
    private lateinit var crew: List<String>
    private val model: MovieModel by lazy {
        ViewModelProvider(this).get(MovieModel::class.java)
    }
    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
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
        loadDetails()
        initLink()
        binding.ivPoster.setOnClickListener {
            movie?.let {
                main.loadBigImage(it.poster)
            }
        }
    }

    private fun initLink() {
        binding.tvCountries.setOnClickListener {
            var s = binding.tvCountries.text
            s = s.substring(s.indexOf(":") + 2)
            if (s.contains(","))
                showMenu(s)
            else
                openMap(s)
        }
        binding.tvCrew.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PeopleFragment.newInstance(crew_ids, crew))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }
        binding.tvCast.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PeopleFragment.newInstance(cast_ids, cast))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }
        binding.tvTryLoadEn.setOnClickListener {
            movieId?.let { model.loadDetailsEn(it) }
        }
    }

    private fun showMenu(list: String) {
        val menu = PopupMenu(requireContext(), binding.tvCountries)
        list.split(",").forEach {
            menu.menu.add(it.trimStart())
        }
        menu.setOnMenuItemClickListener {
            openMap(it.title.toString())
            return@setOnMenuItemClickListener true
        }
        menu.show()
    }

    private fun openMap(country: String) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MapsFragment.newInstance(country))
            ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
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
        movieId?.let { model.loadDetails(it) }
    }

    override fun onChanged(state: MovieState) {
        when (state) {
            is MovieState.SuccessMovie -> {
                showMovie(state.movie)
            }
            is MovieState.SuccessDetails -> {
                showDetails(state.details)
            }
            is MovieState.SuccessAll -> {
                showMovie(state.movie)
                showDetails(state.details)
            }
            is MovieState.Loading -> {
                main.startLoad()
            }
            is MovieState.Error -> {
                main.finishLoad()
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                main.showError(message,
                    getString(R.string.repeat), { loadDetails() })
            }
        }
    }

    private fun showMovie(item: MovieEntity) {
        movie = item
        with(binding) {
            ImageUtils.load(item.poster, ivPoster)
            tvTitle.text = item.title
            tvDate.text = getString(R.string.release_date) + " " + item.date
            tvOriginal.text = item.original
            tvGenres.text = model.genresToString(item.genre_ids)
            des = item.description
            if (des.length == 0)
                binding.tvTryLoadEn.visibility = View.VISIBLE
            else
                binding.tvTryLoadEn.visibility = View.GONE
            note = model.getNote(item.id)
            showDes()
            barVote.progress = (item.vote * 10).toInt()
            tvVote.text = "(${item.vote})"
        }
    }

    private fun showDetails(details: DetailsEntity) {
        with(binding) {
            tvCountries.text = getString(R.string.countries) + details.countries
            cast_ids = details.cast_ids.split(MovieRepository.SEPARATOR)
            cast = details.cast.split(MovieRepository.SEPARATOR)
            tvCast.text = getString(R.string.cast) + limitedArray(cast)
            crew_ids = details.crew_ids.split(MovieRepository.SEPARATOR)
            crew = details.crew.split(MovieRepository.SEPARATOR)
            tvCrew.text = getString(R.string.crew) + limitedArray(crew)
        }
        main.finishLoad()
        model.getState().value = MovieState.Finished
    }

    private fun limitedArray(array: List<String>): String {
        val s = StringBuilder()
        for (n in 0..4) {
            if (n == array.size)
                break
            s.append(", ")
            s.append(array[n])
        }
        if (s.length > 0) {
            s.delete(0, 2)
            return s.toString()
        }
        return array[0]
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