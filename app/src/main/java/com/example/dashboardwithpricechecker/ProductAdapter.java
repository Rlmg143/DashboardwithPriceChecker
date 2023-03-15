package com.example.dashboardwithpricechecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends ArrayAdapter {

    public ProductAdapter(Context context, int resource, List<Product> productList){
        super(context, resource, productList);
    }

    @Override
    public View getView(int position,View convertView, ViewGroup parent) {
        Product product = (Product) getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_cell, parent, false);
        }

        TextView textView =(TextView) convertView.findViewById(R.id.ProductName);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.productImage);

        textView.setText(product.getName());
        imageView.setImageResource(product.getImage());

        return convertView;
    }
}
