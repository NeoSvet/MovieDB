package ru.neosvet.moviedb.model

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.MovieRepoCallbacks
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc
import ru.neosvet.moviedb.utils.PosterUtils

class MovieModel : ViewModel(), MovieRepoCallbacks {
    private val state: MutableLiveData<MovieState> = MutableLiveData()
    private val repository: MovieRepository = MovieRepository(this)

    fun getState() = state

    fun loadDetails(id: Int?) {
        if (id == null)
            return
        state.value = MovieState.Loading
        repository.requestMovie(id)
    }

    fun loadBigPoster(url: String, target: ImageView) {
        PosterUtils.loadBig(url, target)

        target.getLayoutParams().width = 100
        target.getLayoutParams().height = 100
        target.requestLayout()

        val toValue: Int
        val parent = target.parent as View
        val isWidth: Boolean
        if (parent.width < parent.height) {
            toValue = parent.width
            isWidth = true
        } else {
            toValue = parent.height
            isWidth = false
        }

        val animator = ValueAnimator.ofInt(100, toValue)
        animator.duration = 1200
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            if (isWidth) {
                target.getLayoutParams().width = animation.animatedValue as Int
                target.getLayoutParams().height = (target.getLayoutParams().width * 1.33f).toInt()
            } else {
                target.getLayoutParams().height = animation.animatedValue as Int
                target.getLayoutParams().width = (target.getLayoutParams().height * 0.66f).toInt()
            }
            target.requestLayout()
        }

        animator.start()
    }

    fun genresToString(genre_ids: String): String {
        val list = repository.getGenreList(genre_ids)
        val s = StringBuilder()
        list.forEach {
            s.append(", ")
            s.append(it.title)
        }
        s.delete(0, 2)
        return s.toString()
    }

    fun addNote(id: Int, content: String) {
        repository.addNote(id, content)
    }

    fun getNote(id: Int) = repository.getNote(id)

//OVERRIDE

    override fun onSuccess(movie: MovieEntity) {
        state.postValue(MovieState.Success(movie))
    }

    override fun onFailure(error: Throwable) {
        if (error.message == null)
            state.postValue(MovieState.Error(IncorrectResponseExc("")))
        else
            state.postValue(MovieState.Error(error))
    }
}