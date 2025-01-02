package athul.svift.android.ui.dialogs

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

interface MusicPlayerMenuDialogCallbacks{
    fun onChangeLocation()
}
class MusicPlayerMenuDialog(public var callbacks: MusicPlayerMenuDialogCallbacks) {

    val items = arrayOf("Change Location")

    fun show(context: Context){
        MaterialAlertDialogBuilder(context).setTitle("Settings").setItems(items){dialog,which ->
            if(which == 0){
                callbacks.onChangeLocation()
            }
        }.show()
    }
}