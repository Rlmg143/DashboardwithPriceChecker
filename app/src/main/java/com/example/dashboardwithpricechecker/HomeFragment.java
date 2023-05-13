package com.example.dashboardwithpricechecker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RequestQueue queue;
    private String url = "http://192.168.254.106/zantua/admin/get_top_products.php";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fetchData(view);
        return view;
    }


    private void fetchData(View view) {
        LinearLayout topProducts = view.findViewById(R.id.topProducts);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Item>>() {}.getType();
                List<Item> list = gson.fromJson(response, type);

                for(int i = 0; i < list.size(); i++){
                    String productData = list.get(i).toString();
                    LinearLayout productRow = new LinearLayout(getActivity().getApplicationContext());

                    productRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    ImageView image = new ImageView(getActivity().getApplicationContext());
                    image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                    String imageUrl = list.get(i).getImg().isEmpty() ? "http://192.168.254.106/zantua/img/products/prod-placeholder.png" : "http://192.168.254.106/zantua/img/products/" + list.get(i).getImg().split("/")[3];
                    Glide.with(getActivity()).load(imageUrl).into(image);
                    productRow.addView(image);

                    LinearLayout productInfo = new LinearLayout(getActivity().getApplicationContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10, 0,0,0);
                    productInfo.setLayoutParams(layoutParams);

                    productInfo.setOrientation(LinearLayout.VERTICAL);
                    productRow.addView(productInfo);

                    TextView productName = new TextView(getActivity().getApplicationContext());
                    productName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productName.setText(list.get(i).getName());
                    productName.setTypeface(null, Typeface.BOLD);
                    productName.setTextSize(20);
                    productInfo.addView(productName);

                    TextView productDesc = new TextView(getActivity().getApplicationContext());
                    productDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productDesc.setText("Sample description");
                    productDesc.setTextSize(12);
                    productInfo.addView(productDesc);

                    TextView productPrice = new TextView(getActivity().getApplicationContext());
                    productPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productPrice.setText("₱" + list.get(i).getPrice());
                    productPrice.setTextSize(20);
                    productPrice.setTypeface(null, Typeface.BOLD);
                    productInfo.addView(productPrice);

                    TextView category = new TextView(getActivity().getApplicationContext());
                    LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    categoryParams.setMargins(0, 8, 0, 8);
                    category.setLayoutParams(categoryParams);
                    category.setText(list.get(i).getTag().split(",")[0]);
                    category.setTextSize(12);
                    category.setBackgroundResource(R.drawable.round_corners);
                    category.setTextColor(Color.WHITE);
                    productInfo.addView(category);

                    productRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity().getApplicationContext(), ProductDetails.class);
                            intent.putExtra("productDetails", productData);
                            startActivity(intent);
                        }
                    });
                    topProducts.addView(productRow);
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
}