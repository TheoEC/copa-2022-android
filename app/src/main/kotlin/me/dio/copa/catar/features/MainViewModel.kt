package me.dio.copa.catar.features

import android.content.res.Resources.NotFoundException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.dio.copa.catar.core.BaseViewModel
import me.dio.copa.catar.domain.model.MatchDomain
import me.dio.copa.catar.domain.usecase.DisableNotificationUseCase
import me.dio.copa.catar.domain.usecase.EnableNotificationUseCase
import me.dio.copa.catar.domain.usecase.GetMatchesUseCase
import me.dio.copa.catar.remote.UnexpectedException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val getMatchesUseCase: GetMatchesUseCase,
//    val enableNotificationUseCase: EnableNotificationUseCase,
//    val disableNotificationUseCase: DisableNotificationUseCase
): BaseViewModel<MainUIState, MainUiAction>(MainUIState()) {

    init {
        fetchMatches()
    }

    private fun fetchMatches() = viewModelScope.launch{
        getMatchesUseCase()
            .flowOn(Dispatchers.Main)
            .catch {
                when(it) {
                    is NotFoundException -> {sendAction(MainUiAction.MatchesNotFound(it.message ?: "Erro sem mensagem"))}
                    is UnexpectedException -> {sendAction(MainUiAction.Unexpected)}
                }
            }
            .collect {matches ->
                setState {
                    copy(matches = matches)
                }
            }
    }

//    fun enableNotifications() = viewModelScope.launch { enableNotificationUseCase }

}

data class MainUIState(
    val matches: List<MatchDomain> = emptyList()
)

sealed class MainUiAction {
    object Unexpected: MainUiAction()
    data class MatchesNotFound(val message: String) : MainUiAction()
}