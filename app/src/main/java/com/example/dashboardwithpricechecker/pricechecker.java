package com.example.dashboardwithpricechecker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.core.view.MenuItemCompat;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link pricechecker#newInstance} factory method to
 * create an instance of this fragment.
 */
public class pricechecker extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String ip = Helper.ipAddress;
    private String url = "http://" + ip + "/v2/zantua/admin/get_product_with_barcode.php";
    String[] data = {};
    RequestQueue queue;
    private String jsonRespose = "";
    private Button minus;
    private Button add;
    private Button addToReceipt;
    private TextView quantityLbl;
    private ImageView productImage;
    private int quantity = 0;
    private String name = "", price = "", imageUrl = "", tag = "";


    public pricechecker() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment pricechecker.
     */
    // TODO: Rename and change types and number of parameters
    public static pricechecker newInstance(String param1, String param2) {
        pricechecker fragment = new pricechecker();
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

        View view = inflater.inflate(R.layout.fragment_pricechecker, container, false);

        SearchView searchView = view.findViewById(R.id.barcodeInput);
        searchView.onActionViewExpanded();

        productImage = view.findViewById(R.id.priceCheckerProductImage);
        minus = view.findViewById(R.id.minus_pChecker);
        add = view.findViewById(R.id.add_pChecker);
        addToReceipt = view.findViewById(R.id.addToReceipt_pChecker);
        quantityLbl = view.findViewById(R.id.qty_pChecker);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity -= 1;
                    quantityLbl.setText("" + quantity);
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity += 1;
                quantityLbl.setText("" + quantity);
            }
        });

        addToReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(jsonRespose);
                if (quantity > 0) {
                    List<Item> cartContents = PrefConfig.readListFromPref(getActivity().getApplicationContext());

                    if (cartContents == null)
                        cartContents = new ArrayList<>();


                    double finalPrice = Double.parseDouble(price) * quantity;

                    Item item = checkExist(name, cartContents);
                    if (item == null) {
                        cartContents.add(new Item(name, "" + price, quantity, jsonRespose.split("\\|")[1].split(",")[0], "", "0", imageUrl, ""));
                    } else {
                        double newPrice = Double.parseDouble(item.getPrice()) + finalPrice;
                        int newQty = item.getQuantity() + quantity;
                        item.setPrice("" + newPrice);
                        item.setQuantity(newQty);
                        cartContents.set(cartContents.indexOf(item), item);
                    }

                    PrefConfig.writeListInPref(getActivity().getApplicationContext(), cartContents);

                    increaseUnitSold(jsonRespose.split("\\|")[jsonRespose.split("\\|").length - 1]);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please select at least 1(one) quantity.", Toast.LENGTH_LONG).show();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                url = "http://" + ip + "/v2/zantua/admin/get_product_with_barcode.php?barcode=" + query;
                fetchData(view);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    private void fetchData(View view) {
        TextView note = view.findViewById(R.id.note);
        TextView pName = view.findViewById(R.id.productName_pCheck);
        TextView pPrice = view.findViewById(R.id.productPrice_pCheck);
        SearchView searchView = view.findViewById(R.id.barcodeInput);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                jsonRespose = response;
                if (!response.isEmpty()) {
                    note.setVisibility(View.GONE);
                    pName.setText(response.split("\\|")[0]);
                    tag = response.split("\\|")[1].split(",")[0];
                    pPrice.setText(response.split("\\|")[2]);

                    name = response.split("\\|")[0];
                    price = response.split("\\|")[2];

                    try {
                        imageUrl = "http://" + ip + "/v2/zantua/img/products/" + response.split("\\|")[response.split("\\|").length - 2].split("/")[3];
                        Glide.with(getActivity()).load(imageUrl).into(productImage);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        imageUrl = "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png";
                        Glide.with(getActivity()).load(imageUrl).into(productImage);
                    }

                    LinearLayout relatedProducts = view.findViewById(R.id.priceCheckerRelatedProducts);
                    StringRequest requestRelated = new StringRequest(Request.Method.POST, "http://" + ip + "/v2/zantua/admin/get_product_with_category.php?tag=" + tag, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<Item>>() {
                            }.getType();
                            List<Item> list = gson.fromJson(response, type);
                            System.out.println(list);
                            for (int i = 0; i < list.size(); i++) {

                                String productData = list.get(i).toString();
                                LinearLayout productRow = new LinearLayout(getActivity().getApplicationContext());
                                LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                productRowParams.setMargins(0, 0, 0, 20);
                                productRow.setLayoutParams(productRowParams);

                                ImageView image = new ImageView(getActivity().getApplicationContext());
                                image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                                String imageUrl = list.get(i).getImg() == null ? "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png" : list.get(i).getImg().isEmpty() ? "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png" : "http://" + ip + "/v2/zantua/img/products/" + list.get(i).getImg().split("/")[3];
                                Glide.with(getActivity().getApplicationContext()).load(imageUrl).into(image);
                                productRow.addView(image);

                                LinearLayout productInfo = new LinearLayout(getActivity().getApplicationContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(10, 0, 0, 0);
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
                                relatedProducts.addView(productRow);


                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                        }
                    });
                    queue.add(requestRelated);

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Barcode not found.", Toast.LENGTH_LONG).show();
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

    public Item checkExist(String name, List<Item> list) {
        Item item = null;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equalsIgnoreCase(name)) {
                item = list.get(i);
                break;
            }
        }
        return item;
    }

    private void increaseUnitSold(String stockNo) {
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, "http://" + ip + "/v2/zantua/admin/increment_product_sold.php?stockNo=" + stockNo, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity().getApplicationContext(), "Item added to cart.", Toast.LENGTH_LONG).show();
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