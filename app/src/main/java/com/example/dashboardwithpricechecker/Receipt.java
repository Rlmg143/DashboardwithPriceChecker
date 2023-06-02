package com.example.dashboardwithpricechecker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Receipt#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Receipt extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TableLayout tableLayout;
    private Button deleteRowButton;
    //    private Button btnPrintReceipt;
    private Button btnResetReceipt;
    private String stringQuery = "";
    List<Item> cartContents;
    private TextView subTotalLbl;
    private double subTotal = 0;
    //PRODUCTS/ORDER LISTS
    private final ArrayList<ContentValues> products = new ArrayList<>();
    public static final String PRODUCT_NAME = "PROD_NAME";
    public static final String PRODUCT_COST = "PROD_COST";
    public static final String PRODUCT_QUANTITY = "PROD_QUANTITY";

    private TextView search;
    View rootView;
    LinearLayout cart;

    private TextView pName, pTotal, pSubCategory, pCategory, pQty;

    public Receipt() {
        // Required empty public constructor
    }

    public static Receipt newInstance(String param1, String param2) {
        Receipt fragment = new Receipt();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cartContents = PrefConfig.readListFromPref(getActivity().getApplicationContext()) == null ? new ArrayList<>() : PrefConfig.readListFromPref(getActivity().getApplicationContext());
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_receipt, container, false);
//        PrefConfig.writeListInPref(getActivity().getApplicationContext(), new ArrayList<>());
        cart = rootView.findViewById(R.id.cart);

        pName = rootView.findViewById(R.id.receiptProductName);
        pTotal = rootView.findViewById(R.id.receiptTotal);
        pCategory = rootView.findViewById(R.id.receiptCategory);
        pQty = rootView.findViewById(R.id.receiptQty);

        subTotalLbl = rootView.findViewById(R.id.subtotal_fReceipt);

        fetchCart(cart);

        subTotalLbl.setText("₱" + new DecimalFormat("0.00").format(subTotal));


//        btnPrintReceipt = rootView.findViewById(R.id.btn_print_receipt);
//        btnPrintReceipt.setOnClickListener(v -> {
//            if (products.size() > 0){
//                printReceipt();
//            } else {
//                Toast.makeText(getActivity().getApplicationContext(),"Receipt is empty.",Toast.LENGTH_LONG).show();
//            }
//
//        });

        btnResetReceipt = rootView.findViewById(R.id.resetreceipt);
        btnResetReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrefConfig.writeListInPref(getActivity().getApplicationContext(), new ArrayList<>());
                cart.removeAllViews();
                subTotalLbl.setText("₱0.00");
            }
        });

        search = rootView.findViewById(R.id.search_fReceipt);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stringQuery = s.toString();
                fetchCart(cart);
//                for (int i = 0; i < cartContents.size(); i++) {
//                    if (cartContents.get(i).getName().contains(query)){
//                        System.out.println(cartContents.get(i).getName());
//                    }
//                }
            }
        });
        return rootView;
    }

    private void fetchCart(LinearLayout cart) {
        cart.removeAllViews();
        subTotal = 0;
        try {
            if (stringQuery.isEmpty()) {
                for (int i = 0; i < cartContents.size(); i++) {
                    subTotal += Double.parseDouble(cartContents.get(i).getPrice());
                    createReceiptItem(cart, i);
                }
            } else {
                for (int i = 0; i < cartContents.size(); i++) {
                    subTotal += Double.parseDouble(cartContents.get(i).getPrice());
                    if (cartContents.get(i).getName().contains(stringQuery)) {
                        createReceiptItem(cart, i);
                    }
                }
            }

            subTotalLbl.setText("₱" + new DecimalFormat("0.00").format(subTotal));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    private void createReceiptItem(LinearLayout cart, int i) {
        LinearLayout row = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(5, 0, 8, 20);
        row.setLayoutParams(rowParams);

        LinearLayout cardView = createItemCard(cartContents.get(i).getName(), cartContents.get(i).getPrice(), cartContents.get(i).getTag(), cartContents.get(i).getImg(), "" + cartContents.get(i).getQuantity(), "", i);
        row.addView(cardView);

        cart.addView(row);

        ContentValues cartItem = new ContentValues();
        cartItem.put(PRODUCT_NAME, cartContents.get(i).getName());
        cartItem.put(PRODUCT_COST, cartContents.get(i).getPrice());
        cartItem.put(PRODUCT_QUANTITY, cartContents.get(i).getQuantity());
        products.add(cartItem);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public String shortenText(String str) {
        if (str.length() > 10) {
            return str.substring(0, 10) + "\n" + str.substring(10);
        }

        return str;
    }

    private LinearLayout createItemCard(String itemName, String price, String categ, String imageUrl, String prodQuantity, String data, int index) {

        LinearLayout cardContainer = new LinearLayout(getActivity().getApplicationContext());
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

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
        l1.setOrientation(LinearLayout.HORIZONTAL);
        l1.setLayoutParams(params);

        ImageView image = new ImageView(getActivity().getApplicationContext());
        image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        Glide.with(getActivity()).load(imageUrl).into(image);

        l1.addView(image);

        LinearLayout l2 = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 0, 0);
        l2.setLayoutParams(layoutParams);
        l2.setOrientation(LinearLayout.VERTICAL);

        l1.addView(l2);

        TextView productName = new TextView(getActivity().getApplicationContext());
        productName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productName.setText(itemName + " (" + prodQuantity + "x)");
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
        category.setText(categ);
        category.setTextSize(12);
        category.setBackgroundResource(R.drawable.round_corners);
        category.setTextColor(Color.WHITE);
        l2.addView(category);

        TextView productRetailPrice = new TextView(getActivity().getApplicationContext());
        productRetailPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productRetailPrice.setText("Total: ₱" + price);
        productRetailPrice.setTextSize(15);
        l2.addView(productRetailPrice);

        LinearLayout l3 = new LinearLayout(getActivity().getApplicationContext());

        LinearLayout.LayoutParams l3Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        l3Params.gravity = Gravity.CENTER;
        l3.setLayoutParams(l3Params);
        l3.setOrientation(LinearLayout.HORIZONTAL);

//        l1.addView(l3);

        TextView quantity = new TextView(getActivity().getApplicationContext());
        ImageView removeIcon = new ImageView(getActivity().getApplicationContext());
        removeIcon.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
        removeIcon.setImageResource(R.drawable.ic_baseline_remove_circle_24);

        double origPrice = Double.parseDouble(price) / Integer.parseInt(prodQuantity);
        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(quantity.getText().toString()) > 1) {
                    int newQty = Integer.parseInt(quantity.getText().toString()) - 1;
                    double newPrice = newQty * origPrice;
                    quantity.setText("" + newQty);
                    productRetailPrice.setText("Price: ₱" + newPrice);

                }

            }
        });

        l3.addView(removeIcon);

        quantity.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        quantity.setText(prodQuantity);
        quantity.setTextSize(24);
        l3.addView(quantity);

        ImageView plusIcon = new ImageView(getActivity().getApplicationContext());
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(70, 70);
        plusIcon.setLayoutParams(plusParams);
        plusIcon.setImageResource(R.drawable.ic_baseline_add_circle_24);
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQty = Integer.parseInt(quantity.getText().toString()) + 1;
                double newPrice = newQty * origPrice;
                quantity.setText("" + newQty);
                productRetailPrice.setText("Price: ₱" + newPrice);
            }
        });
        l3.addView(plusIcon);

        cardview.addView(l1);

