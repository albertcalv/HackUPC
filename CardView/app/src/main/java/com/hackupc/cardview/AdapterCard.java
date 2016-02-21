package com.hackupc.cardview;

        import android.content.Context;
        import android.graphics.drawable.Drawable;
        import android.net.Uri;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.lang.reflect.Field;

public class AdapterCard extends ArrayAdapter<Card> {


    private Card[] datos;

    public AdapterCard(Context context, Card[] dm) {
        super(context, R.layout.cardbeacon, dm);
        datos = dm;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (position % 2 == 1) {
            View item = inflater.inflate(R.layout.cardbeacon, null);
            TextView lblNombre = (TextView) item.findViewById(R.id.txtSite);
            lblNombre.setText(datos[position].getName());

            ImageView lblImage = (ImageView) item.findViewById(R.id.imgSite);
            Context context = getContext();
            int id = context.getResources().getIdentifier(datos[position].getImg(), "drawable", context.getPackageName());  //Obtint el drawable a partir del nom del arxiu
            Drawable drawable = context.getResources().getDrawable(id);
            lblImage.setImageDrawable(drawable);

            return (item);
        }
        else {
            View item = inflater.inflate(R.layout.cardroute, null);
            TextView lblMinutes = (TextView) item.findViewById(R.id.txtMinutes);
            lblMinutes.setText(Integer.toString(datos[position].getMinutes()) + " minutes");

            return (item);
    }
    }


}