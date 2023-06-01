package com.example.dashboardwithpricechecker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

public class ProductDetails extends AppCompatActivity {

    private int quantity = 0;
    private Button add;
    private Button minus;
    private TextView quantityLbl;
    private Button addToReceipt;
    private ImageView productImage;
    private String imageUrl;
    private TextView tag;
    String ip = Helper.ipAddress;
    RequestQueue queue;

    private String url = "http://" + ip + "/v2/zantua/admin/get_product_with_category.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        initComponent();
        getRelateProducts();
    }

    private void initComponent() {
        Bundle extras = getIntent().getExtras();

        productImage = findViewById(R.id.prodDetailsImage);
        add = findViewById(R.id.add_pDetails);
        minus = findViewById(R.id.minus_pDetails);
        quantityLbl = findViewById(R.id.qty_pDetails);
        addToReceipt = findViewById(R.id.addToReceipt_pDetails);
        tag = findViewById(R.id.productCategory);

        if (extras != null) {
            final String details = extras.getString("productDetails");
            System.out.println(details);

            try {
                url += "?tag=" + details.split(",")[3];
                TextView name = findViewById(R.id.productName);
                name.setText(details.split(",")[0]);

                TextView price = findViewById(R.id.productPrice);
                price.setText(details.split(",")[1]);

                tag.setText(details.split(",")[3]);
            } catch (Exception e) {

            }


            try {
                imageUrl = "http://" + ip + "/v2/zantua/img/products/" + details.split(",")[7].split("/")[3];
                Glide.with(this).load(imageUrl).into(productImage);
            } catch (Exception e) {
                imageUrl = "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png";
                Glide.with(this).load(imageUrl).into(productImage);
            }

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quantity += 1;
                    quantityLbl.setText("" + quantity);
                }
            });

            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (quantity > 0) {
                        quantity -= 1;
                        quantityLbl.setText("" + quantity);
                    }

                }
            });

            addToReceipt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    System.out.println(details);
                    if (quantity > 0) {
                        List<Item> cartContents = PrefConfig.readListFromPref(getApplicationContext());

                        if (cartContents == null)
                            cartContents = new ArrayList<>();

                        double price = Double.parseDouble(details.split(",")[1]) * quantity;

                        Item item = checkExist(details.split(",")[0], cartContents);
                        if (item == null) {
                            cartContents.add(new Item(details.split(",")[0], "" + price, quantity, details.split(",")[3], "", "0", imageUrl, ""));
                        } else {
                            double newPrice = Double.parseDouble(item.getPrice()) + price;
                            int newQty = item.getQuantity() + quantity;
                            item.setPrice("" + newPrice);
                            item.setQuantity(newQty);
                            cartContents.set(cartContents.indexOf(item), item);

                        }

                        PrefConfig.writeListInPref(getApplicationContext(), cartContents);

                        increaseUnitSold(details.split(",")[details.split(",").length - 1]);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select at least 1(one) quantity.", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
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

    private void getRelateProducts() {
        LinearLayout parent = findViewById(R.id.prodDetailsLinearLayoutParent);
        LinearLayout relatedProducts = findViewById(R.id.prodDetailsRelatedProducts);
        queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Item>>() {
                }.getType();
                List<Item> list = gson.fromJson(response, type);
                for (int i = 0; i < list.size(); i++) {

                    String productData = list.get(i).toString();
                    LinearLayout productRow = new LinearLayout(getApplicationContext());
                    LinearLayout.LayoutParams productRowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    productRowParams.setMargins(0, 0, 0, 20);
                    productRow.setLayoutParams(productRowParams);

                    ImageView image = new ImageView(getApplicationContext());
                    image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                    String imageUrl = list.get(i).getImg() == null ? "http://" + ip + "/v2/zantua/img/products/prod-placeholder.png" : list.get(i).getImg().isEmpty() ? "http://" + ip + "/v2/img/products/prod-placeholder.png" : "http://" + ip + "/v2/zantua/img/products/" + list.get(i).getImg().split("/")[3];
                    Glide.with(getApplicationContext()).load(imageUrl).into(image);
                    productRow.addView(image);

                    LinearLayout productInfo = new LinearLayout(getApplicationContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10, 0, 0, 0);
                    productInfo.setLayoutParams(layoutParams);

                    productInfo.setOrientation(LinearLayout.VERTICAL);
                    productRow.addView(productInfo);

                    TextView productName = new TextView(getApplicationContext());
                    productName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productName.setText(list.get(i).getName());
                    productName.setTypeface(null, Typeface.BOLD);
                    productName.setTextSize(20);
                    productInfo.addView(productName);

                    TextView productDesc = new TextView(getApplicationContext());
                    productDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productDesc.setText("Sample description");
                    productDesc.setTextSize(12);
                    productInfo.addView(productDesc);

                    TextView productPrice = new TextView(getApplicationContext());
                    productPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    productPrice.setText("₱" + list.get(i).getPrice());
                    productPrice.setTextSize(20);
                    productPrice.setTypeface(null, Typeface.BOLD);
                    productInfo.addView(productPrice);

                    TextView category = new TextView(getApplicationContext());
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
                            Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
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
        queue.add(request);
    }

    private void increaseUnitSold(String stockNo) {
        System.out.println("http://" + ip + "/v2/zantua/admin/increment_product_sold.php?stockNo=" + stockNo);
        queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, "http://" + ip + "/v2/zantua/admin/increment_product_sold.php?stockNo=" + stockNo, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Item added to cart.", Toast.LENGTH_LONG).show();
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