//        TextView test = new TextView(getActivity().getApplicationContext());
//        test.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        test.setText("Testasdasdasdasdasd");
//        test.setTypeface(null, Typeface.BOLD);
//        cardContainer.addView(l3);

        cardContainer.addView(cardview);

        cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.layout_bottom_sheet, (LinearLayout) rootView.findViewById(R.id.bottomSheetContainer));

                ImageView receiptImage = bottomSheetView.findViewById(R.id.receiptImage);
                Glide.with(getActivity()).load(imageUrl).into(receiptImage);

                ((TextView) bottomSheetView.findViewById(R.id.receiptProductName)).setText(itemName);

                double total = (Double.parseDouble(price) / Integer.parseInt(prodQuantity)) * Integer.parseInt(prodQuantity);
                ((TextView) bottomSheetView.findViewById(R.id.receiptTotal)).setText("" + total);

                ((TextView) bottomSheetView.findViewById(R.id.receiptCategory)).setText(categ);

                ((TextView) bottomSheetView.findViewById(R.id.receiptDecrease)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double originalPrice = (Double.parseDouble(price) / Integer.parseInt(prodQuantity));
                        int quantity = Integer.parseInt(((TextView) bottomSheetView.findViewById(R.id.receiptQty)).getText().toString());
                        if (quantity > 1) {
                            quantity -= 1;

                            ((TextView) bottomSheetView.findViewById(R.id.receiptQty)).setText("" + quantity);

                            double newTotal = originalPrice * quantity;
                            ((TextView) bottomSheetView.findViewById(R.id.receiptTotal)).setText("" + new DecimalFormat("0.00").format(newTotal));
                        }
                    }
                });
                ((TextView) bottomSheetView.findViewById(R.id.receiptQty)).setText(prodQuantity);
                ((TextView) bottomSheetView.findViewById(R.id.receiptIncrease)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double originalPrice = (Double.parseDouble(price) / Integer.parseInt(prodQuantity));
                        int quantity = Integer.parseInt(((TextView) bottomSheetView.findViewById(R.id.receiptQty)).getText().toString());
                        quantity += 1;

                        ((TextView) bottomSheetView.findViewById(R.id.receiptQty)).setText("" + quantity);

                        double newTotal = originalPrice * quantity;
                        ((TextView) bottomSheetView.findViewById(R.id.receiptTotal)).setText("" + new DecimalFormat("0.00").format(newTotal));

                    }
                });

                ((Button) bottomSheetView.findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cartContents.get(index).setPrice(((TextView) bottomSheetView.findViewById(R.id.receiptTotal)).getText().toString());
                        cartContents.get(index).setQuantity(Integer.parseInt(((TextView) bottomSheetView.findViewById(R.id.receiptQty)).getText().toString()));

                        PrefConfig.writeListInPref(getActivity().getApplicationContext(), cartContents);

                        Toast.makeText(getActivity().getApplicationContext(), "Successfully updated item details.", Toast.LENGTH_LONG).show();
                        bottomSheetDialog.dismiss();

                        fetchCart(cart);
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
        return cardContainer;
    }

}


