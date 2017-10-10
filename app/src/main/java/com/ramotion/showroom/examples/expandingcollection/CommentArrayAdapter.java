package com.ramotion.showroom.examples.expandingcollection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramotion.expandingcollection.ECCardContentListItemAdapter;
import com.ramotion.showroom.R;
import com.ramotion.showroom.examples.expandingcollection.pojo.Comment;

import java.util.List;

@SuppressLint({"SetTextI18n", "InflateParams"})
public class CommentArrayAdapter extends ECCardContentListItemAdapter<Comment> {

    public CommentArrayAdapter(@NonNull Context context, @NonNull List<Comment> objects) {
        super(context, R.layout.ec_list_element, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            rowView = inflater.inflate(R.layout.ec_list_element, null);
            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) rowView.findViewById(R.id.ec_first_line_date);
            viewHolder.line1 = (TextView) rowView.findViewById(R.id.ec_first_line);
            viewHolder.line2 = (TextView) rowView.findViewById(R.id.ec_second_line);
            viewHolder.icon = (ImageView) rowView.findViewById(R.id.ec_person_icon);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        final Comment objectItem = getItem(position);
        if (objectItem != null) {
            viewHolder.line1.setText(objectItem.getCommentPersonName() + ":");
            viewHolder.line2.setText(objectItem.getCommentText());
            viewHolder.date.setText(objectItem.getCommentDate());
            viewHolder.icon.setImageResource(objectItem.getCommentPersonPictureRes());
        }

        return rowView;
    }

    static class ViewHolder {
        TextView date;
        TextView line1;
        TextView line2;
        ImageView icon;
    }

}
