package com.example.dashboardwithpricechecker;

import android.content.Context;
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

    private String url = "http://192.168.254.106/zantua/admin/get_product_with_barcode.php";
    String[] data = {};
    RequestQueue queue;

    private Button minus;
    private Button add;
    private Button addToReceipt;
    private TextView quantityLbl;
    private ImageView productImage;
    private int quantity = 0;
    private String name = "", price = "", imageUrl = "";

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
            // 4902430615211 - origin barcode
            // 4800040211253 - Ricoa
            //4800417001210 - barcode cologne
            // 4800888153876 - Rexona


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
                if (quantity > 0) {
                    List<Item> cartContents = PrefConfig.readListFromPref(getActivity().getApplicationContext());

                    if (cartContents == null)
                        cartContents = new ArrayList<>();


                    double finalPrice = Double.parseDouble(price) * quantity;

                    Item item = checkExist(name, cartContents);
                    if (item == null){
                        cartContents.add(new Item(name, "" + price, quantity, "", "", "0", imageUrl));
                    } else {
                        double newPrice = Double.parseDouble(item.getPrice()) + finalPrice;
                        int newQty = item.getQuantity() + quantity;
                        item.setPrice("" + newPrice);
                        item.setQuantity(newQty);
                        cartContents.set(cartContents.indexOf(item), item);
                    }

                    PrefConfig.writeListInPref(getActivity().getApplicationContext(), cartContents);

                    Toast.makeText(getActivity().getApplicationContext(), "Item added to cart.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please select at least 1(one) quantity.", Toast.LENGTH_LONG).show();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println(query);
                url = "http://192.168.254.106/zantua/admin/get_product_with_barcode.php?barcode=" + query;
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
                if (!response.isEmpty()) {
                    note.setVisibility(View.GONE);
                    pName.setText(response.split("\\|")[0]);
                    pPrice.setText(response.split("\\|")[2]);

                    name = response.split("\\|")[0];
                    price = response.split("\\|")[2];

                    try {
                        imageUrl = "http://192.168.254.106/zantua/img/products/" + response.split("\\|")[response.split("\\|").length - 1].split("/")[3];
                        Glide.with(getActivity()).load(imageUrl).into(productImage);
                    } catch (Exception e){
                        imageUrl = "http://192.168.254.106/zantua/img/products/prod-placeholder.png";
                        Glide.with(getActivity()).load(imageUrl).into(productImage);
                    }
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

    public Item checkExist(String name, List<Item> list){
        Item item = null;
        for(int i = 0; i < list.size(); i++){
            if (list.get(i).getName().equalsIgnoreCase(name)){
                item = list.get(i);
                break;
            }
        }
        return item;
    }
}