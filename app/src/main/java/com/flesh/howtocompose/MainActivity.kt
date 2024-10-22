package com.flesh.howtocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flesh.howtocompose.ui.theme.HowToComposeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {

    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.combinedUiState.collectAsState()
            HowToComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        uiState = uiState,
                        actions = MyViewModel.MyActions(
                            onCheckedChangeAction = {
                                viewModel.updateChecked()
                            },
                            onCountChangeAction = {
                                viewModel.updateCount()
                            }
                        ),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    uiState: MyViewModel.UiState,
    actions: MyViewModel.MyActions,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        MyViewModel.UiState.Loading -> {
            LoadingContent(modifier)
        }

        is MyViewModel.UiState.Success -> {
            SuccessContent(uiState, actions, modifier)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HowToComposeTheme {
        Greeting(MyViewModel.UiState.Loading, actions = MyViewModel.MyActions({}, {}))
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun SuccessContent(
    uiState: MyViewModel.UiState.Success,
    actions: MyViewModel.MyActions,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = "Count: ${uiState.count}")
        Text(text = "Checked: ${uiState.isChecked}")
        Text(text = "UnChangedString: ${uiState.unChangedString}")
        Text(text = "Change it String: ${uiState.stringChangedByCountAndCheck}")
        Button(onClick = {
            actions.onCountChangeAction()
        }) {
            Text("Update Count")
        }
        Button(onClick = {
            actions.onCheckedChangeAction()
        }) {
            Text("Update Checked")
        }
    }
}

class MyViewModel : ViewModel() {

    class MyActions(val onCheckedChangeAction: () -> Unit, val onCountChangeAction: () -> Unit)

    sealed class UiState {

        data object Loading : UiState()
        data class Success(
            val count: Int,
            val isChecked: Boolean,
            val unChangedString: String,
            val stringChangedByCountAndCheck: String
        ) : UiState()

    }

    private val checkedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val countFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val unChangedStringFlow: MutableStateFlow<String> = MutableStateFlow("Aaron's App")


    val combinedUiState =
        combine(countFlow, checkedFlow, unChangedStringFlow) { count, checked, unCheckedString ->

            UiState.Success(
                count,
                checked,
                unCheckedString,
                "$unCheckedString with a Count: $count and is checked $checked"
            )

        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    fun updateCount() {
        countFlow.update { count ->
            count.inc()
        }
    }

    fun updateChecked() {
        checkedFlow.update { checked ->
            !checked
        }
    }

}