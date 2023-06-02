package com.example.dashboardwithpricechecker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.cardview.widget.CardView;
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
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

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
    private ImageButton showSidebarButton;
    private boolean isSidebarVisible = false;
    private LinearLayout sidebar;
    String ip = Helper.ipAddress;
    private ArrayList<String> filters = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<Item> filteredItems = new ArrayList<>();
    View view;
    Gson gson;
    Type type;
    String searchQuery = "";
    List<Item> list;
    LinearLayout products;
    private String jsonResponse = "";
    private Button filterButton;
    private Button clearButton;
    private Button ipBtn;
    private EditText ipField;
    SearchView searchView;
    ScrollView scrollView;
    TextView noticeView;
    TextView loadMoreView;
    private int limiter = 20;
    private int filteredLimiter = 20;
    private String url = "http://" + ip + "/v2/zantua/admin/get_products.php";

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
        view = inflater.inflate(R.layout.fragment_product_finder, container, false);
        gson = new Gson();
        type = new TypeToken<ArrayList<Item>>() {
        }.getType();

        searchView = view.findViewById(R.id.productlistSearchView);
        scrollView = view.findViewById(R.id.scrollView);
        noticeView = view.findViewById(R.id.noticeTextView);
        loadMoreView = view.findViewById(R.id.loadMoreView);
        filterButton = view.findViewById(R.id.filterButton);
        clearButton = view.findViewById(R.id.clearButton);
//        ipBtn = view.findViewById(R.id.ipAddressBtn);
//        ipField = view.findViewById(R.id.ipField);
        products = view.findViewById(R.id.listview);

        noticeView.setVisibility(View.VISIBLE);

        getCheckboxes(view);
        request();

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filteredLimiter = 20;
                limiter = 20;
                if (filters.isEmpty()) {
                    filteredItems.clear();
                    products.removeAllViews();
                }
                filters.clear();
                for (CheckBox item : checkBoxes) {
                    if (item.isChecked()) {
                        filters.add(item.getText().toString().toLowerCase());
                    }
                }
                request();
                hideSidebar();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limiter = 20;
                filteredLimiter = 20;

                for (CheckBox checkBox : checkBoxes) {
                    checkBox.setChecked(false);
                }
                filters.clear();

                if (!filteredItems.isEmpty()){
                    filteredItems.clear();
                    products.removeAllViews();

                    request();
                }

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                url = "http://" + ip + "/v2/zantua/admin/get_products.php?query=" + query;
                request();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    url = "http://" + ip + "/v2/zantua/admin/get_products.php";
                    products.removeAllViews();
                    request();
                }
                return false;
            }
        });

        showSidebarButton = view.findViewById(R.id.showSidebarButton);
        sidebar = view.findViewById(R.id.sidebar);

        // Set click listener for the showSidebarButton
        showSidebarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkCount = 0;
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        checkCount += 1;
                    }
                }

                if (checkCount < 1) {
                    filterButton.setEnabled(false);
                    clearButton.setEnabled(false);
                } else {
                    filterButton.setEnabled(true);
                    clearButton.setEnabled(true);
                }

                // Show/hide the sidebar
                if (sidebar.getVisibility() == View.VISIBLE) {
                    hideSidebar();
                } else {
                    showSidebar();
                }
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                    scrollView.setPadding(0, 0, 0, 60);
                    loadMoreView.setVisibility(View.VISIBLE);

                    if (!filters.isEmpty() && filteredItems.size() < 20) {
                        scrollView.setPadding(0, 0, 0, 0);
                        loadMoreView.setVisibility(View.GONE);
                    }
                } else {
                    scrollView.setPadding(0, 0, 0, 0);
                    loadMoreView.setVisibility(View.GONE);
                }
            }
        });

        loadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limiter += 20;
                if (!filters.isEmpty()){
                    filteredLimiter += 20;
                    System.out.println(filteredLimiter);
                }
                scrollView.setPadding(0, 0, 0, 0);
                loadMoreView.setVisibility(View.GONE);
                fetchData();
            }
        });

