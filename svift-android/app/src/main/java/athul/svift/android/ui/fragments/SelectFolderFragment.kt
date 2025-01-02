package athul.svift.android.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import athul.svift.android.MainActivity
import athul.svift.android.R

class SelectFolderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_select_folder, container, false)
        val btnSelectFolder = view.findViewById<Button>(R.id.btn_select_folder)
        btnSelectFolder.setOnClickListener {
            (activity as MainActivity).storageHelper.openFolderPicker()
        }

        return view
    }
}