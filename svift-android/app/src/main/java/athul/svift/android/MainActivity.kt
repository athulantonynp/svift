package athul.svift.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import athul.svift.android.ui.fragments.LoginFragment
import athul.svift.android.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> { MainViewModel.Factory  }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}