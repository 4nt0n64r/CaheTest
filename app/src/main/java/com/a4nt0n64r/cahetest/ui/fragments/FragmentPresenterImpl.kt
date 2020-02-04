package com.a4nt0n64r.cahetest.ui.fragments

import androidx.annotation.WorkerThread
import com.a4nt0n64r.cahetest.domain.model.CloudPlayer
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

    private lateinit var cloudPlayer: CloudPlayer

    private val job: Job by lazy { SupervisorJob() }

    override fun onDeleteButtonWasClicked(name: String) {
        if (name != "") {
            CoroutineScope(Dispatchers.Main + job).launch {
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
            CoroutineScope(Dispatchers.Main + job).launch {
                viewState.showSnackbar("Empty delete request!")
            }
        }
    }

    override fun onSaveButtonWasClicked(name: String, data: String) {
        CoroutineScope(Dispatchers.Main + job).launch {
            if (name != "" && data != "") {
                repository.savePlayer(Player(name, data))
                withContext(Dispatchers.Main) {
                    viewState.showSnackbar("save ${name} ${data}")
                }
            } else {
                CoroutineScope(Dispatchers.Main + job).launch {
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
            CoroutineScope(Dispatchers.Main + job).launch {
                if (player != null) {
                    viewState.showSnackbar("find ${player.await()}")
                    viewState.fillName(player.await().name)
                    viewState.fillData(player.await().data)
                } else {
                    withContext(Dispatchers.Main) { viewState.showSnackbar("There's no players with name $name") }
                }
            }
        } else {
            CoroutineScope(Dispatchers.Main + job).launch {
                viewState.showSnackbar("Empty find request!")
            }
        }
    }

    override fun onShowButtonWasClicked() {
        CoroutineScope(Dispatchers.Main + job).launch {
            val players: List<Player>? = withContext(Dispatchers.IO) {
                repository.getAllPlayers()
            }
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
        CoroutineScope(Dispatchers.Main + job).launch {

            cloudPlayer = withContext(Dispatchers.IO) {
                getCloudPlayer()
            }

            viewState.showSnackbar(cloudPlayer.toString())
        }

    }

    @WorkerThread
    private suspend fun getCloudPlayer(): CloudPlayer {

        try {
            val cloudPlayer = cloudRepository.getPlayer()
            return cloudPlayer!!
        } catch (e: NullPointerException) {
            viewState.showSnackbar("Данные не пришли!")
        }

        return CloudPlayer(Player("Пусто", "Пусто"))
    }

    override fun onDestroy() {

        job.cancel()
    }
}
