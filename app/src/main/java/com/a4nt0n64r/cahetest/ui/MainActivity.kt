package com.a4nt0n64r.cahetest.ui

import android.os.Bundle
import android.util.Log
import com.a4nt0n64r.cahetest.R
import com.a4nt0n64r.cahetest.ui.base.AbstractActivityPresenter
import com.a4nt0n64r.cahetest.ui.base.ActivityView
import com.a4nt0n64r.cahetest.ui.fragments.FirstFragment
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.koin.android.ext.android.get

const val FIRST_FRAGMENT = 0
const val FRAGMENT_CHANGED = "fragment_changed"


class MainActivity : MvpAppCompatActivity(), ActivityView {

    private val TAG = "MainActivity"

    @InjectPresenter
    lateinit var presenter: AbstractActivityPresenter

    @ProvidePresenter
    fun provide(): AbstractActivityPresenter = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter.loadFragment(FIRST_FRAGMENT)

    }

    override fun changeFragment(fragmentId: Int) {
        when (fragmentId) {
            FIRST_FRAGMENT -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame, FirstFragment(), FRAGMENT_CHANGED)
                    .commit()
            }

        }
    }

    //Вызываем у ActivityPresenter метод onDestroy, чтобы избежать утечек контекста и прочих неприятностей.
    public override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

}





