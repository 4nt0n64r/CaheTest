package com.a4nt0n64r.cahetest.ui.fragments

import com.a4nt0n64r.cahetest.domain.model.Player
import com.a4nt0n64r.cahetest.domain.repository.Repository
import com.a4nt0n64r.cahetest.network.NetworkRepository
import com.a4nt0n64r.cahetest.ui.base.AbstractFragmentPresenter
import kotlinx.coroutines.*
import moxy.InjectViewState


@InjectViewState
class FragmentPresenterImpl(
    private val repository: Repository,
    private val cloudRepository: NetworkRepository
) : AbstractFragmentPresenter() {

    private var job: Job? = null

    override fun onDeleteButtonWasClicked(name: String) {
        if (name != "") {
            job = CoroutineScope(Dispatchers.IO).launch {
                val player: Player? = repository.findPlayer(name)
                if (player != null) {
                    repository.deletePlayer(name)
                    withContext(Dispatchers.Main) {
                        viewState.showSnackbar("deleted $name")
                    }
                } else {
                    withContext(Dispatchers.Main) { viewState.showSnackbar("There's no players with name $name") }
                }
            }
        } else {
            job = CoroutineScope(Dispatchers.Main).launch {
                viewState.showSnackbar("Empty delete request!")
            }
        }
    }

    override fun onSaveButtonWasClicked(name: String, data: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            if (name != "" && data != "") {
                repository.savePlayer(Player(name, data))
                withContext(Dispatchers.Main) {
                    viewState.showSnackbar("save ${name} ${data}")
                }
            } else {
                job = CoroutineScope(Dispatchers.Main).launch {
                    viewState.showSnackbar("Enter data and name!")
                }
            }
        }
    }

    override fun onFindButtonWasClicked(name: String) {
        if (name != "") {
            val player: Deferred<Player>? = CoroutineScope(Dispatchers.IO).async {
                repository.findPlayer(name)
            }
            job = CoroutineScope(Dispatchers.IO).launch {
                if (player != null) {
                    viewState.showSnackbar("find ${player.await()}")
                    viewState.fillName(player.await().name)
                    viewState.fillData(player.await().data)
                } else {
                    withContext(Dispatchers.Main) { viewState.showSnackbar("There's no players with name $name") }
                }
            }
        } else {
            job = CoroutineScope(Dispatchers.Main).launch {
                viewState.showSnackbar("Empty find request!")
            }
        }
    }

    override fun onShowButtonWasClicked() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val players: List<Player>? = repository.getAllPlayers()
            if (!players.isNullOrEmpty()) {
                withContext(Dispatchers.Main) { viewState.showSnackbar("show all") }
                var names = ""
                var data = ""
                for (pl in players) {
                    names += (" " + pl.name)
                    data += (" " + pl.data)
                }
                viewState.fillName(names)
                viewState.fillData(data)
            } else {
                withContext(Dispatchers.Main) { viewState.showSnackbar("There's no players!") }
            }
        }
    }

    override fun onNetButtonWasClicked() {
        job = CoroutineScope(Dispatchers.IO).launch {

            var name: String
            var data: String

            cloudRepository.getPlayer { responce ->
                name = responce.player.name
                data = responce.player.data
                val cloudPlayer = Player(name, data)
                CoroutineScope(Dispatchers.Main).launch { viewState.showSnackbar(cloudPlayer.toString()) }
            }
        }
    }

    override fun onDestroy() {
        if (job != null) {
            job!!.cancel()
        }
    }
}
