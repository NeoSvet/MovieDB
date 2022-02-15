package ru.neosvet.moviedb.view

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.list.PeopleAdapter
import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.list.PersonsAdapter
import ru.neosvet.moviedb.model.PeopleModel

class PeopleFragment : Fragment(), Observer<List<Person>> {
    companion object {
        private const val CARD_WIDTH = 120
        private const val ARG_ID = "id"
        private const val ARG_TYPE = "type"
        fun newInstance(movieId: Int, type: PeopleAdapter.Type) =
            PeopleFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, movieId)
                    putString(ARG_TYPE, type.toString())
                }
            }
    }

    private val model: PeopleModel by lazy {
        ViewModelProvider(this).get(PeopleModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            val id = getInt(ARG_ID, -1)
            if (id == -1)
                return@run
            getString(ARG_TYPE)?.let { type ->
                when (type) {
                    PeopleAdapter.Type.CAST.toString() ->
                        model.getCast(id)
                    PeopleAdapter.Type.CREW.toString() ->
                        model.getCrew(id)
                }
            }

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onChanged(list: List<Person>) {
        val adapter = PersonsAdapter(list) { id ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PersonFragment.newInstance(id))
                ?.addToBackStack(MainActivity.MAIN_STACK)?.commit()
        }

        view?.let {
            val rvPeople = it.findViewById<RecyclerView>(R.id.rvPeople)
            val width = getDisplayWidth() / resources.displayMetrics.density
            val count = width.toInt() / CARD_WIDTH
            rvPeople.layoutManager = GridLayoutManager(
                requireContext(), count
            )
            rvPeople.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun getDisplayWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val d = requireActivity().windowManager.currentWindowMetrics
            d.bounds.width()
        } else {
            val p = Point(0, 0)
            requireActivity().windowManager.defaultDisplay.getRealSize(p)
            p.x
        }
    }
}