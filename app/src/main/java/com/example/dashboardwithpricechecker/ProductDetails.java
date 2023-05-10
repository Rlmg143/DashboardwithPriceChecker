package com.example.dashboardwithpricechecker;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initComponent();
    }

    private void initComponent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String details = extras.getString("productDetails");
            try {
                System.out.println("Product details: " +  details);
                TextView name = findViewById(R.id.productName);
                name.setText(details.split(",")[0]);

                TextView price = findViewById(R.id.productPrice);
                price.setText(details.split(",")[1]);
            }catch (Exception e) {

            }

            productImage = findViewById(R.id.prodDetailsImage);
            add = findViewById(R.id.add_pDetails);
            minus = findViewById(R.id.minus_pDetails);
            quantityLbl = findViewById(R.id.qty_pDetails);
            addToReceipt = findViewById(R.id.addToReceipt_pDetails);

            try {
                imageUrl = "http://192.168.254.106/zantua/img/products/" + details.split(",")[details.split(",").length - 1].split("/")[3];
                Glide.with(this).load( imageUrl).into(productImage);
            } catch (Exception e){
                imageUrl = "http://192.168.254.106/zantua/img/products/prod-placeholder.png";
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
                    if (quantity > 0) {
                        List<Item> cartContents = PrefConfig.readListFromPref(getApplicationContext());

                        if (cartContents == null)
                            cartContents = new ArrayList<>();

                        double price = Double.parseDouble(details.split(",")[1]) * quantity;

                        Item item = checkExist(details.split(",")[0], cartContents);
                        if (item == null){
                            cartContents.add(new Item(details.split(",")[0], "" + price, quantity, "", "", "0", imageUrl));
                        } else {
                            double newPrice = Double.parseDouble(item.getPrice()) + price;
                            int newQty = item.getQuantity() + quantity;
                            item.setPrice("" + newPrice);
                            item.setQuantity(newQty);
                            cartContents.set(cartContents.indexOf(item), item);

                        }

                        PrefConfig.writeListInPref(getApplicationContext(), cartContents);

                        Toast.makeText(getApplicationContext(), "Item added to cart.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Please select at least 1(one) quantity.",Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }

    public Item checkExist(String name, List<Item> list){
        System.out.println("Product name: " + name);
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