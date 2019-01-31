package baltasarb.yama.utils

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import baltasarb.yama.R
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Organization

class OrganizationAdapter(val app: YamaApplication, val organizations: Array<Organization>) :
    ArrayAdapter<Organization>(app, -1, organizations) {

    override fun getView(position: Int, contentView: View?, parent: ViewGroup?): View? {
        var rowView: View? = contentView

        if (rowView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.organizations_list_item_layout, parent, false)
        }

        val textViewOrganizationName = rowView!!.findViewById<TextView>(R.id.textViewTitle)
        val avatarImageView = rowView.findViewById<ImageView>(R.id.imageViewAvatar)

        val organization = organizations[position]
        textViewOrganizationName.text = organization.login

        app.avatarsRepository.getAvatar(organization.avatar_url) {
            val myBitmap = it.img
            val emptyBitmap = Bitmap.createBitmap(myBitmap.width, myBitmap.height, myBitmap.config)
            if (!myBitmap.sameAs(emptyBitmap))
                avatarImageView.setImageBitmap(myBitmap)
        }

        return rowView
    }

}
