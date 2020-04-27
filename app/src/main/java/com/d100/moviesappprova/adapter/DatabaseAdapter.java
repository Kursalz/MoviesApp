package com.d100.moviesappprova.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.d100.moviesappprova.R;
import com.d100.moviesappprova.data.TableHelper;

public class DatabaseAdapter extends CursorAdapter {

    public DatabaseAdapter(Context context, Cursor c){
        super(context,c);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater vInflater = LayoutInflater.from(context);
        View vView = vInflater.inflate(R.layout.cell_attivita,null);
        return  vView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView vTitolo, vDescrizione, vDataInserimento;
        CheckedTextView vCheckedTextView;
        int vCheck;
        String vDataCheck;

        vTitolo = view.findViewById(R.id.cellaTitolo);
        vDescrizione = view.findViewById(R.id.cellaDescrizione);
        vDataInserimento = view.findViewById(R.id.cellaData);
        vCheckedTextView = view.findViewById(R.id.checkedTextView);

        vDataCheck = cursor.getString(cursor.getColumnIndex(TableHelper.DATA_CHECK));
        vCheck = cursor.getInt(cursor.getColumnIndex(TableHelper.CHECK));
        if(vCheck == 1)
            vCheckedTextView.setChecked(true);
        else
            vCheckedTextView.setChecked(false);

        vTitolo.setText("Titolo: "+cursor.getString(cursor.getColumnIndex(TableHelper.TITOLO)));
        vDescrizione.setText("Descrizione: "+cursor.getString(cursor.getColumnIndex(TableHelper.DESCRIZIONE)));
        vDataInserimento.setText("Data e ora inserimento: "+cursor.getString(cursor.getColumnIndex(TableHelper.DATA_INSERIMENTO)));
        if (vCheckedTextView.isChecked()) {
            vCheckedTextView.setText("Data e ora check: "+vDataCheck);
            vCheckedTextView.setCheckMarkDrawable(R.drawable.checked);
            vCheckedTextView.setVisibility(View.VISIBLE);
        }else{
            vCheckedTextView.setVisibility(View.GONE);
        }
    }
}
