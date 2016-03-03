package com.app.findme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ItemAdapter extends ArrayAdapter<ItemModel> {
    private final ItemModel[] mModelsArray;
    private final View[] mViewHolder;

    ItemAdapter(Context context, ItemModel[] modelsArray) {
        super(context, R.layout.item_layout, modelsArray);
        this.mModelsArray = modelsArray;
        this.mViewHolder = new View[modelsArray.length];
    }

    private View getRowView(int position, ViewGroup parent) {
        View rowView = mViewHolder[position];
        if (rowView == null) {
            // 1. Create inflater
            LayoutInflater inflater = (LayoutInflater) super.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 2. Get rowView from inflater
            rowView = inflater.inflate(R.layout.item_layout, parent, false);
            mViewHolder[position] = rowView;
        }
        return rowView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        // 2. Get rowView from inflater
        View rowView = getRowView(position, parent);

        // 3. Get icon,title & counter views from the rowView
        ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
        TextView titleView = (TextView) rowView.findViewById(R.id.item_title);

        // 4. Set the text for textView
        imgView.setImageResource(mModelsArray[position].Icon);
        titleView.setText(mModelsArray[position].Title);

        // 5. return rowView
        return rowView;
    }

}

