package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.PeopleAdapter

class PeopleFragment : Fragment() {
    companion object {
        private val ARG_PEOPLE = "people"
        fun newInstance(people: List<String>) =
            PeopleFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_PEOPLE, people.toTypedArray())
                }
            }
    }

    private var people: Array<String>? = null
    private val adapter: PeopleAdapter by lazy {
        PeopleAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getStringArray(ARG_PEOPLE)?.let {
            people = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvPeople = view.findViewById<RecyclerView>(R.id.rvPeople)
        rvPeople.adapter = adapter
        initList()
    }

    private fun initList() {
        people?.forEach {
            adapter.addPerson(it)
        }
        adapter.notifyDataSetChanged()
    }
}