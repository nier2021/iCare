package com.docter.icare.ui.main.expandableList

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.docter.icare.R
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.databinding.DraweGrouprListBinding
import com.docter.icare.databinding.DrawerItemListBinding

class ExpandableListAdapter internal constructor(
    private val context: Context,
    private val expandableListEntity: ExpandableListEntity
) : BaseExpandableListAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var groupBinding: DraweGrouprListBinding
    private lateinit var itemBinding: DrawerItemListBinding

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.expandableListEntity.dataList!!.get(this.expandableListEntity.titleList!![listPosition])!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup?): View {
        var convertView = view
        val holder: ItemViewHolder
        if (convertView == null) {
            itemBinding = DrawerItemListBinding.inflate(inflater)
            convertView = itemBinding.root
            holder = ItemViewHolder()
            holder.label = itemBinding.expandedListItem
            convertView.tag = holder
        } else {
            holder = convertView.tag as ItemViewHolder
        }
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        holder.label!!.text = expandedListText
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.expandableListEntity.dataList!![this.expandableListEntity.titleList!![listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.expandableListEntity.titleList!![listPosition]
    }

    override fun getGroupCount(): Int {
        return this.expandableListEntity.titleList!!.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup?): View {
        var convertView = view
        val holder: GroupViewHolder
        if (convertView == null) {
            groupBinding = DraweGrouprListBinding.inflate(inflater)
            convertView = groupBinding.root
            holder = GroupViewHolder()
            with(holder){
                icon =  groupBinding.ivIcon
                label = groupBinding.listTitle
                imgArrow = groupBinding.ivGroupIndicator
            }
            convertView.tag = holder

        } else {
            holder = convertView.tag as GroupViewHolder
        }
        val listTitle = getGroup(listPosition) as String
        with(holder){
            icon!!.setImageDrawable(getImgDrawable(expandableListEntity.imgList!![listPosition]))
            label!!.text = listTitle
            imgArrow!!.setImageDrawable(getGroupArrow(isExpanded))
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }


//    override fun onGroupExpanded(groupPosition: Int) {
//        super.onGroupExpanded(groupPosition)
//        Log.i("ExpandableListAdapter","onGroupExpanded")
//        Log.i("ExpandableListAdapter",(titleList as ArrayList<String>)[groupPosition] + " List Expanded.")
//    }


//    override fun onGroupCollapsed(groupPosition: Int) {
//        super.onGroupCollapsed(groupPosition)
//        Log.i("ExpandableListAdapter","onGroupCollapsed")
//        Log.i("ExpandableListAdapter",(titleList as ArrayList<String>)[groupPosition] + " List Collapsed.")
//    }

    private fun getGroupArrow(isExpanded: Boolean): Drawable {
       return getImgDrawable(if(isExpanded) R.drawable.icon_arrow_drop_down else R.drawable.icon_arrow_drop_up)
    }

    private fun getImgDrawable(pattern :Int): Drawable{
       return  ContextCompat.getDrawable(context,pattern)!!
    }

    inner class ItemViewHolder {
        internal var label: AppCompatTextView? = null
    }

    inner class GroupViewHolder {
        internal var icon: AppCompatImageView? = null
        internal var label: AppCompatTextView? = null
        internal var imgArrow: AppCompatImageView? = null
    }
}