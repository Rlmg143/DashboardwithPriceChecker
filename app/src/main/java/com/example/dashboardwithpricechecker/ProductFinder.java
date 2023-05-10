package com.example.dashboardwithpricechecker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.*;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductFinder#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductFinder extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String url = "http://192.168.254.106/zantua/admin/get_products.php";

    Spinner spinner;

    String[] data = {};
    RequestQueue queue;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProductFinder() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductFinder.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductFinder newInstance(String param1, String param2) {
        ProductFinder fragment = new ProductFinder();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_finder, container, false);
        fetchData(view);

        SearchView searchView = view.findViewById(R.id.productlistSearchView);

        spinner = view.findViewById(R.id.food_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {

                String selected = spinner.getSelectedItem().toString();
                System.out.println(selected);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                url = "http://192.168.254.106/zantua/admin/get_products.php?query=" + query;
                fetchData(view);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
//        Button frozenBtn = (Button) view.findViewById(R.id.frozenBtn);
//
//
//        frozenBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        return view;
    }

    private void fetchData(View view) {

        LinearLayout products = view.findViewById(R.id.listview);

        TextView loadingText = new TextView(getActivity().getApplicationContext());
        loadingText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingText.setText("Loading items...");
        loadingText.setTextSize(15);
        products.addView(loadingText);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                products.removeAllViews();
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Item>>() {
                }.getType();
                List<Item> list = gson.fromJson(response, type);

                for (int i = 0; i < list.size(); i++) {
                    try {
                        LinearLayout row = new LinearLayout(getActivity().getApplicationContext());
                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        rowParams.setMargins(20, 20, 20, 20);
                        row.setLayoutParams(rowParams);
                        LinearLayout cardview = createItemCard(list.get(i).getName(), list.get(i).getPrice(), list.get(i).getTag(), list.get(i).getImg(), list.get(i).toString());
                        LinearLayout cardview1 = createItemCard(list.get(i + 1).getName(), list.get(i + 1).getPrice(), list.get(i + 1).getTag(), list.get(i + 1).getImg(), list.get(i + 1).toString());
                        i += 1;
                        row.addView(cardview);
                        row.addView(cardview1);
                        products.addView(row);

                    } catch (Exception e) {
                        System.out.println("Error: " + i + " " + e.getMessage());
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        queue.add(request);
    }

    @NotNull
    private LinearLayout createItemCard(String name, String price, String categ, String image, String data) {
        LinearLayout cardContainer = new LinearLayout(getActivity().getApplicationContext());
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        CardView cardview = new CardView(getActivity());

        CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );

        layoutparams.setMargins(20, 10, 10, 2);

        cardview.setLayoutParams(layoutparams);
        cardview.setRadius(15);
        cardview.setPadding(25, 25, 25, 25);
        cardview.setMaxCardElevation(30);
        cardview.setMaxCardElevation(6);


        LinearLayout l1 = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(20, 20, 20, 20);
        l1.setOrientation(LinearLayout.VERTICAL);
        l1.setLayoutParams(params);


        ImageView imageView = new ImageView(getActivity().getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));
        Glide.with(getActivity()).load(image != null ? "http://192.168.254.106/zantua/img/products/" + image.split("/")[3] : "http://192.168.254.106/zantua/img/products/prod-placeholder.png").into(imageView);

        l1.addView(imageView);

        LinearLayout l2 = new LinearLayout(getActivity().getApplicationContext());
        l2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        l2.setOrientation(LinearLayout.VERTICAL);

        l1.addView(l2);

        TextView productName = new TextView(getActivity().getApplicationContext());
        productName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productName.setText(name);
        productName.setTypeface(null, Typeface.BOLD);
        productName.setTextSize(20);

        l2.addView(productName);

        TextView category = new TextView(getActivity().getApplicationContext());
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        categoryParams.setMargins(0, 8, 0, 8);
        category.setLayoutParams(categoryParams);
        category.setText("FOOD");
        category.setTextSize(12);
        category.setBackgroundResource(R.drawable.round_corners);
        category.setTextColor(Color.WHITE);
        l2.addView(category);

        TextView productRetailPrice = new TextView(getActivity().getApplicationContext());
        productRetailPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productRetailPrice.setText("Price: ₱" + price);
        productRetailPrice.setTextSize(15);
        l2.addView(productRetailPrice);

        final String productData = data;
        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ProductDetails.class);
                intent.putExtra("productDetails", productData);
                startActivity(intent);
            }
        });
        cardview.addView(l1);
        cardContainer.addView(cardview);
        return cardContainer;
    }
}