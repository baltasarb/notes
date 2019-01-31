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
import baltasarb.yama.model.TeamMember

class TeamMembersAdapter(val app: YamaApplication, val teamMembers: Array<TeamMember>) :
    ArrayAdapter<TeamMember>(app, -1, teamMembers) {

    override fun getView(position: Int, contentView: View?, parent: ViewGroup?): View? {
        var rowView: View? = contentView

        if (rowView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.team_members_list_item, parent, false)
        }

        val teamMemberTextView = rowView!!.findViewById<TextView>(R.id.teamMemberNameTextView)
        val avatarImageView = rowView.findViewById<ImageView>(R.id.teamMemberAvatarImageView)

        val teamMember = teamMembers[position]
        teamMemberTextView.text = teamMember.login

        app.avatarsRepository.getAvatar(teamMember.avatar_url) {
            val myBitmap = it.img
            val emptyBitmap = Bitmap.createBitmap(myBitmap.width, myBitmap.height, myBitmap.config)
            if (!myBitmap.sameAs(emptyBitmap))
                avatarImageView.setImageBitmap(myBitmap)
        }

        return rowView
    }
}