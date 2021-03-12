package io.github.virresh.matvt.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.virresh.matvt.R;

// This isn't a generic re-usable adapter
// it should be refactored to become a generic adapter
// in case similar dropdowns are needed in future

public class IconStyleSpinnerAdapter extends ArrayAdapter<String> {
    private final List<String> objects;
    private Context context;
    public static Map<String, Integer> textToResourceIdMap = new HashMap<String, Integer>();

    static {
        textToResourceIdMap.put("Default", R.drawable.pointer);
        textToResourceIdMap.put("Light", R.drawable.pointer_light);
    }

    public IconStyleSpinnerAdapter(@NonNull Context context, int resource, int textViewId, @NonNull List<String> objects) {
        super(context, resource, textViewId, objects);
        this.objects = objects;
        this.context = context;
    }

    public static List<String> getResourceList () {
        return new ArrayList<>(textToResourceIdMap.keySet());
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return objects.get(position);
    }

    @Override
    public int getPosition(@Nullable String item) {
        return objects.indexOf(item);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent, boolean setBackcolor) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_icon_text_gui, parent, false);
        TextView label = (TextView) row.findViewById(R.id.textView);
        ImageView icon = (ImageView) row.findViewById(R.id.imageView);

        String selection = objects.get(position);

        label.setText(selection);
        textToResourceIdMap.getOrDefault(selection , R.drawable.pointer);
        icon.setImageDrawable(ContextCompat.getDrawable(context, textToResourceIdMap.getOrDefault(selection, R.drawable.pointer)));
        return row;
    }
}
