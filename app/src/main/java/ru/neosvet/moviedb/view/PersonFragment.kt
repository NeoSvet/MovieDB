package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentPersonBinding
import ru.neosvet.moviedb.model.PersonModel
import ru.neosvet.moviedb.model.PersonState
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.ImageUtils
import ru.neosvet.moviedb.utils.MyException

class PersonFragment : Fragment(), Observer<PersonState> {
    companion object {
        private const val ARG_ID = "id"

        @JvmStatic
        fun newInstance(id: Int) =
            PersonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                }
            }
    }

    private var personId: Int? = null
    private var photo: String? = null
    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private val model: PersonModel by lazy {
        ViewModelProvider(this).get(PersonModel::class.java)
    }
    private val main: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentPersonBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            personId = it.getInt(ARG_ID)
            loadPerson()
        }
        binding.tvPlace.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, MapsFragment.newInstance(binding.tvPlace.text.toString()))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }
        binding.ivPhoto.setOnClickListener {
            photo?.let { main.loadBigImage(it) }
        }
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
        val item = menu.add(R.string.refresh)
        item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_refresh_24)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        personId?.let {
            model.reloadPerson(it)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPerson() {
        personId?.let {
            model.loadPerson(it)
        }
    }

    override fun onChanged(state: PersonState) {
        when (state) {
            is PersonState.Success -> {
                main.finishLoad()
                showPerson(state.person)
            }
            is PersonState.Loading -> {
                main.startLoad()
            }
            is PersonState.Error -> {
                main.finishLoad()
                val message: String?
                if (state.error is MyException)
                    message = state.error.getTranslate(requireContext())
                else
                    message = state.error.message
                main.showError(message,
                    getString(R.string.repeat), { loadPerson() })
            }
        }
    }

    private fun showPerson(person: PersonEntity) {
        with(binding) {
            if (person.photo.length > 0) {
                photo = person.photo
                ImageUtils.load(person.photo, ivPhoto)
            }
            tvName.text = person.name
            tvPlace.text = person.place_of_birth
            tvDates.text = getDates(person.birthday, person.deathday)
            barVote.progress = (person.popularity).toInt()
            tvVote.text = "(${person.popularity})"
            tvBiography.text = person.biography
        }
    }

    private fun getDates(birthday: String, deathday: String): String {
        if (birthday.length > 0) {
            val s = StringBuilder()
            s.append(getString(R.string.birthday))
            s.append(birthday)
            if (deathday.length > 0) {
                s.appendLine()
                s.append(getString(R.string.deathday))
                s.append(deathday)
            }
            return s.toString()
        }
        return ""
    }
}