//        ipBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!ipField.getText().toString().isEmpty()){
//                    Helper.ipAddress = ipField.getText().toString();
//                }
//            }
//        });
        return view;
    }

    private void fetchData() {
        scrollView.setPadding(0, 0, 0, 0);
        loadMoreView.setVisibility(View.GONE);
        if (!filters.isEmpty()) {
//            products.removeAllViews();
            filteredItems.clear();
            for (int i = 0; i < list.size(); i++) {
                if (filters.contains(list.get(i).getTag().split(",")[0].toLowerCase())) {
                    if (!searchView.getQuery().toString().isEmpty()) {
                        if (list.get(i).getName().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                            filteredItems.add(list.get(i));
                        }
                    } else {
                        filteredItems.add(list.get(i));
                    }

                }
            }

            for (int i = filteredLimiter - 20; i < filteredLimiter; i++) {
                try {
                    if (filters.contains(filteredItems.get(i).getTag().split(",")[0].toLowerCase())) {
                        LinearLayout row = new LinearLayout(getActivity().getApplicationContext());
                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        rowParams.setMargins(20, 20, 20, 20);
                        row.setLayoutParams(rowParams);
                        LinearLayout cardview = createItemCard(filteredItems.get(i).getName(), filteredItems.get(i).getPrice(), filteredItems.get(i).getTag(), filteredItems.get(i).getImg(), filteredItems.get(i).toString());
                        LinearLayout cardview1 = createItemCard(filteredItems.get(i + 1).getName(), filteredItems.get(i + 1).getPrice(), filteredItems.get(i + 1).getTag(), filteredItems.get(i + 1).getImg(), filteredItems.get(i + 1).toString());
                        i += 1;
                        row.addView(cardview);
                        row.addView(cardview1);
                        products.addView(row);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + i + " " + e.getMessage());
                }
            }
//
        } else {
            if (!searchQuery.isEmpty()) {
                products.removeAllViews();
                searchQuery = "";
            }
            for (int i = limiter - 20; i < limiter; i++) {
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
        Glide.with(getActivity()).load(image != null ? "http://" + ip + "/v2/zantua/img/products/" + image.split("/")[3] : "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png").into(imageView);

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
        category.setText(categ.split(",")[0]);
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

    private void showSidebar() {
        // Animate the sidebar to slide in from right to left
        Animation slideInAnimation = new TranslateAnimation(sidebar.getWidth(), 0, 0, 0);
        slideInAnimation.setDuration(500);
        sidebar.startAnimation(slideInAnimation);

        // Make the sidebar visible
        sidebar.setVisibility(View.VISIBLE);

        isSidebarVisible = true;
    }

    private void hideSidebar() {
        // Animate the sidebar to slide out from left to right
        Animation slideOutAnimation = new TranslateAnimation(0, sidebar.getWidth(), 0, 0);
        slideOutAnimation.setDuration(500);
        sidebar.startAnimation(slideOutAnimation);

        // Set the sidebar visibility to GONE after the animation finishes
        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sidebar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        isSidebarVisible = false;
    }

    private void getCheckboxes(View view) {

        checkBoxes.add(view.findViewById(R.id.candyCheckBox));
        checkBoxes.add(view.findViewById(R.id.cannedGoodsCheckBox));
        checkBoxes.add(view.findViewById(R.id.condimentsCheckBox));
        checkBoxes.add(view.findViewById(R.id.beveragesCheckBox));
        checkBoxes.add(view.findViewById(R.id.chipsCheckBox));
        checkBoxes.add(view.findViewById(R.id.cerealsCheckBox));
        checkBoxes.add(view.findViewById(R.id.noodleCheckBox));
        checkBoxes.add(view.findViewById(R.id.frozenCheckBox));
        checkBoxes.add(view.findViewById(R.id.hygieneCheckBox));
        checkBoxes.add(view.findViewById(R.id.cigaretteCheckBox));
        checkBoxes.add(view.findViewById(R.id.othersCheckBox));

        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int checkCount = 0;
                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isChecked()) {
                            checkCount += 1;
                        }
                    }

                    if (checkCount < 1) {
                        filterButton.setEnabled(false);
                        clearButton.setEnabled(false);
                    } else {
                        filterButton.setEnabled(true);
                        clearButton.setEnabled(true);
                    }
                }
            });
        }
    }

    private void request() {
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                jsonResponse = response;
                noticeView.setVisibility(View.GONE);
                list = gson.fromJson(jsonResponse, type);
                fetchData();
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