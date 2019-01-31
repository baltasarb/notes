package baltasarb.yama.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import baltasarb.yama.R
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Team

class OrganizationTeamsAdapter(val app: YamaApplication, val teams: Array<Team>) : ArrayAdapter<Team>(app, -1, teams) {

    override fun getView(position: Int, contentView: View?, parent: ViewGroup?): View? {
        var rowView: View? = contentView

        if (rowView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.organization_teams_list_item_layout, parent, false)
        }

        val teamTextView = rowView!!.findViewById<TextView>(R.id.textViewTitle)

        val team = teams[position]
        teamTextView.text = team.name

        return rowView
    }
}