package com.example.kursova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.ArrayList;


public class CustomView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView implements View.OnTouchListener, View.OnFocusChangeListener {
    Drawable cancel;
    public String[] array;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    Marker marker;
    TextWatcher watcher;
    CustomView customView;


    @SuppressLint("ClickableViewAccessibility")
    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        customView = this;

        mDBHelper = new DatabaseHelper(getContext());
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        Cursor cursor = mDb.rawQuery("SELECT name FROM places", null);
        cursor.moveToFirst();
        final ArrayList<String> listName = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            listName.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        array = listName.toArray(new String[listName.size()]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                array);
        setAdapter(adapter);

        cancel = ContextCompat.getDrawable(getContext(), R.drawable.close);
        if(cancel != null){
            cancel.setBounds(0,0,cancel.getIntrinsicWidth(), cancel.getIntrinsicHeight());
        }

        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((MapActivity) getContext()).selectedLocationChange();
                if (isFocused()) {
                    setClearIconVisible(getText().length() > 0);
                } else {
                    setClearIconVisible(false);
                }
                boolean hasMarker = false;
                if(getText().toString().equals("Моє місцезнаходження")) {

                    addMarker(((MapActivity) getContext()).usc, " ");
                    hasMarker = true;
                } else
                {
                    ((MapActivity) getContext()).stopLocationUpdate(customView);

                    for (String m : array) {
                        if (m.equals(getText().toString())) {
                            Cursor cursor = mDb.rawQuery("SELECT latitude, longitude FROM places WHERE name=?", new String[]{m});
                            cursor.moveToFirst();
                            addMarker(new LatLng(cursor.getFloat(0), cursor.getFloat(1)), m);
                            hasMarker = true;
                            cursor.close();
                            break;
                        }
                    }
                }
                if(!hasMarker && marker != null){
                    marker.remove();
                    marker = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };


        super.setOnFocusChangeListener(this);
        super.setOnTouchListener(this);
        super.addTextChangedListener(watcher);

        setClearIconVisible(false);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        CustomView temp = (CustomView) v;
        String edText = temp.getText().toString();
        if(hasFocus && edText.equals(""))
            temp.setText("");
        if (hasFocus) {
            setClearIconVisible(temp.getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(cancel != null) {
            if (event.getX() >= v.getWidth() - cancel.getIntrinsicWidth() && event.getAction() == MotionEvent.ACTION_UP) {
                ((CustomView) v).setText("");
            }
        }
        return false;
    }

    public void addMarker(LatLng latLng, String name){
        if(marker != null){
            marker.remove();
            marker = null;
        }
        marker = ((MapActivity)getContext()).addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
                .draggable(false));
    }

    public void setClearIconVisible(final boolean visible) {
        cancel.setVisible(visible, false);
        final Drawable[] compoundDrawables = getCompoundDrawables();
        setCompoundDrawables(
                compoundDrawables[0],
                compoundDrawables[1],
                visible ? cancel : null,
                compoundDrawables[3]);
    }
}
