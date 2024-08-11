package athul.svift.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import athul.svift.android.R
import athul.svift.android.injection.showToast
import athul.svift.android.viewmodels.MainViewModel

class LoginFragment : Fragment() {
    private val viewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews(){
        view?.findViewById<Button>(R.id.login_button)?.setOnClickListener {
            val userName = view?.findViewById<EditText>(R.id.username)?.text?.toString()
            val password = view?.findViewById<EditText>(R.id.password)?.text?.toString()
            if(userName.isNullOrEmpty()){
                this.context?.showToast("Username cannot be empty")
                return@setOnClickListener
            }
            if(password.isNullOrEmpty()){
                this.context?.showToast("Password cannot be empty")
                return@setOnClickListener
            }
            viewModel.performLogin(userName,password)
        }
    }
}